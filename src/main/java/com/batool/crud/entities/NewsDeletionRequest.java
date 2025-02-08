package com.batool.crud.entities;


import com.batool.crud.enums.DeletionRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_deletion_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @ManyToOne
    @JoinColumn(name = "requested_by_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    private DeletionRequestStatus status = DeletionRequestStatus.PENDING;

    private LocalDateTime requestedAt = LocalDateTime.now();
}
