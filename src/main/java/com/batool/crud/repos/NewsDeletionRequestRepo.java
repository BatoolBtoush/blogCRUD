package com.batool.crud.repos;

import com.batool.crud.entities.News;
import com.batool.crud.entities.NewsDeletionRequest;
import com.batool.crud.enums.DeletionRequestStatus;
import com.batool.crud.enums.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsDeletionRequestRepo extends JpaRepository<NewsDeletionRequest, Long> {
    NewsDeletionRequest findByNewsAndStatus(News news, DeletionRequestStatus status);
}
