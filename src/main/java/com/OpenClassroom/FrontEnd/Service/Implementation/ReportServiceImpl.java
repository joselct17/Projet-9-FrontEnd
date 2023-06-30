package com.OpenClassroom.FrontEnd.Service.Implementation;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Model.ReportEntity;
import com.OpenClassroom.FrontEnd.Service.Interface.IReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;

@Service
public class ReportServiceImpl implements IReportService {

    private Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    @Autowired
    private NoteServiceImpl noteService;

    @Autowired
    private PatientServiceImpl patientService;

    @Override
    public ReportEntity getReportByPatientId(Integer patientId) {
        logger.debug("getReportByPatientId starts here, from ReportServiceImpl");

        List<MedicalNotesEntity> medicalNotes = noteService.getNotesByPatientId(patientId);
        logger.info("Medical notes retrieved for patient with ID: {}", patientId);

        int noteTriggersCount = countNoteTriggers(medicalNotes);
        logger.info("Number of note triggers found: {}", noteTriggersCount);

        String patientGender = patientService.patientById(patientId).getSex();
        logger.info("Gender retrieved for patient with ID: {}", patientId);

        int patientAge = calculateAge(patientId);
        logger.info("Age calculated for patient with ID: {}", patientId);

        String riskLevel;

        if (noteTriggersCount == 0) {
            riskLevel = "None";
        } else if (noteTriggersCount == 2 && patientAge > 30) {
            riskLevel = "Borderline";
        } else if (patientAge < 30) {
            if (patientGender.equalsIgnoreCase("M") && noteTriggersCount >= 3) {
                riskLevel = "In Danger";
            } else if (patientGender.equalsIgnoreCase("F") && noteTriggersCount >= 4) {
                riskLevel = "In Danger";
            } else if (patientGender.equalsIgnoreCase("M") && noteTriggersCount >= 5) {
                riskLevel = "Early onset";
            } else if (patientGender.equalsIgnoreCase("F") && noteTriggersCount >= 7) {
                riskLevel = "Early onset";
            } else {
                riskLevel = "None";
            }
        } else {
            if (patientGender.equalsIgnoreCase("M") && noteTriggersCount >= 6) {
                riskLevel = "In Danger";
            } else if (patientGender.equalsIgnoreCase("F") && noteTriggersCount >= 6) {
                riskLevel = "In Danger";
            } else if (patientGender.equalsIgnoreCase("M") && noteTriggersCount >= 8) {
                riskLevel = "Early onset";
            } else if (patientGender.equalsIgnoreCase("F") && noteTriggersCount >= 8) {
                riskLevel = "Early onset";
            } else {
                riskLevel = "None";
            }
        }

        String patientName = patientService.patientById(patientId).getLastName();
        logger.info("Patient name retrieved for patient with ID: {}", patientId);

        logger.debug("getReportByPatientId ends here, from ReportServiceImpl");

        return new ReportEntity(patientId, patientName, riskLevel);
    }

    @Override
    public ReportEntity getReportByPatLastName(String patientName) {
        return null;
    }

    private int countNoteTriggers(List<MedicalNotesEntity> medicalNotes) {
        logger.debug("countNoteTriggers starts here, from ReportServiceImpl");

        int count = 0;
        for (MedicalNotesEntity note : medicalNotes) {
            if (noteContainsTriggers(note)) {
                count++;
            }
        }

        logger.debug("countNoteTriggers ends here, from ReportServiceImpl");

        return count;
    }

    private boolean noteContainsTriggers(MedicalNotesEntity note) {
        logger.debug("noteContainsTriggers starts here, from ReportServiceImpl");

        List<String> triggers = Arrays.asList("Hémoglobine A1C", "Microalbumine", "Taille", "Poids", "Fumeur", "Anormal",
                "Cholestérol", "Vertige", "Rechute", "Réaction", "Anticorps");

        String noteContent = note.getNote().toLowerCase(); // Convertir la note en minuscules pour une recherche insensible à la casse

        for (String trigger : triggers) {
            if (noteContent.contains(trigger.toLowerCase())) {
                logger.info("Trigger found in medical note: {}", trigger);
                return true;
            }
        }

        logger.debug("noteContainsTriggers ends here, from ReportServiceImpl");

        return false;
    }


    private int calculateAge(Integer id) {
        logger.debug("calculateAge starts here, from ReportServiceImpl");

        PatientEntity patient = patientService.patientById(id);
        logger.info("Patient retrieved for age calculation with ID: {}", id);

        LocalDate birthDate = patient.getBirthDate();
        Period age = Period.between(birthDate, LocalDate.now());
        int patientAge = age.getYears();

        logger.debug("calculateAge ends here, from ReportServiceImpl");

        return patientAge;
    }
}