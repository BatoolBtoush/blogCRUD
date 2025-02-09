package com.batool.crud.security;

import com.batool.crud.customexceptions.InvalidTokenException;
import com.batool.crud.customexceptions.TokenExpiredException;
import com.batool.crud.enums.Role;
import com.batool.crud.entities.User;
import com.batool.crud.repos.UserRepo;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Value("${crud-api.rsa-public-key}")
    private String publicKey;

    @Autowired
    public JwtFilter(@Value("${crud-api.rsa-public-key}") String publicKey) {
        this.publicKey = publicKey;
    }
    public JwtFilter(){}
    @Autowired
    private UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(header == null) {
            chain.doFilter(request, response);
            return;
        }
        if ((header).isEmpty() || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        Claims claims;
        try {
            claims = new JwtTokenUtil().validateJwtToken(token, getPublicKeyFromString(publicKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing public key");
            return;
        } catch (TokenExpiredException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            return;
        } catch (InvalidTokenException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        if(claims == null) {
            chain.doFilter(request, response);
            return;
        }
        User user = userRepo.findByEmail(claims.get("email", String.class));
        if(user == null){
            chain.doFilter(request, response);
            return;
        }
        Role userRole = user.getRole();

        List<GrantedAuthority> authoritiesList = new ArrayList<>();
        if (userRole == null) {
            chain.doFilter(request, response);
            return;
        }

        authoritiesList.add(new SimpleGrantedAuthority(userRole.name()));

        UserDetails userDetails = new CustomerDetails(authoritiesList);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);

    }

    public PublicKey getPublicKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return(publicKey);
    }

}

