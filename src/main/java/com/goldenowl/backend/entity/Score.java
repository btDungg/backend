package com.goldenowl.backend.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="scores")
public class Score {

    @Id // Đánh dấu trường này là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Sử dụng chiến lược tự tăng của CSDL (phù hợp với AUTO_INCREMENT của MySQL)
    private Long id; // Kiểu Long cho ID

    @Column(name = "sbd", unique = true, nullable = false, length = 20) // Ánh xạ tới cột 'sbd', là duy nhất, không null
    private String registrationNumber; // Tên trường Java (nên dùng tiếng Anh)

    @Column(name = "toan", nullable = true) // Ánh xạ tới cột 'toan', cho phép null
    private Float math;                     // Tên trường Java: Điểm Toán

    @Column(name = "ngu_van", nullable = true) // Ánh xạ tới cột 'ngu_van'
    private Float literature;                // Tên trường Java: Điểm Ngữ Văn

    @Column(name = "ngoai_ngu", nullable = true) // Ánh xạ tới cột 'ngoai_ngu'
    private Float foreignLanguage;           // Tên trường Java: Điểm Ngoại Ngữ

    @Column(name = "vat_li", nullable = true) // Ánh xạ tới cột 'vat_li'
    private Float physics;                   // Tên trường Java: Điểm Vật Lý

    @Column(name = "hoa_hoc", nullable = true) // Ánh xạ tới cột 'hoa_hoc'
    private Float chemistry;                 // Tên trường Java: Điểm Hóa Học

    @Column(name = "sinh_hoc", nullable = true) // Ánh xạ tới cột 'sinh_hoc'
    private Float biology;                   // Tên trường Java: Điểm Sinh Học

    @Column(name = "lich_su", nullable = true) // Ánh xạ tới cột 'lich_su'
    private Float history;                   // Tên trường Java: Điểm Lịch Sử

    @Column(name = "dia_li", nullable = true) // Ánh xạ tới cột 'dia_li'
    private Float geography;                 // Tên trường Java: Điểm Địa Lý

    @Column(name = "gdcd", nullable = true) // Ánh xạ tới cột 'gdcd'
    private Float civicEducation;            // Tên trường Java: Điểm GDCD

    @Column(name = "ma_ngoai_ngu", nullable = true, length = 10) // Ánh xạ tới cột 'ma_ngoai_ngu'
    private String foreignLanguageCode;

}
