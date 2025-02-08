package com.batool.crud.security;

import com.batool.crud.customexceptions.InvalidPublicKeyException;
import com.batool.crud.customexceptions.InvalidRefreshTokenException;
import com.batool.crud.customexceptions.InvalidTokenException;
import com.batool.crud.customexceptions.TokenExpiredException;
import com.batool.crud.entities.User;
import com.batool.crud.repos.UserRepo;
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

    @Value("${crud-api.rsa-private-key}")
    private String privateKey;

    @Value("${crud-api.rsa-public-key}")
    private String publicKey;

    public enum Tokens {ACCESS_TOKEN, REFRESH_TOKEN}

    private static final long ACCESS_TOKEN_EXPIRATION = 60_000; // 1 minute
    private static final long REFRESH_TOKEN_EXPIRATION = 3 * 3600 * 1000; // 3 hours



    public PrivateKey getPrivateKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    public PublicKey getPublicKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }


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


    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

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
            throw new TokenExpiredException("Token has expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token", e);
        }
    }

    public String generateAccessTokenFromRefreshToken(String refreshToken) {
        try {
            Claims claims = validateJwtToken(refreshToken, getPublicKeyFromString(publicKey));
            if (claims == null) {
                throw new InvalidRefreshTokenException("Invalid refresh token");
            }
            String email = claims.getSubject();
            return generateToken(email, Tokens.ACCESS_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidRefreshTokenException("Unable to generate access token from refresh token: " + e.getMessage());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidPublicKeyException("Error processing public key", e);
        }
    }

    public boolean isValidRefreshToken(String refreshToken) {
        try {
            Claims claims = validateJwtToken(refreshToken, getPublicKeyFromString(publicKey));
            if (claims == null) {
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

    public String extractTokenFromAuthHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replaceAll("Bearer","").trim();;
            return token;
        }
        return null;
    }

}
