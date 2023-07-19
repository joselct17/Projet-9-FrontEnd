package com.OpenClassroom.FrontEnd.Controller;


import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.random.RandomGenerator;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void testShowPatientList() throws Exception {

        // Perform the GET request
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("patients/list"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("patients"));

    }


    @Test
    public void testShowPatientsAdd() throws Exception {

        // Perform the GET request
        mockMvc.perform(get("/patients/add"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("patients/add"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("patients"));

    }

    @Test
    public void testPostValidatePatient() throws Exception {


        PatientEntity patients = new PatientEntity(1,"John", "Doe", LocalDate.now(), "M", "101-Address", "15451");
        // Perform the POST request
        mockMvc.perform(post("/patients")
                        .flashAttr("patients", patients))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

    }

    @Test
    public void testShowInfo() throws Exception {

        Integer id = 1;

        // Perform the GET request
        mockMvc.perform(get("/patients/info/" + id))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("patients/info"))
              .andExpect(MockMvcResultMatchers.model().attributeExists("patients"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("notes"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("patientId"));

    }

    @Test
    public void testGetUpdatePatient() throws Exception {

        Integer id = 1;

        // Perform the GET request
        mockMvc.perform(get("/patients/update/" + id))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("patients/update"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("patients"));

    }


    @Test
    public void testPostUpdatePatient() throws Exception {


        PatientEntity patients = new PatientEntity(1,"John", "Doe", LocalDate.now(), "M", "101-Address", "15451");
        Integer id = patients.getId();

        // Perform the POST request
        mockMvc.perform(post("/patients/update/" + id)
                        .flashAttr("patients", patients))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

    }

}
