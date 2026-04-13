# ==========================================
# 第一阶段：构建阶段 (Build Stage)
# ==========================================
# 使用包含 JDK (不仅仅是 JRE) 和 Maven 的镜像
FROM maven:3.8.5-openjdk-17 AS builder

WORKDIR /build

# 1. 复制 pom.xml
COPY pom.xml .
# 2. 下载依赖 (利用 Docker 缓存)
RUN mvn dependency:go-offline
# 3. 复制源代码
COPY src ./src
# 4. 执行编译和打包 (这是关键！)
RUN mvn clean package -DskipTests

# ==========================================
# 第二阶段：运行阶段 (Run Stage)
# ==========================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 因为这是最终的镜像，Java 程序会读取这个变量
ENV TZ=Asia/Shanghai

# 2. 配置系统时区文件 (防止 Java 以外的工具时间不对)
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 5. 从“第一阶段”的镜像里，把编译好的 jar 包复制过来
# 注意：这里不需要本地有 jar 包，是从 builder 镜像里拿
COPY --from=builder /build/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]