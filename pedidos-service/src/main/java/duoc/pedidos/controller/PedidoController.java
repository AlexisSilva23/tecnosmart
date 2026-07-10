package duoc.pedidos.controller;

import duoc.pedidos.dto.PedidoRequestDTO;
import duoc.pedidos.model.Pedido;
import duoc.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
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
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<EntityModel<Pedido>> crear(@Valid @RequestBody @NonNull PedidoRequestDTO dto) {
        Pedido nuevo = pedidoService.procesarCompra(dto.getUsuarioId());
        return new ResponseEntity<>(toModel(nuevo), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> listar() {
        List<EntityModel<Pedido>> pedidos = pedidoService.listarTodosLosPedidos().stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Pedido>> collection = CollectionModel.of(pedidos,
                linkTo(methodOn(PedidoController.class).listar()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> obtenerPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(toModel(pedido));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> listarPorUsuario(@PathVariable Long usuarioId) {
        List<EntityModel<Pedido>> pedidos = pedidoService.listarPorUsuario(usuarioId).stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Pedido>> collection = CollectionModel.of(pedidos,
                linkTo(methodOn(PedidoController.class).listarPorUsuario(usuarioId)).withSelfRel(),
                Link.of("/api/usuarios/" + usuarioId).withRel("usuario"));

        return ResponseEntity.ok(collection);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<EntityModel<Pedido>> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        Pedido actualizado = pedidoService.actualizarEstado(id, estado);
        return ResponseEntity.ok(toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }

    // método auxiliar: arma el EntityModel con enlaces HATEOAS, incluyendo enlace
    // cruzado hacia el usuario (vive en otro microservicio, vía Gateway)
    private EntityModel<Pedido> toModel(Pedido pedido) {
        return EntityModel.of(pedido,
                linkTo(methodOn(PedidoController.class).obtenerPorId(pedido.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).listarPorUsuario(pedido.getUsuarioId())).withRel("pedidos-usuario"),
                linkTo(methodOn(PedidoController.class).eliminar(pedido.getId())).withRel("eliminar"),
                Link.of("/api/usuarios/" + pedido.getUsuarioId()).withRel("usuario")
        );
    }
}
/* Explicación: simula ser un cajero, recibe la orden y devuelve la confirmación del pedido hecho */