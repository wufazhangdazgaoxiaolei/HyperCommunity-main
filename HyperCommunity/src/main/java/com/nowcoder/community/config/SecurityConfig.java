package com.nowcoder.community.config;

import com.nowcoder.community.filter.TicketAuthenticationFilter;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig implements CommunityConstant {

    @Autowired
    private TicketAuthenticationFilter ticketAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 将自定义过滤器添加到安全链中
        http.addFilterBefore(ticketAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // 配置安全上下文存储
        http.securityContext(securityContext -> securityContext
                .securityContextRepository(new HttpSessionSecurityContextRepository())
        );

        // 授权配置
        http.authorizeHttpRequests(authorizeHttpRequests ->
                authorizeHttpRequests.requestMatchers(
                                "/user/setting",
                                "/user/upload",
                                "/discuss/add",
                                "/comment/add/**",
                                "/letter/**",
                                "/notice/**",
                                "/like",
                                "/follow",
                                "/unfollow"
                        ).access((authentication, object) -> {
                            // 权限检查前输出日志
                            System.out.println("\n===== 访问受保护资源: " + object.getRequest().getRequestURI() + " =====");

                            // 获取当前认证信息
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            if (auth == null) {
                                System.out.println("当前用户: 未认证");
                                return new AuthorizationDecision(false);
                            }

                            // 输出用户信息
                            System.out.println("当前用户: " + auth.getName());
                            System.out.println("用户权限: " + auth.getAuthorities());

                            // 检查权限
                            boolean hasAuthority = auth.getAuthorities().stream()
                                    .anyMatch(g ->
                                            AUTHORITY_USER.equals(g.getAuthority()) ||
                                                    AUTHORITY_ADMIN.equals(g.getAuthority()) ||
                                                    AUTHORITY_MODERATOR.equals(g.getAuthority()));

                            System.out.println("是否有访问权限: " + hasAuthority);
                            return new AuthorizationDecision(hasAuthority);
                        })
                        .requestMatchers(
                                "/discuss/top",
                                "/discuss/wonderful"
                        ).hasAnyAuthority(
                                AUTHORITY_MODERATOR
                        )
                        .requestMatchers(
                                "/discuss/delete",
                                "/data/**",
                                "/actuator/**"
                        ).hasAnyAuthority(
                                AUTHORITY_ADMIN
                        )
                        .anyRequest().permitAll()
        );

        // 其他配置保持不变...
        http.csrf(csrf -> csrf.disable());

        // 权限不够时的处理
        http.exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        System.out.println("\n===== 未登录访问被拦截 =====");
                        System.out.println("请求路径: " + request.getRequestURI());
                        System.out.println("X-Requested-With: " + request.getHeader("x-requested-with"));

                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录哦!"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                }).accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, jakarta.servlet.ServletException {
                        System.out.println("\n===== 权限不足访问被拦截 =====");
                        System.out.println("请求路径: " + request.getRequestURI());

                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null) {
                            System.out.println("当前用户: " + auth.getName());
                            System.out.println("用户权限: " + auth.getAuthorities());
                        } else {
                            System.out.println("当前用户: 未认证");
                        }

                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限!"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                })
        );

        // 自定义退出逻辑
        http.logout(logout -> logout.logoutUrl("/securitylogout"));

        return http.build();
    }
}
