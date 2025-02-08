package com.batool.crud.services;

import com.batool.crud.dtos.NewsCreationDTO;
import com.batool.crud.dtos.NewsRetrievalForAdminAndContentWriterDTO;
import com.batool.crud.dtos.NewsRetrievalForNormalUserDTO;
import com.batool.crud.dtos.UserSummaryDTO;
import com.batool.crud.entities.*;
import com.batool.crud.enums.DeletionRequestStatus;
import com.batool.crud.enums.NewsStatus;
import com.batool.crud.enums.Role;
import com.batool.crud.repos.NewsDeletionRequestRepo;
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

    @Autowired
    private NewsDeletionRequestRepo newsDeletionRequestRepo;

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
        return newsRepo.findAllActiveNews()
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

    public void deleteOrRequestNewsDeletion(Long newsId, String email) {
        News news = newsRepo.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("News not found"));

        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }

        boolean isCreator = news.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        // Case 1: Content Writer can delete PENDING news directly
        if (news.getStatus() == NewsStatus.PENDING && isCreator) {
            news.setDeleted(true);
            newsRepo.save(news);
            return;
        }

        // Case 2: If news is approved, a deletion request is created
        if (news.getStatus() == NewsStatus.APPROVED) {
            // Ensure a non-admin cannot delete directly
            if (!isAdmin) {
                // Check if a deletion request already exists
                NewsDeletionRequest existingRequest = newsDeletionRequestRepo.findByNewsAndStatus
                        (news, DeletionRequestStatus.PENDING);
                if (existingRequest != null) {
                    throw new IllegalStateException("A deletion request is already pending for this news.");
                }

                // Create a new deletion request
                NewsDeletionRequest deleteRequest = new NewsDeletionRequest();
                deleteRequest.setNews(news);
                deleteRequest.setRequestedBy(user);
                deleteRequest.setStatus(DeletionRequestStatus.PENDING);
                newsDeletionRequestRepo.save(deleteRequest);

                throw new ResponseStatusException(HttpStatus.ACCEPTED, "Delete request has been submitted for admin approval.");
            }

            // Case 3: Admin can soft delete directly
            news.setDeleted(true);
            newsRepo.save(news);
            return;
        }

        throw new AccessDeniedException("You are not allowed to delete this news item.");
    }

    public List<NewsDeletionRequest> getAllDeletionRequests(){
        return newsDeletionRequestRepo.findAll();
    }


    public void processDeletionRequest(Long requestId, boolean approve, String adminEmail) {
        User admin = userRepo.findByEmail(adminEmail);
        if (admin == null || admin.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Only admins can process deletion requests.");
        }

        NewsDeletionRequest request = newsDeletionRequestRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Deletion request not found"));

        if (approve) {
            News news = request.getNews();
            news.setDeleted(true); // Soft delete instead of removing
            newsRepo.save(news);
            request.setStatus(DeletionRequestStatus.APPROVED);
        } else {
            request.setStatus(DeletionRequestStatus.REJECTED);
        }
        newsDeletionRequestRepo.save(request);
    }

}
