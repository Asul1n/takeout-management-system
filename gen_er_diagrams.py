#!/usr/bin/env python3
"""
Generate proper Chen-style E-R diagrams using Graphviz.
- Entity = rectangle (box)
- Attribute = ellipse
- Relationship = diamond
- Primary key attributes use underlined label
"""

import subprocess
import os

BASE = '/home/asuka/takeout/diagrams'
os.makedirs(BASE, exist_ok=True)

def run_dot(name, dot_code):
    """Save .dot file and render to SVG + PNG."""
    dot_path = os.path.join(BASE, f'{name}.dot')
    svg_path = os.path.join(BASE, f'{name}.svg')
    png_path = os.path.join(BASE, f'{name}.png')

    with open(dot_path, 'w', encoding='utf-8') as f:
        f.write(dot_code)

    # Render SVG (better for embedding in documents)
    r = subprocess.run(['dot', '-Kdot', '-Tsvg', '-o', svg_path, dot_path],
                       capture_output=True, text=True)
    if r.returncode != 0:
        print(f'[{name}] dot -> SVG FAILED: {r.stderr[:500]}')
        # Try neato as fallback
        r2 = subprocess.run(['neato', '-Tsvg', '-o', svg_path, dot_path],
                           capture_output=True, text=True)
        if r2.returncode != 0:
            print(f'[{name}] neato -> SVG also FAILED')

    # Also render PNG for WPS compatibility (WPS may not handle SVG well)
    r3 = subprocess.run(['dot', '-Kdot', '-Tpng', '-Gdpi=150', '-o', png_path, dot_path],
                       capture_output=True, text=True)
    if r3.returncode != 0:
        r3 = subprocess.run(['neato', '-Tpng', '-Gdpi=150', '-o', png_path, dot_path],
                           capture_output=True, text=True)

    if os.path.exists(png_path):
        size = os.path.getsize(png_path)
        print(f'[{name}] OK: PNG {size//1024}KB, SVG {os.path.getsize(svg_path)//1024 if os.path.exists(svg_path) else 0}KB')
    else:
        print(f'[{name}] FAILED to generate PNG')

# ===================================================================
# Common node style macros
# ===================================================================
# We use HTML-like labels for entities to get bold text
# For attributes, plain text with underline for PK

ENT_ATTR = 'fontname="SimHei" fontsize=11'
REL_ATTR = 'fontname="SimHei" fontsize=10'
ATTR_NODE_ATTR = 'fontname="SimSun" fontsize=9'

