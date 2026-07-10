package duoc.pedidos;

import duoc.pedidos.client.CarritoClient;
import duoc.pedidos.client.InventarioClient;
import duoc.pedidos.client.ProductoClient;
import duoc.pedidos.dto.CarritoItemDTO;
import duoc.pedidos.dto.ProductoResponseDTO;
import duoc.pedidos.model.Pedido;
import duoc.pedidos.repository.PedidoRepository;
import duoc.pedidos.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;

	@Mock
	private CarritoClient carritoClient;

	@Mock
	private InventarioClient inventarioClient;

	@Mock
	private ProductoClient productoClient;

	@InjectMocks
	private PedidoService service;

	// método auxiliar para no repetir la construcción del DTO en cada test
	private ProductoResponseDTO productoConPrecio(Long id, Double precio) {
		ProductoResponseDTO producto = new ProductoResponseDTO();
		producto.setId(id);
		producto.setPrecio(precio);
		return producto;
	}

	@Test
	void procesarCompra_debeGuardarPedidoYVaciarCarritoCuandoTodoEsCorrecto() {
		CarritoItemDTO item = new CarritoItemDTO();
		item.setProductoId(10L);
		item.setCantidad(2);

		when(carritoClient.obtenerCarrito(1L)).thenReturn(List.of(item));
		when(inventarioClient.validarStock(10L, 2)).thenReturn(true);
		when(productoClient.obtenerProducto(10L)).thenReturn(productoConPrecio(10L, 15000.0));
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> {
			Pedido p = i.getArgument(0);
			p.setId(1L);
			return p;
		});

		Pedido resultado = service.procesarCompra(1L);

		assertEquals(1L, resultado.getUsuarioId());
		assertEquals("PAGADO", resultado.getEstado());
		assertNotNull(resultado.getFecha());
		assertEquals(30000.0, resultado.getTotal()); // 2 unidades x 15000
		verify(pedidoRepository).save(any(Pedido.class));
		verify(carritoClient).vaciarCarrito(1L);
	}

	@Test
	void procesarCompra_debeLanzarExcepcionSiElCarritoEstaVacio() {
		when(carritoClient.obtenerCarrito(1L)).thenReturn(List.of());

		assertThrows(IllegalStateException.class, () -> service.procesarCompra(1L));
		// si el carrito está vacío, no debe tocar ni inventario, ni catálogo, ni repositorio
		verify(inventarioClient, never()).validarStock(any(), any());
		verify(productoClient, never()).obtenerProducto(any());
		verify(pedidoRepository, never()).save(any());
		verify(carritoClient, never()).vaciarCarrito(any());
	}

	@Test
	void procesarCompra_debeLanzarExcepcionSiNoHayStockParaUnProducto() {
		CarritoItemDTO item = new CarritoItemDTO();
		item.setProductoId(10L);
		item.setCantidad(5);

		when(carritoClient.obtenerCarrito(1L)).thenReturn(List.of(item));
		when(inventarioClient.validarStock(10L, 5)).thenReturn(false);

		assertThrows(IllegalStateException.class, () -> service.procesarCompra(1L));
		// si no hay stock, no debe consultarse el precio ni guardarse el pedido
		verify(productoClient, never()).obtenerProducto(any());
		verify(pedidoRepository, never()).save(any());
		verify(carritoClient, never()).vaciarCarrito(any());
	}

	@Test
	void procesarCompra_debeValidarStockDeTodosLosProductosDelCarrito() {
		CarritoItemDTO item1 = new CarritoItemDTO();
		item1.setProductoId(10L);
		item1.setCantidad(1);

		CarritoItemDTO item2 = new CarritoItemDTO();
		item2.setProductoId(20L);
		item2.setCantidad(3);

		when(carritoClient.obtenerCarrito(1L)).thenReturn(List.of(item1, item2));
		// el primer producto tiene stock pero el segundo no
		when(inventarioClient.validarStock(10L, 1)).thenReturn(true);
		when(inventarioClient.validarStock(20L, 3)).thenReturn(false);

		assertThrows(IllegalStateException.class, () -> service.procesarCompra(1L));
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void procesarCompra_debeAsignarItemsDelCarritoAlPedidoConPrecioYTotalCorrectos() {
		CarritoItemDTO item = new CarritoItemDTO();
		item.setProductoId(10L);
		item.setCantidad(2);

		when(carritoClient.obtenerCarrito(1L)).thenReturn(List.of(item));
		when(inventarioClient.validarStock(10L, 2)).thenReturn(true);
		when(productoClient.obtenerProducto(10L)).thenReturn(productoConPrecio(10L, 15000.0));
		when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

		Pedido resultado = service.procesarCompra(1L);

		assertNotNull(resultado.getItems());
		assertEquals(1, resultado.getItems().size());
		assertEquals(10L, resultado.getItems().get(0).getProductoId());
		assertEquals(2, resultado.getItems().get(0).getCantidad());
		assertEquals(15000.0, resultado.getItems().get(0).getPrecioUnitario()); // antes quedaba null
		assertEquals(30000.0, resultado.getTotal()); // antes quedaba null
	}

	@Test
	void listarTodosLosPedidos_debeRetornarListaCompleta() {
		Pedido pedido = new Pedido();
		pedido.setId(1L);
		when(pedidoRepository.findAll()).thenReturn(List.of(pedido));

		List<Pedido> resultado = service.listarTodosLosPedidos();

		assertEquals(1, resultado.size());
		verify(pedidoRepository).findAll();
	}

	@Test
	void buscarPorId_debeRetornarPedidoCuandoExiste() {
		Pedido pedido = new Pedido();
		pedido.setId(1L);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

		Pedido resultado = service.buscarPorId(1L);

		assertEquals(1L, resultado.getId());
		verify(pedidoRepository).findById(1L);
	}

	@Test
	void buscarPorId_debeLanzarExcepcionCuandoNoExiste() {
		when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> service.buscarPorId(99L));
	}

	@Test
	void listarPorUsuario_debeRetornarPedidosDelUsuario() {
		Pedido pedido = new Pedido();
		pedido.setUsuarioId(1L);
		when(pedidoRepository.findByUsuarioId(1L)).thenReturn(List.of(pedido));

		List<Pedido> resultado = service.listarPorUsuario(1L);

		assertEquals(1, resultado.size());
		assertEquals(1L, resultado.get(0).getUsuarioId());
		verify(pedidoRepository).findByUsuarioId(1L);
	}

	@Test
	void listarPorUsuario_debeRetornarListaVaciaSiNoTienePedidos() {
		when(pedidoRepository.findByUsuarioId(99L)).thenReturn(List.of());

		List<Pedido> resultado = service.listarPorUsuario(99L);

		assertTrue(resultado.isEmpty());
	}

	@Test
	void actualizarEstado_debeActualizarYGuardarElNuevoEstado() {
		Pedido pedido = new Pedido();
		pedido.setId(1L);
		pedido.setEstado("PAGADO");
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		Pedido resultado = service.actualizarEstado(1L, "ENVIADO");

		assertEquals("ENVIADO", resultado.getEstado());
		verify(pedidoRepository).save(pedido);
	}

	@Test
	void actualizarEstado_debeLanzarExcepcionSiPedidoNoExiste() {
		when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> service.actualizarEstado(99L, "ENVIADO"));
		verify(pedidoRepository, never()).save(any());
	}

	@Test
	void eliminarPedido_debeEliminarCuandoExiste() {
		Pedido pedido = new Pedido();
		pedido.setId(1L);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

		service.eliminarPedido(1L);

		verify(pedidoRepository).delete(pedido);
	}

	@Test
	void eliminarPedido_debeLanzarExcepcionSiPedidoNoExiste() {
		when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> service.eliminarPedido(99L));
		verify(pedidoRepository, never()).delete(any());
	}
}