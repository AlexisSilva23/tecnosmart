package duoc.pedidos.config;

import duoc.pedidos.model.Pedido;
import duoc.pedidos.model.PedidoItem;
import duoc.pedidos.repository.PedidoRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final PedidoRepository pedidoRepository;

    public DataLoader(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // rango coherente con lo sembrado en usuarios-service (15) y catálogo-service (20 productos)
    private static final int TOTAL_USUARIOS = 15;
    private static final int TOTAL_PRODUCTOS = 20;

    // AJUSTAR si los valores reales de estado en tu código son distintos
    private static final String[] ESTADOS = {"PENDIENTE", "PAGADO", "ENVIADO", "COMPLETADO"};

    @Override
    public void run(String... args) {
        if (pedidoRepository.count() > 0) {
            return; // evita duplicar datos si ya hay registros
        }

        Faker faker = new Faker();

        for (int i = 0; i < 10; i++) {
            Pedido pedido = new Pedido();
            pedido.setUsuarioId((long) faker.number().numberBetween(1, TOTAL_USUARIOS + 1));
            pedido.setFecha(LocalDateTime.now().minusDays(faker.number().numberBetween(0, 30)));
            pedido.setEstado(ESTADOS[faker.number().numberBetween(0, ESTADOS.length)]);

            List<PedidoItem> items = new ArrayList<>();
            int cantidadItems = faker.number().numberBetween(1, 4);
            double total = 0.0;

            for (int j = 0; j < cantidadItems; j++) {
                PedidoItem item = new PedidoItem();
                item.setProductoId((long) faker.number().numberBetween(1, TOTAL_PRODUCTOS + 1));
                item.setCantidad(faker.number().numberBetween(1, 5));
                item.setPrecioUnitario(faker.number().randomDouble(2, 5000, 800000));
                item.setPedido(pedido); // necesario para que la FK pedido_id se guarde bien
                total += item.getCantidad() * item.getPrecioUnitario();
                items.add(item);
            }

            pedido.setItems(items);
            pedido.setTotal(total);

            pedidoRepository.save(pedido); // cascade ALL guarda los items automáticamente
        }

        System.out.println("Datafaker: 10 pedidos de prueba generados.");
    }
}