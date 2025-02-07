package com.batool.crud.repo;

import com.batool.crud.entity.News;
import com.batool.crud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepo extends JpaRepository<News, Long> {
}
