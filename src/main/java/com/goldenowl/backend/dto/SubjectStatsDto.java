package com.goldenowl.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectStatsDto {
    private long level1Count;
    private long level2Count;
    private long level3Count;
    private long level4Count;

}
