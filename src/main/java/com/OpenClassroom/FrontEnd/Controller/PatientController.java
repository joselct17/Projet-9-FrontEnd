package com.OpenClassroom.FrontEnd.Controller;

import com.OpenClassroom.FrontEnd.Model.DTO.PatientPageDTO;
import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.NoteServiceImpl;
import com.OpenClassroom.FrontEnd.Service.Implementation.PatientServiceImpl;
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



import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PatientController {
    private final Logger logger = LoggerFactory.getLogger(PatientController.class);
    @Autowired
    private final RestTemplate restTemplate;

    @Autowired
    PatientServiceImpl patientService;

    @Autowired
    NoteServiceImpl noteService;

    public PatientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }



    @GetMapping("/patients")
    public String showPatientList(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PatientPageDTO patientPage = patientService.getAllPaginatedPatients(page, size);
        List<PatientEntity> patients = patientPage.getContent();
        int totalPages = patientPage.getTotalPages();

        model.addAttribute("patients", patients);
        model.addAttribute("totalPages", totalPages);
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
        patientService.createPatient(patient);

        PatientPageDTO patients = patientService.getAllPaginatedPatients(page, size);
        model.addAttribute("patients", patients);

        String redirectUrl = "/patients";
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setExposeModelAttributes(false);

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

        PatientEntity patientEntity = patientService.patientById(id);

        model.addAttribute("patients", patientEntity);

        List<MedicalNotesEntity> medicalNotesEntity = noteService.getNotesByPatientId(id);

                model.addAttribute("notes", medicalNotesEntity);


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
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {

        PatientPageDTO patientPage = patientService.getPatientsByLastName(lastName, page, size);
        List<PatientEntity> paginatedPatients = patientPage.getContent();
        int totalPages = patientPage.getTotalPages();

        model.addAttribute("patients", paginatedPatients);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);

        return "patients/list";
    }





}