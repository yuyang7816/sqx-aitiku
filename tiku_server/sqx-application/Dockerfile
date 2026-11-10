# 使用官方的Java运行时作为父镜像
FROM openjdk:8-jdk-slim

# 设置作者信息
LABEL maintainer="sqx"

# 设置工作目录
WORKDIR /sqx

# 将本地的JAR文件复制到容器中
COPY target/tiku.jar /app/tiku.jar

# 暴露应用程序端口
EXPOSE 9090

# 在容器启动时运行命令
ENTRYPOINT ["java","-jar","/app/tiku.jar"]