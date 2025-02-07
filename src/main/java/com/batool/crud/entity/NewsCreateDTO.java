package com.batool.crud.entity;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class NewsCreateDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Title (in Arabic) is required")
    private String arabicTitle;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Description (in Arabic) is required")
    private String arabicDescription;

    @NotNull(message = "Publish date is required")
    private LocalDate publishDate;

    private String imageUrl;
}
