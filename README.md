# Fullstack Todo App

> Vue3 + Vite + Spring Boot 3 + MySQL 8 — Docker Compose 一键部署

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite 5 + Axios + Nginx |
| 后端 | Java 17 + Spring Boot 3 + Spring Data JPA |
| 数据库 | MySQL 8.0 |
| 部署 | Docker + Docker Compose |

## 项目结构

```
fullstack-app/
├── frontend/               # Vite + Vue3 前端
│   ├── src/
│   │   └── App.vue         # 主页面组件
│   ├── nginx.conf          # Nginx 配置（含 API 反向代理）
│   ├── Dockerfile          # 多阶段构建：Node → Nginx
│   └── package.json
├── backend/                # Spring Boot 后端
│   ├── src/main/java/com/example/demo/
│   │   ├── controller/     # REST API
│   │   ├── service/        # 业务逻辑
│   │   ├── entity/         # JPA 实体
│   │   └── repository/     # 数据访问层
│   ├── src/main/resources/
│   │   └── application.yml # 配置文件
│   └── Dockerfile          # 多阶段构建：Maven → JRE
├── mysql/
│   └── init.sql            # 数据库初始化脚本
├── docker-compose.yml      # 服务编排
├── .env                    # 环境变量（可修改密码等）
└── README.md
```

## 快速启动

### 前提条件

- 安装 [Docker](https://docs.docker.com/get-docker/)（含 Docker Compose）

### 一键启动

```bash
# 克隆/进入项目目录
cd fullstack-app

# 启动所有服务（首次构建需要几分钟）
docker compose up -d

# 查看启动日志
docker compose logs -f
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost |
| 后端 API | http://localhost:8080/api/todos |
| MySQL | localhost:3306 |

### 停止服务

```bash
# 停止并保留数据
docker compose down

# 停止并清除所有数据（包括数据库）
docker compose down -v
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/todos` | 获取全部 Todo |
| GET | `/api/todos/{id}` | 获取单个 Todo |
| POST | `/api/todos` | 创建 Todo |
| PUT | `/api/todos/{id}` | 更新 Todo |
| DELETE | `/api/todos/{id}` | 删除 Todo |

**创建/更新请求体：**
```json
{
  "title": "待办事项标题",
  "completed": false
}
```

## 本地开发

### 前端开发模式

```bash
cd frontend
npm install
npm run dev       # 访问 http://localhost:5173
```

### 后端开发模式

```bash
# 确保 MySQL 已运行（可用 docker compose up mysql -d 单独启动）
cd backend
mvn spring-boot:run
```

## 环境变量

修改 `.env` 文件可自定义数据库配置：

```env
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=todoapp
MYSQL_USER=appuser
MYSQL_PASSWORD=apppassword
```

## 功能特性

- ✅ 添加待办事项
- ✅ 标记完成/未完成
- ✅ 双击编辑标题
- ✅ 删除待办事项
- ✅ 按状态筛选（全部/待完成/已完成）
- ✅ 批量清除已完成
- ✅ 数据持久化（MySQL）
