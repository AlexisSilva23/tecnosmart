package duoc.despacho.config;

import duoc.despacho.model.Despacho;
import duoc.despacho.repository.DespachoRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;

@Component
public class DataLoader implements CommandLineRunner {

    private final DespachoRepository despachoRepository;

    public DataLoader(DespachoRepository despachoRepository) {
        this.despachoRepository = despachoRepository;
    }

    private static final int TOTAL_PEDIDOS = 10;
    private static final String[] ESTADOS = {"PENDIENTE", "EN_RUTA", "ENTREGADO"};

    @Override
    public void run(String... args) {
        if (despachoRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker(new Locale("es"));

        for (int i = 0; i < TOTAL_PEDIDOS; i++) {
            Despacho despacho = Despacho.builder()
                    .pedidoId((long) (i + 1))
                    .direccion(faker.address().streetAddress())
                    .comuna(faker.address().city())
                    .estado(ESTADOS[faker.number().numberBetween(0, ESTADOS.length)])
                    .fechaProgramada(LocalDate.now().plusDays(faker.number().numberBetween(1, 10)))
                    .build();
            despachoRepository.save(despacho);
        }

        System.out.println("Datafaker: " + TOTAL_PEDIDOS + " despachos de prueba generados.");
    }
}
