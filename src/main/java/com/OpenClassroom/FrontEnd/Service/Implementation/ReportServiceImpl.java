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
        logger.debug("getReportByPatientId commence ici, depuis ReportServiceImpl");

        // Récupère les notes médicales pour l'ID de patient donné
        List<MedicalNotesEntity> medicalNotes = noteService.getNotesByPatientId(patientId);
        logger.info("Notes médicales récupérées pour le patient avec l'ID : {}", patientId);

        // Compte le nombre de déclencheurs de notes dans les notes médicales

        MedicalNotesEntity[] medicalNoes = medicalNotes.toArray(new MedicalNotesEntity[0]);
        // Convertir la liste 'medicalNotes' en un tableau 'MedicalNotesEntity[]' et le stocker dans 'medicalNoes'

        List<String> notes = Arrays.stream(medicalNoes).map(MedicalNotesEntity::getNote).toList();
        // Utiliser un flux pour extraire les notes de chaque objet 'MedicalNotesEntity' et créer une nouvelle liste 'notes'

        Long termsTrigger = countTriggerWords(notes);
        // Appeler la méthode 'countTriggerWords' avec la liste 'notes' et stocker le nombre de déclencheurs de mots dans 'termsTrigger'

        logger.info("Nombre de déclencheurs de notes trouvés : {}", termsTrigger);
        // Afficher un message d'information indiquant le nombre de déclencheurs de mots trouvés



        // Récupère le sexe du patient
        String patientGender = patientService.patientById(patientId).getSex();
        logger.info("Sexe récupéré pour le patient avec l'ID : {}", patientId);

        // Calcule l'âge du patient
        int patientAge = calculateAge(patientId);
        logger.info("Âge calculé pour le patient avec l'ID : {}", patientId);

        String riskLevel;

        // Détermine le niveau de risque en fonction du nombre de déclencheurs de notes et de l'âge du patient
        if (termsTrigger == 0) {
            riskLevel = "none";
            logger.info("Aucun mot déclencheur trouvé. Niveau de risque : none");
        } else if (termsTrigger == 2 && patientAge > 30) {
            riskLevel = "Borderline";
            logger.info("2 mots déclencheurs trouvés et âge du patient > 30 ans. Niveau de risque : Borderline");
        } else if (patientAge < 30) {
            if (patientGender.equalsIgnoreCase("M") && termsTrigger >= 3) {
                riskLevel = "In Danger";
                logger.info("3 mots déclencheurs trouvés, sexe masculin et âge du patient < 30 ans. Niveau de risque : In Danger");
            } else if (patientGender.equalsIgnoreCase("F") && termsTrigger >= 4) {
                riskLevel = "In Danger";
                logger.info("4 mots déclencheurs trouvés, sexe féminin et âge du patient < 30 ans. Niveau de risque : In Danger");
            } else if (patientGender.equalsIgnoreCase("M") && termsTrigger >= 5) {
                riskLevel = "Early onset";
                logger.info("5 mots déclencheurs trouvés, sexe masculin et âge du patient < 30 ans. Niveau de risque : Early onset");
            } else if (patientGender.equalsIgnoreCase("F") && termsTrigger >= 7) {
                riskLevel = "Early onset";
                logger.info("7 mots déclencheurs trouvés, sexe féminin et âge du patient < 30 ans. Niveau de risque : Early onset");
            } else {
                riskLevel = "none";
                logger.info("Aucun niveau de risque déterminé pour le patient < 30 ans. Niveau de risque : none");
            }
        } else {
            if (patientGender.equalsIgnoreCase("M") && termsTrigger >= 6) {
                riskLevel = "En danger";
                logger.info("6 mots déclencheurs trouvés, sexe masculin et âge du patient >= 30 ans. Niveau de risque : En danger");
            } else if (patientGender.equalsIgnoreCase("F") && termsTrigger >= 6) {
                riskLevel = "En danger";
                logger.info("6 mots déclencheurs trouvés, sexe féminin et âge du patient >= 30 ans. Niveau de risque : En danger");
            } else if (patientGender.equalsIgnoreCase("M") && termsTrigger >= 8) {
                riskLevel = "Early onset";
                logger.info("8 mots déclencheurs trouvés, sexe masculin et âge du patient >= 30 ans. Niveau de risque : Early onset");
            } else if (patientGender.equalsIgnoreCase("F") && termsTrigger >= 8) {
                riskLevel = "Early onset";
                logger.info("8 mots déclencheurs trouvés, sexe féminin et âge du patient >= 30 ans. Niveau de risque : Early onset");
            } else {
                riskLevel = "Unknown";
                logger.info("Niveau de risque inconnu pour le patient. Niveau de risque : Unknown");
            }
        }


        // Récupère le nom du patient
        String patientName = patientService.patientById(patientId).getLastName();
        logger.info("Nom du patient récupéré pour le patient avec l'ID : {}", patientId);

        logger.debug("getReportByPatientId se termine ici, depuis ReportServiceImpl");

        // Retourne une nouvelle entité de rapport avec l'ID du patient, le nom du patient et le niveau de risque
        return new ReportEntity(patientId, patientName, riskLevel);
    }

    @Override
    public ReportEntity getReportByPatLastName(String patientName) {
        return null;
    }


    public long countTriggerWords(List<String> comments) {
        // Début de la méthode 'countTriggerWords'. Message de débogage pour indiquer le début de l'exécution.
        logger.debug("countTriggerWords commence ici, depuis ReportServiceImpl");


        // Liste des mots déclencheurs à rechercher dans les commentaires.
        List<String> triggerWords = Arrays.asList(
                "hémoglobine a1c", "microalbumine", "taille", "poids", "fumeur", "anormal", "cholestérol", "vertige", "rechute", "réaction", "anticorps", "fume");

        long count = comments.stream()
                // Convertit tous les commentaires en minuscules pour une recherche insensible à la casse.
                .map(String::toLowerCase)
                // Applique un filtre pour rechercher les mots déclencheurs dans chaque commentaire.
                .flatMap(comment -> triggerWords.stream().filter(comment::contains))
                // Supprime les doublons pour ne compter chaque mot déclencheur qu'une seule fois.
                .distinct()
                .peek(System.out::println)
                // Compte le nombre total de mots déclencheurs trouvés dans tous les commentaires.
                .count();

        // Affiche le nombre total de mots déclencheurs (facultatif).
        System.out.println("Le nombre des mots déclencheurs est: " + count);

        // Message d'information pour indiquer que la méthode 'countTriggerWords' a été appelée avec succès et le nombre de mots déclencheurs calculé.
        logger.info("countTriggerWords a été appelée avec succès et a calculé le nombre de mots déclencheurs : {}, depuis ReportServiceImpl", count);

        // Retourne le nombre total de mots déclencheurs.
        return count;

    }





    private int calculateAge(Integer id) {
        logger.debug("calculateAge commence ici, depuis ReportServiceImpl");

        // Récupère le patient pour le calcul de l'âge avec l'ID donné
        PatientEntity patient = patientService.patientById(id);
        logger.info("Patient récupéré pour le calcul de l'âge avec l'ID : {}", id);

        LocalDate birthDate = patient.getBirthDate();
        // Calcule l'âge en années
        Period age = Period.between(birthDate, LocalDate.now());
        int patientAge = age.getYears();

        logger.debug("calculateAge se termine ici, depuis ReportServiceImpl");

        // Retourne l'âge du patient
        return patientAge;
    }
}