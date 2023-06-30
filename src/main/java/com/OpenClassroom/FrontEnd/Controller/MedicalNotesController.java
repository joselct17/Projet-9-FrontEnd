package com.OpenClassroom.FrontEnd.Controller;


import com.OpenClassroom.FrontEnd.Model.DTO.MedicalNotesPageDTO;
import com.OpenClassroom.FrontEnd.Model.DTO.PatientPageDTO;
import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.NoteServiceImpl;
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

    @Autowired
    NoteServiceImpl noteService;

    public MedicalNotesController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @GetMapping("/notes")
    public String getAllNotes(Model model) {

            List<MedicalNotesEntity> notes = noteService.getAllNotes();
            model.addAttribute("notes", notes);

        return "notes/notes";
    }


    @PostMapping("/notes/{patientId}")
    public RedirectView createNote(@PathVariable Integer patientId, @RequestParam("message") String message) {
        noteService.createMedicalNoteForPatient(patientId, message);

        // Créer l'URL de redirection
        String redirectUrl = "/patients/info/" + patientId;
        RedirectView redirectView = new RedirectView(redirectUrl);
        redirectView.setExposeModelAttributes(false); // Optionnel, pour éviter l'exposition des attributs du modèle

        return redirectView;
    }
}
