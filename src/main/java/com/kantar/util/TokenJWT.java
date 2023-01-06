package com.kantar.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kantar.vo.UserVO;

import javax.annotation.PostConstruct;

import java.util.Base64;
import java.util.Date;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class TokenJWT {

    @Value("${spring.jwt.secret}")
    private String secretKey;
    final Long expiredTime = 1000 * 60L * 60L * 2L; // 토큰 유효 시간 (2시간)

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public void main(String[] args) throws UnsupportedEncodingException {
        // TokenJWT tokenJWT = new TokenJWT();
        // System.out.println(secretKey);

        // String jwt = tokenJWT.createToken(null, "USER");
        // System.out.println(jwt);
        
        // Map<String, Object> claimMap = tokenJWT.verifyJWT(jwt);
        // System.out.println(claimMap); // 토큰이 만료되었거나 문제가있으면 null
    }
        
    /**
     * 토큰 생성
     * @param userPk
     * @param _ulvl
     * @param _data
     * @return token = jwt
     */
    public String createToken(Map<String, Object> _data, String roles) {
        //Header 부분 설정
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT");
        headers.put("alg", "HS256");

        //payload 부분 설정
        Claims payloads = Jwts.claims();
        payloads.put("role_type", roles);
        payloads.put("user_id", _data.get("user_id"));
        payloads.put("user_status", _data.get("user_status"));
        
        Date ext = new Date(); // 토큰 만료 시간
        ext.setTime(ext.getTime() + expiredTime);

        // 토큰 Builder
        String jwt = Jwts.builder()
            .setHeader(headers) // Headers 설정
            .setClaims(payloads) // Claims 설정
            .setExpiration(ext) // 토큰 만료 시간 설정
            .signWith(SignatureAlgorithm.HS256, secretKey) // HS256과 Key로 Sign
            .compact(); // 토큰 생성

        return jwt;
    }

        /**
     * 토큰 검증
     * @param token
     * @return Map
     */
    public Map<String, Object> verifyJWT(String token) {
        Map<String, Object> claimMap = null;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes()) // Set Key
                    .parseClaimsJws(token) // 파싱 및 검증, 실패 시 에러
                    .getBody();

            claimMap = claims;

            //Date expiration = claims.get("exp", Date.class);
            //String data = claims.get("data", String.class);
            
        } catch (ExpiredJwtException e) { // 토큰이 만료되었을 경우
            System.out.println("token expired");
        } catch (Exception e) { // 그외 에러났을 경우
            System.out.println("token verify error");
        }
        return claimMap;
    }

    /**
     * token 파싱
     * * Request의 Header에서 token 파싱 : "X-AUTH-TOKEN: jwt토큰"
     * @param req
     * @return X-AUTH-TOKEN token String
     */
    public String resolveToken(HttpServletRequest req) {
        String a = req.getHeader("X-AUTH-TOKEN");
        if(a==null || "".equals(a)){
            if(req.getCookies() != null){
                Cookie o[] = req.getCookies();
                for(int i=0;i<o.length;i++){
                    if(o[i].getName().equals("token")){
                        a = o[i].getValue();
                    }
                }
            }
        }
        return a;
    }

    /**
     * data 파싱
     * @param token
     * @return UserVO
     */
    public UserVO getRoles(String token) {
        Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token).getBody();
        ObjectMapper a = new ObjectMapper();
        UserVO b = a.convertValue(claims.get("data"), UserVO.class);
        return b;
    }

    /**
     * Claims에 저장된 데이터 뽑기
     * @param token
     * @param target
     * @return String
     */
    public String getClaims(String token, String target) {
        Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token).getBody();
        return String.valueOf(claims.get(target));
    }
}
