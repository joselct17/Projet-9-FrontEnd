package com.OpenClassroom.FrontEnd.Controller;

import com.OpenClassroom.FrontEnd.Model.DTO.PatientPageDTO;
import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.NoteServiceImpl;
import com.OpenClassroom.FrontEnd.Service.Implementation.PatientServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    private final RestTemplate restTemplate;

    private final PatientServiceImpl patientService;


    private final NoteServiceImpl noteService;

    public PatientController(RestTemplate restTemplate, PatientServiceImpl patientService, NoteServiceImpl noteService) {
        this.restTemplate = restTemplate;
        this.patientService = patientService;
        this.noteService = noteService;
    }


    /**
     * Affiche la liste des patients paginée.
     *
     * @param model le modèle pour passer les données à la vue
     * @param page le numéro de page
     * @param size la taille de la page
     * @return le nom de la vue pour afficher la liste des patients
     */
    @Operation(summary = "Affiche la liste des patients paginée")
    @GetMapping("/")
    public String showPatientList(
            Model model,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size) {
        PatientPageDTO patientPage = patientService.getAllPaginatedPatients(page, size);
        List<PatientEntity> patients = patientPage.getContent();
        int totalPages = patientPage.getTotalPages();

        model.addAttribute("patients", patients);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);

        return "patients/list";
    }

    /**
     * Affiche le formulaire d'ajout d'un patient.
     *
     * @param bid l'entité du patient
     * @param model le modèle pour passer les données à la vue
     * @return le nom de la vue pour afficher le formulaire d'ajout
     */
    @Operation(summary = "Affiche le formulaire d'ajout d'un patient")
    @GetMapping("/patients/add")
    public String addUser(PatientEntity bid, Model model) {
        logger.info("GET:/patients/add");
        model.addAttribute("patients", bid);
        return "patients/add";
    }

    /**
     * Valide l'ajout d'un patient.
     *
     * @param patient l'entité du patient
     * @param model le modèle pour passer les données à la vue
     * @param page le numéro de page
     * @param size la taille de la page
     * @return la redirection vers la liste des patients
     */
    @Operation(summary = "Valide l'ajout d'un patient")
    @PostMapping("/patients")
    public RedirectView validate(
            @Parameter(description = "Patient à ajouter") @ModelAttribute("patients") PatientEntity patient,
            Model model,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size) {
        patientService.createPatient(patient);

        PatientPageDTO patients = patientService.getAllPaginatedPatients(page, size);
        model.addAttribute("patients", patients);

        String redirectUrl = "/";
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setExposeModelAttributes(false);

        return redirectView;
    }

    /**
     * Supprime un patient par son ID.
     *
     * @param id l'ID du patient à supprimer
     * @return la redirection vers la liste des patients
     */
    @Operation(summary = "Supprime un patient par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirection vers la liste des patients"),
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la suppression des données")
    })
    @GetMapping("/patients/delete/{id}")
    public String deletePatient(
            @Parameter(description = "ID du patient") @PathVariable Integer id) {
        String externalServiceUrl = "http://192.168.1.128:8090/api/patients/" + id;
        String externalServiceNotesUrl = "http://192.168.1.128:9090/api/notes/by-patientId/" + id;

        try {
            // Appel de l'API externe pour supprimer les données
            restTemplate.delete(externalServiceUrl);
            restTemplate.delete(externalServiceNotesUrl);

            return "redirect:/";
        } catch (Exception e) {
            return ("Erreur lors de la suppression des données.");
        }
    }

    /**
     * Affiche les informations d'un patient par son ID.
     *
     * @param id l'ID du patient
     * @param model le modèle pour passer les données à la vue
     * @return le nom de la vue pour afficher les informations du patient
     */
    @Operation(summary = "Affiche les informations d'un patient par son ID")
    @GetMapping("/patients/info/{id}")
    public String getPatientById(
            @Parameter(description = "ID du patient") @PathVariable Integer id,
            Model model) {
        PatientEntity patientEntity = patientService.patientById(id);
        model.addAttribute("patients", patientEntity);

        List<MedicalNotesEntity> medicalNotesEntity = noteService.getNotesByPatientId(id);
        model.addAttribute("notes", medicalNotesEntity);

        model.addAttribute("patientId", id);

        return "patients/info";
    }

    /**
     * Affiche le formulaire de mise à jour d'un patient par son ID.
     *
     * @param id l'ID du patient
     * @param model le modèle pour passer les données à la vue
     * @return le nom de la vue pour afficher le formulaire de mise à jour
     */
    @Operation(summary = "Affiche le formulaire de mise à jour d'un patient par son ID")
    @GetMapping("/patients/update/{id}")
    public String updatePatient(
            @Parameter(description = "ID du patient") @PathVariable Integer id,
            Model model) {
        String externalServiceUrl = "http://192.168.1.128:8090/api/patients/" + id;

        ResponseEntity<PatientEntity> response = restTemplate.getForEntity(externalServiceUrl, PatientEntity.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            PatientEntity patient = response.getBody();
            model.addAttribute("patients", patient);
        } else {
            throw new RuntimeException("Erreur lors de la récupération du patient");
        }
        return "patients/update";
    }

    /**
     * Valide la mise à jour d'un patient par son ID.
     *
     * @param patient l'entité du patient mise à jour
     * @param id l'ID du patient
     * @return la redirection vers la liste des patients
     */
    @Operation(summary = "Valide la mise à jour d'un patient par son ID")
    @PostMapping("/patients/update/{id}")
    public String validateUpdatePatient(
            @Parameter(description = "Patient à mettre à jour") @ModelAttribute("patients") PatientEntity patient,
            @Parameter(description = "ID du patient") @PathVariable Integer id) {
        String externalServiceUrl = "http://192.168.1.128:8090/api/patients/" + id;

        List<MedicalNotesEntity> notes = noteService.getNotesByPatientId(id);
        for (MedicalNotesEntity note : notes) {
            note.setPatientLastName(patient.getLastName());
            note.setPatientId(patient.getId());
            noteService.updateNotes(note.getId(), note);
        }

        try {
            // Appel de l'API externe pour mettre à jour les données du patient
            restTemplate.put(externalServiceUrl, patient);

            return "redirect:/";
        } catch (Exception e) {
            return ("Erreur lors de la mise à jour des données.");
        }
    }

    /**
     * Affiche la liste des patients filtrée par leur nom de famille.
     *
     * @param lastName le nom de famille du patient
     * @param model le modèle pour passer les données à la vue
     * @param page le numéro de page
     * @param size la taille de la page
     * @return le nom de la vue pour afficher la liste des patients filtrée
     */
    @Operation(summary = "Affiche la liste des patients filtrée par leur nom de famille")
    @GetMapping("/patients/{lastName}")
    public String getPatientByLastName(
            @Parameter(description = "Nom de famille du patient") @PathVariable String lastName,
            Model model,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size) {
        PatientPageDTO patientPage = patientService.getPatientsByLastName(lastName, page, size);
        List<PatientEntity> paginatedPatients = patientPage.getContent();
        int totalPages = patientPage.getTotalPages();

        model.addAttribute("patients", paginatedPatients);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);

        return "patients/list";
    }





}