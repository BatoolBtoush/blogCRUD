package com.batool.crud.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


public class Hasher {
    public static String hashPasswordWithSalt(String password, String salt) {
        try {
            String saltedPassword = password + salt;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(saltedPassword.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();
            BigInteger bi = new BigInteger(1, hash);
            return String.format("%0" + (hash.length << 1) + "X", bi);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: SHA-256 algorithm not found", e);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

        public static String getSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        String saltedString = Base64.getEncoder().encodeToString(salt);
        return saltedString;
    }

}



