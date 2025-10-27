package com.rehabai.patient_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class PatientDtos {

    public record CreateRequest(
            @NotBlank @Size(max=150) String fullName,
            @Email @Size(max=150) String email,
            @Size(max=10) String dateOfBirth,
            @Size(max=20) String gender
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max=150) String fullName,
            @Email @Size(max=150) String email,
            @Size(max=10) String dateOfBirth,
            @Size(max=20) String gender
    ) {}

    public record Response(
            UUID id,
            String fullName,
            String email,
            String dateOfBirth,
            String gender
    ) {}
}

