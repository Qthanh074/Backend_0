package org.example.backend9.dto.response.core;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WorkShiftResponse {
    private Integer id;
    private String storeName;
    private String employeeName;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;
}