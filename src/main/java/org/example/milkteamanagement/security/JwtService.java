package org.example.milkteamanagement.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.example.milkteamanagement.entity.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret:VerySecretKeyForSpringMilkTeaJwtSigningVerySecret123}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private OctetSequenceKey getKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return new OctetSequenceKey.Builder(keyBytes).build();
    }

    public String generateToken(UserAccount user) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .claim("role", user.getRole().name())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusMillis(jwtExpirationMs)))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            signedJWT.sign(new MACSigner(getKey().toByteArray()));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            return null;
        }
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verified = signedJWT.verify(new MACVerifier(getKey().toByteArray()));
            if (!verified) return false;
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            String username = claims.getSubject();
            Date exp = claims.getExpirationTime();
            return username != null && username.equals(userDetails.getUsername()) && exp != null && exp.after(new Date());
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }
}

