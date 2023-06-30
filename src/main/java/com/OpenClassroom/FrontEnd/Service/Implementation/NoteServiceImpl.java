package com.OpenClassroom.FrontEnd.Service.Implementation;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Interface.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements INoteService {

    @Autowired
    RestTemplate restTemplate;
    private final String apiUrl="http://localhost:9090/api/notes";

    @Override
    public List<MedicalNotesEntity> getNotesByPatientId(Integer patientId) {


        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .pathSegment("by-patientId")
                .pathSegment(String.valueOf(patientId))
                .build()
                .toUriString();

        ResponseEntity<List<MedicalNotesEntity>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MedicalNotesEntity>>() {},
                patientId
        );

        List<MedicalNotesEntity> medicalNotes = response.getBody();

        List<MedicalNotesEntity> patientNotes = medicalNotes.stream()
                .filter(note -> note.getPatientId() != null && note.getPatientId().equals(patientId))
                .collect(Collectors.toList());

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve medical notes. Status code: " + response.getStatusCodeValue());
        }
    }

    @Override
    public List<MedicalNotesEntity> getAllNotes() {

        ResponseEntity<List<MedicalNotesEntity>> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MedicalNotesEntity>>() {}
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            List<MedicalNotesEntity> notes = response.getBody();

            return notes;

        } else {
            throw new RuntimeException("Erreur lors de la récupération des notes");
        }

    }

    @Override
    public void createMedicalNoteForPatient(Integer patientId, String message) {
        MedicalNotesEntity medicalNotes = new MedicalNotesEntity();

        // Faire une requête à l'API Patient pour récupérer les informations du patient
        String patientApiUrl = "http://localhost:8090/api/patients/" + patientId;
        ResponseEntity<PatientEntity> patientResponse = restTemplate.exchange(patientApiUrl, HttpMethod.GET, null, PatientEntity.class);

        if (patientResponse.getStatusCode().is2xxSuccessful()) {
            PatientEntity patient = patientResponse.getBody();
            if (patient != null) {
                medicalNotes.setPatientId(patientId);
                medicalNotes.setPatientLastName(patient.getLastName());
                medicalNotes.setNote(message);
                medicalNotes.setDateTimeAtCreation(LocalDateTime.now());
            } else {
                throw new IllegalArgumentException("Patient not found");
            }
        } else {
            throw new RuntimeException("Failed to retrieve patient information. Status code: " + patientResponse.getStatusCode().value());
        }

        String externalServiceUrl = "http://localhost:9090/api/notes";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MedicalNotesEntity> requestEntity = new HttpEntity<>(medicalNotes, headers);
        ResponseEntity<MedicalNotesEntity> response = restTemplate.postForEntity(externalServiceUrl, requestEntity, MedicalNotesEntity.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Notes created successfully.");
        } else {
            System.out.println("Failed to create notes. Status code: " + response.getStatusCode().value());
        }
    }
}


