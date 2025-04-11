# Sử dụng JDK 23 làm base image
FROM eclipse-temurin:23-jdk-alpine AS build

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép file POM và tải các dependencies trước khi sao chép toàn bộ code
# Điều này giúp tận dụng Docker cache để tăng tốc quá trình build
COPY ./pom.xml .
COPY ./.mvn ./.mvn
COPY ./mvnw .
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Sao chép mã nguồn vào container
COPY ./src ./src

# Build ứng dụng với Maven
RUN ./mvnw package -DskipTests

# Tạo một image mới với JRE để giảm kích thước
FROM eclipse-temurin:23-jre-alpine

# Tạo thư mục uploads để lưu trữ file tải lên
RUN mkdir -p /app/src/main/resources/static/uploads

WORKDIR /app

# Sao chép file JAR từ phase build
COPY --from=build /app/target/*.jar app.jar

# Mở port cho ứng dụng
EXPOSE 8080

# Thiết lập biến môi trường kết nối Aiven MySQL
ENV SPRING_DATASOURCE_URL=jdbc:mysql://fsghackathontal-01-zander.k.aivencloud.com:12629/psychological_treatment?useSSL=true&requireSSL=true&verifyServerCertificate=false
ENV SPRING_DATASOURCE_USERNAME=avnadmin
ENV SPRING_DATASOURCE_PASSWORD=AVNS_dUm1dTIsI455euciU9a

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
