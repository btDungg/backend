package com.goldenowl.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopStudentDto {
    private String registrationNumber;
    private Float math;
    private Float physics;
    private Float chemistry;
    private Float totalScoreA;
}
