package duoc.pedidos.client;

import duoc.pedidos.dto.ProductoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogo-service") // debe coincidir con el spring.application.name registrado en Eureka
public interface ProductoClient {

    @GetMapping("/api/productos/{id}")
    ProductoResponseDTO obtenerProducto(@PathVariable("id") Long id);
}