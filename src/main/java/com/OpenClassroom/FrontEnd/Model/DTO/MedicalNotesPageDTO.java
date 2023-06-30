package com.OpenClassroom.FrontEnd.Model.DTO;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class MedicalNotesPageDTO {
    private ArrayList<MedicalNotesEntity> medicalNotes;
}