# ===================================================================
# DIAGRAM 1: 用户管理模块局部ER图
# ===================================================================
D1 = '''digraph ER_UserModule {
    rankdir=TB;
    splines=ortho;
    nodesep=0.6;
    ranksep=0.8;

    // === Global node defaults ===
    node [fontname="SimHei", fontsize=10];
    edge [fontname="SimSun", fontsize=9];

    // === ENTITIES (rectangles) ===
    node [shape=box, style="filled,bold", fillcolor="#D6EAF8", fontsize=11];
    E_User    [label="用户"];
    E_Admin   [label="管理员"];
    E_Merchant [label="商家"];
    E_Customer [label="顾客"];
    E_Rider   [label="骑手"];

    // === RELATIONSHIPS (diamonds) ===
    node [shape=diamond, style=filled, fillcolor="#FDEBD0", fontsize=9, width=0.7, height=0.7];
    R_ISA1 [label="ISA"];
    R_ISA2 [label="ISA"];
    R_ISA3 [label="ISA"];
    R_ISA4 [label="ISA"];

    // === ATTRIBUTES for 用户 (ellipses) ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=8, fontname="SimSun"];
    A_UserID    [label=<&#x5C;&#x5C;用户ID&#x5C;&#x5C;>];  // underline for PK
    A_Phone     [label="手机号"];
    A_Password  [label="登录密码"];
    A_Role      [label="用户身份"];
    A_Status    [label="账号状态"];
    A_RegTime   [label="注册时间"];

    // === ATTRIBUTES for 管理员 ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=8];
    A_AdminID   [label=<&#x5C;&#x5C;管理员ID&#x5C;&#x5C;>];
    A_AdminName [label="姓名"];

    // === ATTRIBUTES for 商家 ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=7];
    A_MerID       [label=<&#x5C;&#x5C;商家ID&#x5C;&#x5C;>];
    A_MerName     [label="商家名称"];
    A_MerPhone    [label="联系电话"];
    A_MerAddrProv [label="营业地址-省"];
    A_MerAddrCity [label="营业地址-市"];
    A_MerAddrDist [label="营业地址-区"];
    A_MerAddrDet  [label="营业地址-详细"];
    A_MerOpenTime [label="营业时间-起始"];
    A_MerEndTime  [label="营业时间-结束"];
    A_MerNotice   [label="商家公告"];
    A_MerBizStat  [label="营业状态"];
    A_MerAudit    [label="入驻审核状态"];
    A_MerMonSales [label="月销量"];

    // === ATTRIBUTES for 顾客 ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=8];
    A_CusID       [label=<&#x5C;&#x5C;顾客ID&#x5C;&#x5C;>];
    A_CusName     [label="姓名"];
    A_CusAddrProv [label="配送地址-省"];
    A_CusAddrCity [label="配送地址-市"];
    A_CusAddrDist [label="配送地址-区"];
    A_CusAddrDet  [label="配送地址-详细"];

    // === ATTRIBUTES for 骑手 ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=8];
    A_RiderID     [label=<&#x5C;&#x5C;骑手ID&#x5C;&#x5C;>];
    A_RiderName   [label="姓名"];
    A_RiderStat   [label="配送状态"];
    A_RiderTotal  [label="累计配送单数"];

    // === Edges: Entities to Relationships ===
    edge [style=solid, penwidth=1.2];
    E_User -> R_ISA1 [headlabel="1", labeldistance=1.5];
    R_ISA1 -> E_Admin [headlabel="1", labeldistance=1.5];
    E_User -> R_ISA2 [headlabel="1", labeldistance=1.5];
    R_ISA2 -> E_Merchant [headlabel="1", labeldistance=1.5];
    E_User -> R_ISA3 [headlabel="1", labeldistance=1.5];
    R_ISA3 -> E_Customer [headlabel="1", labeldistance=1.5];
    E_User -> R_ISA4 [headlabel="1", labeldistance=1.5];
    R_ISA4 -> E_Rider [headlabel="1", labeldistance=1.5];

    // === Edges: Entities to Attributes ===
    edge [style=solid, penwidth=0.8, arrowhead=none];
    E_User -> A_UserID;
    E_User -> A_Phone;
    E_User -> A_Password;
    E_User -> A_Role;
    E_User -> A_Status;
    E_User -> A_RegTime;

    E_Admin -> A_AdminID;
    E_Admin -> A_AdminName;

    E_Merchant -> A_MerID;
    E_Merchant -> A_MerName;
    E_Merchant -> A_MerPhone;
    E_Merchant -> A_MerAddrProv;
    E_Merchant -> A_MerAddrCity;
    E_Merchant -> A_MerAddrDist;
    E_Merchant -> A_MerAddrDet;
    E_Merchant -> A_MerOpenTime;
    E_Merchant -> A_MerEndTime;
    E_Merchant -> A_MerNotice;
    E_Merchant -> A_MerBizStat;
    E_Merchant -> A_MerAudit;
    E_Merchant -> A_MerMonSales;

    E_Customer -> A_CusID;
    E_Customer -> A_CusName;
    E_Customer -> A_CusAddrProv;
    E_Customer -> A_CusAddrCity;
    E_Customer -> A_CusAddrDist;
    E_Customer -> A_CusAddrDet;

    E_Rider -> A_RiderID;
    E_Rider -> A_RiderName;
    E_Rider -> A_RiderStat;
    E_Rider -> A_RiderTotal;

    // Layout hints: group attributes with their entities
    { rank=same; E_Admin; A_AdminID; A_AdminName; }
    { rank=same; E_User; A_UserID; A_Phone; A_Password; A_Role; A_Status; A_RegTime; }
    { rank=same; E_Merchant; A_MerID; A_MerName; }
}
'''

