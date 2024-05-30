package com.newtouch.filter;

import com.google.common.collect.Sets;
import com.newtouch.service.AccountService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class SessionCheckFilter implements Filter {

    @Autowired
    private AccountService accountService;

    private Set<String> whiteRootPaths = Sets.newHashSet("login", "msgsocket", "test");

    // 解决ajax跨域问题
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");

        String[] uriSplit = httpServletRequest.getRequestURI().split("/");
        if (uriSplit.length < 2) {
            httpServletRequest.getRequestDispatcher("/login/loginfail").forward(servletRequest, servletResponse);
        } else {
            if (!whiteRootPaths.contains(uriSplit[1])) {    //不在白名单 验证token
                if (accountService.accountExistInCache(httpServletRequest.getParameter("token"))) {
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    httpServletRequest.getRequestDispatcher("/login/loginfail").forward(servletRequest, servletResponse);
                }
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
