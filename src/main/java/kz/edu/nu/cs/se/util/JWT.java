package kz.edu.nu.cs.se.util;
import kz.edu.nu.cs.se.constants.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.Key;

import io.jsonwebtoken.*;

import java.util.Date;  

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;  
 
//Sample method to construct a JWT
public class JWT{
	public static String createJWT(String id, String issuer, String subject, long ttlMillis) {
 
    	//The JWT signature algorithm we will be using to sign the token
	    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	 
	    long nowMillis = System.currentTimeMillis();
	    Date now = new Date(nowMillis);
	 
	    //We will sign our JWT with our ApiKey secret
	    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(CONST.API_KEY);
	    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	 
	    //Let's set the JWT Claims
	    JwtBuilder builder = Jwts.builder().setId(id)
	                                .setIssuedAt(now)
	                                .setSubject(subject)
	                                .setIssuer(issuer)
	                                .signWith(signatureAlgorithm, signingKey);
	 
	    //if it has been specified, let's add the expiration
	    if (ttlMillis >= 0) {
	    long expMillis = nowMillis + ttlMillis;
	        Date exp = new Date(expMillis);
	        builder.setExpiration(exp);
	    }
	 
	    //Builds the JWT and serializes it to a compact, URL-safe string
    	return builder.compact();
	}
	public static Pair<String, Long> parseJWT(String jwt){
		if(jwt == null || jwt.length() == 0)return new Pair(null,0);
		Claims claims = Jwts.parser()         
       		.setSigningKey(DatatypeConverter.parseBase64Binary(CONST.API_KEY))
       		.parseClaimsJws(jwt).getBody();
    		return new Pair(claims.getId(),claims.getExpiration().getTime());
	}


}