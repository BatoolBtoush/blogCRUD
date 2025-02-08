package com.batool.crud.controllers;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        System.out.println("token:: "+ token);
        String email = jwtTokenUtil.getEmailFromToken(token);
        System.out.println("email:: "+ email);

        User contentWriter = userRepo.findByEmail(email.toLowerCase());
        if (contentWriter == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        News createdNews = newsService.createNews(newsCreationDTO, contentWriter);
        return ResponseEntity.status(HttpStatus.CREATED).body("News created successfully");

    }


    @PreAuthorize("hasAnyRole('ROLE_CONTENT_WRITER', 'ROLE_ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<List<NewsRetrievalForAdminAndContentWriterDTO>> getAllNews(HttpServletRequest request) {
        List<NewsRetrievalForAdminAndContentWriterDTO> allNews = newsService.getAllNews();
        return ResponseEntity.status(HttpStatus.OK).body(allNews);
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

    @GetMapping("/get-approved")
    public ResponseEntity<List<NewsRetrievalForNormalUserDTO>> getApprovedNews() {
        List<NewsRetrievalForNormalUserDTO> approvedNews = newsService.getApprovedNews();
        return ResponseEntity.ok(approvedNews);
    }

    @DeleteMapping("/delete/{newsId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROLE_CONTENT_WRITER')")
    public ResponseEntity<?> deleteNews(@PathVariable Long newsId,
                                        HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenUtil.extractTokenFromAuthHeader(authHeader);
        String email = jwtTokenUtil.getEmailFromToken(token);
        newsService.deleteNews(newsId, email);

        return ResponseEntity.ok("News item deleted successfully");
    }

}
