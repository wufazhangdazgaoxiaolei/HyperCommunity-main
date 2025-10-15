package com.nowcoder.community.filter;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
public class TicketAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从Cookie获取Ticket
        String ticket = CookieUtil.getValue(request, "Ticket");
        //System.out.println("Filter ticket: " + ticket);

        // 2. 验证Ticket有效性
        if (StringUtils.hasText(ticket)) {
            LoginTicket loginTicket = userService.getLoginTicket(ticket);

            // 3. 检查Ticket状态和有效期
            if (loginTicket != null &&
                    loginTicket.getStatus() == 0 &&
                    loginTicket.getExpired().after(new Date())) {

                // 4. 加载用户信息
                User user = userService.findUserById(loginTicket.getUserId());

                // 5. 创建认证对象
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        user.getPassword(),
                        userService.getAuthorities(user.getId())
                );

                // 6. 创建安全上下文
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);

                // 7. 设置到当前线程的安全上下文
                SecurityContextHolder.setContext(context);

                // 8. 保存到Session确保跨请求可用
                request.getSession().setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        context
                );
            }
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}