# ===================================================================
# DIAGRAM 2: 商家与菜品管理模块局部ER图
# ===================================================================
D2 = '''digraph ER_MerchantDish {
    rankdir=LR;
    splines=polyline;
    nodesep=0.5;
    ranksep=1.0;

    node [fontname="SimHei", fontsize=10];
    edge [fontname="SimSun", fontsize=9];

    // === ENTITIES ===
    node [shape=box, style="filled,bold", fillcolor="#D6EAF8", fontsize=11];
    E_Merchant [label="商家"];
    E_Category [label="菜品分类"];
    E_Dish     [label="菜品"];

    // === RELATIONSHIPS ===
    node [shape=diamond, style=filled, fillcolor="#FDEBD0", fontsize=9, width=0.7, height=0.7];
    R_HasCat  [label="拥有"];
    R_Contains [label="包含"];
    R_HasDish [label="直接拥有"];

    // === 商家 attributes ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=7];
    A_MID       [label=<&#x5C;&#x5C;商家ID&#x5C;&#x5C;>];
    A_MName     [label="商家名称"];
    A_MPhone    [label="联系电话"];
    A_MBizStat  [label="营业状态"];
    A_MMonSales [label="月销量"];

    // === 菜品分类 attributes ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=8];
    A_CatID   [label=<&#x5C;&#x5C;分类ID&#x5C;&#x5C;>];
    A_CatName [label="分类名称"];
    A_CatSort [label="排序序号"];

    // === 菜品 attributes ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=7];
    A_DishID    [label=<&#x5C;&#x5C;菜品ID&#x5C;&#x5C;>];
    A_DishName  [label="菜品名称"];
    A_DishPrice [label="菜品价格"];
    A_DishDesc  [label="菜品描述"];
    A_DishImg   [label="菜品图片URL"];
    A_DishStock [label="库存量"];
    A_DishStat  [label="上架状态"];

    // === Relationships ===
    edge [style=solid, penwidth=1.2];
    E_Merchant -> R_HasCat [headlabel="1", labeldistance=2];
    R_HasCat -> E_Category [headlabel="N", labeldistance=2];
    E_Category -> R_Contains [headlabel="1", labeldistance=2];
    R_Contains -> E_Dish [headlabel="N", labeldistance=2];
    E_Merchant -> R_HasDish [headlabel="1", labeldistance=2];
    R_HasDish -> E_Dish [headlabel="N", labeldistance=2];

    // === Attributes ===
    edge [style=solid, penwidth=0.8, arrowhead=none];
    E_Merchant -> A_MID;
    E_Merchant -> A_MName;
    E_Merchant -> A_MPhone;
    E_Merchant -> A_MBizStat;
    E_Merchant -> A_MMonSales;

    E_Category -> A_CatID;
    E_Category -> A_CatName;
    E_Category -> A_CatSort;

    E_Dish -> A_DishID;
    E_Dish -> A_DishName;
    E_Dish -> A_DishPrice;
    E_Dish -> A_DishDesc;
    E_Dish -> A_DishImg;
    E_Dish -> A_DishStock;
    E_Dish -> A_DishStat;

    // Layout
    { rank=min; E_Merchant; }
    { rank=same; R_HasCat; R_HasDish; }
    { rank=same; E_Category; }
    { rank=max; E_Dish; }
}
'''

