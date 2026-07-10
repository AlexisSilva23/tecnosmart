package duoc.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // ignora el campo "_links" que agrega HATEOAS
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private Double precio;
}