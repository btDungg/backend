package com.goldenowl.backend.dto;
import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreReportDto {
    private Map<String,SubjectStatsDto> subjectStatistics;
}
