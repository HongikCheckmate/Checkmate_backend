package project.project1.user.social;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import java.util.Base64;
import java.util.Optional;

public class CookieUtils {
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 응답에 쿠키를 추가합니다.
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .maxAge(maxAge)
//                .sameSite("Lax") // localhost 개발 환경을 위한 SameSite 설정
                 .secure(true) // 배포 시(HTTPS)에는 "None"과 함께 이 옵션을 켜야 합니다.
                 .sameSite("None") // 배포 시(HTTPS) "Lax" 대신 "None"으로 변경
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 쿠키를 삭제합니다.
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    ResponseCookie deleteCookie = ResponseCookie.from(name, "")
                            .path("/")
                            .httpOnly(true)
                            .maxAge(0) // 만료 시간을 0으로 설정
                            //.sameSite("Lax") // 추가 (addCookie와 동일하게)
                             .secure(true) // 배포 시(HTTPS) 추가
                             .sameSite("None") // 배포 시(HTTPS) 추가
                            .build();

                    response.addHeader("Set-Cookie", deleteCookie.toString());
                }
            }
        }
    }
}
