package com.batool.crud.controllers;

import com.batool.crud.customexceptions.UserNotFoundException;
import com.batool.crud.dtos.NewsCreationDTO;
import com.batool.crud.dtos.NewsRetrievalForAdminAndContentWriterDTO;
import com.batool.crud.dtos.NewsRetrievalForNormalUserDTO;
import com.batool.crud.dtos.UserRetrievalDTO;
import com.batool.crud.entities.*;
import com.batool.crud.repos.UserRepo;
import com.batool.crud.security.JwtTokenUtil;
import com.batool.crud.services.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PreAuthorize("hasAnyRole('ROLE_CONTENT_WRITER', 'ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createNews(@RequestBody NewsCreationDTO newsCreationDTO,
                                        HttpServletRequest request) {
        String email = jwtTokenUtil.getEmailFromTokenHTTPRequest(request);

        User contentWriter = userRepo.findByEmail(email.toLowerCase());
        if (contentWriter == null) {
            throw new UserNotFoundException("User with email " + email + " not found");
        }
        newsService.createNews(newsCreationDTO, contentWriter);
        return new ResponseEntity<>("News created successfully", HttpStatus.CREATED);

    }


    @PreAuthorize("hasAnyRole('ROLE_CONTENT_WRITER', 'ROLE_ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllNews(HttpServletRequest request) {
        try {
            List<NewsRetrievalForAdminAndContentWriterDTO> allNews = newsService.getAllNews();
            return new ResponseEntity<>(allNews, HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity<>("An error occurred while fetching users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/approve/{newsId}")
    public ResponseEntity<?> approveNews(@PathVariable Long newsId, HttpServletRequest request) {
        String email = jwtTokenUtil.getEmailFromTokenHTTPRequest(request);

        User admin = userRepo.findByEmail(email.toLowerCase());
        if (admin == null) {
            throw new UserNotFoundException("User with email " + email + " not found");
        }
        News approvedNews = newsService.approveNews(newsId);
        return new ResponseEntity<>(approvedNews, HttpStatus.OK);

    }

    @PreAuthorize("hasAnyRole('ROLE_CONTENT_WRITER', 'ROLE_ADMIN', 'ROLE_NORMAL')")
    @GetMapping("/get-approved")
    public ResponseEntity<?> getApprovedNews() {
        try {
            List<NewsRetrievalForNormalUserDTO> approvedNews = newsService.getApprovedNews();
            return new ResponseEntity<>(approvedNews, HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity<>("An error occurred while fetching users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_CONTENT_WRITER')")
    @DeleteMapping("/delete/{newsId}")
    public ResponseEntity<?> deleteOrRequestNewsDeletion(@PathVariable Long newsId,
                                                         HttpServletRequest request) {
        String email = jwtTokenUtil.getEmailFromTokenHTTPRequest(request);

        newsService.deleteOrRequestNewsDeletion(newsId, email);
        return new ResponseEntity<>("News item deleted successfully", HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/get-all-deletion-requests")
    public ResponseEntity<?> getAllDeletionRequests() {
        try {
            List<NewsDeletionRequest> allNewsDeletionRequests = newsService.getAllDeletionRequests();
            return new ResponseEntity<>(allNewsDeletionRequests, HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity<>("An error occurred while fetching users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/process-deletion-request/{requestId}")
    public ResponseEntity<String> processDeletionRequest(@PathVariable Long requestId,
                                                         @RequestParam boolean approve,
                                                         HttpServletRequest request) {

        String email = jwtTokenUtil.getEmailFromTokenHTTPRequest(request);

        newsService.processDeletionRequest(requestId, approve, email);
        String message = approve ? "Deletion request approved. News has been soft deleted." :
                "Deletion request rejected. News remains.";
        return ResponseEntity.ok(message);

    }

}
