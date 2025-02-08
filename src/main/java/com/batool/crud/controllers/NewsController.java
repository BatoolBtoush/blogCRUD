package com.batool.crud.controllers;

import com.batool.crud.customexceptions.UserNotFoundException;
import com.batool.crud.dtos.NewsCreationDTO;
import com.batool.crud.dtos.NewsRetrievalForAdminAndContentWriterDTO;
import com.batool.crud.dtos.NewsRetrievalForNormalUserDTO;
import com.batool.crud.entities.*;
import com.batool.crud.repos.UserRepo;
import com.batool.crud.security.JwtTokenUtil;
import com.batool.crud.services.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
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

        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenUtil.extractTokenFromAuthHeader(authHeader);
        String email = jwtTokenUtil.getEmailFromToken(token);

        User contentWriter = userRepo.findByEmail(email.toLowerCase());
        if (contentWriter == null) {
            throw new UserNotFoundException("User with email " + email + " not found");
        }
        newsService.createNews(newsCreationDTO, contentWriter);
        return new ResponseEntity<>("News created successfully", HttpStatus.CREATED);

    }


    @PreAuthorize("hasAnyRole('ROLE_CONTENT_WRITER', 'ROLE_ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<List<NewsRetrievalForAdminAndContentWriterDTO>> getAllNews(HttpServletRequest request) {
        List<NewsRetrievalForAdminAndContentWriterDTO> allNews = newsService.getAllNews();
        return new ResponseEntity<>(allNews, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/approve/{newsId}")
    public ResponseEntity<?> approveNews(@PathVariable Long newsId, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenUtil.extractTokenFromAuthHeader(authHeader);
        System.out.println("token:: "+ token);
        String email = jwtTokenUtil.getEmailFromToken(token);
        System.out.println("email:: "+ email);

        User admin = userRepo.findByEmail(email.toLowerCase());
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        News approvedNews = newsService.approveNews(newsId);
        return ResponseEntity.ok(approvedNews);
    }

    @PreAuthorize("hasRole('ROLE_NORMAL')")
    @GetMapping("/get-approved")
    public ResponseEntity<List<NewsRetrievalForNormalUserDTO>> getApprovedNews() {
        List<NewsRetrievalForNormalUserDTO> approvedNews = newsService.getApprovedNews();
        return ResponseEntity.ok(approvedNews);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_CONTENT_WRITER')")
    @DeleteMapping("/delete/{newsId}")
    public ResponseEntity<?> deleteOrRequestNewsDeletion(@PathVariable Long newsId,
                                        HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenUtil.extractTokenFromAuthHeader(authHeader);
        String email = jwtTokenUtil.getEmailFromToken(token);
        try{
            newsService.deleteOrRequestNewsDeletion(newsId, email);
            return new ResponseEntity<>("News item deleted successfully", HttpStatus.OK);
        }catch (EntityNotFoundException e){
            return new ResponseEntity<>("News not found", HttpStatus.NOT_FOUND);
        }catch (IllegalStateException e){
            return new ResponseEntity<>("A deletion request is already pending for this news.", HttpStatus.CONFLICT);
        }catch (ResponseStatusException e){
            return new ResponseEntity<>("Delete request has been submitted for admin approval.", HttpStatus.ACCEPTED);
        }catch (AccessDeniedException e){
            return new ResponseEntity<>("You are not allowed to delete this news item.", HttpStatus.UNAUTHORIZED);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/get-all-deletion-requests")
    public ResponseEntity<List<NewsDeletionRequest>> getAllDeletionRequests(){
        List<NewsDeletionRequest> allNewsDeletionRequests = newsService.getAllDeletionRequests();
        return ResponseEntity.status(HttpStatus.OK).body(allNewsDeletionRequests);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/process-deletion-request/{requestId}")
    public ResponseEntity<String> processDeletionRequest(
            @PathVariable Long requestId,
            @RequestParam boolean approve,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenUtil.extractTokenFromAuthHeader(authHeader);
        String email = jwtTokenUtil.getEmailFromToken(token);

        try{
            newsService.processDeletionRequest(requestId, approve, email);
            String message = approve ? "Deletion request approved. News has been soft deleted." :
                    "Deletion request rejected. News remains.";
            return ResponseEntity.ok(message);

        }catch (AccessDeniedException e){
            return new ResponseEntity<>("Only admins can process deletion requests.", HttpStatus.NOT_ACCEPTABLE);
        }catch (EntityNotFoundException e){
            return new ResponseEntity<>("News not found", HttpStatus.NOT_FOUND);
        }
    }

}
