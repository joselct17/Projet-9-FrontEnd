package com.OpenClassroom.FrontEnd.Controller;
import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.NoteServiceImpl;
import com.OpenClassroom.FrontEnd.Service.Implementation.PatientServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.RedirectView;

import javax.print.attribute.standard.DateTimeAtCreation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MedicalNotesControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @MockBean
    private PatientServiceImpl patientService;

    @Mock
    private NoteServiceImpl noteService;

    @Mock
    private Model model;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @InjectMocks
    private MedicalNotesController medicalNotesController;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        medicalNotesController = new MedicalNotesController(restTemplate,patientService, noteService);
        ReflectionTestUtils.setField(noteService, "apiUrl", "http://192.168.1.128:9090/api/notes");
        mockMvc = MockMvcBuilders.standaloneSetup(context).build();
    }


    @Test
    void testGetAllNotes() {
        // Arrange
        List<MedicalNotesEntity> notes = new ArrayList<>();

        ResponseEntity<List<MedicalNotesEntity>> responseEntity = new ResponseEntity<>(notes, HttpStatus.OK);
        when(restTemplate.exchange(eq("http://192.168.1.128:9090/api/notes"), eq(HttpMethod.GET), eq(null),
                eq(new ParameterizedTypeReference<List<MedicalNotesEntity>>() {})))
                .thenReturn(responseEntity);

        // Act
        List<MedicalNotesEntity> actualNotes = noteService.getAllNotes();

        // Assert
        assertEquals(notes, actualNotes);
    }

    @Test
    void testCreateNote() {
        // Arrange
        Integer patientId = 1;
        String message = "This is a test note";

        String patientApiUrl = "http://192.168.1.128:8090/api/patients/" + patientId;
        String externalServiceUrl = "http://192.168.1.128:9090/api/notes";
        String redirectUrl = "/patients/info/" + patientId;

        // Mock de la réponse de l'API externe pour récupérer les informations du patient
        ResponseEntity<PatientEntity> patientResponse = new ResponseEntity<>(new PatientEntity(), HttpStatus.OK);
        when(restTemplate.exchange(eq(patientApiUrl), eq(HttpMethod.GET), eq(null), eq(PatientEntity.class)))
                .thenReturn(patientResponse);

        // Mock de la réponse de l'API externe pour la création de la note médicale
        ResponseEntity<MedicalNotesEntity> noteResponse = new ResponseEntity<>(new MedicalNotesEntity(), HttpStatus.OK);
        when(restTemplate.postForEntity(eq(externalServiceUrl), any(HttpEntity.class), eq(MedicalNotesEntity.class)))
                .thenReturn(noteResponse);

        // Act
        RedirectView actualRedirectView = medicalNotesController.createNote(patientId, message);

        // Assert
        assertEquals(redirectUrl, actualRedirectView.getUrl());
    }


    @Test
    void testDeleteById() {
        // Arrange
        Integer id = 1;
        Integer patientId = 2;
        String externalServiceUrl = "http://192.168.1.128:9090/api/notes/" + id;
        String redirectUrl = "/patients/info/" + patientId;

        MedicalNotesEntity note = new MedicalNotesEntity();
        note.setPatientId(patientId);

        // Mock de noteService.getNoteById()
        when(noteService.getNoteById(id)).thenReturn(note);

        // Mock de restTemplate.exchange() pour renvoyer une réponse réussie
        ResponseEntity<List<MedicalNotesEntity>> response = new ResponseEntity<>(Collections.singletonList(note), HttpStatus.OK);
        when(restTemplate.exchange(
                eq("http://192.168.1.128:9090/api/notes"),
                eq(HttpMethod.GET),
                eq(null),
                eq(new ParameterizedTypeReference<List<MedicalNotesEntity>>() {})
        )).thenReturn(response);

        // Mock de la suppression de l'entité via restTemplate.delete()
        doNothing().when(restTemplate).delete(externalServiceUrl);

        // Act
        String actualResult = medicalNotesController.deleteById(id);

        // Assert
        assertEquals("redirect:" + redirectUrl, actualResult);
        verify(noteService, times(1)).getNoteById(id);
        verify(restTemplate, times(1)).delete(externalServiceUrl);
    }


    @Test
    void testGetUpdateNote() {
        // Arrange
        Integer id = 1;
        MedicalNotesEntity note = new MedicalNotesEntity();
        when(noteService.getNoteById(id)).thenReturn(note);

        // Act
        String viewName = medicalNotesController.getUpdateNote(id, model);

        // Assert
        assertEquals("notes/update", viewName);
        verify(model).addAttribute("notes", note);
    }

    @Test
    void testValidateUpdateNotes() {
        // Arrange
        Integer id = 1;
        MedicalNotesEntity updatedNote = new MedicalNotesEntity();
        updatedNote.setPatientId(2);
        RedirectView expectedRedirectView = new RedirectView("/patients/info/2");

        // Act
        RedirectView redirectView = medicalNotesController.validateUpdateNotes(updatedNote, id);

        // Assert
        assertEquals(expectedRedirectView.getUrl(), redirectView.getUrl());
        verify(noteService).updateNotes(id, updatedNote);
    }

    @Test
    public void testMedicalNotesEntity() {
        // Crée une instance de l'entité MedicalNotesEntity
        MedicalNotesEntity medicalNote = new MedicalNotesEntity();
        medicalNote.setId(1);
        medicalNote.setPatientId(2);
        medicalNote.setPatientLastName("Doe");
        medicalNote.setNote("This is a medical note.");
        medicalNote.setDateTimeAtCreation(LocalDateTime.now());

        // Effectue les assertions
        assertEquals(1, medicalNote.getId());
        assertEquals(2, medicalNote.getPatientId());
        assertEquals("Doe", medicalNote.getPatientLastName());
        assertEquals("This is a medical note.", medicalNote.getNote());
        assertNotNull(medicalNote.getDateTimeAtCreation());
    }
}