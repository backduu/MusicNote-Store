package com.store.store.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long validityInMilliseconds = 1000L * 60 * 60; // 1시간

    public JwtTokenProvider() {
        // TODO 클라우드에 붙을 땐 비밀키 생성할 수 있게 만들기.
        String base64Secret = "u8nFz9J3Q1y7m4aVt0pX9qL2s5Yh8BvCj3kR6nW0zPqU1xY2rT5vZ8mN1oQ4wE7";
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    // 토큰 생성할 때 email을 subject로 설정
    // + 토큰에 role, nickname 사용자 정보 클레임에 넣기
    public String createToken(String email, String role, String nickname, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMilliseconds);

        // Claims 생성
        Claims claims = Jwts.claims().setSubject(email); // 사용자 식별자: email

        // 추가 클레임이 있다면 합치기
        if (!extraClaims.isEmpty()) {
            claims.putAll(extraClaims);
        }

        claims.put("role", role);
        claims.put("nickname", nickname);



        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 Claims 전체 추출
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 이메일(subject) 추출
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // 토큰에서 role 추출
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    // 토큰에서 nickname 추출
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).get("username", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)          // 서명 검증용 키 설정
                    .build()
                    .parseClaimsJws(token);      // 파싱 시 예외 발생 여부로 유효성 판단
            return true;
        } catch (Exception e) {
            return false;                        // 서명 불일치, 만료 등 예외 발생 시 false
        }
    }



}
