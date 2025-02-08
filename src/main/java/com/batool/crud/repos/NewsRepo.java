package com.batool.crud.repos;

import com.batool.crud.entities.News;
import com.batool.crud.entities.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepo extends JpaRepository<News, Long> {
    List<News> findByStatus(NewsStatus newsStatus);
}
