#!/usr/bin/env python3
"""部署管理后台与后端到 ECS"""
import os
import sys
import time
import paramiko

HOST = os.environ.get('XYJY_DEPLOY_HOST', '118.31.106.63')
USER = os.environ.get('XYJY_DEPLOY_USER', 'ecs-user')
PASSWORD = os.environ.get('XYJY_DEPLOY_PASSWORD', '')
DOMAIN = os.environ.get('XYJY_DEPLOY_DOMAIN', 'tongxingshikong.cn')
APP_DIR = '/opt/xyjy'
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))


def run(client, cmd, timeout=600):
    print(f'\n>>> {cmd}')
    stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode()
    err = stderr.read().decode()
    code = stdout.channel.recv_exit_status()
    if out.strip():
        print(out.strip())
    if err.strip():
        print(err.strip())
    if code != 0:
        raise RuntimeError(f'命令失败({code}): {cmd}')
    return out


def upload_dir(sftp, local_dir, remote_dir):
    for root, _, files in os.walk(local_dir):
        rel = os.path.relpath(root, local_dir)
        remote_root = remote_dir if rel == '.' else f'{remote_dir}/{rel.replace(os.sep, "/")}'
        try:
            sftp.stat(remote_root)
        except FileNotFoundError:
            run_sftp_mkdirs(sftp, remote_root)
        for name in files:
            local_path = os.path.join(root, name)
            remote_path = f'{remote_root}/{name}'
            print(f'上传 {local_path} -> {remote_path}')
            sftp.put(local_path, remote_path)


def run_sftp_mkdirs(sftp, path):
    parts = path.split('/')
    cur = ''
    for part in parts:
        if not part:
            continue
        cur += '/' + part
        try:
            sftp.stat(cur)
        except FileNotFoundError:
            sftp.mkdir(cur)


def main():
    if not PASSWORD:
        print('请设置环境变量 XYJY_DEPLOY_PASSWORD')
        sys.exit(1)
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, username=USER, password=PASSWORD, timeout=30)

    run(client, 'sudo apt-get update -y', timeout=900)
    run(client, 'sudo DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-17-jdk nginx mysql-server python3-pymysql', timeout=1800)
    run(client, f'sudo mkdir -p {APP_DIR}/admin {APP_DIR}/uploads {APP_DIR}/sql {APP_DIR}/logs')
    run(client, f'sudo chown -R {USER}:{USER} {APP_DIR}')

    prod_yml = f"""spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xyjy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
file:
  upload-dir: {APP_DIR}/uploads
app:
  mode: dev
"""
    sftp = client.open_sftp()
    with sftp.file(f'{APP_DIR}/application-prod.yml', 'w') as f:
        f.write(prod_yml)

    jar_local = os.path.join(PROJECT_ROOT, 'backend/target/xyjy-backend.jar')
    print(f'上传 {jar_local}')
    sftp.put(jar_local, f'{APP_DIR}/xyjy-backend.jar')

    upload_dir(sftp, os.path.join(PROJECT_ROOT, 'frontend/admin/dist'), f'{APP_DIR}/admin')
    upload_dir(sftp, os.path.join(PROJECT_ROOT, 'uploads'), f'{APP_DIR}/uploads')
    upload_dir(sftp, os.path.join(PROJECT_ROOT, 'sql'), f'{APP_DIR}/sql')
    sftp.close()

    mysql_setup = r"""
sudo systemctl enable mysql nginx
sudo systemctl start mysql
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456'; FLUSH PRIVILEGES;" 2>/dev/null || true
mysql -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS xyjy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
cd /opt/xyjy/sql && python3 run_sql.py 01_schema.sql
cd /opt/xyjy/sql && python3 run_sql.py 02_mock_data.sql xyjy
"""
    for line in mysql_setup.strip().split('\n'):
        if line.strip():
            run(client, line.strip(), timeout=300)

    nginx_conf = f"""server {{
    listen 80;
    server_name {DOMAIN} www.{DOMAIN} {HOST};

    root {APP_DIR}/admin;
    index index.html;

    client_max_body_size 60m;

    location / {{
        try_files $uri $uri/ /index.html;
    }}

    location /api/ {{
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }}

    location /uploads/ {{
        proxy_pass http://127.0.0.1:8080/api/uploads/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }}
}}
"""
    with client.open_sftp().file('/tmp/xyjy-admin.conf', 'w') as f:
        f.write(nginx_conf)
    run(client, 'sudo mv /tmp/xyjy-admin.conf /etc/nginx/sites-available/xyjy-admin')
    run(client, 'sudo ln -sf /etc/nginx/sites-available/xyjy-admin /etc/nginx/sites-enabled/xyjy-admin')
    run(client, 'sudo rm -f /etc/nginx/sites-enabled/default')
    run(client, 'sudo nginx -t')
    run(client, 'sudo systemctl reload nginx')

    systemd_unit = f"""[Unit]
Description=XYJY Backend
After=network.target mysql.service

[Service]
Type=simple
User={USER}
WorkingDirectory={APP_DIR}
ExecStart=/usr/bin/java -jar {APP_DIR}/xyjy-backend.jar --spring.profiles.active=prod --spring.config.additional-location=file:{APP_DIR}/application-prod.yml
SuccessExitStatus=143
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
"""
    with client.open_sftp().file('/tmp/xyjy-backend.service', 'w') as f:
        f.write(systemd_unit)
    run(client, 'sudo mv /tmp/xyjy-backend.service /etc/systemd/system/xyjy-backend.service')
    run(client, 'sudo systemctl daemon-reload')
    run(client, 'sudo systemctl enable xyjy-backend')
    run(client, 'sudo systemctl restart xyjy-backend')

    for i in range(20):
        time.sleep(2)
        stdin, stdout, stderr = client.exec_command('curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/api/auth/mode')
        code = stdout.read().decode().strip()
        print('后端健康检查:', code)
        if code == '200':
            break

    stdin, stdout, stderr = client.exec_command(f'curl -s -o /dev/null -w "%{{http_code}}" http://127.0.0.1/')
    print('管理后台首页:', stdout.read().decode().strip())
    client.close()
    print(f'\n部署完成: http://{DOMAIN}  管理后台账号 admin / admin123')


if __name__ == '__main__':
    main()
