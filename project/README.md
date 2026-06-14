# 🍕 外卖管理系统

> 基于 Spring Boot 3 + Vue 3 的外卖管理平台，支持管理员、商家、顾客、骑手四角色协同工作。

## 项目结构

```
takeout/
├── takeout-server/                    # 后端 (Spring Boot)
│   ├── pom.xml                        # Maven 父 POM
│   ├── takeout-common/                # 公共模块（枚举、异常、统一响应、JWT）
│   ├── takeout-framework/             # 框架模块（Security、Redis、MyBatis-Plus、AOP）
│   ├── takeout-service/               # 业务启动模块
│   │   ├── src/main/java/com/takeout/
│   │   │   ├── TakeoutApplication.java
│   │   │   ├── config/DataInitializer.java     # 默认管理员初始化
│   │   │   └── module/
│   │   │       ├── user/              # 用户 + 认证 + 通知 + SSE
│   │   │       ├── merchant/          # 商家
│   │   │       ├── dish/              # 菜品 + 分类 + 购物车
│   │   │       ├── order/             # 订单
│   │   │       ├── delivery/          # 配送 + 骑手
│   │   │       └── statistics/        # 统计
│   │   └── src/main/resources/
│   │       ├── application.yml        # 公共配置
│   │       ├── application-dev.yml    # 开发环境
│   │       ├── sql/schema.sql         # 建表脚本（含 CHECK 约束）
│   │       └── mapper/                # MyBatis XML
│
├── takeout-admin/                     # 前端 (Vue 3 管理端)
│   ├── src/
│   │   ├── api/                       # Axios 接口封装
│   │   ├── router/                    # 路由 + 角色守卫
│   │   ├── stores/                    # Pinia 状态管理
│   │   ├── views/                     # 页面组件
│   │   │   ├── login/                 # 登录页
│   │   │   ├── dashboard/             # 首页（按角色差异化）
│   │   │   ├── user/                  # 用户管理 (管理员)
│   │   │   ├── merchant/              # 商家管理/审核/菜品/订单 (管理员+商家)
│   │   │   ├── customer/              # 浏览商家/我的订单/地址 (顾客)
│   │   │   ├── rider/                 # 配送任务/历史 (骑手)
│   │   │   ├── order/                 # 订单管理/详情 (管理员)
│   │   │   ├── delivery/              # 配送管理 (管理员)
│   │   │   └── statistics/            # 数据统计 (管理员)
│   │   ├── components/                # 公共组件
│   │   ├── utils/                     # 工具函数
│   │   └── directives/                # 权限指令
│   ├── package.json
│   └── vite.config.ts
│
└── README.md
```

## 技术栈

| 层级 | 技术 |
|------|------|
| **后端语言** | Java 17 |
| **后端框架** | Spring Boot 3.2 |
| **ORM** | MyBatis-Plus 3.5 |
| **数据库** | MySQL 8.0 |
| **缓存** | Redis 7.0 |
| **认证** | Spring Security + JWT (jjwt 0.12) |
| **API 文档** | Knife4j 4.4 (Swagger) |
| **工具库** | Hutool 5.8 / Lombok |
| **构建** | Maven 3.9 |
| **前端语言** | TypeScript 5 |
| **前端框架** | Vue 3 (Composition API) |
| **UI 库** | Element Plus 2.7 |
| **路由** | Vue Router 4 |
| **状态管理** | Pinia 2 |
| **构建** | Vite 5 |

## 快速开始

### 环境要求

| 工具 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 17+ | `JAVA_HOME` 需配置 |
| Maven | 3.6+ | 推荐 3.9 |
| MySQL | 8.0+ | 默认端口 3306 |
| Redis | 6.0+ | 默认端口 6379 |
| Node.js | 18+ | 推荐 20 LTS |
| npm | 9+ | 随 Node.js 自带 |

### 1. 克隆项目

```bash
git clone <repo-url>
cd takeout/project
```

### 2. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本（会创建 takeout 数据库 + 14 张表 + CHECK 约束）
source takeout-server/takeout-service/src/main/resources/sql/schema.sql;
```

> 数据库名 `takeout`，字符集 `utf8mb4`。如需修改连接信息，编辑 `application-dev.yml`。

### 3. 启动 Redis

```bash
# Linux
sudo systemctl start redis-server

# macOS
brew services start redis

# 或直接运行
redis-server
```

### 4. 启动后端

```bash
cd takeout-server

# 编译打包
mvn clean package -DskipTests

# 启动（开发环境）
java -jar takeout-service/target/takeout-service-1.0.0.jar --spring.profiles.active=dev
```

启动成功后会看到：

```
Started TakeoutApplication in 3.xxx seconds
默认管理员账号已创建
  手机号: 13800000000
  密码: 123456
