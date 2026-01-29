package com.example.procurement.config;



import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class PasswordGenerator {
  
  public  String createSecurePassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialCharacters = "!@#$%^&*()-_+=<>?";
        String allCharacters = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each required category
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));

        // Fill the rest of the password with random characters
        for (int i = 3; i < 8; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        // Shuffle the password for better randomness
        return shuffleString(password.toString());
    }

    private String shuffleString(String input) {
        SecureRandom random = new SecureRandom();
        char[] characters = input.toCharArray();

        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }

        return new String(characters);
    }
}

