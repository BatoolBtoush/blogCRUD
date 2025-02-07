package com.batool.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFetchDTO {

    private Long id;

    private String fullName;

    private String email;

    private LocalDate dateOfBirth;

    private Role role;
}
