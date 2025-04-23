package com.goldenowl.backend.service.interfaceService;

import com.goldenowl.backend.dto.ScoreDto;
import com.goldenowl.backend.dto.ScoreReportDto;
import com.goldenowl.backend.dto.TopStudentDto;
import com.goldenowl.backend.entity.Score;

import java.util.List;

public interface IScoreService {

    ScoreDto findScoreByRegistrationNumber(String registrationNumber);
    ScoreReportDto generateScoreReport();
    List<TopStudentDto> getTopAStudents(int count);
}
