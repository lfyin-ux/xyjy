#!/usr/bin/env python3
"""仅部署后端 JAR 并执行数据库迁移"""
import os
import sys
import time
import subprocess
import paramiko

HOST = os.environ.get('XYJY_DEPLOY_HOST', '118.31.106.63')
USER = os.environ.get('XYJY_DEPLOY_USER', 'ecs-user')
PASSWORD = os.environ.get('XYJY_DEPLOY_PASSWORD', '')
APP_DIR = '/opt/xyjy'
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))


def upload_dir(sftp, local_dir, remote_dir):
    for root, _, files in os.walk(local_dir):
        rel = os.path.relpath(root, local_dir)
        remote_root = remote_dir if rel == '.' else f'{remote_dir}/{rel.replace(os.sep, "/")}'
        parts = remote_root.split('/')
        cur = ''
        for part in parts:
            if not part:
                continue
            cur += '/' + part
            try:
                sftp.stat(cur)
            except FileNotFoundError:
                sftp.mkdir(cur)
        for name in files:
            local_path = os.path.join(root, name)
            remote_path = f'{remote_root}/{name}'
            print(f'上传 {local_path} -> {remote_path}')
            sftp.put(local_path, remote_path)


def run(client, cmd, timeout=300):
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


def main():
    if not PASSWORD:
        print('请设置环境变量: export XYJY_DEPLOY_PASSWORD=你的服务器密码')
        sys.exit(1)

    jar_local = os.path.join(PROJECT_ROOT, 'backend/target/xyjy-backend.jar')
    admin_dist = os.path.join(PROJECT_ROOT, 'frontend/admin/dist')
    if not os.path.exists(jar_local):
        print('正在打包后端...')
        subprocess.check_call(['mvn', 'package', '-DskipTests', '-q'], cwd=os.path.join(PROJECT_ROOT, 'backend'))
    if os.path.isdir(os.path.join(PROJECT_ROOT, 'frontend/admin')):
        print('正在构建管理后台...')
        subprocess.check_call(['npm', 'run', 'build'], cwd=os.path.join(PROJECT_ROOT, 'frontend/admin'))

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(HOST, username=USER, password=PASSWORD, timeout=30)

    sftp = client.open_sftp()
    print(f'上传 {jar_local}')
    sftp.put(jar_local, f'{APP_DIR}/xyjy-backend.jar')
    sftp.put(os.path.join(PROJECT_ROOT, 'sql/04_comment_visibility.sql'), f'{APP_DIR}/sql/04_comment_visibility.sql')
    sftp.put(os.path.join(PROJECT_ROOT, 'sql/05_user_address.sql'), f'{APP_DIR}/sql/05_user_address.sql')
    sftp.put(os.path.join(PROJECT_ROOT, 'sql/06_refund_apply.sql'), f'{APP_DIR}/sql/06_refund_apply.sql')
    sftp.put(os.path.join(PROJECT_ROOT, 'sql/07_refunded_order_status.sql'), f'{APP_DIR}/sql/07_refunded_order_status.sql')
    wechat_secret = os.environ.get('WECHAT_APP_SECRET', '')
    sms_key_id = os.environ.get('SMS_ACCESS_KEY_ID', '')
    sms_key_secret = os.environ.get('SMS_ACCESS_KEY_SECRET', '')
    sms_sign = os.environ.get('SMS_SIGN_NAME', '内蒙古因源果网络科技服务有限公司')
    sms_template = os.environ.get('SMS_TEMPLATE_CODE', 'SMS_512095649')

    existing_yml = ''
    try:
        with sftp.file(f'{APP_DIR}/application-prod.yml', 'r') as f:
            existing_yml = f.read().decode()
    except Exception:
        pass

    if existing_yml:
        import re
        if not wechat_secret:
            m = re.search(r'app-secret:\s*(\S+)', existing_yml)
            if m:
                wechat_secret = m.group(1)
        if not sms_key_id:
            m = re.search(r'access-key-id:\s*(\S+)', existing_yml)
            if m:
                sms_key_id = m.group(1)
        if not sms_key_secret:
            m = re.search(r'access-key-secret:\s*(\S+)', existing_yml)
            if m:
                sms_key_secret = m.group(1)
        m = re.search(r'sign-name:\s*(.+)', existing_yml)
        if m and not os.environ.get('SMS_SIGN_NAME'):
            sms_sign = m.group(1).strip()
        m = re.search(r'template-code:\s*(\S+)', existing_yml)
        if m and not os.environ.get('SMS_TEMPLATE_CODE'):
            sms_template = m.group(1).strip()

    wx_mch_id = os.environ.get('WXPAY_MCH_ID', '')
    wx_api_v3_key = os.environ.get('WXPAY_API_V3_KEY', '')
    wx_serial = os.environ.get('WXPAY_MERCHANT_SERIAL', '')
    wx_public_key_id = os.environ.get('WXPAY_PUBLIC_KEY_ID', '')
    wx_private_key_local = os.environ.get('WXPAY_PRIVATE_KEY_FILE', '')
    wx_public_key_local = os.environ.get(
        'WXPAY_PUBLIC_KEY_FILE',
        os.path.join(PROJECT_ROOT, '资料', 'pub_key.pem'),
    )

    if existing_yml:
        import re
        if not wx_mch_id:
            m = re.search(r'wxpay:\s*\n(?:.*\n)*?.*mch-id:\s*(\S+)', existing_yml)
            if m:
                wx_mch_id = m.group(1)
        if not wx_api_v3_key:
            m = re.search(r'api-v3-key:\s*(\S+)', existing_yml)
            if m:
                wx_api_v3_key = m.group(1)
        if not wx_serial:
            m = re.search(r'merchant-serial-number:\s*(\S+)', existing_yml)
            if m:
                wx_serial = m.group(1)
        if not wx_public_key_id:
            m = re.search(r'public-key-id:\s*(\S+)', existing_yml)
            if m:
                wx_public_key_id = m.group(1)

    cert_files = []
    if wx_private_key_local and os.path.isfile(wx_private_key_local):
        cert_files.append((wx_private_key_local, f'{APP_DIR}/certs/apiclient_key.pem'))
    if wx_public_key_local and os.path.isfile(wx_public_key_local):
        cert_files.append((wx_public_key_local, f'{APP_DIR}/certs/pub_key.pem'))
    if cert_files:
        run(client, f'mkdir -p {APP_DIR}/certs')
        for local_path, remote_path in cert_files:
            sftp.put(local_path, remote_path)

    if wechat_secret or (sms_key_id and sms_key_secret) or (wx_mch_id and wx_api_v3_key):
        sms_enabled = 'true' if sms_key_id and sms_key_secret else 'false'
        sms_block = f"""sms:
  enabled: {sms_enabled}
  access-key-id: {sms_key_id}
  access-key-secret: {sms_key_secret}
  sign-name: {sms_sign}
  template-code: {sms_template}
  code-expire-minutes: 5
""" if sms_key_id and sms_key_secret else """sms:
  enabled: false
  code-expire-minutes: 5
"""
        wechat_block = f"""wechat:
  app-id: wx13b60dd991c5b29e
  app-secret: {wechat_secret}
  sec-check:
    enabled: true
""" if wechat_secret else ''
        wxpay_block = ''
        if wx_mch_id and wx_api_v3_key and wx_serial:
            public_key_lines = ''
            if wx_public_key_id:
                public_key_lines = f"""  public-key-id: {wx_public_key_id}
  public-key-path: {APP_DIR}/certs/pub_key.pem
"""
            wxpay_block = f"""wxpay:
  mch-id: {wx_mch_id}
  api-v3-key: {wx_api_v3_key}
  merchant-serial-number: {wx_serial}
  private-key-path: {APP_DIR}/certs/apiclient_key.pem
{public_key_lines}  notify-url: https://tongxingshikong.cn/api/pay/notify
"""
        prod_yml = f"""spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xyjy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
file:
  upload-dir: {APP_DIR}/uploads
app:
  mode: prod
{sms_block}{wxpay_block}{wechat_block}"""
        with sftp.file(f'{APP_DIR}/application-prod.yml', 'w') as f:
            f.write(prod_yml)
    if os.path.isdir(admin_dist):
        upload_dir(sftp, admin_dist, f'{APP_DIR}/admin')
    sftp.close()

    run(client, f'mysql -uroot -p123456 xyjy < {APP_DIR}/sql/04_comment_visibility.sql', timeout=120)
    run(client, f'mysql -uroot -p123456 xyjy < {APP_DIR}/sql/05_user_address.sql', timeout=120)
    run(client, f'mysql -uroot -p123456 xyjy < {APP_DIR}/sql/06_refund_apply.sql', timeout=120)
    run(client, f'mysql -uroot -p123456 xyjy < {APP_DIR}/sql/07_refunded_order_status.sql', timeout=120)
    run(client, 'sudo systemctl restart xyjy-backend')

    for i in range(15):
        time.sleep(2)
        stdin, stdout, stderr = client.exec_command('curl -s http://127.0.0.1:8080/api/square/comments/1?userId=2')
        body = stdout.read().decode()
        if 'visibility' in body or 'replyToUserId' in body:
            print('迁移成功：评论接口已返回 visibility 字段')
            break
        if i == 14:
            print('警告：接口暂未返回新字段，请检查服务日志')

    client.close()
    print('\n生产环境部署完成（后端 + 管理后台）。小程序请在开发者工具中重新上传。')


if __name__ == '__main__':
    main()