# ===================================================================
# DIAGRAM 3: 订单与配送管理模块局部ER图
# (Most complex - need careful layout)
# ===================================================================
D3 = '''digraph ER_OrderDelivery {
    rankdir=TB;
    splines=polyline;
    nodesep=0.4;
    ranksep=0.7;

    node [fontname="SimHei", fontsize=9];
    edge [fontname="SimSun", fontsize=8];

    // === ENTITIES ===
    node [shape=box, style="filled,bold", fillcolor="#D6EAF8", fontsize=11];
    E_Customer  [label="顾客"];
    E_Merchant  [label="商家"];
    E_CartItem   [label="购物车项"];
    E_Dish      [label="菜品"];
    E_Order     [label="订单"];
    E_OrderDet  [label="订单明细"];
    E_Delivery  [label="配送"];
    E_Rider     [label="骑手"];

    // === RELATIONSHIPS ===
    node [shape=diamond, style=filled, fillcolor="#FDEBD0", fontsize=8, width=0.6, height=0.6];
    R_CartAdd   [label="加入"];
    R_CartRef   [label="对应"];
    R_OrderSub  [label="提交"];
    R_OrderRec  [label="接收"];
    R_OrderCont [label="包含"];
    R_DetRef    [label="对应"];
    R_DelGen    [label="生成"];
    R_DelExe    [label="执行"];

    // === Attributes (limited to key ones for readability) ===
    node [shape=ellipse, style=filled, fillcolor="#E8F8F5", fontsize=7];
    // Customer
    A_CID   [label=<&#x5C;&#x5C;顾客ID&#x5C;&#x5C;>];
    A_CName [label="姓名"];
    A_CAddr [label="配送地址"];
    // Merchant
    A_MID2  [label=<&#x5C;&#x5C;商家ID&#x5C;&#x5C;>];
    A_MName2 [label="商家名称"];
    // CartItem
    A_CIID  [label=<&#x5C;&#x5C;购物车项ID&#x5C;&#x5C;>];
    A_CINum [label="数量"];
    // Dish
    A_DisID    [label=<&#x5C;&#x5C;菜品ID&#x5C;&#x5C;>];
    A_DisName  [label="菜品名称"];
    A_DisPrice [label="菜品价格"];
    A_DisStock [label="库存量"];
    // Order
    A_OID    [label=<&#x5C;&#x5C;订单编号&#x5C;&#x5C;>];
    A_OTime  [label="下单时间"];
    A_OStat  [label="订单状态"];
    A_OTotal [label="总金额"];
    A_ONote  [label="订单备注"];
    // Order Detail
    A_ODID   [label=<&#x5C;&#x5C;明细ID&#x5C;&#x5C;>];
    A_ODNum  [label="数量"];
    A_ODPrice [label="成交单价"];
    // Delivery
    A_DelID  [label=<&#x5C;&#x5C;配送ID&#x5C;&#x5C;>];
    A_DelPick [label="取餐时间"];
    A_DelArr  [label="送达时间"];
    A_DelStat [label="配送状态"];
    // Rider
    A_RID2    [label=<&#x5C;&#x5C;骑手ID&#x5C;&#x5C;>];
    A_RName2  [label="姓名"];
    A_RStat2  [label="配送状态"];
    A_RTotal2 [label="累计配送单数"];

    // === Relationship edges ===
    edge [style=solid, penwidth=1.2];
    E_Customer -> R_CartAdd [headlabel="1", labeldistance=2];
    R_CartAdd -> E_CartItem [headlabel="N", labeldistance=2];
    E_CartItem -> R_CartRef [headlabel="N", labeldistance=2];
    R_CartRef -> E_Dish [headlabel="1", labeldistance=2];

    E_Customer -> R_OrderSub [headlabel="1", labeldistance=2];
    R_OrderSub -> E_Order [headlabel="N", labeldistance=2];
    E_Merchant -> R_OrderRec [headlabel="1", labeldistance=2];
    R_OrderRec -> E_Order [headlabel="N", labeldistance=2];

    E_Order -> R_OrderCont [headlabel="1", labeldistance=2];
    R_OrderCont -> E_OrderDet [headlabel="N", labeldistance=2];
    E_OrderDet -> R_DetRef [headlabel="N", labeldistance=2];
    R_DetRef -> E_Dish [headlabel="1", labeldistance=2];

    E_Order -> R_DelGen [headlabel="1", labeldistance=2];
    R_DelGen -> E_Delivery [headlabel="1", labeldistance=2];
    E_Rider -> R_DelExe [headlabel="1", labeldistance=2];
    R_DelExe -> E_Delivery [headlabel="N", labeldistance=2];

    // === Attribute edges ===
    edge [style=solid, penwidth=0.6, arrowhead=none];
    E_Customer -> A_CID; E_Customer -> A_CName; E_Customer -> A_CAddr;
    E_Merchant -> A_MID2; E_Merchant -> A_MName2;
    E_CartItem -> A_CIID; E_CartItem -> A_CINum;
    E_Dish -> A_DisID; E_Dish -> A_DisName; E_Dish -> A_DisPrice; E_Dish -> A_DisStock;
    E_Order -> A_OID; E_Order -> A_OTime; E_Order -> A_OStat; E_Order -> A_OTotal; E_Order -> A_ONote;
    E_OrderDet -> A_ODID; E_OrderDet -> A_ODNum; E_OrderDet -> A_ODPrice;
    E_Delivery -> A_DelID; E_Delivery -> A_DelPick; E_Delivery -> A_DelArr; E_Delivery -> A_DelStat;
    E_Rider -> A_RID2; E_Rider -> A_RName2; E_Rider -> A_RStat2; E_Rider -> A_RTotal2;

    // Layout constraints: group sections
    { rank=min; E_Customer; E_Merchant; }
    { rank=same; E_CartItem; E_Order; R_CartAdd; R_OrderSub; R_OrderRec; }
    { rank=same; E_Dish; E_OrderDet; E_Delivery; R_CartRef; R_OrderCont; R_DetRef; R_DelGen; }
    { rank=max; E_Rider; R_DelExe; }
}
'''

