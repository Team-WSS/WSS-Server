package org.websoso.WSSServer.auth.jwt;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtKeyProvider {

    private final SecretKey signingKey;

    public JwtKeyProvider(@Value("${jwt.secret}") String secret) {
        String encoded = Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
        this.signingKey = Keys.hmacShaKeyFor(encoded.getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey getSigningKey() {
        return signingKey;
    }
}
