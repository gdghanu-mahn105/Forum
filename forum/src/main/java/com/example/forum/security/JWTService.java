package com.example.forum.security;

import com.example.forum.entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 1000 * 60 * 15;

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

    public Date extractIssuedDate(String token){
        return extractClaim(token, Claims::getIssuedAt);
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

    public String generateAccessToken(UserEntity user, String deviceId){
        Map<String, Object> extraclaims = new HashMap<>();
        extraclaims.put("deviceId", deviceId);
        return generateToken(extraclaims, user);
    }

    private String generateToken(
            Map<String, Object> extraClaims,
            UserEntity userDetails
    ) {
        List<String> roles= userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        extraClaims.put("userId", userDetails.getUserId());
        extraClaims.put("roles", roles);
        Date now = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key getSigningKey (){
        byte [] keyByte = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyByte);
    }

}
