// package com.sr.api_gateway_3.Security;

// import io.jsonwebtoken.*;
// import io.jsonwebtoken.io.Decoders;
// import io.jsonwebtoken.security.Keys;
// import org.springframework.stereotype.Service;

// import java.security.Key;

// @Service
// public class JwtService {
// private static final String SECRET =
// "aPlB62IF40noDsuM5CCBghcih1D0rGhlcrRst6f795+fuKapL+QNxFtGnch/kj3sVTppptzDJDYGdZVLI+b9qQ==";

// private Key getSignKey() {
// byte[] keyBytes = Decoders.BASE64.decode(SECRET);
// return Keys.hmacShaKeyFor(keyBytes);
// }

// public String extractUsername(String token) {
// return Jwts.parserBuilder().setSigningKey(getSignKey())
// .build().parseClaimsJws(token)
// .getBody().getSubject();
// }

// public boolean isTokenValid(String token) {
// try {
// Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
// return true;
// } catch (JwtException e) {
// return false;
// }
// }
// }
