package com.OpenClassroom.FrontEnd.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PatientEntity {

    @JsonProperty
    private  Integer id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String lastName;

    @JsonProperty
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonProperty
    private String sex;

    @JsonProperty
    private String address;

    @JsonProperty
    private String phone;


}
