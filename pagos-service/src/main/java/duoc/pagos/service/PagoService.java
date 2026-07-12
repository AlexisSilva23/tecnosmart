package duoc.pagos.service;

import duoc.pagos.dto.PagoRequest;
import duoc.pagos.model.Pago;
import duoc.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);
    private final PagoRepository repository;

    @Transactional
    public Pago procesarPago(PagoRequest request) {
        log.info("[Service] Iniciando procesamiento de pago para Pedido ID: {}", request.getPedidoId());

        Pago pago = Pago.builder()
                .pedidoId(request.getPedidoId())
                .metodo(request.getMetodo())
                .monto(request.getMonto())
                .estado("APROBADO")
                .fechaPago(LocalDateTime.now())
                .build();

        Pago pagoGuardado = repository.save(pago);
        log.info("[Service] Pago registrado exitosamente con ID: {}", pagoGuardado.getId());

        return pagoGuardado;
    }

    public List<Pago> listarTodos() {
        log.info("[Service] Consultando todos los pagos");
        return repository.findAll();
    }

    public Pago buscarPorId(Long id) {
        log.info("[Service] Buscando pago con ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado con ID: " + id));
    }

    public Pago buscarPorPedido(Long pedidoId) {
        log.info("[Service] Buscando pago del pedido ID: {}", pedidoId);
        return repository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new NoSuchElementException("No existe pago para el pedido ID: " + pedidoId));
    }

    public void eliminarPago(Long id) {
        log.warn("[Service] Eliminando pago con ID: {}", id);
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Pago no encontrado con ID: " + id);
        }
        repository.deleteById(id);
    }
}