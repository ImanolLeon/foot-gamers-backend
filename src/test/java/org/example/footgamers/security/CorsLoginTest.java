package org.example.footgamers.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CorsLoginTest {
    private CorsFilter filter() {
        SecurityConfig config = new SecurityConfig(null, null, null, null);
        ReflectionTestUtils.setField(config, "allowedOrigins",
                "http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://127.0.0.1:5174,http://localhost:4173,http://127.0.0.1:4173");
        ReflectionTestUtils.setField(config, "frontendUrl", "https://frontend.example.com");
        return new CorsFilter(config.corsConfigurationSource());
    }

    @Test
    void permitsLoginAndPreflightFromConfiguredFrontends() throws Exception {
        for (String origin : new String[]{"http://localhost:5173", "http://127.0.0.1:5173",
                "http://localhost:5174", "http://127.0.0.1:5174", "http://localhost:4173",
                "http://127.0.0.1:4173", "https://frontend.example.com"}) {
            for (String method : new String[]{"OPTIONS", "POST"}) {
                var request = new MockHttpServletRequest(method, "/auth/login");
                request.addHeader("Origin", origin);
                if (method.equals("OPTIONS")) {
                    request.addHeader("Access-Control-Request-Method", "POST");
                    request.addHeader("Access-Control-Request-Headers", "content-type,authorization");
                }
                var response = new MockHttpServletResponse();
                filter().doFilter(request, response, (req, res) -> res.setContentType("application/json"));
                assertThat(response.getStatus()).as(origin + " " + method).isEqualTo(200);
                assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo(origin);
            }
        }
    }

    @Test
    void rejectsUnknownOrigins() throws Exception {
        var request = new MockHttpServletRequest("POST", "/auth/login");
        request.addHeader("Origin", "https://untrusted.example.com");
        var response = new MockHttpServletResponse();
        filter().doFilter(request, response, (req, res) -> { throw new AssertionError("Unexpected access"); });
        assertThat(response.getStatus()).isEqualTo(403);
    }
}