# ===================================================================
# DIAGRAM 4: 全局ER图 (entities + relationships only, no attributes)
# ===================================================================
D4 = '''digraph ER_Global {
    rankdir=TB;
    splines=polyline;
    nodesep=0.5;
    ranksep=0.8;

    node [fontname="SimHei", fontsize=10];
    edge [fontname="SimSun", fontsize=8];

    // === ENTITIES ===
    node [shape=box, style="filled,bold", fillcolor="#D6EAF8", fontsize=11, width=1.2, height=0.5];
    E_User     [label="用户"];
    E_Admin    [label="管理员"];
    E_Merchant [label="商家"];
    E_Customer [label="顾客"];
    E_Rider    [label="骑手"];
    E_Category [label="菜品分类"];
    E_Dish     [label="菜品"];
    E_CartItem [label="购物车项"];
    E_Order    [label="订单"];
    E_OrderDet [label="订单明细"];
    E_Delivery [label="配送"];

    // === RELATIONSHIPS ===
    node [shape=diamond, style=filled, fillcolor="#FDEBD0", fontsize=8, width=0.55, height=0.55];
    R_ISA1 [label="ISA"];
    R_ISA2 [label="ISA"];
    R_ISA3 [label="ISA"];
    R_ISA4 [label="ISA"];
    R_HasCat  [label="拥有"];
    R_Contains [label="包含"];
    R_HasDish [label="直接拥有"];
    R_CartAdd  [label="加入"];
    R_CartRef  [label="对应"];
    R_OrderSub [label="提交"];
    R_OrderRec [label="接收"];
    R_OrderCont [label="包含"];
    R_DetRef   [label="对应"];
    R_DelGen   [label="生成"];
    R_DelExe   [label="执行"];

    // === Edges: ISA hierarchy ===
    edge [style=solid, penwidth=1.2];
    E_User -> R_ISA1 [headlabel="1"];
    R_ISA1 -> E_Admin [headlabel="1"];
    E_User -> R_ISA2 [headlabel="1"];
    R_ISA2 -> E_Merchant [headlabel="1"];
    E_User -> R_ISA3 [headlabel="1"];
    R_ISA3 -> E_Customer [headlabel="1"];
    E_User -> R_ISA4 [headlabel="1"];
    R_ISA4 -> E_Rider [headlabel="1"];

    // === Edges: Merchant-Category-Dish ===
    E_Merchant -> R_HasCat [headlabel="1"];
    R_HasCat -> E_Category [headlabel="N"];
    E_Category -> R_Contains [headlabel="1"];
    R_Contains -> E_Dish [headlabel="N"];
    E_Merchant -> R_HasDish [headlabel="1"];
    R_HasDish -> E_Dish [headlabel="N"];

    // === Edges: Cart ===
    E_Customer -> R_CartAdd [headlabel="1"];
    R_CartAdd -> E_CartItem [headlabel="N"];
    E_CartItem -> R_CartRef [headlabel="N"];
    R_CartRef -> E_Dish [headlabel="1"];

    // === Edges: Order ===
    E_Customer -> R_OrderSub [headlabel="1"];
    R_OrderSub -> E_Order [headlabel="N"];
    E_Merchant -> R_OrderRec [headlabel="1"];
    R_OrderRec -> E_Order [headlabel="N"];
    E_Order -> R_OrderCont [headlabel="1"];
    R_OrderCont -> E_OrderDet [headlabel="N"];
    E_OrderDet -> R_DetRef [headlabel="N"];
    R_DetRef -> E_Dish [headlabel="1"];

    // === Edges: Delivery ===
    E_Order -> R_DelGen [headlabel="1"];
    R_DelGen -> E_Delivery [headlabel="1"];
    E_Rider -> R_DelExe [headlabel="1"];
    R_DelExe -> E_Delivery [headlabel="N"];

    // === Layout ===
    { rank=min; E_User; }
    { rank=same; R_ISA1; R_ISA2; R_ISA3; R_ISA4; }
    { rank=same; E_Admin; E_Merchant; E_Customer; E_Rider; }
    { rank=same; R_HasCat; R_OrderSub; R_OrderRec; R_CartAdd; R_DelExe; }
    { rank=same; E_Category; E_CartItem; E_Order; R_HasDish; }
    { rank=same; R_Contains; R_CartRef; R_OrderCont; R_DelGen; }
    { rank=same; E_Dish; E_OrderDet; E_Delivery; R_DetRef; }
}
'''

