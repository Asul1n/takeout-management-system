#!/usr/bin/env python3
"""
Generate .drawio files for Chen-style E-R diagrams.
Open with https://app.diagrams.net (free, no install needed).

Each diagram has manually positioned elements:
  - Entity = blue rectangle
  - Attribute = green ellipse
  - Relationship = orange diamond
  - PK attributes = underlined text
"""

import os
import html as html_mod
import xml.etree.ElementTree as ET

BASE = '/home/asuka/takeout/diagrams'
os.makedirs(BASE, exist_ok=True)

def escape_xml(s):
    """Escape HTML content for XML attribute value."""
    return html_mod.escape(s, quote=True)

def make_drawio(cells, diagram_name="Page-1"):
    """Wrap cells into a complete .drawio XML file."""
    cell_xml = '\n'.join(cells)
    return f'''<?xml version="1.0" encoding="UTF-8"?>
<mxfile host="app.diagrams.net" modified="2026-05-16T00:00:00.000Z" agent="Claude" version="24.0.0">
  <diagram name="{diagram_name}" id="diagram-1">
    <mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1600" pageHeight="1200" math="0" shadow="0">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        {cell_xml}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

# Shape styles
STYLE_ENTITY = 'rounded=0;whiteSpace=wrap;html=1;fillColor=#D6EAF8;strokeColor=#2E86C1;strokeWidth=2;fontFamily=SimHei;fontSize=14;fontStyle=1;'
STYLE_ATTR   = 'ellipse;whiteSpace=wrap;html=1;fillColor=#E8F8F5;strokeColor=#1ABC9C;strokeWidth=1;fontFamily=SimSun;fontSize=10;'
STYLE_ATTR_PK = 'ellipse;whiteSpace=wrap;html=1;fillColor=#E8F8F5;strokeColor=#1ABC9C;strokeWidth=1;fontFamily=SimSun;fontSize=10;fontStyle=4;'  # underline=4
STYLE_DIAMOND = 'rhombus;whiteSpace=wrap;html=1;fillColor=#FDEBD0;strokeColor=#E67E22;strokeWidth=1.5;fontFamily=SimHei;fontSize=10;'
STYLE_EDGE_SOLID = 'edgeStyle=entityRelationEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;strokeWidth=1.5;fontFamily=SimSun;fontSize=9;startArrow=none;endArrow=none;'
STYLE_EDGE_DASHED = 'edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;strokeWidth=1;dashed=1;fontFamily=SimSun;fontSize=9;'
STYLE_EDGE_ATTR = 'edgeStyle=entityRelationEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;strokeWidth=0.8;endArrow=none;startArrow=none;fontFamily=SimSun;fontSize=8;'

cell_id = [2]  # mutable counter

def E(label, x, y, w=120, h=50):
    """Entity rectangle"""
    cid = cell_id[0]; cell_id[0] += 1
    return f'<mxCell id="{cid}" value="{label}" style="{STYLE_ENTITY}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>', cid

def A(label, x, y, pk=False, w=90, h=28):
    """Attribute ellipse"""
    cid = cell_id[0]; cell_id[0] += 1
    st = STYLE_ATTR_PK if pk else STYLE_ATTR
    return f'<mxCell id="{cid}" value="{label}" style="{st}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>', cid

def R(label, x, y, w=60, h=40):
    """Relationship diamond"""
    cid = cell_id[0]; cell_id[0] += 1
    return f'<mxCell id="{cid}" value="{label}" style="{STYLE_DIAMOND}" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>', cid

def edge(src, tgt, label="", dashed=False):
    """Edge between two cells"""
    cid = cell_id[0]; cell_id[0] += 1
    st = STYLE_EDGE_DASHED if dashed else STYLE_EDGE_SOLID
    return f'<mxCell id="{cid}" value="{label}" style="{st}" edge="1" parent="1" source="{src}" target="{tgt}"><mxGeometry relative="1" as="geometry"/></mxCell>', cid

def edge_attr(src, tgt):
    """Edge from entity to attribute (no arrow)"""
    cid = cell_id[0]; cell_id[0] += 1
    return f'<mxCell id="{cid}" value="" style="{STYLE_EDGE_ATTR}" edge="1" parent="1" source="{src}" target="{tgt}"><mxGeometry relative="1" as="geometry"/></mxCell>', cid

# ======================================================================
# DIAGRAM 1: 用户管理模块局部ER图
# ======================================================================
def diagram1():
    cells = []
    # --- Entities (rectangles) ---
    e_user,     uid_user     = E('用户', 300, 80, 130, 55)
    e_admin,    uid_admin    = E('管理员', 60, 400, 110, 50)
    e_merchant, uid_merchant = E('商家', 250, 400, 110, 50)
    e_cust,     uid_cust     = E('顾客', 440, 400, 110, 50)
    e_rider,    uid_rider    = E('骑手', 630, 400, 110, 50)
    cells += [e_user, e_admin, e_merchant, e_cust, e_rider]

    # --- ISA Diamonds ---
    r_isa1, uid_isa1 = R('ISA', 95, 250, 55, 35)
    r_isa2, uid_isa2 = R('ISA', 285, 250, 55, 35)
    r_isa3, uid_isa3 = R('ISA', 475, 250, 55, 35)
    r_isa4, uid_isa4 = R('ISA', 665, 250, 55, 35)
    cells += [r_isa1, r_isa2, r_isa3, r_isa4]

    # --- Edges: User → ISA → Sub-entity ---
    cells += [edge(uid_user, uid_isa1, '1')[0], edge(uid_isa1, uid_admin, '1')[0]]
    cells += [edge(uid_user, uid_isa2, '1')[0], edge(uid_isa2, uid_merchant, '1')[0]]
    cells += [edge(uid_user, uid_isa3, '1')[0], edge(uid_isa3, uid_cust, '1')[0]]
    cells += [edge(uid_user, uid_isa4, '1')[0], edge(uid_isa4, uid_rider, '1')[0]]

    # --- User attributes (above) ---
    a_uid,  au1 = A('用户ID', 310, 20, pk=True)
    a_tel,  au2 = A('手机号', 440, 20)
    a_pwd,  au3 = A('登录密码', 20, 120)
    a_role, au4 = A('用户身份', 20, 160)
    a_stat, au5 = A('账号状态', 480, 120)
    a_time, au6 = A('注册时间', 480, 160)
    cells += [a_uid, a_tel, a_pwd, a_role, a_stat, a_time]
    for au in [au1, au2, au3, au4, au5, au6]:
        cells += [edge_attr(uid_user, au)[0]]

    # --- Admin attributes ---
    a_aid,  aa1 = A('管理员ID', -20, 440, pk=True)
    a_aname, aa2 = A('姓名', -20, 480)
    cells += [a_aid, a_aname]
    cells += [edge_attr(uid_admin, aa1)[0], edge_attr(uid_admin, aa2)[0]]

    # --- Merchant attributes ---
    merch_attrs = [
        ('商家ID', 180, 480, True), ('商家名称', 300, 480, False),
        ('联系电话', 180, 520, False), ('营业地址-省', 300, 520, False),
        ('营业地址-市', 180, 560, False), ('营业地址-区', 300, 560, False),
        ('营业地址-详细', 180, 600, False), ('营业时间-起始', 300, 600, False),
        ('营业时间-结束', 180, 640, False), ('商家公告', 300, 640, False),
        ('营业状态', 180, 680, False), ('入驻审核状态', 300, 680, False),
        ('月销量', 180, 720, False),
    ]
    for label, x, y, pk in merch_attrs:
        a, aid = A(label, x, y, pk=pk)
        cells += [a, edge_attr(uid_merchant, aid)[0]]

    # --- Customer attributes ---
    cust_attrs = [
        ('顾客ID', 390, 480, True), ('姓名', 520, 480, False),
        ('配送地址-省', 390, 520, False), ('配送地址-市', 520, 520, False),
        ('配送地址-区', 390, 560, False), ('配送地址-详细', 520, 560, False),
    ]
    for label, x, y, pk in cust_attrs:
        a, aid = A(label, x, y, pk=pk)
        cells += [a, edge_attr(uid_cust, aid)[0]]

    # --- Rider attributes ---
    rider_attrs = [
        ('骑手ID', 600, 480, True), ('姓名', 730, 480, False),
        ('配送状态', 600, 520, False), ('累计配送单数', 730, 520, False),
    ]
    for label, x, y, pk in rider_attrs:
        a, aid = A(label, x, y, pk=pk)
        cells += [a, edge_attr(uid_rider, aid)[0]]

    return make_drawio(cells, "用户管理模块局部E-R图")

# ======================================================================
# DIAGRAM 2: 商家与菜品管理模块局部ER图
# ======================================================================
def diagram2():
    cells = []

    # Entities
    e_mer, uid_mer = E('商家', 60, 300, 120, 55)
    e_cat, uid_cat = E('菜品分类', 350, 300, 120, 55)
    e_dish, uid_dish = E('菜品', 640, 300, 120, 55)
    cells += [e_mer, e_cat, e_dish]

    # Relationships
    r_has,  uid_has  = R('拥有', 210, 305, 55, 35)
    r_cont, uid_cont = R('包含', 500, 305, 55, 35)
    r_direct, uid_direct = R('直接拥有', 340, 160, 70, 45)
    cells += [r_has, r_cont, r_direct]

    # Edges
    cells += [edge(uid_mer, uid_has, '1')[0], edge(uid_has, uid_cat, 'N')[0]]
    cells += [edge(uid_cat, uid_cont, '1')[0], edge(uid_cont, uid_dish, 'N')[0]]
    cells += [edge(uid_mer, uid_direct, '1')[0], edge(uid_direct, uid_dish, 'N')[0]]

    # Merchant attributes (left)
    for i, (label, pk) in enumerate([('商家ID', True), ('商家名称', False), ('联系电话', False), ('营业状态', False), ('月销量', False)]):
        a, aid = A(label, 20, 20 + i*35, pk=pk)
        cells += [a, edge_attr(uid_mer, aid)[0]]

    # Category attributes (top)
    for i, (label, pk) in enumerate([('分类ID', True), ('分类名称', False), ('排序序号', False)]):
        a, aid = A(label, 340 + i*110, 20, pk=pk)
        cells += [a, edge_attr(uid_cat, aid)[0]]

    # Dish attributes (right)
    y = 20
    for label, pk in [('菜品ID', True), ('菜品名称', False), ('菜品价格', False), ('菜品描述', False), ('菜品图片URL', False), ('库存量', False), ('上架状态', False)]:
        a, aid = A(label, 640, y, pk=pk)
        y += 35
        cells += [a, edge_attr(uid_dish, aid)[0]]

    # Category FK attributes
    a_cfkid, afid1 = A('商家ID(FK)', 350, 380)
    cells += [a_cfkid, edge_attr(uid_cat, afid1)[0]]
    # Dish FK attributes
    a_dfk1, afid2 = A('商家ID(FK)', 640, 380)
    a_dfk2, afid3 = A('分类ID(FK)', 640, 415)
    cells += [a_dfk1, edge_attr(uid_dish, afid2)[0]]
    cells += [a_dfk2, edge_attr(uid_dish, afid3)[0]]

    return make_drawio(cells, "商家与菜品管理模块局部E-R图")

# ======================================================================
# DIAGRAM 3: 订单与配送管理模块局部ER图
# ======================================================================
def diagram3():
    cells = []

    # Entities - arranged in 3 rows
    e_cust, uid_cust = E('顾客', 60, 80, 110, 50)
    e_mer,  uid_mer  = E('商家', 400, 80, 110, 50)

    e_cart, uid_cart = E('购物车项', 60, 300, 110, 50)
    e_order, uid_order = E('订单', 250, 300, 110, 50)
    e_odet, uid_odet  = E('订单明细', 440, 300, 110, 50)

    e_dish, uid_dish   = E('菜品', 60, 550, 110, 50)
    e_del,  uid_del    = E('配送', 280, 550, 110, 50)
    e_rider, uid_rider = E('骑手', 500, 550, 110, 50)
    cells += [e_cust, e_mer, e_cart, e_order, e_odet, e_dish, e_del, e_rider]

    # Relationships
    r_cadd, uid_cadd = R('加入', 130, 180, 50, 35)
    r_cref, uid_cref = R('对应', 130, 440, 50, 35)
    r_osub, uid_osub = R('提交', 200, 180, 50, 35)
    r_orec, uid_orec = R('接收', 360, 180, 50, 35)
    r_ocon, uid_ocon = R('包含', 300, 440, 50, 35)
    r_dref, uid_dref = R('对应', 440, 440, 50, 35)
    r_dgen, uid_dgen = R('生成', 230, 650, 50, 35)
    r_dexe, uid_dexe = R('执行', 440, 650, 50, 35)
    cells += [r_cadd, r_cref, r_osub, r_orec, r_ocon, r_dref, r_dgen, r_dexe]

    # Relationship edges
    # Cart: 顾客→加入→购物车项→对应→菜品
    cells += [edge(uid_cust, uid_cadd, '1')[0], edge(uid_cadd, uid_cart, 'N')[0]]
    cells += [edge(uid_cart, uid_cref, 'N')[0], edge(uid_cref, uid_dish, '1')[0]]
    # Order: 顾客→提交→订单, 商家→接收→订单
    cells += [edge(uid_cust, uid_osub, '1')[0], edge(uid_osub, uid_order, 'N')[0]]
    cells += [edge(uid_mer, uid_orec, '1')[0], edge(uid_orec, uid_order, 'N')[0]]
    # Order contains details
    cells += [edge(uid_order, uid_ocon, '1')[0], edge(uid_ocon, uid_odet, 'N')[0]]
    cells += [edge(uid_odet, uid_dref, 'N')[0], edge(uid_dref, uid_dish, '1')[0]]
    # Delivery
    cells += [edge(uid_del, uid_dgen, '1')[0], edge(uid_dgen, uid_order, '1')[0]]  # Note: reversed for layout
    cells += [edge(uid_rider, uid_dexe, '1')[0], edge(uid_dexe, uid_del, 'N')[0]]

    # Attributes (limited to key ones for readability)
    # Customer attrs
    for i, (label, pk) in enumerate([('顾客ID', True), ('姓名', False), ('配送地址', False)]):
        a, aid = A(label, -50, 110 + i*30, pk=pk)
        cells += [a, edge_attr(uid_cust, aid)[0]]
    # Merchant attrs
    for i, (label, pk) in enumerate([('商家ID', True), ('商家名称', False)]):
        a, aid = A(label, 540, 90 + i*30, pk=pk)
        cells += [a, edge_attr(uid_mer, aid)[0]]
    # CartItem attrs
    for i, (label, pk) in enumerate([('购物车项ID', True), ('数量', False)]):
        a, aid = A(label, -50, 320 + i*30, pk=pk)
        cells += [a, edge_attr(uid_cart, aid)[0]]
    # Order attrs
    for i, (label, pk) in enumerate([('订单编号', True), ('下单时间', False), ('订单状态', False), ('总金额', False), ('备注', False)]):
        a, aid = A(label, 190, 370 + i*28, pk=pk)
        cells += [a, edge_attr(uid_order, aid)[0]]
    # OrderDetail attrs
    for i, (label, pk) in enumerate([('明细ID', True), ('数量', False), ('成交单价', False)]):
        a, aid = A(label, 570, 320 + i*30, pk=pk)
        cells += [a, edge_attr(uid_odet, aid)[0]]
    # Dish attrs
    for i, (label, pk) in enumerate([('菜品ID', True), ('菜品名称', False), ('菜品价格', False), ('库存量', False)]):
        a, aid = A(label, -50, 570 + i*30, pk=pk)
        cells += [a, edge_attr(uid_dish, aid)[0]]
    # Delivery attrs
    for i, (label, pk) in enumerate([('配送ID', True), ('取餐时间', False), ('送达时间', False), ('配送状态', False)]):
        a, aid = A(label, 220, 580 + i*30, pk=pk)
        cells += [a, edge_attr(uid_del, aid)[0]]
    # Rider attrs
    for i, (label, pk) in enumerate([('骑手ID', True), ('姓名', False), ('配送状态', False), ('累计配送单数', False)]):
        a, aid = A(label, 640, 570 + i*30, pk=pk)
        cells += [a, edge_attr(uid_rider, aid)[0]]

    return make_drawio(cells, "订单与配送管理模块局部E-R图")

# ======================================================================
# DIAGRAM 4: 全局ER图 — 全部实体 + 全部属性 + 全部联系
# Canvas: 2400 x 1800
# ======================================================================
def diagram4():
    cells = []
    # Use a wider attr function for this diagram
    def A2(label, x, y, pk=False, w=85, h=26):
        return A(label, x, y, pk=pk, w=w, h=h)

    # ========================
    # ROW 1: 用户实体 + 属性 (y~20-110)
    # ========================
    e_user, uid_user = E('用户', 500, 30, 120, 55)
    cells.append(e_user)
    uattrs = [('用户ID', 430, 5, True), ('手机号', 530, 5, False), ('登录密码', 370, 55, False),
              ('用户身份', 370, 85, False), ('账号状态', 630, 55, False), ('注册时间', 630, 85, False)]
    ua_ids = []
    for label, x, y, pk in uattrs:
        a, aid = A2(label, x, y, pk)
        cells.append(a); ua_ids.append(aid)
        cells.append(edge_attr(uid_user, aid)[0])

    # ========================
    # ROW 2: ISA diamonds (y~160)
    # ========================
    isa_ids = {}
    for i, name in enumerate(['Admin','Merchant','Customer','Rider']):
        r, rid = R('ISA', 65 + i*285, 160, 55, 35)
        cells.append(r)
        isa_ids[name] = rid
        cells.append(edge(uid_user, rid, '1')[0])

    # ========================
    # ROW 3: 管理员 + 商家 + 顾客 + 骑手 (y~260)
    # ========================
    # --- 管理员 (x~20) ---
    e_admin, uid_admin = E('管理员', 30, 260, 100, 48)
    cells.append(e_admin)
    cells.append(edge(isa_ids['Admin'], uid_admin, '1')[0])
    for i, (lbl, pk) in enumerate([('管理员ID', True), ('姓名', False)]):
        a, aid = A2(lbl, -20, 310 + i*30, pk)
        cells += [a, edge_attr(uid_admin, aid)[0]]

    # --- 商家 (x~280) ---
    e_mer, uid_mer = E('商家', 280, 260, 100, 48)
    cells.append(e_mer)
    cells.append(edge(isa_ids['Merchant'], uid_mer, '1')[0])
    merch_attrs = [('商家ID', True), ('商家名称', False), ('联系电话', False), ('营业地址-省', False),
                   ('营业地址-市', False), ('营业地址-区', False), ('营业地址-详细', False),
                   ('营业时间-起始', False), ('营业时间-结束', False), ('商家公告', False),
                   ('营业状态', False), ('入驻审核状态', False), ('月销量', False)]
    for i, (lbl, pk) in enumerate(merch_attrs):
        col = i % 2; row = i // 2
        a, aid = A2(lbl, 240 + col*115, 310 + row*30, pk)
        cells += [a, edge_attr(uid_mer, aid)[0]]

    # --- 顾客 (x~600) ---
    e_cust, uid_cust = E('顾客', 600, 260, 100, 48)
    cells.append(e_cust)
    cells.append(edge(isa_ids['Customer'], uid_cust, '1')[0])
    for i, (lbl, pk) in enumerate([('顾客ID', True), ('姓名', False), ('配送地址-省', False),
                                    ('配送地址-市', False), ('配送地址-区', False), ('配送地址-详细', False)]):
        col = i % 2; row = i // 2
        a, aid = A2(lbl, 560 + col*115, 310 + row*30, pk)
        cells += [a, edge_attr(uid_cust, aid)[0]]

    # --- 骑手 (x~880) ---
    e_rider, uid_rider = E('骑手', 880, 260, 100, 48)
    cells.append(e_rider)
    cells.append(edge(isa_ids['Rider'], uid_rider, '1')[0])
    for i, (lbl, pk) in enumerate([('骑手ID', True), ('姓名', False), ('配送状态', False), ('累计配送单数', False)]):
        col = i % 2; row = i // 2
        a, aid = A2(lbl, 850 + col*115, 310 + row*30, pk)
        cells += [a, edge_attr(uid_rider, aid)[0]]

    # ========================
    # ROW 4: 菜品分类 + 购物车项 + 订单 (y~500) + relationships between them
    # ========================
    # --- 菜品分类 (x~150) ---
    e_cat, uid_cat = E('菜品分类', 200, 530, 110, 50)
    cells.append(e_cat)
    for i, (lbl, pk) in enumerate([('分类ID', True), ('分类名称', False), ('排序序号', False)]):
        a, aid = A2(lbl, 140 + i*90, 470, pk)
        cells += [a, edge_attr(uid_cat, aid)[0]]
    a_catfk, aid_catfk = A2('商家ID(FK)', 170, 590)
    cells += [a_catfk, edge_attr(uid_cat, aid_catfk)[0]]

    # --- 购物车项 (x~450) ---
    e_cart, uid_cart = E('购物车项', 480, 530, 110, 50)
    cells.append(e_cart)
    for i, (lbl, pk) in enumerate([('购物车项ID', True), ('数量', False)]):
        a, aid = A2(lbl, 430 + i*100, 470, pk)
        cells += [a, edge_attr(uid_cart, aid)[0]]
    a_cartfk1, aid_c1 = A2('顾客ID(FK)', 440, 600)
    a_cartfk2, aid_c2 = A2('菜品ID(FK)', 540, 600)
    cells += [a_cartfk1, edge_attr(uid_cart, aid_c1)[0]]
    cells += [a_cartfk2, edge_attr(uid_cart, aid_c2)[0]]

    # --- 订单 (x~750) ---
    e_ord, uid_ord = E('订单', 760, 530, 110, 50)
    cells.append(e_ord)
    ord_attrs = [('订单编号', True), ('下单时间', False), ('订单状态', False), ('总金额', False), ('备注', False)]
    for i, (lbl, pk) in enumerate(ord_attrs):
        col = i % 3; row = i // 3
        a, aid = A2(lbl, 700 + col*95, 470 + row*30, pk)
        cells += [a, edge_attr(uid_ord, aid)[0]]
    a_ofk1, ao1 = A2('顾客ID(FK)', 700, 600)
    a_ofk2, ao2 = A2('商家ID(FK)', 800, 600)
    cells += [a_ofk1, edge_attr(uid_ord, ao1)[0]]
    cells += [a_ofk2, edge_attr(uid_ord, ao2)[0]]

    # ========================
    # ROW 5: 菜品 + 订单明细 + 配送 (y~780)
    # ========================
    # --- 菜品 (x~180) ---
    e_dish, uid_dish = E('菜品', 200, 780, 100, 50)
    cells.append(e_dish)
    dish_attrs = [('菜品ID', True), ('菜品名称', False), ('菜品价格', False), ('菜品描述', False),
                  ('菜品图片URL', False), ('库存量', False), ('上架状态', False)]
    for i, (lbl, pk) in enumerate(dish_attrs):
        col = i % 2; row = i // 2
        a, aid = A2(lbl, 130 + col*130, 720 + row*30, pk)
        cells += [a, edge_attr(uid_dish, aid)[0]]
    a_dishfk1, ad1 = A2('商家ID(FK)', 130, 840)
    a_dishfk2, ad2 = A2('分类ID(FK)', 260, 840)
    cells += [a_dishfk1, edge_attr(uid_dish, ad1)[0]]
    cells += [a_dishfk2, edge_attr(uid_dish, ad2)[0]]

    # --- 订单明细 (x~480) ---
    e_odet, uid_odet = E('订单明细', 480, 780, 110, 50)
    cells.append(e_odet)
    for i, (lbl, pk) in enumerate([('明细ID', True), ('数量', False), ('成交单价', False)]):
        a, aid = A2(lbl, 420 + i*90, 720, pk)
        cells += [a, edge_attr(uid_odet, aid)[0]]
    a_odfk1, aod1 = A2('订单编号(FK)', 440, 840)
    a_odfk2, aod2 = A2('菜品ID(FK)', 540, 840)
    cells += [a_odfk1, edge_attr(uid_odet, aod1)[0]]
    cells += [a_odfk2, edge_attr(uid_odet, aod2)[0]]

    # --- 配送 (x~760) ---
    e_del, uid_del = E('配送', 760, 780, 100, 50)
    cells.append(e_del)
    for i, (lbl, pk) in enumerate([('配送ID', True), ('取餐时间', False), ('送达时间', False), ('配送状态', False)]):
        col = i % 2; row = i // 2
        a, aid = A2(lbl, 720 + col*110, 720 + row*30, pk)
        cells += [a, edge_attr(uid_del, aid)[0]]
    a_delfk1, adf1 = A2('订单编号(FK)', 720, 840)
    a_delfk2, adf2 = A2('骑手ID(FK)', 840, 840)
    cells += [a_delfk1, edge_attr(uid_del, adf1)[0]]
    cells += [a_delfk2, edge_attr(uid_del, adf2)[0]]

    # ========================
    # RELATIONSHIP DIAMONDS (placed between entities)
    # ========================
    # Merchant → 拥有 → Category   (y~410, between row3 and row4)
    r_has, uid_has = R('拥有', 160, 440, 50, 32)
    cells.append(r_has)
    cells += [edge(uid_mer, uid_has, '1')[0], edge(uid_has, uid_cat, 'N')[0]]

    # Category → 包含 → Dish   (y~650, between row4 and row5)
    r_cont, uid_cont = R('包含', 180, 690, 50, 32)
    cells.append(r_cont)
    cells += [edge(uid_cat, uid_cont, '1')[0], edge(uid_cont, uid_dish, 'N')[0]]

    # Merchant → 直接拥有 → Dish
    r_dir, uid_dir = R('直接拥有', 100, 700, 70, 40)
    cells.append(r_dir)
    cells += [edge(uid_mer, uid_dir, '1')[0], edge(uid_dir, uid_dish, 'N')[0]]

    # Customer → 加入 → CartItem
    r_cadd, uid_cadd = R('加入', 390, 440, 50, 32)
    cells.append(r_cadd)
    cells += [edge(uid_cust, uid_cadd, '1')[0], edge(uid_cadd, uid_cart, 'N')[0]]

    # CartItem → 对应 → Dish
    r_cref, uid_cref = R('对应', 340, 690, 50, 32)
    cells.append(r_cref)
    cells += [edge(uid_cart, uid_cref, 'N')[0], edge(uid_cref, uid_dish, '1')[0]]

    # Customer → 提交 → Order
    r_osub, uid_osub = R('提交', 670, 440, 50, 32)
    cells.append(r_osub)
    cells += [edge(uid_cust, uid_osub, '1')[0], edge(uid_osub, uid_ord, 'N')[0]]

    # Merchant → 接收 → Order
    r_orec, uid_orec = R('接收', 720, 420, 50, 32)
    cells.append(r_orec)
    cells += [edge(uid_mer, uid_orec, '1')[0], edge(uid_orec, uid_ord, 'N')[0]]

    # Order → 包含 → OrderDetail
    r_ocon, uid_ocon = R('包含', 640, 690, 50, 32)
    cells.append(r_ocon)
    cells += [edge(uid_ord, uid_ocon, '1')[0], edge(uid_ocon, uid_odet, 'N')[0]]

    # OrderDetail → 对应 → Dish
    r_dref, uid_dref = R('对应', 370, 700, 50, 32)
    cells.append(r_dref)
    cells += [edge(uid_odet, uid_dref, 'N')[0], edge(uid_dref, uid_dish, '1')[0]]

    # Order → 生成 → Delivery
    r_dgen, uid_dgen = R('生成', 710, 710, 50, 32)
    cells.append(r_dgen)
    cells += [edge(uid_ord, uid_dgen, '1')[0], edge(uid_dgen, uid_del, '1')[0]]

    # Rider → 执行 → Delivery
    r_dexe, uid_dexe = R('执行', 880, 690, 50, 32)
    cells.append(r_dexe)
    cells += [edge(uid_rider, uid_dexe, '1')[0], edge(uid_dexe, uid_del, 'N')[0]]

    # Custom wrapper with larger canvas
    cell_xml = '\n'.join(cells)
    return f'''<?xml version="1.0" encoding="UTF-8"?>
