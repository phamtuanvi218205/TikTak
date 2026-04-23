# ==========================================
# BƯỚC 1: BUILD ỨNG DỤNG BẰNG MAVEN (JAVA 21)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml và tải thư viện (Cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và đóng gói
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# BƯỚC 2: CHẠY ỨNG DỤNG (MÔI TRƯỜNG DEBIAN/UBUNTU)
# ==========================================
# Dùng bản jammy (Ubuntu 22.04) thay vì alpine để tương thích 100% với jave-all-deps
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Thiết lập múi giờ Việt Nam
ENV TZ=Asia/Ho_Chi_Minh

# Cài đặt FFmpeg bằng apt-get (của Ubuntu/Debian) và dọn rác để giảm dung lượng
RUN apt-get update \
    && apt-get install -y ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Copy file jar từ bước build sang
COPY --from=build /app/target/TikTok-0.0.1-SNAPSHOT.jar app.jar

# Tạo thư mục tạm và cấp quyền đọc/ghi
RUN mkdir -p /app/uploads/video /app/uploads/images \
    && chmod -R 777 /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]