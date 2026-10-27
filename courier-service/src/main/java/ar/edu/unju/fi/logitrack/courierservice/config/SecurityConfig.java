package ar.edu.unju.fi.logitrack.courierservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Resource server de {@code courier-service} (S2-06).
 * <p>
 * Valida los JWT emitidos por {@code security-service}. Las consultas (incluida
 * {@code /disponibilidad}, que consume shipment-service propagando el token del usuario) requieren
 * estar autenticado; el alta y la modificación de repartidores quedan reservadas a {@code ADMIN}.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Endpoints públicos: documentación, health para Docker y el dispatch de errores. */
    private static final String[] RUTAS_PUBLICAS = {
            "/actuator/health", "/actuator/health/**", "/actuator/info",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationEntryPoint entryPoint,
            JwtAccessDeniedHandler accessDeniedHandler,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(RUTAS_PUBLICAS).permitAll()
                // S2-06: solo un ADMIN gestiona el alta y el estado de los repartidores.
                .requestMatchers(HttpMethod.POST, "/api/v1/repartidores").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/repartidores/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/repartidores/*/estado").hasRole("ADMIN")
                // Consultas (listado, detalle y disponibilidad): cualquier usuario autenticado.
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .exceptionHandling(excepciones -> excepciones
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    /**
     * Valida los JWT con el mismo secreto compartido que usa {@code security-service}.
     * En Docker llega por la variable de entorno {@code JWT_SECRET}.
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        SecretKey clave = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** Convierte el claim {@code roles: ["ADMIN"]} en la autoridad {@code ROLE_ADMIN}. */
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("roles");
        autoridades.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(autoridades);
        return converter;
    }
}
