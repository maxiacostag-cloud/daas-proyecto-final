package ar.edu.unju.fi.logitrack.courierservice.integration;

import ar.edu.unju.fi.logitrack.courierservice.entity.Repartidor;
import ar.edu.unju.fi.logitrack.courierservice.entity.enums.EstadoRepartidor;
import ar.edu.unju.fi.logitrack.courierservice.repository.RepartidorRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración de punta a punta: controller + service + repository + H2 en memoria.
 * Recorre el ciclo de vida de un repartidor y el endpoint de disponibilidad que consume shipment-service.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GestionRepartidoresIntegrationTest {

    private static final String URL = "/api/v1/repartidores";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepartidorRepository repartidorRepository;

    @BeforeEach
    void limpiarBase() {
        repartidorRepository.deleteAll();
    }

    private String registrar(String nombre, String email) throws Exception {
        String body = """
                {"nombre": "%s", "tipoVehiculo": "MOTO", "email": "%s", "telefono": "+54 388 400-2000"}
                """.formatted(nombre, email);
        String respuesta = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.id");
    }

    @Test
    @DisplayName("flujo completo: registrar, consultar, listar y desactivar (cambia la disponibilidad)")
    void flujoCompleto() throws Exception {
        String id = registrar("Juan Pérez", "juan.perez@logitrack.test");

        // 1. Detalle del repartidor recién creado
        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        // 2. Lista de disponibles
        mockMvc.perform(get(URL).param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // 3. Disponibilidad remota: ACTIVO -> disponible
        mockMvc.perform(get(URL + "/{id}/disponibilidad", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repartidorId").value(id))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.disponible").value(true));

        // 4. Desactivar
        mockMvc.perform(patch(URL + "/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));

        // 5. Ya no figura como disponible y la consulta remota lo refleja
        mockMvc.perform(get(URL).param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get(URL + "/{id}/disponibilidad", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"))
                .andExpect(jsonPath("$.disponible").value(false));

        // 6. Persistencia
        Repartidor guardado = repartidorRepository.findById(UUID.fromString(id)).orElseThrow();
        assertThat(guardado.getEstado()).isEqualTo(EstadoRepartidor.INACTIVO);
    }

    @Test
    @DisplayName("email único: el duplicado devuelve 409 y el contacto se puede actualizar")
    void emailUnicoYActualizacion() throws Exception {
        String id = registrar("Ana Torres", "ana@logitrack.test");

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Otra Persona", "tipoVehiculo": "AUTO", "email": "ANA@logitrack.test", "telefono": "+54 388 400-5000"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Recurso duplicado"));

        mockMvc.perform(put(URL + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Ana Torres", "tipoVehiculo": "AUTO", "email": "ana@logitrack.test", "telefono": "+54 388 400-9999"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value("+54 388 400-9999"))
                .andExpect(jsonPath("$.tipoVehiculo").value("AUTO"));
    }

    @Test
    @DisplayName("un repartidor INACTIVO deja de estar disponible para asignación")
    void repartidorInactivoNoEstaDisponible() throws Exception {
        String id = registrar("Carlos Gómez", "carlos@logitrack.test");

        mockMvc.perform(patch(URL + "/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get(URL + "/{id}/disponibilidad", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));

        mockMvc.perform(get(URL + "/{id}/disponibilidad", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("listar sin filtro devuelve todos, ordenados por nombre")
    void listarOrdenadoPorNombre() throws Exception {
        registrar("Zoe Álvarez", "zoe@logitrack.test");
        registrar("Ana Torres", "ana@logitrack.test");

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Ana Torres"))
                .andExpect(jsonPath("$[1].nombre").value("Zoe Álvarez"));
    }

    @Test
    @DisplayName("recursos inexistentes devuelven 404")
    void recursosInexistentes() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get(URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(get(URL + "/{id}/disponibilidad", id))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch(URL + "/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put(URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Nadie", "tipoVehiculo": "MOTO", "email": "nadie@logitrack.test", "telefono": "+54 388 400-2000"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("datos inválidos devuelven 400 con el detalle por campo")
    void validacionDeDatos() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\", \"tipoVehiculo\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.errors.nombre").exists())
                .andExpect(jsonPath("$.errors.tipoVehiculo").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }
}
