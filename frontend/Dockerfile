# ========== 阶段一：构建前端 ==========
FROM node AS builder

# 设置工作目录
WORKDIR /app

# 复制项目依赖配置文件
COPY package*.json ./

RUN npm install

# 复制全部源码及配置文件
COPY . .

# 执行构建命令，生成 dist 目录
RUN npm run build

# ========== 阶段二：部署 Nginx ==========
FROM nginx

# 从构建阶段复制生成的静态文件到 Nginx 服务目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制自定义 Nginx 配置文件
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

# 启动 Nginx（默认 CMD 已存在，无需额外定义）