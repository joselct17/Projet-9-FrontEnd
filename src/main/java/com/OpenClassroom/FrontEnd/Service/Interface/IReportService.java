package com.OpenClassroom.FrontEnd.Service.Interface;

import com.OpenClassroom.FrontEnd.Model.ReportEntity;

public interface IReportService {
    ReportEntity getReportByPatientId(Integer patientId);

    ReportEntity getReportByPatLastName(String patientName);

}
