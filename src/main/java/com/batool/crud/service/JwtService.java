package com.batool.crud.service;

import com.batool.crud.entity.User;
import com.batool.crud.repo.UserRepo;
import com.batool.crud.security.JwtAuthenticator;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Autowired
    JwtAuthenticator jwtAuthenticator;

    @Value("${login-api.rsa-private-key}")
    private String privateKey;

    @Value("${login-api.rsa-public-key}")
    private String publicKey;


    @Autowired
    public UserRepo userRepo;

    public enum Tokens {ACCESS_TOKEN, REFRESH_TOKEN}

    private static final long ACCESS_TOKEN_EXPIRATION = 3 * 3600 * 1000; // 1 minute
    private static final long REFRESH_TOKEN_EXPIRATION = 3 * 3600 * 1000; // 3 hours

    @Autowired
    public JwtService(@Value("${login-api.rsa-private-key}") String privateKey, @Value("${login-api.rsa-public-key}") String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }


    public PrivateKey getKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
//        System.out.println("key::: "+key);
        byte[] privateKeyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//        System.out.println("privateKey::: "+privateKey);
        return (privateKey);
    }

    public PublicKey getPublicKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return (publicKey);
    }


    public static String getEmailDomain(String email) {
        if (email != null && email.contains("@")) {
            String domain = email.substring(email.indexOf('@') + 1);
            int dotIndex = domain.indexOf('.');
            if (dotIndex != -1) {
                return domain.substring(0, dotIndex); // Get the part before the dot
            }
            return domain; // Return the whole domain if no dot is found
        }
        return null;
    }


    public String generateToken(String email, Tokens tokenType) {
        Date now = new Date();
        User user = userRepo.findByEmailIgnoreCase(email);
        String domain = getEmailDomain(user.getEmail());

        PrivateKey privKey = null;
        try {
            privKey = getKeyFromString(privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        String token = Jwts.builder()
                .subject(email.toLowerCase())
                .claim("email", email.toLowerCase())
                .claim("role",user.getRole().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() +ACCESS_TOKEN_EXPIRATION))
                .signWith(privKey)
                .compact();

        return token;
    }

    public String generateAccessTokenFromRefreshToken(String refreshToken) {

        try {
            Claims claims = jwtAuthenticator.validateJwtToken(refreshToken, getPublicKeyFromString(publicKey));
            String email = claims.getSubject();
            return generateToken(email, Tokens.ACCESS_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Unable to generate access token from refresh token");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidRefreshToken(String refreshToken) {
        try {
            Claims claims = jwtAuthenticator.validateJwtToken(refreshToken, getPublicKeyFromString(publicKey));
            if (claims == null) {
                // Token is invalid (either expired or other JWT-related issue)
                return false;
            }
            Date expirationDate = claims.getExpiration();
            Date now = new Date();
            return !expirationDate.before(now);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (ExpiredJwtException e) {
            System.out.println("token has expired");        }
        return false;
    }

    public String extractEmailFromRefreshTokenJwtService(String refreshToken) {
        try {
            String userEmail = jwtAuthenticator.extractEmailFromRefreshTokenJwtAuthenticator(refreshToken, getPublicKeyFromString(publicKey));
            return userEmail;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }



}

