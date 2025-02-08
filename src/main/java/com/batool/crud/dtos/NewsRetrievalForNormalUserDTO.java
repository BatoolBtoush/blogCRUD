package com.batool.crud.dtos;


import com.batool.crud.entities.NewsStatus;
import com.batool.crud.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsRetrievalForNormalUserDTO {

    private String title;

    private String arabicTitle;

    private String description;

    private String arabicDescription;

    private LocalDate publishDate;

    private String imageUrl;

    private UserSummaryDTO createdBy;

}
