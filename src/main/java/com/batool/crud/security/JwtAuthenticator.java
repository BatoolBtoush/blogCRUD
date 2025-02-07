package com.batool.crud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class JwtAuthenticator {
    @Value("${login-api.rsa-public-key}")
    private String publicKey;

    public String getPublicKey() {
        return publicKey;
    }

    public PublicKey getPublicKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return (publicKey);
    }


    public Claims validateJwtToken(String token, PublicKey key) {
        try {
            var claimsEntity = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            Claims claims = claimsEntity.getPayload();
            return claims;
        } catch (ExpiredJwtException expiredException) {
            // Token has expired
            System.out.println("Token has expired");
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public String extractEmailFromRefreshTokenJwtAuthenticator(String refreshToken, PublicKey key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Parse the refreshToken to extract claims
        var claimsEntity = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken);
        Claims claims = claimsEntity.getPayload();
        String email = (String) claims.get("email");

        return email;
    }

}



