package com.example.imbank.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter  extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extract JWT from request
            String jwt = getJwtFromRequest(request);
            // 2. Check if token exists and is valid
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) { /// //NB if token is invalid or missing, we dont hrow an exception, spring security rejects the request
                // 3. Extract username from token
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                // 4. Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username); ///fetch from db , also wrapped in  CustomUserDetails because tokenm might be valid but user might be deleted

                // 5. Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // Principal (the user)
                                null,                  // Credentials (password - not needed, already authenticated)
                                userDetails.getAuthorities()  // Roles/permissions
                        );

                // 6. Set additional details (IP address, session ID, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. Set authentication in Security Context ----> most important after this line spring knows thisuser is authentcted
                SecurityContextHolder.getContext().setAuthentication(authentication);

                ///the context (currenbt) for this user is authenticxated
//                Before: SecurityContext = [anonymous]
//                After:  SecurityContext = [jsmith, ROLE_USER, ROLE_ADMIN]

                log.debug("User '{}' authenticated successfully", username);
            }

        } catch (Exception ex) {
            // Log error but don't stop the request
            log.error("Could not set user authentication in security context", ex);
        }

        // 8. Continue filter chain (pass to next filter or controller)
        filterChain.doFilter(request, response); ///----> paass request to the next filter chain e.g request stops here and not reaching the controller.
    }

    /**
     *Skip filter for public endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean skip = path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/h2-console");
        log.info("JWT Filter - Path: {}, Skip: {}", path, skip);
        return skip;
    }

    /**
     * Extract JWT token from Authorization header
     * Header format: "Authorization: Bearer <token>"
     *
     * @param request HTTP request
     * @return JWT token string or null if not present
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Get Authorization header
        String bearerToken = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token (remove "Bearer " prefix)
            return bearerToken.substring(7);  // "Bearer " is 7 characters
        }

        return null;  // No token found
    }
}
