package com.OpenClassroom.FrontEnd.Controller;


import com.OpenClassroom.FrontEnd.Model.DTO.MedicalNotesPageDTO;
import com.OpenClassroom.FrontEnd.Model.DTO.PatientPageDTO;
import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MedicalNotesController {

    private final Logger logger = LoggerFactory.getLogger(MedicalNotesEntity.class);
    @Autowired
    private final RestTemplate restTemplate;

    public MedicalNotesController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/notes")
    public String getAllNotes(Model model) {
        String externalServiceUrl = "http://localhost:9090/api/notes";

        ResponseEntity<List<MedicalNotesEntity>> response = restTemplate.exchange(
                externalServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MedicalNotesEntity>>() {}
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            List<MedicalNotesEntity> notes = response.getBody();
            model.addAttribute("notes", notes);
        } else {
            throw new RuntimeException("Erreur lors de la récupération des notes");
        }
        return "notes/notes";
    }

    @PostMapping("/notes/{patientId}")
    public RedirectView createNote(@PathVariable Integer patientId, @RequestParam("message") String message) {
        MedicalNotesEntity medicalNotes = new MedicalNotesEntity();

        // Faire une requête à l'API Patient pour récupérer les informations du patient
        String patientApiUrl = "http://localhost:8090/api/patients/" + patientId;
        RestTemplate restTemplate = new RestTemplate();
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

        // Créer l'URL de redirection
        String redirectUrl = "/patients/info/" + patientId;
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setExposeModelAttributes(false); // Optionnel, pour éviter l'exposition des attributs du modèle

        return redirectView;
    }


}
