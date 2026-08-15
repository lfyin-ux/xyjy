# 为Mock数据生成占位图片 解决图片404问题
import os
from PIL import Image, ImageDraw

# uploads根目录
BASE = os.path.join(os.path.dirname(__file__), '..', 'uploads')

# 配色 参照设计稿
COLORS = [
    (108, 92, 231), (255, 85, 124), (162, 155, 254),
    (0, 184, 148), (253, 203, 110), (255, 154, 158),
    (146, 184, 255), (233, 211, 255)
]

# 需要生成的图片相对路径 与Mock数据一致
FILES = [
    'avatar/u1.png', 'avatar/u2.png', 'avatar/u3.png', 'avatar/u4.png',
    'avatar/u5.png', 'avatar/u6.png', 'avatar/u7.png', 'avatar/u8.png',
    'idcard/f1.png', 'idcard/b1.png', 'idcard/f2.png', 'idcard/b2.png',
    'idcard/f3.png', 'idcard/b3.png', 'idcard/f8.png', 'idcard/b8.png',
    'school/s1.png', 'school/s2.png', 'school/s3.png', 'school/s8.png',
    'photo/p1_1.png', 'photo/p1_2.png', 'photo/p2_1.png',
    'photo/p3_1.png', 'photo/p3_2.png',
    'post/post1.png', 'post/post2.png', 'post/post4.png',
    'second/g1.png', 'second/g2.png', 'second/g3.png', 'second/g4.png',
    'mall/m1.png', 'mall/m2.png', 'mall/m3.png', 'mall/m4.png', 'mall/m5.png'
]


def make_image(path, idx):
    full = os.path.join(BASE, path.replace('/', os.sep))
    os.makedirs(os.path.dirname(full), exist_ok=True)
    color = COLORS[idx % len(COLORS)]
    img = Image.new('RGB', (400, 400), color)
    draw = ImageDraw.Draw(img)
    # 画一个简单的对角渐变块和标签
    c2 = COLORS[(idx + 3) % len(COLORS)]
    draw.ellipse([120, 90, 280, 250], fill=c2)
    label = os.path.basename(path).split('.')[0]
    draw.text((150, 300), label, fill=(255, 255, 255))
    img.save(full)


for i, f in enumerate(FILES):
    make_image(f, i)

print('占位图片生成完成，共', len(FILES), '张')
