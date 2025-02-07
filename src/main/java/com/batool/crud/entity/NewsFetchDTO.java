package com.batool.crud.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsFetchDTO {
    private Long id;

    private String title;

    private String arabicTitle;

    private String description;

    private String arabicDescription;

    private LocalDate publishDate;

    private String imageUrl;

    private NewsStatus status;

    private User createdBy;

}
