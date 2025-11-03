package com.sr.Auth_Service.Auth;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        // Generate a key for HS512 (strongest)
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        // Encode as Base64 so you can paste in application.properties
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("Your JWT Secret Key:");
        System.out.println(base64Key);
    }
}
