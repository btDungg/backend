-- File: src/main/resources/db/migration/V1__Create_scores_table.sql

-- Lệnh tạo bảng 'scores' với các cột tương ứng file CSV và entity
CREATE TABLE scores (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,          -- Khóa chính (ID) tự động tăng
                        sbd VARCHAR(20) NOT NULL UNIQUE,              -- Cột 'sbd', không được null và phải là duy nhất
                        toan FLOAT,                                   -- Cột 'toan' (Điểm Toán)
                        ngu_van FLOAT,                                -- Cột 'ngu_van' (Điểm Ngữ Văn)
                        ngoai_ngu FLOAT,                              -- Cột 'ngoai_ngu' (Điểm Ngoại Ngữ)
                        vat_li FLOAT,                                 -- Cột 'vat_li' (Điểm Vật Lý)
                        hoa_hoc FLOAT,                                -- Cột 'hoa_hoc' (Điểm Hóa Học)
                        sinh_hoc FLOAT,                               -- Cột 'sinh_hoc' (Điểm Sinh Học)
                        lich_su FLOAT,                                -- Cột 'lich_su' (Điểm Lịch Sử)
                        dia_li FLOAT,                                 -- Cột 'dia_li' (Điểm Địa Lý)
                        gdcd FLOAT,                                   -- Cột 'gdcd' (Điểm GDCD)
                        ma_ngoai_ngu VARCHAR(10)                      -- Cột mới 'ma_ngoai_ngu' (Mã Ngoại Ngữ)


) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; -- Sử dụng InnoDB engine và charset utf8mb4

-- Tạo các chỉ mục (index) để tăng tốc độ truy vấn trên các cột thường dùng
CREATE INDEX idx_scores_sbd ON scores (sbd);                -- Chỉ mục cho cột 'sbd'
CREATE INDEX idx_scores_toan ON scores (toan);              -- Chỉ mục cho cột 'toan'
CREATE INDEX idx_scores_ngu_van ON scores (ngu_van);        -- Chỉ mục cho cột 'ngu_van'
CREATE INDEX idx_scores_ngoai_ngu ON scores (ngoai_ngu);    -- Chỉ mục cho cột 'ngoai_ngu'
CREATE INDEX idx_scores_vat_li ON scores (vat_li);          -- Chỉ mục cho cột 'vat_li'
CREATE INDEX idx_scores_hoa_hoc ON scores (hoa_hoc);        -- Chỉ mục cho cột 'hoa_hoc'
CREATE INDEX idx_scores_sinh_hoc ON scores (sinh_hoc);      -- Chỉ mục cho cột 'sinh_hoc'
CREATE INDEX idx_scores_lich_su ON scores (lich_su);        -- Chỉ mục cho cột 'lich_su'
CREATE INDEX idx_scores_dia_li ON scores (dia_li);          -- Chỉ mục cho cột 'dia_li'
CREATE INDEX idx_scores_gdcd ON scores (gdcd);              -- Chỉ mục cho cột 'gdcd'

-- Chỉ mục tổng hợp cho việc tìm kiếm top khối A (Toán, Lý, Hóa)
CREATE INDEX idx_scores_group_a ON scores (toan, vat_li, hoa_hoc);