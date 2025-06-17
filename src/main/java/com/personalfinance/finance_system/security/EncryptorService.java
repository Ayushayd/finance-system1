package com.personalfinance.finance_system.security;

import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptorService {

    private final AES256TextEncryptor encryptor;

    public EncryptorService(@Value("${security.encryptor.password}") String password) {
        encryptor = new AES256TextEncryptor();
        encryptor.setPassword(password);
    }

    public String encrypt(String text) {
        return encryptor.encrypt(text);
    }

    public String decrypt(String encryptedText) {
        return encryptor.decrypt(encryptedText);
    }
}