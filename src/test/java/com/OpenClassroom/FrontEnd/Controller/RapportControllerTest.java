package com.OpenClassroom.FrontEnd.Controller;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Model.ReportEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.PatientServiceImpl;
import com.OpenClassroom.FrontEnd.Service.Implementation.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import org.springframework.http.HttpMethod;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class RapportControllerTest {

    @Mock
    ReportServiceImpl reportService;

    @MockBean
    PatientServiceImpl patientService;
    @Mock
    private RestTemplate restTemplate;
    @Autowired
    private WebApplicationContext context;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testGetReportByPatientId() throws Exception {
        Integer patientId = 3;

        PatientEntity patient = new PatientEntity();
        patient.setSex("M"); // Définissez le sexe du patient à tester
        patient.setBirthDate(LocalDate.now());

        when(reportService.getReportByPatientId(patientId)).thenReturn(new ReportEntity());
        when(patientService.patientById(patientId)).thenReturn(patient);

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/info/rapport/" + patientId))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
