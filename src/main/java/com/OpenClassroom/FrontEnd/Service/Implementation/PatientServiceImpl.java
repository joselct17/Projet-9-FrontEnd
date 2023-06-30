package com.OpenClassroom.FrontEnd.Service.Implementation;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Interface.IPatientsService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PatientServiceImpl implements IPatientsService {

    @Override
    public PatientEntity patientById(Integer id) {
        String apiUrl = "http://localhost:8090/api/patients/{id}";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<PatientEntity> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PatientEntity>() {},
                id
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve medical notes. Status code: " + response.getStatusCode().value());
        }

    }
}
