package com.goldenowl.backend.dto;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreDto {
    private String registrationNumber;
    private Float math;
    private Float literature;
    private Float foreignLanguage;
    private Float physics;
    private Float chemistry;
    private Float biology;
    private Float history;
    private Float geography;
    private Float civicEducation;
    private String foreignLanguageCode;


}
