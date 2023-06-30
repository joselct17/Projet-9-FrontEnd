package com.OpenClassroom.FrontEnd.Controller;

import com.OpenClassroom.FrontEnd.Model.DTO.PatientPageDTO;
import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PatientController {
    private final Logger logger = LoggerFactory.getLogger(PatientController.class);
    @Autowired
    private final RestTemplate restTemplate;

    public PatientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }



    @GetMapping("/patients")
    public String showPatientList(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        // Appel de l'API externe pour récupérer la liste paginée des patients
        String externalServiceUrl = "http://localhost:8090/api/patients?page=" + page + "&size=" + size;

        ResponseEntity<PatientPageDTO> response = restTemplate.exchange(
                externalServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PatientPageDTO>() {}
        );

        PatientPageDTO patientPage = response.getBody();
        List<PatientEntity> patients = patientPage.getContent();
        int totalPages = patientPage.getTotalPages();

        model.addAttribute("patients", patients);
        model.addAttribute("totalPages", totalPages);

        // Ajouter la variable currentPage avec la valeur de la page actuelle
        model.addAttribute("currentPage", page);

        return "patients/list";
    }

    @GetMapping("/patients/add")
    public String addUser(PatientEntity bid, Model model) {

        logger.info("GET:/patients/add");
        model.addAttribute("patients", bid);
        return "patients/add";
    }


    @PostMapping("/patients")
    public RedirectView validate(@ModelAttribute("patients") PatientEntity patient, Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        String externalServiceUrl = "http://localhost:8090/api/patients";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PatientEntity> requestEntity = new HttpEntity<>(patient, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<PatientEntity> response = restTemplate.postForEntity(externalServiceUrl, requestEntity, PatientEntity.class);


        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Patient created successfully.");
        } else {
            System.out.println("Failed to create patient. Status code: " + response.getStatusCode().value());
        }

        String externalPagination = "http://localhost:8090/api/patients?page=" + page + "&size=" + size;

        ResponseEntity<PatientPageDTO> pagination = restTemplate.exchange(
                externalPagination,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PatientPageDTO>() {}
        );

        PatientPageDTO patientPage = pagination.getBody();
        List<PatientEntity> patients = patientPage.getContent();


        model.addAttribute("patients", patients);


        // Créer l'URL de redirection
        String redirectUrl = "/patients";
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setExposeModelAttributes(false); // Optionnel, pour éviter l'exposition des attributs du modèle

        return redirectView;
    }

    @DeleteMapping("/patients/delete/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable Integer id) {

        String externalServiceUrl = "http://localhost:8090/api/patients/" +id;
        try {
            // Appel de l'API externe pour supprimer les données
            restTemplate.delete(externalServiceUrl);

            return ResponseEntity.ok("Données supprimées avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression des données.");
        }
    }


    @GetMapping("/patients/info/{id}")
    public String getPatientById(@PathVariable Integer id, Model model) {
        String externalServiceUrl = "http://localhost:8090/api/patients/" + id;

        ResponseEntity<PatientEntity> response = restTemplate.getForEntity(externalServiceUrl, PatientEntity.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            PatientEntity patient = response.getBody();
            model.addAttribute("patients", patient);

            String notesUrl = "http://localhost:9090/api/notes?patientId=" + id;
            ResponseEntity<List<MedicalNotesEntity>> notesResponse = restTemplate.exchange(
                    notesUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MedicalNotesEntity>>() {} );

            if (notesResponse.getStatusCode().is2xxSuccessful()) {
                List<MedicalNotesEntity> allNotes = notesResponse.getBody();

                // Filtrer les notes pour obtenir uniquement celles liées au patient spécifique
                List<MedicalNotesEntity> patientNotes = allNotes.stream()
                        .filter(note -> note.getPatientId() != null && note.getPatientId().equals(id))
                        .collect(Collectors.toList());
                model.addAttribute("notes", patientNotes);
            } else {
                throw new RuntimeException("Erreur lors de la récupération des notes médicales");
            }
        } else {
            throw new RuntimeException("Erreur lors de la récupération du patient");
        }

        model.addAttribute("patientId", id);

        return "patients/info";
    }

    @GetMapping("/patients/update/{id}")
    public String updatePatient(@PathVariable Integer id, Model model) {
        String externalServiceUrl = "http://localhost:8090/api/patients/" + id;

        ResponseEntity<PatientEntity> response = restTemplate.getForEntity(externalServiceUrl, PatientEntity.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            PatientEntity patient = response.getBody();
            model.addAttribute("patients", patient);
        } else {
            throw new RuntimeException("Erreur lors de la récupération du patient");
        }
        return "patients/update";
    }

    @PostMapping("/patients/update/{id}")
    public String validateUpdatePatient(@ModelAttribute("patients") PatientEntity patient, Integer id) {
        String externalServiceUrl = "http://localhost:8090/api/patients/" +id;

        try {
            // Appel de l'API externe pour mettre à jour les données du patient
            restTemplate.put(externalServiceUrl, patient);

            return "patients/list";
        } catch (Exception e) {
            return ("Erreur lors de la mise à jour des données.");
        }

    }


    @GetMapping("/patients/{lastName}")
    public String getPatientByLastName(@PathVariable String lastName, Model model,
                                       @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        String externalServiceUrl = "http://localhost:8090/api/patients/by-lastName/" + lastName + "?page=" + page + "&size=" + size;

        ResponseEntity<PatientPageDTO> response = restTemplate.exchange(externalServiceUrl, HttpMethod.GET,null, new ParameterizedTypeReference<PatientPageDTO>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            PatientPageDTO patientPage = response.getBody();
            List<PatientEntity> paginatedPatients = patientPage.getContent();

            model.addAttribute("patients", paginatedPatients);

            int totalPages = patientPage.getTotalPages();
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", page);
        } else {
            throw new RuntimeException("Erreur lors de la récupération du patient");
        }


        return "patients/list";
    }





}