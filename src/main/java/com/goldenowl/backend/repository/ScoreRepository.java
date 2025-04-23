package com.goldenowl.backend.repository;

import com.goldenowl.backend.entity.Score;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByRegistrationNumber(String registrationNumber);
    @Query("SELECT s FROM Score s WHERE s.math IS NOT NULL AND s.physics IS NOT NULL AND s.chemistry IS NOT NULL ORDER BY (s.math + s.physics + s.chemistry) DESC")
    List<Score> findTopGroupAStudents(Pageable pageable); // Tham số Pageable dùng để giới hạn số lượng kết quả (ví dụ: lấy 10 bản ghi đầu tiên)

}
