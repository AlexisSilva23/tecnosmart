package duoc.carrito.config;

import duoc.carrito.model.CarritoItem;
import duoc.carrito.repository.CarritoRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final CarritoRepository carritoRepository;

    public DataLoader(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    // rangos coherentes con lo sembrado en usuarios-service (15 usuarios) y catálogo-service (20 productos)
    private static final int TOTAL_USUARIOS = 15;
    private static final int TOTAL_PRODUCTOS = 20;

    @Override
    public void run(String... args) {
        if (carritoRepository.count() > 0) {
            return; // evita duplicar datos si ya hay registros
        }

        Faker faker = new Faker();

        for (int i = 0; i < 15; i++) {
            CarritoItem item = new CarritoItem();
            item.setUsuarioId((long) faker.number().numberBetween(1, TOTAL_USUARIOS + 1));
            item.setProductoId((long) faker.number().numberBetween(1, TOTAL_PRODUCTOS + 1));
            item.setCantidad(faker.number().numberBetween(1, 5));
            carritoRepository.save(item);
        }

        System.out.println("Datafaker: 15 items de carrito de prueba generados.");
    }
}