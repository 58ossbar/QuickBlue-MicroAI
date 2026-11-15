# ============================================================
# QuickBlue 后端通用镜像构建文件（所有微服务共用）
#
# compose 中每个服务通过 build.args 指定:
#   MODULE   要打包的 Maven 模块路径（如 QuickBlue-modules/QuickBlue-system）
#   JAR_NAME 产出的 jar 文件名（如 QuickBlue-system-4.0.0.jar）
#
# 容器内 Maven 构建：宿主机无需安装 JDK/Maven，只装 Docker 即可。
# ============================================================

# ---------- 阶段一：Maven 打包 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 整个后端仓库进入构建上下文（.dockerignore 已排除 target/、database、docs 等无关目录）
COPY . .

# 仅构建目标模块及其依赖（-am 自动带上 QuickBlue-common / QuickBlue-api）
ARG MODULE
RUN mvn -q -pl "${MODULE}" -am package -DskipTests

# ---------- 阶段二：运行时 ----------
FROM eclipse-temurin:21-jre-noble
ARG MODULE
ARG JAR_NAME

# 安装 curl 供容器健康检查使用
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/${MODULE}/target/${JAR_NAME} app.jar

ENV TZ=Asia/Shanghai
EXPOSE 8080

# JAVA_OPTS 由 compose 按服务注入（堆大小等）
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
