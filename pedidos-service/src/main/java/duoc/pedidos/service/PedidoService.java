package duoc.pedidos.service;

import duoc.pedidos.client.CarritoClient;
import duoc.pedidos.client.InventarioClient;
import duoc.pedidos.client.ProductoClient;
import duoc.pedidos.dto.CarritoItemDTO;
import duoc.pedidos.dto.ProductoResponseDTO;
import duoc.pedidos.model.Pedido;
import duoc.pedidos.model.PedidoItem;
import duoc.pedidos.repository.PedidoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PedidoService {
    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private InventarioClient inventarioClient;

    @Autowired
    private CarritoClient carritoClient;

    @Autowired
    private ProductoClient productoClient;

    @Transactional
    public Pedido procesarCompra(Long usuarioId) {
        log.info("Iniciando proceso de compra para el usuario ID: {}", usuarioId);

        List<CarritoItemDTO> productosEnCarrito = carritoClient.obtenerCarrito(usuarioId);
        if (productosEnCarrito.isEmpty()) {
            log.warn("El carrito del usuario {} está vacío", usuarioId);
            throw new IllegalStateException("No hay productos en el carrito para procesar");
        }

        for (CarritoItemDTO item : productosEnCarrito) {
            boolean hayStock = inventarioClient.validarStock(item.getProductoId(), item.getCantidad());
            if (!hayStock) {
                log.error("Stock insuficiente para el producto ID: {}", item.getProductoId());
                throw new IllegalStateException("Stock insuficiente para el producto: " + item.getProductoId());
            }
        }

        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuarioId(usuarioId);
        nuevoPedido.setFecha(LocalDateTime.now());
        nuevoPedido.setEstado("PAGADO");

        List<PedidoItem> items = new ArrayList<>();
        double total = 0.0;

        for (CarritoItemDTO carritoItem : productosEnCarrito) {
            ProductoResponseDTO producto = productoClient.obtenerProducto(carritoItem.getProductoId());

            PedidoItem pedidoItem = new PedidoItem();
            pedidoItem.setProductoId(carritoItem.getProductoId());
            pedidoItem.setCantidad(carritoItem.getCantidad());
            pedidoItem.setPrecioUnitario(producto.getPrecio());
            pedidoItem.setPedido(nuevoPedido);
            items.add(pedidoItem);

            total += producto.getPrecio() * carritoItem.getCantidad();
        }
        nuevoPedido.setItems(items);
        nuevoPedido.setTotal(total);

        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);
        log.info("Pedido guardado con éxito, ID: {}", pedidoGuardado.getId());

        log.info("Vaciando carrito del usuario {}...", usuarioId);
        carritoClient.vaciarCarrito(usuarioId);

        return pedidoGuardado;
    }

    public List<Pedido> listarTodosLosPedidos() {
        log.info("Consultando historial completo de pedidos");
        return pedidoRepository.findAll();
    }

    public Pedido buscarPorId(Long id) {
        log.info("Buscando pedido con ID: {}", id);
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado con ID: " + id));
    }

    public List<Pedido> listarPorUsuario(Long usuarioId) {
        log.info("Consultando pedidos del usuario ID: {}", usuarioId);
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public Pedido actualizarEstado(Long id, String nuevoEstado) {
        log.info("Actualizando estado del pedido ID {} a {}", id, nuevoEstado);
        Pedido pedido = buscarPorId(id);
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    public void eliminarPedido(Long id) {
        log.warn("Eliminando pedido con ID: {}", id);
        Pedido pedido = buscarPorId(id);
        pedidoRepository.delete(pedido);
    }
}


/* Explicación: comunica todos los servicios entre sí y se asegura de que si algo falla,
no se cobre ni cree el pedido */