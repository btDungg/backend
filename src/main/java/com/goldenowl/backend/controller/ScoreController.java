package com.goldenowl.backend.controller;

import com.goldenowl.backend.dto.ScoreDto;
import com.goldenowl.backend.dto.ScoreReportDto;
import com.goldenowl.backend.dto.TopStudentDto;
import com.goldenowl.backend.service.ImplementService.ScoreService;
import com.goldenowl.backend.service.interfaceService.IScoreService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scores")
@Validated
public class ScoreController {

    @Autowired
    private IScoreService scoreService;


    //Lấy điểm thi theo SBD
    @GetMapping("/{registrationNumber}")
    public ResponseEntity<ScoreDto> getScoreByRegistrationNumber(
            @PathVariable
            @NotBlank(message = "Số báo danh không được để trống!")
            @Pattern(regexp = "^[0-9]{8}$", message = "Định dạng số báo danh không hợp lệ")
            String registrationNumber) {
        ScoreDto scoreDto= scoreService.findScoreByRegistrationNumber(registrationNumber);

        return ResponseEntity.ok(scoreDto);
    }

    @GetMapping("/report")
    public ResponseEntity<ScoreReportDto> getScoreReport() {
        ScoreReportDto report = scoreService.generateScoreReport();
        // Trả về HTTP 200 OK cùng với dữ liệu báo cáo
        return ResponseEntity.ok(report);
    }

    @GetMapping("/top-a")
    public ResponseEntity<List<TopStudentDto>> getTopAStudents(
            // Lấy giá trị từ query parameter 'count', nếu không có thì mặc định là 10
            @RequestParam(defaultValue = "10")
            // Validation: Phải là số dương (cần dependency 'validation')
            @Positive(message = "Số lượng 'count' phải là số dương")
            int count) {

        // Gọi service để lấy danh sách top sinh viên
        List<TopStudentDto> topStudents = scoreService.getTopAStudents(count);
        // Trả về HTTP 200 OK cùng với danh sách
        return ResponseEntity.ok(topStudents);
    }
}
