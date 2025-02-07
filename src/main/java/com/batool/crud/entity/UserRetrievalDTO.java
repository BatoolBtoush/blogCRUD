package com.batool.crud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRetrievalDTO {

    private Long id;

    private String fullName;

    private String email;

    private LocalDate dateOfBirth;

    private Role role;
}
