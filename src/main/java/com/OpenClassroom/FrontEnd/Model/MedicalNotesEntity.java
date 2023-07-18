package com.OpenClassroom.FrontEnd.Model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalNotesEntity {

    @Generated
    private Integer id;

    private Integer patientId;

    private String patientLastName;

    private String note;

    private LocalDateTime dateTimeAtCreation;
}
