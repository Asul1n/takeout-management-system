#!/bin/bash
# ============================================================
#  外卖管理系统 — 一键启动脚本
#  使用: bash start.sh [build]
#  参数: build — 重新编译后端后再启动
# ============================================================
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/takeout-server"
FRONTEND_DIR="$PROJECT_DIR/takeout-admin"
BACKEND_LOG="/tmp/takeout-backend.log"
FRONTEND_LOG="/tmp/takeout-frontend.log"
BACKEND_JAR="$BACKEND_DIR/takeout-service/target/takeout-service-1.0.0.jar"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }
title() { echo -e "\n${BLUE}════════════════════════════════════════${NC}"; echo -e "${BLUE}  $1${NC}"; echo -e "${BLUE}════════════════════════════════════════${NC}"; }

# ========== 1. 环境检查 ==========
title "1/5 环境检查"

check_cmd() {
    if command -v "$1" &>/dev/null; then
        info "$1 → $(command -v $1)"
    else
        error "$1 未安装，请先安装"; exit 1
    fi
}

check_cmd java
check_cmd mvn
check_cmd node
check_cmd mysql

# 检查 MySQL 是否运行
if mysqladmin ping -u root -proot -h 127.0.0.1 --silent 2>/dev/null; then
    MYSQL_OK=1; info "MySQL 已运行"
else
    warn "MySQL 未连接，尝试启动..."
    if sudo -n systemctl start mysql 2>/dev/null; then
        info "MySQL 已启动"
    elif echo 'asuka' | sudo -S systemctl start mysql 2>/dev/null; then
        info "MySQL 已启动（sudo）"
    else
        error "无法启动 MySQL，请手动启动后重试"; exit 1
    fi
fi

# 检查 Redis
if redis-cli ping &>/dev/null 2>&1; then
    REDIS_OK=1; info "Redis 已运行"
else
    warn "Redis 未运行，尝试启动..."
    if sudo -n systemctl start redis-server 2>/dev/null; then
        info "Redis 已启动"
    elif echo 'asuka' | sudo -S systemctl start redis-server 2>/dev/null; then
        info "Redis 已启动（sudo）"
    else
        warn "无法启动 Redis（SSE和Token黑名单功能不可用）"
    fi
fi

# 检查并创建数据库
if mysql -u root -proot -h 127.0.0.1 -e "USE takeout" 2>/dev/null; then
    info "数据库 takeout 已存在"
else
    info "创建数据库 takeout..."
    mysql -u root -proot -h 127.0.0.1 -e "CREATE DATABASE IF NOT EXISTS takeout DEFAULT CHARACTER SET utf8mb4" 2>/dev/null
    info "导入建表脚本..."
    mysql -u root -proot -h 127.0.0.1 takeout < "$BACKEND_DIR/takeout-service/src/main/resources/sql/schema.sql" 2>/dev/null
    info "数据库初始化完成"
fi

# ========== 2. 编译后端 ==========
title "2/5 编译后端"

if [ "$1" = "build" ] || [ ! -f "$BACKEND_JAR" ]; then
    info "正在编译后端（首次或手动 build）..."
    cd "$BACKEND_DIR"
    mvn clean package -DskipTests -q
    info "编译完成"
else
    info "后端已编译，跳过（加 build 参数可强制重新编译）"
fi

# ========== 3. 启动后端 ==========
title "3/5 启动后端 (Spring Boot :8080)"

# 停掉旧进程
if lsof -t -i:8080 &>/dev/null; then
    info "停止旧的后端进程..."
    kill $(lsof -t -i:8080) 2>/dev/null
    sleep 2
fi

nohup java -jar "$BACKEND_JAR" --spring.profiles.active=dev > "$BACKEND_LOG" 2>&1 &
BACKEND_PID=$!
info "后端 PID=$BACKEND_PID，等待启动..."

# 等待后端起来
for i in $(seq 1 15); do
    if curl -s http://localhost:8080/api/v1/auth/login -X POST \
       -H "Content-Type: application/json" \
       -d '{"phone":"13800000000","password":"123456"}' 2>/dev/null | grep -q '"code":200'; then
        info "后端启动成功！"
        break
    fi
    if [ $i -eq 15 ]; then
        error "后端启动超时，查看日志: tail -f $BACKEND_LOG"
        exit 1
    fi
    sleep 1
done

# ========== 4. 启动前端 ==========
title "4/5 启动前端 (Vite :5173)"

if lsof -t -i:5173 &>/dev/null; then
    info "停止旧的前端进程..."
    kill $(lsof -t -i:5173) 2>/dev/null
    sleep 1
fi

cd "$FRONTEND_DIR"

# 首次需要 install
if [ ! -d "node_modules" ]; then
    info "安装前端依赖..."
    npm install --silent
fi

# 使用 nvm 的 node（如果存在）
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"

nohup npx vite --host 0.0.0.0 > "$FRONTEND_LOG" 2>&1 &
FRONTEND_PID=$!
info "前端 PID=$FRONTEND_PID"

sleep 3
if grep -q "Local" "$FRONTEND_LOG" 2>/dev/null; then
    info "前端启动成功！"
else
    error "前端启动失败，查看日志: tail -f $FRONTEND_LOG"
fi

# ========== 5. 完成 ==========
title "5/5 启动完成！"

echo ""
echo -e "  ${GREEN}后端 API${NC}    http://localhost:8080"
echo -e "  ${GREEN}API 文档${NC}   http://localhost:8080/doc.html"
echo -e "  ${GREEN}前端管理端${NC}  http://localhost:5173"
echo ""
echo -e "  ┌──────────┬─────────────────┬────────┐"
echo -e "  │ 角色     │ 手机号           │ 密码   │"
echo -e "  ├──────────┼─────────────────┼────────┤"
echo -e "  │ 管理员   │ 13800000000     │ 123456 │"
echo -e "  │ 商家     │ 13900000002     │ 123456 │"
echo -e "  │ 顾客     │ 13900000001     │ 123456 │"
echo -e "  │ 骑手     │ 13900000003     │ 123456 │"
echo -e "  └──────────┴─────────────────┴────────┘"
echo ""
echo -e "  日志: tail -f $BACKEND_LOG    (后端)"
echo -e "        tail -f $FRONTEND_LOG   (前端)"
echo -e "  停止: ${YELLOW}kill \$(lsof -t -i:8080) \$(lsof -t -i:5173)${NC}"
echo ""

cd "$PROJECT_DIR"
