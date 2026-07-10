package duoc.carrito.controller;

import duoc.carrito.dto.CarritoRequestDTO;
import duoc.carrito.model.CarritoItem;
import duoc.carrito.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @PostMapping
    public ResponseEntity<EntityModel<CarritoItem>> agregar(@Valid @RequestBody CarritoRequestDTO dto) {
        CarritoItem nuevo = carritoService.agregarItem(dto);
        return new ResponseEntity<>(toModel(nuevo), HttpStatus.CREATED);
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<CarritoItem>>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        List<EntityModel<CarritoItem>> items = carritoService.obtenerCarritoPorUsuario(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<CarritoItem>> collection = CollectionModel.of(items,
                linkTo(methodOn(CarritoController.class).obtenerPorUsuario(usuarioId)).withSelfRel(),
                Link.of("/api/usuarios/" + usuarioId).withRel("usuario"),
                Link.of("/api/carrito/vaciar/" + usuarioId).withRel("vaciar-carrito")
        );

        return ResponseEntity.ok(collection);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<CarritoItem>> actualizarCantidad(@PathVariable Long id, @RequestParam Integer cantidad) {
        CarritoItem actualizado = carritoService.actualizarCantidad(id, cantidad);
        return ResponseEntity.ok(toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        carritoService.eliminarItem(id);
        return ResponseEntity.noContent().build();
    }

    // vaciar carrito
    @DeleteMapping("/vaciar/{usuarioId}")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }

    // metodo auxiliar: arma el EntityModel con enlaces HATEOAS, incluyendo enlaces
    // "cruzados" hacia usuario y producto (viven en otros microservicios, vía Gateway)
    private EntityModel<CarritoItem> toModel(CarritoItem item) {
        return EntityModel.of(item,
                linkTo(methodOn(CarritoController.class).obtenerPorUsuario(item.getUsuarioId())).withRel("carrito-usuario"),
                linkTo(methodOn(CarritoController.class).eliminarItem(item.getId())).withRel("eliminar"),
                Link.of("/api/usuarios/" + item.getUsuarioId()).withRel("usuario"),
                Link.of("/api/productos/" + item.getProductoId()).withRel("producto")
        );
    }
}

/* Explicación: permite ver el carrito, añadir y/o borrar cosas del carrito */

