package com.OpenClassroom.FrontEnd.Service.Implementation;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Service.Interface.INoteService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NoteServiceImpl implements INoteService {

    @Override
    public List<MedicalNotesEntity> getNotesByPatientId(Integer patientId) {
        String apiUrl = "http://localhost:9090/api/notes/by-patientId/{patientId}";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<MedicalNotesEntity>> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MedicalNotesEntity>>() {},
                patientId
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve medical notes. Status code: " + response.getStatusCodeValue());
        }
    }


    @Override
    public MedicalNotesEntity[] getNotesByPatientLastName(String patientLastName) {
        return new MedicalNotesEntity[0];
    }
}