```

> API 文档：浏览器访问 http://localhost:8080/doc.html

### 5. 启动前端

```bash
cd takeout-admin

# 安装依赖（仅首次）
npm install

# 启动开发服务器
npm run dev
```

浏览器访问 **http://localhost:5173**

### 6. 登录测试

| 角色 | 手机号 | 密码 |
|------|--------|------|
| 管理员 | 13800000000 | 123456 |
| 商家 | 13900000002 | 123456 |
| 顾客 | 13900000001 | 123456 |
| 骑手 | 13900000003 | 123456 |

> 前端顶栏有 **「切换角色」** 按钮，可一键切换不用反复登录。

## 后端配置说明

### 开发环境 (`application-dev.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/takeout?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root          # ← 改为你的 MySQL 密码
  data:
    redis:
      host: localhost
      port: 6379
      password:              # ← 如果 Redis 有密码，填这里
```

### 生产环境

```bash
java -jar takeout-service-1.0.0.jar --spring.profiles.active=prod \
  -DMYSQL_USER=your_user \
  -DMYSQL_PASSWORD=your_password \
  -DREDIS_HOST=your_redis_host \
  -DREDIS_PASSWORD=your_redis_password
```

## API 接口概览

| 模块 | 路径前缀 | 需要角色 |
|------|---------|---------|
| 认证 | `/api/v1/auth` | 无 |
| 用户 | `/api/v1/user` | 已登录 |
| 管理员 | `/api/v1/admin` | ADMIN |
| 商家 | `/api/v1/merchant` / `/api/v1/merchants` | MERCHANT |
| 菜品 | `/api/v1/dishes` / `/api/v1/merchant/dishes` | 混合 |
| 订单 | `/api/v1/orders` / `/api/v1/customer/orders` / `/api/v1/merchant/orders` | 按角色 |
| 配送 | `/api/v1/rider` / `/api/v1/admin/deliveries` | RIDER / ADMIN |
| 统计 | `/api/v1/admin/statistics` / `/api/v1/merchant/statistics` / `/api/v1/rider/statistics` | 按角色 |
| 通知 | `/api/v1/notifications` | 已登录 |
| SSE | `/api/v1/sse/events` | 已登录 |

详细接口文档：**http://localhost:8080/doc.html**

## VS Code 连接数据库（可视化查看表和数据）

安装扩展 **Database Client** (作者 Weijan Chen) 后配置连接：

| 配置项 | 值 | 注意 |
|--------|-----|------|
| Host | **`127.0.0.1`** | ⚠️ 不要填 `localhost` |
| Port | `3306` | |
| User | `root` | |
| Password | **`root`** | 必须填！空着会报 `Access denied` |
| Database | `takeout` | |

> 为什么用 `127.0.0.1`？MySQL 对 `localhost` 走 Unix Socket 认证（免密），`127.0.0.1` 才走 TCP + 密码认证。

---

## 数据库设计

### ER 图

```
  ┌─────────┐       ┌─────────────┐       ┌──────────┐
  │   user   │1────1│  customer   │1────*│  address │
  │ (用户账号) │      │  (顾客信息)   │       │ (配送地址) │
  └────┬─────┘      └──────┬──────┘      └──────────┘
       │1                  │1
       │1                  │*
  ┌────┴─────┐      ┌──────┴──────┐
  │ merchant │      │ order_info  │ ────── 1:1 ──── delivery ──── N:1 ──── rider
  │ (商家信息) │      │  (订单信息)  │
  └────┬─────┘      └──┬──┬──┬───┘
       │1              │  │  │
       │*              │* │  │
  ┌────┴─────┐    ┌────┴──┴──┐
  │ category │    │order_item│
  │(菜品分类) │    │(订单明细)  │
  └────┬─────┘    └────┬─────┘
       │1              │*
       │*              │
  ┌────┴─────┐         │
  │   dish   │◄────────┘      cart_item ──── N:1 ──── customer
  │ (菜品信息) │
  └──────────┘
