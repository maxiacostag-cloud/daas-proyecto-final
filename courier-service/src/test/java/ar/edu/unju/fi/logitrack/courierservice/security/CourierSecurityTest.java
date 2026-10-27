package ar.edu.unju.fi.logitrack.courierservice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * S2-06: verifica el resource server de {@code courier-service} con JWT reales firmados con el
 * secreto de test. Las consultas exigen autenticación (shipment-service llega acá propagando el
 * token del usuario) y la gestión de repartidores queda reservada a {@code ADMIN}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CourierSecurityTest {

    private static final String REPARTIDORES = "/api/v1/repartidores";

    @Autowired
    private MockMvc mockMvc;

    private static String bodyRepartidor() {
        return """
            {
              "nombre": "Juan Pérez",
              "tipoVehiculo": "MOTO",
              "email": "repartidor.%s@logitrack.com",
              "telefono": "+54 388 400-2000"
            }
            """.formatted(UUID.randomUUID());
    }

    @Test
    @DisplayName("Sin token, un endpoint de negocio responde 401 en formato ProblemDetail")
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get(REPARTIDORES))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("No autorizado"))
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Un token vencido se rechaza con 401")
    void tokenVencidoResponde401() throws Exception {
        mockMvc.perform(get(REPARTIDORES)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtTestSupport.tokenExpirado("ADMIN")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un token firmado con otro secreto se rechaza con 401")
    void firmaInvalidaResponde401() throws Exception {
        mockMvc.perform(get(REPARTIDORES)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtTestSupport.tokenConOtroSecreto("ADMIN")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un usuario autenticado puede consultar repartidores y disponibilidad (token relay)")
    void autenticadoPuedeConsultar() throws Exception {
        String token = JwtTestSupport.token("CLIENTE");

        mockMvc.perform(get(REPARTIDORES).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());

        // El 404 confirma que pasó la seguridad y llegó al service: el repartidor no existe.
        mockMvc.perform(get(REPARTIDORES + "/{id}/disponibilidad", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Un ADMIN puede dar de alta un repartidor")
    void adminPuedeCrearRepartidor() throws Exception {
        mockMvc.perform(post(REPARTIDORES)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtTestSupport.token("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyRepartidor()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("Un CLIENTE no puede dar de alta un repartidor (403)")
    void clienteNoPuedeCrearRepartidor() throws Exception {
        mockMvc.perform(post(REPARTIDORES)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtTestSupport.token("CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyRepartidor()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Acceso denegado"))
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Un REPARTIDOR no puede modificar repartidores ni sus estados (403)")
    void repartidorNoPuedeModificar() throws Exception {
        String token = JwtTestSupport.token("REPARTIDOR");
        UUID repartidorId = UUID.randomUUID();

        mockMvc.perform(put(REPARTIDORES + "/{id}", repartidorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyRepartidor()))
            .andExpect(status().isForbidden());

        mockMvc.perform(patch(REPARTIDORES + "/{id}/estado", repartidorId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\": \"INACTIVO\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un ADMIN puede cambiar el estado de un repartidor")
    void adminPuedeCambiarEstado() throws Exception {
        // El 404 confirma que pasó la seguridad y llegó al service: el repartidor no existe.
        mockMvc.perform(patch(REPARTIDORES + "/{id}/estado", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtTestSupport.token("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\": \"INACTIVO\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Salud y documentación quedan públicos para Docker y Swagger")
    void endpointsPublicosNoPidenToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists());
    }
}
