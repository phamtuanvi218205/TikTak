# ==========================================
# BƯỚC 1: BUILD ỨNG DỤNG
# ==========================================
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app

# Copy file pom.xml trước để tải thư viện (Cache bước này lại để build nhanh hơn)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code và tiến hành đóng gói
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# BƯỚC 2: CHẠY ỨNG DỤNG (MÔI TRƯỜNG RUNTIME)
# ==========================================
FROM eclipse-temurin:22-jre-alpine
WORKDIR /app

# Thiết lập múi giờ (Rất quan trọng cho app có tính năng chat/post video)
ENV TZ=Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata ffmpeg

# Copy file jar từ Bước 1 sang
COPY --from=build /app/target/TikTok-0.0.1-SNAPSHOT.jar app.jar

# Tạo thư mục chứa media và cấp quyền (tránh lỗi Permission Denied khi FFmpeg xuất file)
RUN mkdir -p /app/uploads/video /app/uploads/images \
    && chmod -R 777 /app/uploads

EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]