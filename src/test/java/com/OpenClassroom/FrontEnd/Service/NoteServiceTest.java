package com.OpenClassroom.FrontEnd.Service;

import com.OpenClassroom.FrontEnd.Model.MedicalNotesEntity;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Implementation.NoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;

@SpringBootTest
public class NoteServiceTest {

    @Mock
    private RestTemplate restTemplate;


    private MockRestServiceServer mockServer;

    @InjectMocks
    private NoteServiceImpl noteService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(noteService, "apiUrl", "http://192.168.1.128:9090/api/notes");
    }



    @Test
    public void testGetNotesByPatientId() {
        // Arrange
        Integer patientId = 1;
        MedicalNotesEntity expected = new MedicalNotesEntity(1,1,"Doe", "Note", LocalDateTime.now());
        List<MedicalNotesEntity> notes = new ArrayList<>();
        notes.add(expected);

        ResponseEntity<List<MedicalNotesEntity>> responseEntity = new ResponseEntity<>(notes, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(patientId))
        ).thenReturn(responseEntity);


        // Act

        List<MedicalNotesEntity> result = noteService.getNotesByPatientId(patientId);

        // Assert
        assertEquals(notes, result);
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(patientId)
        );
    }

    @Test
    public void testGetAllNotes() {
        // Arrange
        List<MedicalNotesEntity> notes = new ArrayList<>();
        notes.add(new MedicalNotesEntity());

        ResponseEntity<List<MedicalNotesEntity>> responseEntity = new ResponseEntity<>(notes, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class))
        ).thenReturn(responseEntity);

        // Act
        List<MedicalNotesEntity> result = noteService.getAllNotes();

        // Assert
        assertEquals(notes, result);
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
    }


    @Test
    public void testGetNoteById() {
        // Arrange
        Integer id = 1;
        MedicalNotesEntity expectedNote = new MedicalNotesEntity();
        expectedNote.setId(id);

        List<MedicalNotesEntity> notes = new ArrayList<>();
        notes.add(expectedNote);

        ResponseEntity<List<MedicalNotesEntity>> responseEntity = new ResponseEntity<>(notes, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class))
        ).thenReturn(responseEntity);

        // Act
        MedicalNotesEntity result = noteService.getNoteById(id);

        // Assert
        assertEquals(expectedNote, result);
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void testCreateMedicalNoteForPatient() {
        // Arrange
        Integer patientId = 1;
        String message = "Test message";

        PatientEntity patient = new PatientEntity();
        patient.setLastName("Doe");
        ResponseEntity<PatientEntity> patientResponse = new ResponseEntity<>(patient, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("http://192.168.1.128:8090/api/patients/" + patientId),
                eq(HttpMethod.GET),
                isNull(),
                eq(PatientEntity.class))
        ).thenReturn(patientResponse);

        MedicalNotesEntity expectedNote = new MedicalNotesEntity();
        expectedNote.setPatientId(patientId);
        expectedNote.setPatientLastName(patient.getLastName());
        expectedNote.setNote(message);
        expectedNote.setDateTimeAtCreation(LocalDateTime.now());
        ResponseEntity<MedicalNotesEntity> noteResponse = new ResponseEntity<>(expectedNote, HttpStatus.OK);
        when(restTemplate.postForEntity(
                eq("http://192.168.1.128:9090/api/notes"),
                any(HttpEntity.class),
                eq(MedicalNotesEntity.class))
        ).thenReturn(noteResponse);

        // Act
        assertDoesNotThrow(() -> noteService.createMedicalNoteForPatient(patientId, message));

        // Assert
        verify(restTemplate, times(1)).exchange(
                eq("http://192.168.1.128:8090/api/patients/" + patientId),
                eq(HttpMethod.GET),
                isNull(),
                eq(PatientEntity.class)
        );
        verify(restTemplate, times(1)).postForEntity(
                eq("http://192.168.1.128:9090/api/notes"),
                any(HttpEntity.class),
                eq(MedicalNotesEntity.class)
        );
    }

    @Test
    void testUpdateNotes() {
        // Arrange
        Integer id = 1;
        MedicalNotesEntity existingNote = new MedicalNotesEntity();
        existingNote.setId(id);
        existingNote.setPatientId(1);
        existingNote.setPatientLastName("Doe");
        existingNote.setNote("This is an existing note");
        existingNote.setDateTimeAtCreation(LocalDateTime.now());

        List<MedicalNotesEntity> notes = new ArrayList<>();
        notes.add(existingNote);

        ResponseEntity<List<MedicalNotesEntity>> responseEntity = new ResponseEntity<>(notes, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class))
        ).thenReturn(responseEntity);

        MedicalNotesEntity updatedNote = new MedicalNotesEntity();
        updatedNote.setId(existingNote.getId());
        updatedNote.setPatientId(existingNote.getPatientId());
        updatedNote.setPatientLastName(existingNote.getPatientLastName());
        updatedNote.setNote("This is a NEW note");
        updatedNote.setDateTimeAtCreation(LocalDateTime.now());

        String externalServiceUrl = "http://192.168.1.128:9090/api/notes/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MedicalNotesEntity> requestEntity = new HttpEntity<>(updatedNote, headers);

        ResponseEntity<Void> responseEntityPut = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.exchange(
                eq(externalServiceUrl),
                eq(HttpMethod.PUT),
                eq(requestEntity),
                eq(Void.class)
        )).thenReturn(responseEntityPut);

        // Create a mock for NoteServiceImpl
        NoteServiceImpl noteServiceMock = Mockito.mock(NoteServiceImpl.class);
        when(noteServiceMock.getNoteById(id)).thenReturn(existingNote);

        // Act
        MedicalNotesEntity result = noteServiceMock.getNoteById(id); // Appel au mock de getNoteById pour obtenir la note existante
        noteService.updateNotes(id, updatedNote);

        // Assert
        assertEquals(existingNote, result); // Vérification que la note existante est égale au résultat du mock de getNoteById

        // Utilisation de l'ArgumentCaptor pour capturer l'objet HttpEntity utilisé dans l'appel à exchange
        ArgumentCaptor<HttpEntity<MedicalNotesEntity>> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(1)).exchange(
                eq(externalServiceUrl),
                eq(HttpMethod.PUT),
                requestEntityCaptor.capture(),
                eq(Void.class)
        );

        // Comparaison de l'objet capturé avec l'objet requestEntity attendu
        HttpEntity<MedicalNotesEntity> capturedRequestEntity = requestEntityCaptor.getValue();
        assertEquals(requestEntity.getBody(), capturedRequestEntity.getBody());
    }




}
