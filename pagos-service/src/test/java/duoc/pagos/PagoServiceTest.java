package duoc.pagos;

import duoc.pagos.dto.PagoRequest;
import duoc.pagos.model.Pago;
import duoc.pagos.repository.PagoRepository;
import duoc.pagos.service.PagoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository repository;

    @InjectMocks
    private PagoService service;

    @Test
    void procesarPago_debeGuardarPagoConEstadoAprobado() {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(1L);
        request.setMetodo("TARJETA");
        request.setMonto(50000);

        when(repository.save(any(Pago.class))).thenAnswer(i -> {
            Pago p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        Pago resultado = service.procesarPago(request);

        assertEquals(1L, resultado.getPedidoId());
        assertEquals("TARJETA", resultado.getMetodo());
        assertEquals(50000, resultado.getMonto());
        assertEquals("APROBADO", resultado.getEstado());
        assertNotNull(resultado.getFechaPago());
        verify(repository).save(any(Pago.class));
    }

    @Test
    void procesarPago_debePersistirFechaPago() {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(1L);
        request.setMetodo("EFECTIVO");
        request.setMonto(10000);

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);
        when(repository.save(any(Pago.class))).thenAnswer(i -> i.getArgument(0));

        Pago resultado = service.procesarPago(request);

        assertNotNull(resultado.getFechaPago());
        assertTrue(resultado.getFechaPago().isAfter(antes));
    }

    @Test
    void listarTodos_debeRetornarListaDePagos() {
        Pago pago = Pago.builder().id(1L).pedidoId(1L).estado("APROBADO").build();
        when(repository.findAll()).thenReturn(List.of(pago));

        List<Pago> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        verify(repository).findAll();
    }

    @Test
    void listarTodos_debeRetornarListaVaciaSiNoHayPagos() {
        when(repository.findAll()).thenReturn(List.of());

        List<Pago> resultado = service.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorId_debeRetornarPagoCuandoExiste() {
        Pago pago = Pago.builder().id(1L).pedidoId(1L).estado("APROBADO").build();
        when(repository.findById(1L)).thenReturn(Optional.of(pago));

        Pago resultado = service.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        verify(repository).findById(1L);
    }

    @Test
    void buscarPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void buscarPorPedido_debeRetornarPagoCuandoExiste() {
        Pago pago = Pago.builder().id(1L).pedidoId(5L).estado("APROBADO").build();
        when(repository.findByPedidoId(5L)).thenReturn(Optional.of(pago));

        Pago resultado = service.buscarPorPedido(5L);

        assertEquals(5L, resultado.getPedidoId());
        verify(repository).findByPedidoId(5L);
    }

    @Test
    void buscarPorPedido_debeLanzarExcepcionSiPedidoNoTienePago() {
        when(repository.findByPedidoId(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.buscarPorPedido(99L));
    }

    @Test
    void eliminarPago_debeEliminarCuandoExiste() {
        when(repository.existsById(1L)).thenReturn(true);

        service.eliminarPago(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void eliminarPago_debeLanzarExcepcionCuandoNoExiste() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> service.eliminarPago(99L));
        verify(repository, never()).deleteById(any());
    }
}