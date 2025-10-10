package com.farmchainx.farmchainx.security;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
	
	private final String SECRET_KEY = "farmchainx_secret";
	
	private final long EXPIRATION = 1000*60*60;
	
	public String generateToken(String email) {
		return Jwts.builder()
				.setSubject(email)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis()+EXPIRATION))
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY)
				.compact();
	}
	
	public String extractEmail(String token) {
		return Jwts.parser()
		.setSigningKey(SECRET_KEY)
		.parseClaimsJwt(token)
		.getBody()
		.getSubject();
		
	}
	
	
	public boolean validateToken(String token, String email) {
		String extractedEmail = extractEmail(token);
		return (extractedEmail.equals(email)&&!isExpired(token));
	}
	
	private Boolean isExpired(String token) {
		Date exp = Jwts.parser()
				.setSigningKey(SECRET_KEY)
				.parseClaimsJws(token)
				.getBody()
				.getExpiration();
				
				return exp.before(new Date());
	}

}
