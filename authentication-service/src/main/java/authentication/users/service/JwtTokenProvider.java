package authentication.users.service;

import authentication.users.domain.RefreshToken;
import authentication.users.domain.Users;
import authentication.users.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key secretKey;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenProvider(Key secretKey, Long accessTokenExpiration, Long refreshTokenExpiration, RefreshTokenRepository refreshTokenRepository) {
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(Users users) {
        return buildAccessToken(users, accessTokenExpiration);
    }

    public String generateRefreshToken(Users users) {
        String refreshToken = buildRefreshToken(users, refreshTokenExpiration);
        Date expirationDate = new Date(System.currentTimeMillis() + refreshTokenExpiration);
        RefreshToken tokenEntity = new RefreshToken(users, refreshToken, expirationDate);
        refreshTokenRepository.save(tokenEntity);
        return refreshToken;
    }

    private String buildAccessToken(Users users, Long expiration) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        return Jwts.builder()
                .setSubject(users.getEmail())
                .claim("roles", users.getRole())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    private String buildRefreshToken(Users users, Long expiration) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        return Jwts.builder()
                .setSubject(users.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            validateExpire(claims);
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalStateException("유효하지 않은 토큰");
        }
    }

    public void validateExpire(Jws<Claims> claims) {
        Date expiration = claims.getBody().getExpiration();
        if (expiration.before(new Date(System.currentTimeMillis()))) {
            throw new IllegalStateException("토큰 만료");
        }
    }
}
