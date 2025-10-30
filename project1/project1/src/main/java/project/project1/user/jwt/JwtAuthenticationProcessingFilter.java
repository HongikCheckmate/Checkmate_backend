package project.project1.user.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import project.project1.user.CustomUserDetails;
import project.project1.user.SiteUser;
import project.project1.user.UserRepository;
import project.project1.user.social.PasswordUtil;

import java.io.IOException;

/**
 * Jwt 인증 필터
 * "/login" 이외의 URI 요청이 왔을 때 처리하는 필터
 * 기본적으로 사용자는 요청 헤더에 AccessToken만 담아서 요청
 * AccessToken 만료 시에만 RefreshToken을 요청 헤더에 AccessToken과 함께 요청
 * 1. RefreshToken이 없고, AccessToken이 유효한 경우 -> 인증 성공 처리, RefreshToken을 재발급하지는 않는다.
 * 2. RefreshToken이 없고, AccessToken이 없거나 유효하지 않은 경우 -> 인증 실패 처리, 403 ERROR
 * 3. RefreshToken이 있는 경우 -> DB의 RefreshToken과 비교하여 일치하면 AccessToken 재발급, RefreshToken 재발급(RTR 방식)
 *                              인증 성공 처리는 하지 않고 실패 처리
 *
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    /**
     * 특정 URL은 이 필터를 거치지 않도록 설정
     * true를 반환하면 doFilterInternal()이 실행되지 않음
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String[] permitAllPaths = {
                "/api/user/signup",
                "/user/signup",
                "/api/user/login",
                "/user/login",
                "/swagger-ui/**",
                "/v3/**",
                "/h2-console/**",
        };

        for (String path : permitAllPaths) {
            if (pathMatcher.match(path, requestURI)) {
                return true; // true를 반환하여 필터링을 건너뜀
            }
        }

        return false;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (refreshToken != null) {
            userRepository.findByRefreshToken(refreshToken)
                    .ifPresent(user -> {
                        String reIssuedRefreshToken = reIssueRefreshToken(user);
                        jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getId(), user.getUsername()),
                                reIssuedRefreshToken);
                    });
        }


        checkAccessTokenAndAuthentication(request, response, filterChain);

    }

    //[리프레시 토큰으로 유저 정보 찾기 & 액세스 토큰/리프레시 토큰 재발급 메소드]
    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getId(), user.getUsername()),
                            reIssuedRefreshToken);
                });
    }


     //[리프레시 토큰 재발급 & DB에 리프레시 토큰 업데이트 메소드]
    private String reIssueRefreshToken(SiteUser user) {
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        user.updateRefreshToken(reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }

    //[액세스 토큰 체크 & 인증 처리 메소드]
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {
        log.info("checkAccessTokenAndAuthentication() 호출");
        jwtService.extractAccessToken(request)
                .ifPresentOrElse(
                        accessToken -> {
                            log.info("액세스 토큰 존재함. 유효성 검사 시작");
                            if (jwtService.isTokenValid(accessToken)) {
                                log.info("액세스 토큰 유효함. 사용자명 추출 시도");
                                jwtService.extractUsername(accessToken)
                                        .ifPresentOrElse(
                                                username -> {
                                                    log.info("사용자명 추출 성공: {}", username);
                                                    userRepository.findByUsername(username)
                                                            .ifPresentOrElse(
                                                                    this::saveAuthentication,
                                                                    () -> log.warn("DB에 해당 사용자({})가 없습니다.", username)
                                                            );
                                                },
                                                () -> log.warn("액세스 토큰에서 사용자명을 추출할 수 없습니다.")
                                        );
                            } else {
                                log.warn("유효하지 않은 액세스 토큰입니다.");
                            }
                        },
                        () -> log.info("요청 헤더에 액세스 토큰이 없습니다.")
                );

        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(SiteUser myUser) {
        log.info("{} 사용자의 인증 정보를 저장합니다.", myUser.getUsername());
        String password = myUser.getPassword();
        if (password == null) { // 소셜 로그인 유저의 비밀번호 임의로 설정 하여 소셜 로그인 유저도 인증 되도록 설정
            password = PasswordUtil.generateRandomPassword();
        }
        CustomUserDetails userDetails = new CustomUserDetails(myUser);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, // <--- 수정된 부분!
                        null,
                        userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("SecurityContext에 인증 정보 저장을 완료했습니다.");
    }
}
