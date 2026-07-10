package duoc.catalogo.controller;

import duoc.catalogo.dto.ProductoRequestDTO;
import duoc.catalogo.model.Producto;
import duoc.catalogo.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // crea un producto nuevo
    @PostMapping
    public ResponseEntity<EntityModel<Producto>> crearProducto(@Valid @RequestBody ProductoRequestDTO dto) {
        Producto nuevo = productoService.guardarProducto(dto);
        return new ResponseEntity<>(toModel(nuevo), HttpStatus.CREATED);
    }

    // lista todos los productos
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listarProductos() {
        List<EntityModel<Producto>> productos = productoService.listarLosProductos().stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Producto>> collection = CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    // busca productos por su ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> buscarPorId(@PathVariable long id) {
        Producto producto = productoService.buscarProductoPorId(id);
        return ResponseEntity.ok(toModel(producto));
    }

    // actualiza los productos
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        Producto actualizado = productoService.actualizarProducto(id, dto);
        return ResponseEntity.ok(toModel(actualizado));
    }

    // elimina los productos
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // método auxiliar: arma el EntityModel con sus enlaces HATEOAS
    private EntityModel<Producto> toModel(Producto producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).buscarPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("todos-los-productos"),
                linkTo(methodOn(CategoriaController.class).obtenerPorId(producto.getCategoria().getId())).withRel("categoria")
        );
    }
}