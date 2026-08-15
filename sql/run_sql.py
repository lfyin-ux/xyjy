# 使用PyMySQL执行SQL脚本 精确控制UTF8编码
import sys
import pymysql

def run_sql_file(path, use_db=None):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    conn = pymysql.connect(host='localhost', user='root', password='123456',
                           charset='utf8mb4', autocommit=True,
                           database=use_db)
    cur = conn.cursor()
    # 按分号拆分语句 忽略注释行
    statements = []
    buf = []
    for line in content.splitlines():
        stripped = line.strip()
        if stripped.startswith('--') or stripped == '':
            continue
        buf.append(line)
        if stripped.endswith(';'):
            statements.append('\n'.join(buf))
            buf = []
    ok = 0
    for stmt in statements:
        s = stmt.strip()
        if not s:
            continue
        try:
            cur.execute(s)
            ok += 1
        except Exception as e:
            print('执行失败:', s[:60], '->', e)
    print('执行完成，成功语句数:', ok)
    cur.close()
    conn.close()

if __name__ == '__main__':
    path = sys.argv[1]
    db = sys.argv[2] if len(sys.argv) > 2 else None
    run_sql_file(path, db)
