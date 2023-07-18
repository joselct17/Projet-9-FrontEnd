package com.OpenClassroom.FrontEnd.Controller;


import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.NoteServiceImpl;
import com.OpenClassroom.FrontEnd.Service.Implementation.PatientServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class MedicalNotesController {

    private final Logger logger = LoggerFactory.getLogger(MedicalNotesEntity.class);

    private final RestTemplate restTemplate;

    private final PatientServiceImpl patientService;

    private final NoteServiceImpl noteService;

    @Autowired
    public MedicalNotesController(RestTemplate restTemplate, PatientServiceImpl patientService, NoteServiceImpl noteService) {
        this.restTemplate = restTemplate;
        this.patientService = patientService;
        this.noteService = noteService;
    }


    /**
     * Récupère toutes les notes médicales.
     *
     * @param model le modèle pour passer les données à la vue
     * @return le nom de la vue pour afficher toutes les notes médicales
     */
    @Operation(summary = "Récupère toutes les notes médicales")
    @GetMapping("/notes")
    public String getAllNotes(Model model) {
        List<MedicalNotesEntity> notes = noteService.getAllNotes();
        model.addAttribute("notes", notes);
        return "notes/notes";
    }

    /**
     * Crée une nouvelle note médicale pour un patient.
     *
     * @param patientId l'ID du patient
     * @param message le message de la note médicale
     * @return la redirection vers les informations du patient
     */
    @Operation(summary = "Crée une nouvelle note médicale pour un patient")
    @PostMapping("/notes/{patientId}")
    public RedirectView createNote(
            @Parameter(description = "ID du patient") @PathVariable Integer patientId,
            @Parameter(description = "Message de la note médicale") @RequestParam("message") String message) {
        noteService.createMedicalNoteForPatient(patientId, message);

        // Créer l'URL de redirection
        String redirectUrl = "/patients/info/" + patientId;
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setExposeModelAttributes(false); // Optionnel, pour éviter l'exposition des attributs du modèle

        return redirectView;
    }

    /**
     * Supprime une note médicale par son ID.
     *
     * @param id l'ID de la note médicale à supprimer
     * @return la redirection vers les informations du patient
     */
    @Operation(summary = "Supprime une note médicale par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirection vers les informations du patient"),
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la suppression des données")
    })
    @GetMapping("/notes/delete/{id}")
    public String deleteById(@Parameter(description = "ID de la note médicale") @PathVariable Integer id) {
        String externalServiceUrl = "http://192.168.1.128:9090/api/notes/" + id;

        MedicalNotesEntity note = noteService.getNoteById(id);
        Integer patientId = note.getPatientId();

        try {
            restTemplate.delete(externalServiceUrl);
            return "redirect:/patients/info/" + patientId;
        } catch (Exception e) {
            return ("Erreur lors de la suppression des données.");
        }
    }

    /**
     * Affiche le formulaire de mise à jour d'une note médicale par son ID.
     *
     * @param id l'ID de la note médicale
     * @param model le modèle pour passer les données à la vue
     * @return le nom de la vue pour afficher le formulaire de mise à jour
     */
    @Operation(summary = "Affiche le formulaire de mise à jour d'une note médicale par son ID")
    @GetMapping("/notes/update/{id}")
    public String getUpdateNote(
            @Parameter(description = "ID de la note médicale") @PathVariable Integer id,
            Model model) {
        MedicalNotesEntity note = noteService.getNoteById(id);
        model.addAttribute("notes", note);
        return "notes/update";
    }

    /**
     * Valide la mise à jour d'une note médicale par son ID.
     *
     * @param updatedNote la note médicale mise à jour
     * @param id l'ID de la note médicale
     * @return la redirection vers les informations du patient
     */
    @Operation(summary = "Valide la mise à jour d'une note médicale par son ID")
    @PostMapping("/notes/update/{id}")
    public RedirectView validateUpdateNotes(
            @Parameter(description = "Note médicale mise à jour") @ModelAttribute("notes") MedicalNotesEntity updatedNote,
            @Parameter(description = "ID de la note médicale") Integer id) {
        noteService.updateNotes(id, updatedNote);
        Integer patientId = updatedNote.getPatientId();

        // Reste du code pour la redirection
        String redirectUrl = "/patients/info/" + patientId;
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }

}
