package com.batool.crud.services;

import com.batool.crud.dtos.NewsCreationDTO;
import com.batool.crud.dtos.NewsRetrievalForAdminAndContentWriterDTO;
import com.batool.crud.dtos.NewsRetrievalForNormalUserDTO;
import com.batool.crud.dtos.UserSummaryDTO;
import com.batool.crud.entities.*;
import com.batool.crud.repos.NewsRepo;
import com.batool.crud.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepo newsRepo;

    @Autowired
    private UserRepo userRepo;

    public News createNews(NewsCreationDTO newsCreationDTO, User contentWriter) {
        News news = new News();
        news.setTitle(newsCreationDTO.getTitle());
        news.setArabicTitle(newsCreationDTO.getArabicTitle());
        news.setDescription(newsCreationDTO.getDescription());
        news.setArabicDescription(newsCreationDTO.getArabicDescription());
        news.setPublishDate(newsCreationDTO.getPublishDate());
        news.setImageUrl(newsCreationDTO.getImageUrl());
        news.setStatus(NewsStatus.PENDING);
        news.setCreatedBy(contentWriter);
        return newsRepo.save(news);

    }

    public List<NewsRetrievalForAdminAndContentWriterDTO> getAllNews() {
        return newsRepo.findAll()
                .stream()
                .map(news -> new NewsRetrievalForAdminAndContentWriterDTO(
                        news.getId(),
                        news.getTitle(),
                        news.getArabicTitle(),
                        news.getDescription(),
                        news.getArabicDescription(),
                        news.getPublishDate(),
                        news.getImageUrl(),
                        news.getStatus(),
                        new UserSummaryDTO(
                                news.getCreatedBy().getId(),
                                news.getCreatedBy().getFullName(),
                                news.getCreatedBy().getEmail()
                        )
                ))
                .collect(Collectors.toList());
    }

    public News approveNews(Long newsId) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("News item not found"));

        if (news.getStatus() == NewsStatus.APPROVED) {
            throw new IllegalStateException("News is already approved.");
        }

        news.setStatus(NewsStatus.APPROVED);
        return newsRepo.save(news);
    }

    public List<NewsRetrievalForNormalUserDTO> getApprovedNews() {
        return newsRepo.findByStatus(NewsStatus.APPROVED)
                .stream()
                .map(news -> new NewsRetrievalForNormalUserDTO(
                        news.getTitle(),
                        news.getArabicTitle(),
                        news.getDescription(),
                        news.getArabicDescription(),
                        news.getPublishDate(),
                        news.getImageUrl(),
                        new UserSummaryDTO(
                                news.getCreatedBy().getId(),
                                news.getCreatedBy().getFullName(),
                                news.getCreatedBy().getEmail()
                        )
                ))
                .collect(Collectors.toList());
    }

    public void deleteNews(Long newsId, String email) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("News not found"));

        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        boolean isCreator = news.getCreatedBy().getId().equals(user.getId());

        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        if (news.getStatus() == NewsStatus.PENDING && isCreator) {
            newsRepo.delete(news);
        }
        else if (news.getStatus() == NewsStatus.APPROVED && isAdmin) {
            newsRepo.delete(news);
        }
        else {
            throw new AccessDeniedException("You are not allowed to delete this news item");
        }
    }

}
