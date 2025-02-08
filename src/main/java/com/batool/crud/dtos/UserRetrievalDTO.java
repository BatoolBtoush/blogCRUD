package com.batool.crud.dtos;

import com.batool.crud.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
