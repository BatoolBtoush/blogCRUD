package com.batool.crud.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
    private byte[] hash;

    public Hasher(String input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes("UTF-8"));
            hash = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public String getHash() {
        BigInteger bi = new BigInteger(1, hash);
        return String.format("%0" + (hash.length << 1) + "X", bi);
    }
}


