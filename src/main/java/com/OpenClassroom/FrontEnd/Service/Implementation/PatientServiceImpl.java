package com.OpenClassroom.FrontEnd.Service.Implementation;

import com.OpenClassroom.FrontEnd.Model.DTO.PatientPageDTO;
import com.OpenClassroom.FrontEnd.Model.PatientEntity;
import com.OpenClassroom.FrontEnd.Service.Interface.IPatientsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PatientServiceImpl implements IPatientsService {

    //private final String apiUrl = "http://localhost:8090/api/patients";

    private final String apiUrl = "http://192.168.1.128:8090/api/patients";

    @Autowired
    private final RestTemplate restTemplate;

    public PatientServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public PatientEntity patientById(Integer id) {


        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .pathSegment(String.valueOf(id))
                .build()
                .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<PatientEntity> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PatientEntity>() {},
                id
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve patient. Status code: " + response.getStatusCode().value());
        }

    }

    @Override
    public PatientPageDTO getAllPaginatedPatients(int page, int size) {


        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .toUriString();

        ResponseEntity<PatientPageDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PatientPageDTO>() {}
        );


        return response.getBody();
    }

    @Override
    public void createPatient(PatientEntity patient) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PatientEntity> requestEntity = new HttpEntity<>(patient, headers);

        restTemplate.postForEntity(apiUrl, requestEntity, PatientEntity.class);
    }

    @Override
    public PatientPageDTO getPatientsByLastName(String lastName, int page, int size) {


        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .pathSegment("by-lastName")
                .pathSegment(lastName)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()
                .toUriString();

        ResponseEntity<PatientPageDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PatientPageDTO>() {}
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Erreur lors de la récupération des patients");
        }
    }
}

