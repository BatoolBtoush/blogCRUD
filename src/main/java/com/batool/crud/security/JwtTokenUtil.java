package com.batool.crud.security;

import com.batool.crud.entity.User;
import com.batool.crud.repo.UserRepo;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.PublicKey;

@Component
public class JwtTokenUtil {

    @Autowired
    public UserRepo userRepo;

    @Value("${login-api.rsa-private-key}")
    private String privateKey;

    @Value("${login-api.rsa-public-key}")
    private String publicKey;

    public enum Tokens {ACCESS_TOKEN, REFRESH_TOKEN}

    private static final long ACCESS_TOKEN_EXPIRATION = 60_000; // 1 minute
    private static final long REFRESH_TOKEN_EXPIRATION = 3 * 3600 * 1000; // 3 hours




    public PrivateKey getPrivateKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
//        System.out.println("key::: "+key);
        byte[] privateKeyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//        System.out.println("privateKey::: "+privateKey);
        return privateKey;
    }

    public PublicKey getPublicKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }


    // Generates a JWT token using the private key
    public String generateToken(String email, Tokens tokens) {
        User user = userRepo.findByEmailIgnoreCase(email);
        Date now = new Date();
        PrivateKey privKey = null;
        try {
            privKey = getPrivateKeyFromString(privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        long expirationTime = tokens == Tokens.ACCESS_TOKEN ? ACCESS_TOKEN_EXPIRATION : REFRESH_TOKEN_EXPIRATION;

        return Jwts.builder()
                .subject(email.toLowerCase())
                .claim("email", email.toLowerCase())
                .claim("role", user.getRole().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .signWith(privKey)
                .compact();
    }


    // Extracts username from token using the public key
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
//
//    // Extracts expiration date from token
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }


    public Claims validateJwtToken(String token, PublicKey publicKey) {
        try {
            var claimsEntity = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return claimsEntity.getPayload();
        } catch (ExpiredJwtException expiredException) {
            // Token has expired
            System.out.println("Token has expired");
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public String generateAccessTokenFromRefreshToken(String refreshToken) {
        try {
            Claims claims = validateJwtToken(refreshToken, getPublicKeyFromString(publicKey));
            String email = claims.getSubject();
            return generateToken(email, Tokens.ACCESS_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Unable to generate access token from refresh token");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
//
//
    public boolean isValidRefreshToken(String refreshToken) {
        try {
            Claims claims = validateJwtToken(refreshToken, getPublicKeyFromString(publicKey));
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

//    // Extracts claims from the token using the public key
    private Claims getClaimsFromToken(String token) {
        PublicKey publicKey1 = null;
        try {
            publicKey1 = getPublicKeyFromString(publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        var claimsEntity = Jwts.parser()
                .verifyWith(publicKey1)
                .build()
                .parseSignedClaims(token);
        return claimsEntity.getPayload();

    }
//
//    // Checks if the token is expired
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }
//
//    // Validates token against user details
//    public boolean validateToken(String token, UserDetails userDetails) {
//        return !isTokenExpired(token) && userDetails.getUsername().equals(getUsernameFromToken(token));
//    }
}
