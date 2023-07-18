package com.OpenClassroom.FrontEnd.Service;


import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Model.ReportEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.NoteServiceImpl;
import com.OpenClassroom.FrontEnd.Service.Implementation.PatientServiceImpl;
import com.OpenClassroom.FrontEnd.Service.Implementation.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@SpringBootTest
@AutoConfigureMockMvc
public class ReportServiceTest {
    @Mock
    private NoteServiceImpl noteService;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PatientServiceImpl patientService;
    @InjectMocks
    private ReportServiceImpl reportService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReportByPatientId() {
        Integer patientId = 1;

        // Mock les données nécessaires pour le test
        PatientEntity patient = new PatientEntity(patientId, "John", "Doe", LocalDate.of(1990, 1, 1), "M", "101-Address", "15451");
        when(patientService.patientById(patientId)).thenReturn(patient);

        List<MedicalNotesEntity> medicalNotes = new ArrayList<>();
        // Ajouter des objets MedicalNotesEntity à medicalNotes

        when(noteService.getNotesByPatientId(patientId)).thenReturn(medicalNotes);
        when(patientService.patientById(patientId)).thenReturn(patient);

        // Appeler la méthode à tester
        ReportEntity report = reportService.getReportByPatientId(patientId);

        // Vérifier les résultats
        assertEquals(patientId, report.getId());
        assertEquals("Doe", report.getPatientName());
        assertEquals("none", report.getDiabetesReport());

    }
}
