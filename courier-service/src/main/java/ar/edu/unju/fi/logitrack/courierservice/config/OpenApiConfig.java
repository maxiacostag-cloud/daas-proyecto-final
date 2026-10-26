package ar.edu.unju.fi.logitrack.courierservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    public OpenAPI logitrackOpenApi() {
        return new OpenAPI().info(new Info()
                .title("LogiTrack - courier-service")
                .version("v1")
                .description("API de repartidores y disponibilidad, consumida por shipment-service."));
    }
}
