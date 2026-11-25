package com.railway.registration;



import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String password = "Admin@12345";

        String hashedPassword = encoder.encode(password);

        System.out.println("BCrypt Hash: " + hashedPassword);
    }
}
