package com.OpenClassroom.FrontEnd.Service.Interface;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;

import java.util.List;

public interface INoteService {
    List<MedicalNotesEntity> getNotesByPatientId(Integer patientId);
    MedicalNotesEntity[] getNotesByPatientLastName(String patientName);
}
