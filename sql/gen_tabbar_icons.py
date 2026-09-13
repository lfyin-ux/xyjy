# 生成小程序tabBar图标 未选中灰色 选中紫色
import os
from PIL import Image, ImageDraw

# 图标输出目录
OUT = os.path.join(os.path.dirname(__file__), '..', 'frontend', 'miniapp', 'images', 'tabbar')
os.makedirs(OUT, exist_ok=True)

SIZE = 81  # 微信推荐81x81
GRAY = (187, 185, 196)
PURPLE = (108, 92, 231)


def draw_icon(draw, name, color):
    # 根据名称画不同的简单图形
    cx, cy = SIZE // 2, SIZE // 2
    if name == 'match':
        # 爱心
        draw.polygon([(cx, cy + 20), (cx - 24, cy - 6), (cx + 24, cy - 6)], fill=color)
        draw.ellipse([cx - 24, cy - 22, cx - 2, cy], fill=color)
        draw.ellipse([cx + 2, cy - 22, cx + 24, cy], fill=color)
    elif name == 'life':
        # 拼图方块
        draw.rounded_rectangle([cx - 24, cy - 24, cx - 4, cy - 4], radius=4, fill=color)
        draw.rounded_rectangle([cx + 4, cy - 24, cx + 24, cy - 4], radius=4, fill=color)
        draw.rounded_rectangle([cx - 24, cy + 4, cx - 4, cy + 24], radius=4, fill=color)
        draw.rounded_rectangle([cx + 4, cy + 4, cx + 24, cy + 24], radius=4, fill=color)
    elif name == 'mall':
        # 购物袋
        draw.rounded_rectangle([cx - 22, cy - 12, cx + 22, cy + 26], radius=6, fill=color)
        draw.arc([cx - 14, cy - 30, cx + 14, cy + 2], 180, 360, fill=color, width=5)
    elif name == 'chat':
        # 对话气泡
        draw.rounded_rectangle([cx - 24, cy - 20, cx + 24, cy + 12], radius=10, fill=color)
        draw.polygon([(cx - 10, cy + 10), (cx - 2, cy + 22), (cx + 6, cy + 10)], fill=color)
    elif name == 'mine':
        # 用户头像
        draw.ellipse([cx - 12, cy - 26, cx + 12, cy - 2], fill=color)
        draw.pieslice([cx - 22, cy - 2, cx + 22, cy + 40], 180, 360, fill=color)


def make(name):
    for state, color in [('', GRAY), ('_on', PURPLE)]:
        # 部分微信开发者工具/基础库组合会将 TabBar PNG 的 alpha 通道整体处理为透明。
        # TabBar 背景本身为白色，因此直接输出无 alpha 的 RGB PNG，以保证图标稳定显示。
        img = Image.new('RGB', (SIZE, SIZE), (255, 255, 255))
        draw = ImageDraw.Draw(img)
        draw_icon(draw, name, color)
        img.save(os.path.join(OUT, name + state + '_solid.png'), optimize=True)


for n in ['match', 'life', 'mall', 'chat', 'mine']:
    make(n)

print('tabBar图标生成完成')
