package com.example.forum.config;

import com.example.forum.entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    public static String SECRET_KEY ="6ebfb29c41131ca794e51b7db9e5e0017020e07996f80cee1c14849507cccb81";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

    public boolean isValidToken(String token , UserDetails userDetails) {
        final String userName = userDetails.getUsername();
        return userName.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public boolean isTokenExpired (String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); // get name from token
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsSolver) {
        final Claims claims = extractAllClaims(token);
        return claimsSolver.apply(claims);
    }

    private Claims extractAllClaims (String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(UserEntity userDetails) {
        return generateToken( new HashMap<>(), userDetails);
    }

    private String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key getSigningKey (){
        byte [] keyByte = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyByte);
    }

}
