package com.kantar.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kantar.vo.SummaryVO;

import jakarta.servlet.http.HttpServletRequest;

public class BaseController {
    /**
	 * Client IP 조회
	 * @param request
	 * @return
	 */
	protected String getClientIP(HttpServletRequest req) {
		String ip = req.getHeader("X-FORWARDED-FOR");
		if (ip == null || ip.length() == 0) {
			ip = req.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0) {
			ip = req.getHeader("WL-Proxy-Client-IP"); // 웹로직
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("HTTP_CLIENT_IP");
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = req.getHeader("X-Real-IP"); 
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = req.getHeader("X-RealIP"); 
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = req.getHeader("REMOTE_ADDR");
        }
		if (ip == null || ip.length() == 0) {
			ip = req.getRemoteAddr();
		}
		return ip;
	}

	public static Map<String, Object> beanToMap(Object bean, boolean isAllowNull) {
		Map<String, Object> map = new HashMap<String, Object>();
		putValues(bean, map, null, false, false, isAllowNull);
		return map;
	}

	public static Map<String, Object> beanToMap(Object bean, String prefixOverrides, boolean isAllowNull) {
		Map<String, Object> map = new HashMap<String, Object>();
		putValues(bean, map, prefixOverrides, false, false, isAllowNull);
		return map;
	}

	/**
	 * Map에 값 셋팅
	 *
	 * @param bean
	 * @param map
	 * @param prefixOverrides 제거할 prefix 문자열 (ex. sch_)
	 * @param keyUpperCase
	 * @param camelCase
	 * @return
	 */
	private static void putValues(Object bean, Map<String, Object> map,
		String prefixOverrides, boolean keyUpperCase, boolean camelCase, boolean isAllowNull) {

		Class<?> cls = bean.getClass();

		for (Field field : cls.getDeclaredFields()) {
			field.setAccessible(true);

			Object value = null;
			String key;

			try {
				value = field.get(bean);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			if (prefixOverrides == null) {
				key = field.getName();
			} else {
				key = field.getName().replaceFirst(prefixOverrides, "");
			}

			if (keyUpperCase) {
				key = key.toUpperCase();
			}

			if (camelCase) {
				StringBuffer buffer = new StringBuffer();
				for (String token : key.toLowerCase().split("_")) {
					buffer.append(StringUtils.capitalize(token));
				}
				key = StringUtils.uncapitalize(buffer.toString());
			}

			if (isValue(value)) {
				if(isAllowNull) {
					map.put(key, value);
				} else {
					if(value != null) {
						map.put(key, value);
					}
				}
			} else if (value instanceof BigDecimal) {
				map.put(key, value);
			} else {
				System.out.println("- DataMappingError >> " + value + "," + map + "," + key + "," + keyUpperCase + "," + camelCase + "," + isAllowNull);
			}
		}
	}

	private static final Set<Class<?>> valueClasses = (
		Collections.unmodifiableSet(
			new HashSet<>(
				Arrays.asList(
					Object.class, Boolean.class, Byte.class, Short.class, Character.class,
					String.class, Integer.class, Long.class, Float.class, Double.class
				)
			)
		)
	);

	private static boolean isValue(Object value) {
		return value == null || valueClasses.contains(value.getClass());
	}

	/**
	 * transfer http post protocol
	 * @param targetUrl
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String transferHttpPost(String targetUrl, SummaryVO params, String APIKEY) throws Exception {
		Map<String, Object> paramMap = beanToMap(params, false);
		StringBuilder paramUri = new StringBuilder();

		for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
			if("serialVersionUID".equals(entry.getKey())) {
				continue;
			}
			paramUri.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			paramUri.append("=");
			paramUri.append(URLEncoder.encode(entry.getValue()+"", "UTF-8"));
			paramUri.append("&");
		}

		String paramStr = paramUri.toString();
		paramStr = paramStr.length() > 0 ? paramStr.substring(0, paramStr.length() - 1) : paramStr;

		URL obj = new URL(targetUrl);
		disableSslVerification();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		if(APIKEY.equals("")){
			con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
		}else{
			con.setRequestProperty( "Content-Type", "application/json");
			con.setRequestProperty( "Authorization", "Bearer " + APIKEY);
		}
		con.setConnectTimeout(10000);
		con.setReadTimeout(10000);
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.write(paramStr.getBytes(StandardCharsets.UTF_8));
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String output;
		StringBuffer response = new StringBuffer();

		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();
		con.disconnect();
		return response.toString();
	}

	public static String transferHttpPost(String targetUrl, String params, String APIKEY) throws Exception {
		URL obj = new URL(targetUrl);
		disableSslVerification();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty( "Content-Type", "application/json");
		con.setRequestProperty( "Authorization", "Bearer " + APIKEY);
		con.setConnectTimeout(10000);
		con.setReadTimeout(10000);
		con.setDoOutput(true);
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
		wr.write(params);
		wr.flush();
		wr.close();

		Charset charset = Charset.forName("UTF-8");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
		String output;
		StringBuffer response = new StringBuffer();

		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();
		con.disconnect();
		return response.toString();
	}

	/**
     * get Map Object from JSON String by GSON
     * @param jsonStr
     * @return List<Map<String, String>>
     */
    public static List<Map<String, String>> getListFromJson(String jsonStr) {
		return new Gson().fromJson(jsonStr, new TypeToken<List<Map<String, String>>>() {
        }.getType());
	}
	
	// ssl security Exception 방지
	public static void disableSslVerification(){
		// TODO Auto-generated method stub
		try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType){
	            }
	            public void checkServerTrusted(X509Certificate[] certs, String authType){
	            }
	        }
	        };
	
	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	
	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session){
	                return true;
	            }
	        };
	
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    }
	}
}
