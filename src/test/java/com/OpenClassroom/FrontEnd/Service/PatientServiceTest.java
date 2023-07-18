package com.OpenClassroom.FrontEnd.Service;


import com.OpenClassroom.FrontEnd.Model.DTO.PatientPageDTO;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.PatientServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest
class PatientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @MockBean
    private PatientServiceImpl patientService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockServer = MockRestServiceServer.createServer(restTemplate);
        patientService = new PatientServiceImpl(restTemplate);
    }



    @Test
    void testGetAllPaginatedPatients() {
        // Arrange
        int page = 0;
        int size = 10;
        PatientPageDTO expectedPageDTO = new PatientPageDTO();
        ResponseEntity<PatientPageDTO> responseEntity = ResponseEntity.ok(expectedPageDTO);

        String url = "http://192.168.1.128:8090/api/patients?page=0&size=10";
        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), eq(new ParameterizedTypeReference<PatientPageDTO>() {})))
                .thenReturn(responseEntity);

        // Act
        PatientPageDTO actualPageDTO = patientService.getAllPaginatedPatients(page, size);

        // Assert
        assertEquals(expectedPageDTO, actualPageDTO);

    }

    @Test
    void testCreatePatient() {
        // Arrange
        PatientEntity patient = new PatientEntity();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PatientEntity> requestEntity = new HttpEntity<>(patient, headers);

        String url = "http://192.168.1.128:8090/api/patients";
        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withNoContent());

        // Act
        patientService.createPatient(patient);

    }

    @Test
    void testGetPatientsByLastName() {
        // Arrange
        String lastName = "Doe";
        int page = 0;
        int size = 10;
        PatientPageDTO expectedPageDTO = new PatientPageDTO();
        ResponseEntity<PatientPageDTO> responseEntity = ResponseEntity.ok(expectedPageDTO);

        String url = "http://192.168.1.128:8090/api/patients/by-lastName/Doe?page=0&size=10";
        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), isNull(), eq(new ParameterizedTypeReference<PatientPageDTO>() {})))
                .thenReturn(responseEntity);

        // Act
        PatientPageDTO actualPageDTO = patientService.getPatientsByLastName(lastName, page, size);

        // Assert
        assertEquals(expectedPageDTO, actualPageDTO);

    }
}