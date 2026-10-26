package ar.edu.unju.fi.logitrack.configserver;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;


@EnableConfigServer
@SpringBootApplication(exclude = {
    org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration.class
})
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}


