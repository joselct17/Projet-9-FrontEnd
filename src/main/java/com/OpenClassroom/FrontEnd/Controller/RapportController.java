package com.OpenClassroom.FrontEnd.Controller;

import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Model.ReportEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.PatientServiceImpl;
import com.OpenClassroom.FrontEnd.Service.Implementation.ReportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Period;

@Controller
public class RapportController {

    @Autowired
    ReportServiceImpl reportService;

    @Autowired
    PatientServiceImpl patientService;

    /**
     * Obtient le rapport pour un patient donné.
     *
     * @param patientId l'ID du patient
     * @param model le modèle pour passer les données à la vue
     * @return le nom de la vue pour afficher le rapport
     */

    @Operation(summary = "Obtenir le rapport d'un patient par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rapport récupéré avec succès"),
            @ApiResponse(responseCode = "404", description = "Patient non trouvé")
    })
    @GetMapping("/patients/info/rapport/{patientId}")
    public String getReportBYPatientId(@Parameter(description = "ID du patient")
                                           @PathVariable Integer patientId, Model model) {
       ReportEntity rapport = reportService.getReportByPatientId(patientId);

        PatientEntity patient = patientService.patientById(patientId);

        LocalDate dateOfBirth = patient.getBirthDate();

        LocalDate currentDate = LocalDate.now();

        int age = Period.between(dateOfBirth, currentDate).getYears();

        model.addAttribute("patients", patient);
        model.addAttribute("rapports" ,rapport);
        model.addAttribute("age", age);

        return "notes/rapport";

    }


}