```

### 建表 SQL（完整 DDL）

建表脚本位于 `takeout-server/takeout-service/src/main/resources/sql/schema.sql`，可直接执行：

```bash
mysql -u root -proot -h 127.0.0.1 < takeout-server/takeout-service/src/main/resources/sql/schema.sql
```

#### 用户账号表 `user`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 用户ID（全局唯一，关联各角色子表） |
| `phone` | CHAR(11) UNIQUE | 手机号（登录凭证） |
| `password` | VARCHAR(128) | BCrypt 加密 |
| `role` | VARCHAR(8) | ADMIN / MERCHANT / CUSTOMER / RIDER |
| `status` | VARCHAR(4) | 正常 / 禁用 |
| `create_time` | DATETIME | 注册时间 |
| `update_time` | DATETIME | 更新时间 |

CHECK: `role IN ('ADMIN','MERCHANT','CUSTOMER','RIDER')`, `status IN ('正常','禁用')`

#### 商家信息表 `merchant`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK | = user.id（ISA 继承） |
| `name` | VARCHAR(50) UNIQUE | 商家名称 |
| `phone` | CHAR(11) | 联系电话 |
| `province`, `city`, `district` | VARCHAR(20) | 省/市/区 |
| `address_detail` | VARCHAR(100) | 详细地址 |
| `open_time`, `close_time` | CHAR(5) | 营业时间 HH:MM |
| `notice` | VARCHAR(500) | 商家公告 |
| `biz_status` | VARCHAR(6) | 营业中 / 休息中 |
| `audit_status` | VARCHAR(6) | 待审核 / 已通过 / 已驳回 |
| `monthly_sales` | INT | 月销量（近30天完成订单数） |
| `auto_accept` | TINYINT | 是否自动接单 0/1 |

CHECK: `biz_status IN ('营业中','休息中')`, `audit_status IN ('待审核','已通过','已驳回')`, `monthly_sales >= 0`

#### 菜品分类表 `category`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 分类ID |
| `merchant_id` | BIGINT FK | 所属商家 |
| `name` | VARCHAR(20) | 分类名称 |
| `sort` | INT | 排序序号 |

#### 菜品信息表 `dish`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 菜品ID |
| `merchant_id` | BIGINT FK | 所属商家 |
| `category_id` | BIGINT FK | 所属分类 |
| `name` | VARCHAR(50) | 菜品名称（同商家内唯一） |
| `price` | DECIMAL(10,2) | 价格（元） |
| `description` | VARCHAR(500) | 描述 |
| `image_url` | VARCHAR(255) | 图片URL |
| `stock` | INT | 库存量 |
| `status` | VARCHAR(4) | 上架 / 下架 |

CHECK: `price > 0`, `stock >= 0`, `status IN ('上架','下架')`

#### 购物车表 `cart_item`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 购物车项ID |
| `customer_id` | BIGINT FK | 顾客ID |
| `dish_id` | BIGINT FK | 菜品ID |
| `quantity` | INT | 数量 |

UNIQUE: `(customer_id, dish_id)` — 同一顾客对同一菜品只有一条记录，重复加入合并数量  
CHECK: `quantity > 0`

#### 订单信息表 `order_info`

| 列 | 类型 | 说明 |
|----|------|------|
| `order_no` | CHAR(14) PK | 订单编号 YYYYMMDD + 6位流水 |
| `customer_id` | BIGINT FK | 顾客ID |
| `merchant_id` | BIGINT FK | 商家ID |
| `address_id` | BIGINT FK | 配送地址ID |
| `status` | VARCHAR(8) | 已提交/待接单/备餐中/待配送/配送中/已送达/已完成/已取消 |
| `total_amount` | DECIMAL(12,2) | 总金额 |
| `remark` | VARCHAR(200) | 备注 |
| `payment_method` | VARCHAR(16) | 支付方式（预留） |
| `payment_status` | VARCHAR(8) | 支付状态（预留） |

#### 订单明细表 `order_item`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 明细ID |
| `order_no` | CHAR(14) FK | 订单编号 |
| `dish_id` | BIGINT FK | 菜品ID |
| `dish_name` | VARCHAR(50) | ⚠️ 菜品名称快照 |
| `unit_price` | DECIMAL(10,2) | ⚠️ 成交单价快照 |
| `quantity` | INT | 数量 |

CHECK: `quantity > 0`, `unit_price > 0`

#### 骑手信息表 `rider`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK | = user.id |
| `name` | VARCHAR(20) | 姓名 |
| `phone` | CHAR(11) | 手机号 |
| `status` | VARCHAR(6) | 空闲 / 配送中 / 离线 |
| `total_deliveries` | INT | 累计配送单数 |

CHECK: `status IN ('空闲','配送中','离线')`, `total_deliveries >= 0`

#### 配送信息表 `delivery`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 配送ID |
| `order_no` | CHAR(14) UNIQUE FK | 订单编号（1:1） |
| `rider_id` | BIGINT FK | 骑手ID |
| `status` | VARCHAR(8) | 待取餐 / 配送中 / 已送达 |
| `pickup_time` | DATETIME | 取餐时间 |
| `deliver_time` | DATETIME | 送达时间 |

CHECK: `status IN ('待取餐','配送中','已送达')`

#### 通知记录表 `notification`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 通知ID |
| `user_id` | BIGINT FK | 接收用户ID |
| `event_type` | VARCHAR(32) | 事件类型 |
| `title` | VARCHAR(100) | 通知标题 |
| `content` | VARCHAR(500) | 通知内容 |
| `ref_id` | VARCHAR(64) | 关联对象ID |
| `is_read` | TINYINT | 0未读 / 1已读 |

CHECK: `event_type IN ('order_new','order_accepted','order_ready','delivery_accepted','delivery_arrived','merchant_audited')`

#### 操作日志表 `operation_log`

| 列 | 类型 | 说明 |
|----|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | 日志ID |
| `user_id` | BIGINT FK | 操作用户ID |
| `user_role` | VARCHAR(8) | 操作用户身份 |
| `operation` | VARCHAR(32) | 操作类型 |
| `target_type` | VARCHAR(32) | 操作对象类型 |
| `target_id` | VARCHAR(64) | 操作对象ID |
| `detail` | VARCHAR(500) | 操作详情 |
| `ip` | VARCHAR(45) | 操作IP |
| `user_agent` | VARCHAR(255) | 用户代理 |

### 预置测试数据

系统首次启动后，`DataInitializer` 会自动创建：

| 表 | 数据 |
|----|------|
| `user` | 管理员账号 13800000000 / 123456（角色 ADMIN） |
| `user` | 顾客 13900000001（角色 CUSTOMER） |
| `user` | 商家 13900000002（角色 MERCHANT）→ 审核后显示为"美味餐厅" |
| `user` | 骑手 13900000003（角色 RIDER） |
| `customer` | 测试顾客 + 默认地址（深圳福田区华强北路1008号） |
| `merchant` | 美味餐厅（深圳南山区科技园路1号，已通过审核） |
| `merchant` | 湘味馆（长沙岳麓区麓山南路1号，已通过审核） |
| `category` | 主食、饮品 |
| `dish` | 宫保鸡丁 ¥28、可乐 ¥5 |
| `order_info` | 2 笔测试订单（已完成 + 待接单） |
| `delivery` | 1 条配送记录（骑手已完成） |

### 数据一致性约束汇总

所有约束已写入 `schema.sql`，建表时自动生效：

| 类型 | 数量 | 示例 |
|------|------|------|
| PRIMARY KEY | 14 | 每表一个 |
| FOREIGN KEY | 19 | `fk_order_customer`, `fk_dish_merchant` 等 |
| UNIQUE KEY | 5 | `uk_phone`, `uk_merchant_name`, `uk_customer_dish` 等 |
| INDEX | 18 | `idx_role`, `idx_status`, `idx_order_no` 等 |
| CHECK | 21 | `price > 0`, `stock >= 0`, `status IN (...)` 等 |

## 前端页面清单

| 页面 | 路径 | 角色 |
|------|------|------|
| 登录 | `/login` | 所有 |
| 首页仪表盘 | `/dashboard` | 所有（内容按角色不同） |
| 用户管理 | `/users` | 管理员 |
| 商家管理 | `/merchants` | 管理员 |
| 商家审核 | `/merchants/audit` | 管理员 |
| 订单管理 | `/orders` | 管理员 |
| 配送管理 | `/deliveries` | 管理员 |
| 数据统计 | `/statistics` | 管理员 |
| 菜品管理 | `/merchant/dishes` | 商家 |
| 订单处理 | `/merchant/my-orders` | 商家 |
| 营业统计 | `/merchant/stats` | 商家 |
| 浏览商家 | `/customer/shops` | 顾客 |
| 我的订单 | `/customer/my-orders` | 顾客 |
| 收货地址 | `/customer/addresses` | 顾客 |
| 配送任务 | `/rider/tasks` | 骑手 |
| 配送记录 | `/rider/history` | 骑手 |
| 403 | `/403` | 所有 |

## 安全检查清单

- [x] 密码 BCrypt 加密存储
- [x] JWT 无状态认证 + 24h 过期
- [x] 登出 Token 加入 Redis 黑名单
- [x] `@PreAuthorize` 方法级权限控制
- [x] 订单查询数据级权限（顾客只能看自己的）
- [x] 前端路由守卫 + 菜单按角色显隐
- [x] 数据库 CHECK 约束（21 条）
- [x] 操作日志 AOP 自动写入 `operation_log`
- [x] SSE 推送需 JWT 认证

## 常见问题

**Q: 启动报 `Unsupported character encoding 'utf8mb4'`？**
A: JDBC URL 中 `characterEncoding` 应填 `UTF-8`，不是 `utf8mb4`。最新代码已修复。

**Q: 启动报 `Access denied for user 'root'@'localhost'`？**
A: 检查 `application-dev.yml` 中 MySQL 用户名密码是否正确。

**Q: 前端页面刷新后 404？**
A: Vite 开发模式下无此问题。生产部署需 Nginx 配置 `try_files $uri /index.html`。

**Q: 如何重置管理员密码？**
A: 删除 `user` 表中管理员记录后重启后端，`DataInitializer` 会自动重建（13800000000 / 123456）。
