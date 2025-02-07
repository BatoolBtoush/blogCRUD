package com.batool.crud.service;

import com.batool.crud.entity.*;
import com.batool.crud.repo.NewsRepo;
import com.batool.crud.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepo newsRepo;

    @Autowired
    private UserRepo userRepo;

    public News createNews(NewsCreateDTO newsCreateDTO, User contentWriter) {
        News news = new News();
        news.setTitle(newsCreateDTO.getTitle());
        news.setArabicTitle(newsCreateDTO.getArabicTitle());
        news.setDescription(newsCreateDTO.getDescription());
        news.setArabicDescription(newsCreateDTO.getArabicDescription());
        news.setPublishDate(newsCreateDTO.getPublishDate());
        news.setImageUrl(newsCreateDTO.getImageUrl());
        news.setStatus(NewsStatus.PENDING);
        news.setCreatedBy(contentWriter);
        return newsRepo.save(news);

    }

    public List<NewsFetchDTO> getAllNews() {
        return newsRepo.findAll()
                .stream()
                .map(news -> new NewsFetchDTO(
                        news.getId(),
                        news.getTitle(),
                        news.getArabicTitle(),
                        news.getDescription(),
                        news.getArabicDescription(),
                        news.getPublishDate(),
                        news.getImageUrl(),
                        news.getStatus(),
                        news.getCreatedBy()

                ))
                .collect(Collectors.toList());
    }

}
