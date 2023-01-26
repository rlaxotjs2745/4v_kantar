package com.kantar.util;

import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.kantar.vo.UserVO;

import java.util.Date;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenJWT {

    @Value("${spring.jwt.secret}")
    private String secretKey;
    private Long expiredTime = 1000 * 60L * 60L * 2L; // 토큰 유효 시간 (2시간)
        
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
        payloads.put("idx_user", _data.get("idx_user"));
        payloads.put("user_id", _data.get("user_id"));
        payloads.put("user_status", _data.get("user_status"));
        
        Date ext = new Date(); // 토큰 만료 시간
        ext.setTime(ext.getTime() + expiredTime);

        // 토큰 Builder
        String jwt = Jwts.builder()
            .setHeader(headers) // Headers 설정
            .setClaims(payloads) // Claims 설정
            .setSubject("user-auth")
            .setExpiration(ext) // 토큰 만료 시간 설정
            .signWith(SignatureAlgorithm.HS256, secretKey.getBytes()) // HS256과 Key로 Sign
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
        } catch (ExpiredJwtException e) { // 토큰이 만료되었을 경우
            System.out.println(token);
            System.out.println("token expired");
        } catch (Exception e) { // 그외 에러났을 경우
            System.err.println(e);
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
        UserVO b = new Gson().fromJson(claims.toString(), UserVO.class);
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
