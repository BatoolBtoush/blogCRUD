package com.batool.crud.repos;

import com.batool.crud.entities.News;
import com.batool.crud.entities.User;
import com.batool.crud.enums.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepo extends JpaRepository<News, Long> {

    @Query("SELECT n FROM News n WHERE n.deleted = false")
    List<News> findAllActiveNews();

    @Query("SELECT n FROM News n WHERE n.status = :status AND n.deleted = false")
    List<News> findByStatus(@Param("status") NewsStatus status);

    List<News> findByCreatedBy(User user);
}
