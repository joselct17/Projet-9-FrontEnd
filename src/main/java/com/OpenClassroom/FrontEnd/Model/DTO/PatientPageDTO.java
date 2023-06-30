package com.OpenClassroom.FrontEnd.Model.DTO;

import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class PatientPageDTO {
    @JsonProperty
    private ArrayList<PatientEntity> content;
    @JsonProperty
    private int totalPages;

}
