package com.study.codingswamp.auth.token;

import com.study.codingswamp.auth.service.MemberPayload;
import com.study.codingswamp.auth.utils.AuthTokenExtractor;
import com.study.codingswamp.common.exception.UnauthorizedException;
import com.study.codingswamp.member.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class TokenProvider {

    private static final String TOKEN_TYPE = "Bearer";
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";

    private final AuthTokenExtractor authTokenExtractor;
    private final Key secretKey;
    private final long validityInMilliseconds;

    public TokenProvider(AuthTokenExtractor authTokenExtractor,
                         @Value("${jwt.secret}") String secretKey,
                         @Value("${jwt.token-validity-in-milliseconds}") long validityInMilliseconds) {
        this.authTokenExtractor = authTokenExtractor;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createAccessToken(final Long id, final Role role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(ACCESS_TOKEN_SUBJECT)
                .setIssuedAt(now)
                .setExpiration(validity)
                .claim("id", id)
                .claim("role", role)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidToken(String authorizationHeader) {
        String token = authTokenExtractor.extractToken(authorizationHeader, TOKEN_TYPE);
        try {
            Jws<Claims> claims = getClaims(token);
            return isAccessToken(claims) && isNotExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public MemberPayload getPayload(String authorizationHeader) {
        String token = authTokenExtractor.extractToken(authorizationHeader, TOKEN_TYPE);
        Claims body = getClaims(token).getBody();

        try {
            Long id = body.get("id", Long.class);
            Role role = Role.valueOf(body.get("role", String.class));
            return new MemberPayload(id, role);
        } catch (RequiredTypeException | NullPointerException | IllegalArgumentException e) {
            throw new UnauthorizedException("token", "만료된 토큰입니다.");
        }
    }

    public long getValidityInMilliseconds() {
        return validityInMilliseconds;
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    private boolean isAccessToken(Jws<Claims> claims) {
        return claims.getBody()
                .getSubject()
                .equals(ACCESS_TOKEN_SUBJECT);
    }

    private boolean isNotExpired(Jws<Claims> claims) {
        return claims.getBody()
                .getExpiration()
                .after(new Date());
    }
}