# ===================================================================
# DIAGRAM 5: 数据库表关系图 (keep using proper DB schema notation)
# Each table as a box with columns, FK relationships
# ===================================================================
D5 = '''digraph DB_Schema {
    rankdir=TB;
    splines=ortho;
    nodesep=0.3;
    ranksep=0.5;

    node [fontname="SimSun", fontsize=9];
    edge [fontname="SimSun", fontsize=8, penwidth=1.0];

    // Tables as records (structured box showing columns)
    node [shape=record, style=filled, fillcolor="#EBF5FB"];

    T_User [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#3498DB"><font color="white"><b>用户 (User)</b></font></td></tr>
        <tr><td align="left"><u>用户ID</u> (PK)</td></tr>
        <tr><td align="left">手机号 (UNIQUE)</td></tr>
        <tr><td align="left">登录密码</td></tr>
        <tr><td align="left">用户身份</td></tr>
        <tr><td align="left">账号状态</td></tr>
        <tr><td align="left">注册时间</td></tr>
        </table>
    >];

    T_Admin [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#2ECC71"><font color="white"><b>管理员 (Admin)</b></font></td></tr>
        <tr><td align="left"><u>管理员ID</u> (PK/FK)</td></tr>
        <tr><td align="left">姓名</td></tr>
        </table>
    >];

    T_Merchant [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#2ECC71"><font color="white"><b>商家 (Merchant)</b></font></td></tr>
        <tr><td align="left"><u>商家ID</u> (PK/FK)</td></tr>
        <tr><td align="left">商家名称 (UNIQUE)</td></tr>
        <tr><td align="left">联系电话</td></tr>
        <tr><td align="left">营业地址(省/市/区/详细)</td></tr>
        <tr><td align="left">营业时间(起始/结束)</td></tr>
        <tr><td align="left">商家公告</td></tr>
        <tr><td align="left">营业状态</td></tr>
        <tr><td align="left">入驻审核状态</td></tr>
        <tr><td align="left">月销量</td></tr>
        </table>
    >];

    T_Customer [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#2ECC71"><font color="white"><b>顾客 (Customer)</b></font></td></tr>
        <tr><td align="left"><u>顾客ID</u> (PK/FK)</td></tr>
        <tr><td align="left">姓名</td></tr>
        <tr><td align="left">配送地址(省/市/区/详细)</td></tr>
        </table>
    >];

    T_Rider [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#2ECC71"><font color="white"><b>骑手 (Rider)</b></font></td></tr>
        <tr><td align="left"><u>骑手ID</u> (PK/FK)</td></tr>
        <tr><td align="left">姓名</td></tr>
        <tr><td align="left">配送状态</td></tr>
        <tr><td align="left">累计配送单数</td></tr>
        </table>
    >];

    T_Category [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#E67E22"><font color="white"><b>菜品分类 (Category)</b></font></td></tr>
        <tr><td align="left"><u>分类ID</u> (PK)</td></tr>
        <tr><td align="left">商家ID (FK)</td></tr>
        <tr><td align="left">分类名称</td></tr>
        <tr><td align="left">排序序号</td></tr>
        </table>
    >];

    T_Dish [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#E67E22"><font color="white"><b>菜品 (Dish)</b></font></td></tr>
        <tr><td align="left"><u>菜品ID</u> (PK)</td></tr>
        <tr><td align="left">商家ID (FK)</td></tr>
        <tr><td align="left">分类ID (FK)</td></tr>
        <tr><td align="left">菜品名称</td></tr>
        <tr><td align="left">菜品价格</td></tr>
        <tr><td align="left">菜品描述</td></tr>
        <tr><td align="left">菜品图片URL</td></tr>
        <tr><td align="left">库存量</td></tr>
        <tr><td align="left">上架状态</td></tr>
        </table>
    >];

    T_CartItem [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#9B59B6"><font color="white"><b>购物车项 (CartItem)</b></font></td></tr>
        <tr><td align="left"><u>购物车项ID</u> (PK)</td></tr>
        <tr><td align="left">顾客ID (FK)</td></tr>
        <tr><td align="left">菜品ID (FK)</td></tr>
        <tr><td align="left">数量</td></tr>
        </table>
    >];

    T_Order [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#E74C3C"><font color="white"><b>订单 (Order)</b></font></td></tr>
        <tr><td align="left"><u>订单编号</u> (PK)</td></tr>
        <tr><td align="left">顾客ID (FK)</td></tr>
        <tr><td align="left">商家ID (FK)</td></tr>
        <tr><td align="left">下单时间</td></tr>
        <tr><td align="left">订单状态</td></tr>
        <tr><td align="left">总金额</td></tr>
        <tr><td align="left">订单备注</td></tr>
        </table>
    >];

    T_OrderDet [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#E74C3C"><font color="white"><b>订单明细 (OrderDetail)</b></font></td></tr>
        <tr><td align="left"><u>明细ID</u> (PK)</td></tr>
        <tr><td align="left">订单编号 (FK)</td></tr>
        <tr><td align="left">菜品ID (FK)</td></tr>
        <tr><td align="left">数量</td></tr>
        <tr><td align="left">成交单价</td></tr>
        </table>
    >];

    T_Delivery [label=<
        <table border="0" cellborder="1" cellspacing="0" cellpadding="3">
        <tr><td bgcolor="#1ABC9C"><font color="white"><b>配送 (Delivery)</b></font></td></tr>
        <tr><td align="left"><u>配送ID</u> (PK)</td></tr>
        <tr><td align="left">订单编号 (FK, UNIQUE)</td></tr>
        <tr><td align="left">骑手ID (FK)</td></tr>
        <tr><td align="left">取餐时间</td></tr>
        <tr><td align="left">送达时间</td></tr>
        <tr><td align="left">配送状态</td></tr>
        </table>
    >];

    // === FK Relationships ===
    edge [style=dashed, penwidth=1.0, color="#C0392B"];

    T_Admin:e -> T_User:w [label="  管理员ID=用户ID (1:1)"];
    T_Merchant:e -> T_User:w [label="  商家ID=用户ID (1:1)"];
    T_Customer:e -> T_User:w [label="  顾客ID=用户ID (1:1)"];
    T_Rider:e -> T_User:w [label="  骑手ID=用户ID (1:1)"];

    T_Category -> T_Merchant [label="  商家ID FK (N:1)"];
    T_Dish:w -> T_Merchant:e [label="  商家ID FK (N:1)"];
    T_Dish -> T_Category [label="  分类ID FK (N:1)"];

    T_CartItem:w -> T_Customer:e [label="  顾客ID FK (N:1)"];
    T_CartItem -> T_Dish [label="  菜品ID FK (N:1)"];

    T_Order:w -> T_Customer:e [label="  顾客ID FK (N:1)"];
    T_Order -> T_Merchant [label="  商家ID FK (N:1)"];

    T_OrderDet:w -> T_Order:e [label="  订单编号 FK (N:1)"];
    T_OrderDet -> T_Dish [label="  菜品ID FK (N:1)"];

    T_Delivery:w -> T_Order:e [label="  订单编号 FK (1:1)"];
    T_Delivery:e -> T_Rider:w [label="  骑手ID FK (N:1)"];

    // Layout
    { rank=min; T_User; }
    { rank=same; T_Admin; T_Merchant; T_Customer; T_Rider; }
    { rank=same; T_Category; T_Order; T_CartItem; }
    { rank=same; T_Dish; T_OrderDet; T_Delivery; }
}
'''

# ===================================================================
# Generate all
# ===================================================================
diagrams = [
    ('01-用户管理模块局部ER图', D1),
    ('02-商家与菜品管理模块局部ER图', D2),
    ('03-订单与配送管理模块局部ER图', D3),
    ('04-全局ER图', D4),
    ('05-数据库表关系图', D5),
]

for name, code in diagrams:
    run_dot(name, code)

print("\nDone. Check /home/asuka/takeout/diagrams/ for output.")
