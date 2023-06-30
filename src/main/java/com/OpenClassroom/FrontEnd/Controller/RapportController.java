package com.OpenClassroom.FrontEnd.Controller;

import com.OpenClassroom.FrontEnd.Model.ReportEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.ReportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class RapportController {

    @Autowired
    ReportServiceImpl reportService;


    @GetMapping("/patients/info/rapport/{patientId}")
    public String getReportBYPatientId(@PathVariable Integer patientId, Model model) {
       ReportEntity rapport = reportService.getReportByPatientId(patientId);
        model.addAttribute("rapports" ,rapport);

        return "notes/rapport";

    }
}
