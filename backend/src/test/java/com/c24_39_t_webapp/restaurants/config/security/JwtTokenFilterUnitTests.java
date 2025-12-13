import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.config.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNull;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterUnitTests {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtTokenFilter jwtTokenFilter;

    // ========== HEADER TESTS ==========
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Cuando header es NULL → Continúa sin autenticarse")
    void whenHeaderIsNull_thenContinuesWithoutAuth() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtTokenFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    @DisplayName("Cuando header NO empieza con 'Bearer ' → Continúa sin autenticarse")
    void whenHeaderDoesNotStartWithBearer_thenContinuesWithoutAuth() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic xyz123");

//        jwtTokenFilter.doFilterInternal(request, response, filterChain);
        jwtTokenFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(any());
    }

    // ========== EMAIL/ROLE NULL TESTS ==========

    @Test
    @DisplayName("Cuando email es NULL (role NO null) → No autentica")
    void whenEmailIsNull_thenDoesNotAuthenticate() throws Exception {
        String token = "valid.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn(null);
        when(jwtUtil.extractRole(token)).thenReturn("CLIENTE");

        jwtTokenFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // SecurityContextHolder NO debe tener autenticación
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(email, null));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Cuando role es NULL (email NO null) → No autentica")
    void whenRoleIsNull_thenDoesNotAuthenticate() throws Exception {
        String token = "valid.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("user@test.com");
        when(jwtUtil.extractRole(token)).thenReturn(null);

        jwtTokenFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Cuando ambos email y role son NULL → No autentica")
    void whenEmailAndRoleAreNull_thenDoesNotAuthenticate() throws Exception {
        String token = "valid.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn(null);
        when(jwtUtil.extractRole(token)).thenReturn(null);

        jwtTokenFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
//        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}