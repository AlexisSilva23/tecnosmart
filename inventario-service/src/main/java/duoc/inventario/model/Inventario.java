package duoc.inventario.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventarios")
@Data
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // guarda el ID del producto que vive en 'catálogo'
    @Column(nullable = false, unique = true)
    private Long productoId;

    @Column(nullable = false)
    private Integer cantidad;
}

/*
    Explicación: su función es registrar las unidades de cada producto,
    y se asegura de que no existan 2 filas distintas del mismo producto.
     */