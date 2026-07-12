package duoc.pagos.controller;

import duoc.pagos.dto.PagoRequest;
import duoc.pagos.model.Pago;
import duoc.pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private static final Logger log = LoggerFactory.getLogger(PagoController.class);
    private final PagoService service;

    // CORRECCIÓN: quitada la barra final de @PostMapping("/")
    @PostMapping
    public ResponseEntity<Pago> registrarPago(@Valid @RequestBody PagoRequest request) {
        log.info("[Controller] POST /api/pagos - registrar pago");
        Pago nuevoPago = service.procesarPago(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPago);
    }

    @GetMapping
    public ResponseEntity<List<Pago>> listarTodos() {
        log.info("[Controller] GET /api/pagos - listar todos");
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> buscarPorId(@PathVariable Long id) {
        log.info("[Controller] GET /api/pagos/{} - buscar por id", id);
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Pago> buscarPorPedido(@PathVariable Long pedidoId) {
        log.info("[Controller] GET /api/pagos/pedido/{} - buscar por pedido", pedidoId);
        return ResponseEntity.ok(service.buscarPorPedido(pedidoId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPago(@PathVariable Long id) {
        log.warn("[Controller] DELETE /api/pagos/{} - eliminar pago", id);
        service.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }
}