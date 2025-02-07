package com.batool.crud.controller;

import com.batool.crud.entity.*;
import com.batool.crud.repo.UserRepo;
import com.batool.crud.security.JwtTokenUtil;
import com.batool.crud.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<?> createNews(@RequestBody NewsCreateDTO newsCreateDTO,
                                        HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        String token = jwtTokenUtil.extractTokenFromAuthHeader(authHeader);

        System.out.println("token:: "+ token);
        String email = jwtTokenUtil.getEmailFromToken(token);
        System.out.println("email:: "+ email);
//        User contentWriter = userRepo.findByEmail(email.toLowerCase());
//        if (contentWriter == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//        }
//
//        News createdNews = newsService.createNews(newsCreateDTO, contentWriter);
        return ResponseEntity.status(HttpStatus.CREATED).body("createdNews");

    }

    @GetMapping("/get-all")
    public ResponseEntity<List<NewsFetchDTO>> getAllNews() {
        List<NewsFetchDTO> allNews = newsService.getAllNews();
        return ResponseEntity.ok(allNews);
    }

}
