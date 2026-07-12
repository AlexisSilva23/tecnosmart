package duoc.inventario.config;

import duoc.inventario.model.Inventario;
import duoc.inventario.repository.InventarioRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final InventarioRepository inventarioRepository;

    public DataLoader(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    // coherente con los 20 productos sembrados en catalogo-service
    private static final int TOTAL_PRODUCTOS = 20;

    @Override
    public void run(String... args) {
        if (inventarioRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker();

        // un registro por cada producto, ya que productoId es UNIQUE
        for (long productoId = 1; productoId <= TOTAL_PRODUCTOS; productoId++) {
            Inventario inventario = new Inventario();
            inventario.setProductoId(productoId);
            inventario.setCantidad(faker.number().numberBetween(0, 100));
            inventarioRepository.save(inventario);
        }

        System.out.println("Datafaker: " + TOTAL_PRODUCTOS + " registros de inventario generados.");
    }
}