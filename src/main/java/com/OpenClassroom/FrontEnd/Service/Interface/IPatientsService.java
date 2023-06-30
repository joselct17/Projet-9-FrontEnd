package com.OpenClassroom.FrontEnd.Service.Interface;

import com.OpenClassroom.FrontEnd.Model.DTO.PatientPageDTO;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPatientsService {

    PatientEntity patientById(Integer id);

    PatientPageDTO getAllPaginatedPatients(int page, int size);

    void createPatient(PatientEntity patient);

    PatientPageDTO getPatientsByLastName(String lastName, int page, int size);
}
