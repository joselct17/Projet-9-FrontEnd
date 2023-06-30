package com.OpenClassroom.FrontEnd.Model;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MedicalNotesEntity {

    @Generated
    private Integer id;

    private Integer patientId;

    private String patientLastName;

    private String note;

    private LocalDateTime dateTimeAtCreation;
}
