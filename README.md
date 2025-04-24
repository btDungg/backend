# Hướng dẫn chạy
- Tạo csdl trong MySQL (có thể có tên là gscores_db)
- tuỳ chỉnh kết nối đến DB (MySQL) trong file 'application.properties'
    - spring.datasource.url=jdbc:mysql://localhost:3306/gscores_db
    - spring.datasource.username=${DB_USERNAME}
    - spring.datasource.password=${DB_PASSWORD}
- mở terminal trong dự án. (./backend)
- Chạy lệnh: 'mvn spring-boot:run' để khởi chạy chương trình
- các endpoint để test: 
    + GET /api/v1/scores/{registrationNumber}: để lấy điểm chi tiết theo số báo danh
    + GET /api/v1/scores/report: để lấy báo cáo thống kê điểm
    + GET /api/v1/scores/top-a?count=N (N là số lượng muốn lấy, mặc định là 10): để lấy danh sách top N sinh viên khối A
