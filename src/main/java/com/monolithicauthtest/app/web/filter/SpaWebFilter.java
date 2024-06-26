package com.monolithicauthtest.app.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class SpaWebFilter extends OncePerRequestFilter {

    /**
     * Forwards any unmapped paths (except those containing a period) to the client {@code index.html}.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        // Request URI includes the contextPath if any, removed it.
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (
            !path.startsWith("/api") &&
            !path.startsWith("/api/**") &&
            !path.startsWith("/management") &&
            !path.startsWith("/v3/api-docs") &&
            !path.startsWith("/h2-console") &&
            !path.startsWith("/authorize-github") &&
            !path.startsWith("/authorize-gitlab") &&
            !path.startsWith("/authorize-bitbucket") &&
            !path.startsWith("/login/oauth2/code/github") &&
            !path.startsWith("/login/oauth2/code/gitlab") &&
            !path.startsWith("/login/oauth2/code/bitbucket") &&
            !path.startsWith("/github/repositories") &&
            !path.startsWith("/gitlab/repositories") &&
            !path.startsWith("/bitbucket/repositories") &&
            !path.startsWith("/api/save-client-url") &&
            !path.startsWith("/suggest-buildpack") &&
            !path.startsWith("/clone-repo") &&
            !path.startsWith("/execute-build-command") &&
            !path.startsWith("/docker-entities") &&
            !path.startsWith("/push-to-registry") &&
            !path.contains(".") &&
            path.matches("/(.*)")
        ) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
