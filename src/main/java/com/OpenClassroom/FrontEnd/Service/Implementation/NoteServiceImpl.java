package com.OpenClassroom.FrontEnd.Service.Implementation;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Interface.INoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements INoteService {

    @Autowired
    RestTemplate restTemplate;
    private final String apiUrl="http://192.168.1.128:9090/api/notes";

    Logger logger = LoggerFactory.getLogger(NoteServiceImpl.class);

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
            return patientNotes;
        } else {
            throw new RuntimeException("Failed to retrieve medical notes. Status code: " + response.getStatusCode().value());
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
    public void updateNotes(Integer id, MedicalNotesEntity updatedNote) {
        String externalServiceUrl = "http://192.168.1.128:9090/api/notes/" + id;

        MedicalNotesEntity notes = getNoteById(id);

        Integer patientId = notes.getPatientId();

        updatedNote.setPatientId(notes.getPatientId());
        updatedNote.setPatientLastName(notes.getPatientLastName());
        updatedNote.setDateTimeAtCreation(LocalDateTime.now());

        // Créer l'en-tête de la requête
        HttpEntity<MedicalNotesEntity> requestEntity = new HttpEntity<>(updatedNote);

        // Envoyer la requête PUT
        restTemplate.exchange(externalServiceUrl, HttpMethod.PUT, requestEntity, Void.class);
    }


    @Override
    public MedicalNotesEntity getNoteById(Integer id) {
        logger.debug("getNoteById method starts here, MedicalNoteServiceImpl");

        ResponseEntity<List<MedicalNotesEntity>>response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MedicalNotesEntity>>() {}
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            List<MedicalNotesEntity> entities = response.getBody();
            if (entities != null && !entities.isEmpty()) {
                Optional<MedicalNotesEntity> matchingEntity = entities.stream()
                        .filter(entity -> entity.getId().equals(id))
                        .findFirst();
                if (matchingEntity.isPresent()) {
                    return matchingEntity.get();
                } else {
                    logger.error("Note with id:{} not found in the database, from MedicalNoteServiceImpl", id);
                    throw new RuntimeException(String.format("Note with id:%d not found in DB!", id));
                }
            } else {
                logger.error("No entities found in the response, from MedicalNoteServiceImpl");
                throw new RuntimeException("No entities found in the response!");
            }
        } else {
            throw new RuntimeException("Failed to retrieve entities. Status code: " + response.getStatusCode().value());
        }
    }


    @Override
    public void createMedicalNoteForPatient(Integer patientId, String message) {
        MedicalNotesEntity medicalNotes = new MedicalNotesEntity();

        // Faire une requête à l'API Patient pour récupérer les informations du patient
        String patientApiUrl = "http://192.168.1.128:8090/api/patients/" + patientId;
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

        String externalServiceUrl = "http://192.168.1.128:9090/api/notes";

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


