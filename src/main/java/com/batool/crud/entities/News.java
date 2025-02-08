package com.batool.crud.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "news")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String arabicTitle;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String arabicDescription;
    @Column(nullable = false)
    private LocalDate publishDate;
    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private NewsStatus status;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;
}



