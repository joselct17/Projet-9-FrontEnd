package com.OpenClassroom.FrontEnd.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportEntity {
    private Integer id;
    private String patientName;
    private String diabetesReport;

    public ReportEntity(Integer id, String patientName, String diabetesReport) {
        this.id = id;
        this.patientName = patientName;
        this.diabetesReport = diabetesReport;
    }
}