<mxfile host="app.diagrams.net" modified="2026-05-16T00:00:00.000Z" agent="Claude" version="24.0.0">
  <diagram name="外卖管理系统全局E-R图" id="diagram-4">
    <mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1100" pageHeight="900" math="0" shadow="0">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        {cell_xml}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

# ======================================================================
# DIAGRAM 5: 数据库表关系图 (DB Schema)
# ======================================================================
def diagram5():
    import html as _html
    lines = []

    def build_table_html(name, cols, color='#3498DB'):
        rows = []
        rows.append(f'<tr><td bgcolor="{color}" style="color:white;font-weight:bold;font-size:12px;text-align:center;padding:4px 8px;">{name}</td></tr>')
        for label, anno in cols:
            if 'PK' in anno and 'FK' in anno:
                text = f'<u><i>{label}</i></u>'
            elif 'PK' in anno:
                text = f'<u>{label}</u>'
            elif 'FK' in anno:
                text = f'<i>{label}</i>'
            else:
                text = label
            if anno:
                text += f' <font color="#888888">({anno})</font>'
            rows.append(f'<tr><td style="font-size:9px;text-align:left;padding:2px 6px;">{text}</td></tr>')
        return '<table border="0" cellborder="1" cellspacing="0" cellpadding="0" style="width:160px;">' + ''.join(rows) + '</table>'

    def table(name, cols, x, y, color='#3498DB'):
        html = build_table_html(name, cols, color)
        cid = cell_id[0]; cell_id[0] += 1
        st = 'rounded=0;whiteSpace=wrap;html=1;fillColor=#EBF5FB;strokeColor=#2980B9;strokeWidth=1.5;'
        escaped = _html.escape(html, quote=True)
        return f'''        <mxCell id="{cid}" value="{escaped}" style="{st}" vertex="1" parent="1">
          <mxGeometry x="{x}" y="{y}" width="160" height="28" as="geometry"/>
        </mxCell>''', cid

    def fk_edge(src, tgt, label=""):
        cid = cell_id[0]; cell_id[0] += 1
        st = 'edgeStyle=entityRelationEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;strokeWidth=1.5;strokeColor=#C0392B;fontFamily=SimSun;fontSize=8;startArrow=none;endArrow=none;'
        return f'        <mxCell id="{cid}" value="{label}" style="{st}" edge="1" parent="1" source="{src}" target="{tgt}"><mxGeometry relative="1" as="geometry"/></mxCell>'

    def add(cell):
        lines.append(cell[0])

    # ROW 1: 用户表
    add(table('用户 (User)', [('用户ID','PK'),('手机号','UNIQUE'),('登录密码',''),('用户身份',''),('账号状态',''),('注册时间','')], 400, 20, '#2C3E50'))

    # ROW 2: 4个子表
    add(table('管理员 (Admin)', [('管理员ID','PK/FK'),('姓名','')], 20, 250, '#27AE60'))
    add(table('商家 (Merchant)', [('商家ID','PK/FK'),('商家名称','UNIQUE'),('联系电话',''),('营业地址(省市区详细)',''),('营业时间(起止)',''),('商家公告',''),('营业状态',''),('入驻审核状态',''),('月销量','')], 220, 250, '#27AE60'))
    add(table('顾客 (Customer)', [('顾客ID','PK/FK'),('姓名',''),('配送地址(省市区详细)','')], 430, 250, '#27AE60'))
    add(table('骑手 (Rider)', [('骑手ID','PK/FK'),('姓名',''),('配送状态',''),('累计配送单数','')], 640, 250, '#27AE60'))

    # ROW 3
    add(table('菜品分类 (Category)', [('分类ID','PK'),('商家ID','FK'),('分类名称',''),('排序序号','')], 20, 520, '#E67E22'))
    add(table('菜品 (Dish)', [('菜品ID','PK'),('商家ID','FK'),('分类ID','FK'),('菜品名称',''),('菜品价格',''),('菜品描述',''),('图片URL',''),('库存量',''),('上架状态','')], 220, 520, '#E67E22'))
    add(table('购物车项 (CartItem)', [('购物车项ID','PK'),('顾客ID','FK'),('菜品ID','FK'),('数量','')], 430, 520, '#8E44AD'))
    add(table('订单 (Order)', [('订单编号','PK'),('顾客ID','FK'),('商家ID','FK'),('下单时间',''),('订单状态',''),('总金额',''),('备注','')], 640, 520, '#C0392B'))

    # ROW 4
    add(table('订单明细 (OrderDetail)', [('明细ID','PK'),('订单编号','FK'),('菜品ID','FK'),('数量',''),('成交单价','')], 200, 820, '#C0392B'))
    add(table('配送 (Delivery)', [('配送ID','PK'),('订单编号','FK UNIQUE'),('骑手ID','FK'),('取餐时间',''),('送达时间',''),('配送状态','')], 500, 820, '#1ABC9C'))

    # FK edges - get IDs from cell_id counter sequence
    # User=2, Admin=3, Merchant=4, Customer=5, Rider=6, Category=7, Dish=8, CartItem=9, Order=10, OrderDetail=11, Delivery=12
    uid_user=2; uid_admin=3; uid_mer=4; uid_cust=5; uid_rider=6
    uid_cat=7; uid_dish=8; uid_cart=9; uid_order=10; uid_odet=11; uid_del=12

    # ISA FK edges
    lines.append(fk_edge(uid_admin, uid_user, '管理员ID→用户ID (1:1)'))
    lines.append(fk_edge(uid_mer, uid_user, '商家ID→用户ID (1:1)'))
    lines.append(fk_edge(uid_cust, uid_user, '顾客ID→用户ID (1:1)'))
    lines.append(fk_edge(uid_rider, uid_user, '骑手ID→用户ID (1:1)'))

    # FK edges
    lines.append(fk_edge(uid_cat, uid_mer, '商家ID FK (N:1)'))
    lines.append(fk_edge(uid_dish, uid_mer, '商家ID FK (N:1)'))
    lines.append(fk_edge(uid_dish, uid_cat, '分类ID FK (N:1)'))
    lines.append(fk_edge(uid_cart, uid_cust, '顾客ID FK (N:1)'))
    lines.append(fk_edge(uid_cart, uid_dish, '菜品ID FK (N:1)'))
    lines.append(fk_edge(uid_order, uid_cust, '顾客ID FK (N:1)'))
    lines.append(fk_edge(uid_order, uid_mer, '商家ID FK (N:1)'))
    lines.append(fk_edge(uid_odet, uid_order, '订单编号 FK (N:1)'))
    lines.append(fk_edge(uid_odet, uid_dish, '菜品ID FK (N:1)'))
    lines.append(fk_edge(uid_del, uid_order, '订单编号 FK (1:1)'))
    lines.append(fk_edge(uid_del, uid_rider, '骑手ID FK (N:1)'))

    cell_xml = '\n'.join(lines)
    return f'''<?xml version="1.0" encoding="UTF-8"?>
<mxfile host="app.diagrams.net" modified="2026-05-17T00:00:00.000Z" agent="Claude" version="24.0.0">
  <diagram name="数据库表关系图" id="diagram-5">
    <mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="900" pageHeight="1100" math="0" shadow="0">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
{cell_xml}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

# ======================================================================
# Generate all 5 .drawio files
# ======================================================================
diagrams = [
    ('01-用户管理模块局部ER图', diagram1),
    ('02-商家与菜品管理模块局部ER图', diagram2),
    ('03-订单与配送管理模块局部ER图', diagram3),
    ('04-全局ER图', diagram4),
    ('05-数据库表关系图', diagram5),
]

for name, func in diagrams:
    cell_id[0] = 2  # reset counter
    drawio_xml = func()
    path = os.path.join(BASE, f'{name}.drawio')
    with open(path, 'w', encoding='utf-8') as f:
        f.write(drawio_xml)
    size_kb = os.path.getsize(path) // 1024
    print(f'Generated: {name}.drawio ({size_kb}KB)')

print(f'\nDone. Files in {BASE}/')
print('Open https://app.diagrams.net → File → Open from → Device → select .drawio file')
