package duoc.pagos.config;

import duoc.pagos.model.Pago;
import duoc.pagos.repository.PagoRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final PagoRepository pagoRepository;

    public DataLoader(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    // coherente con los 10 pedidos sembrados en pedidos-service
    private static final int TOTAL_PEDIDOS = 10;
    private static final String[] METODOS = {"TARJETA_CREDITO", "TARJETA_DEBITO", "TRANSFERENCIA", "WEBPAY"};
    private static final String[] ESTADOS = {"PENDIENTE", "APROBADO", "RECHAZADO"};

    @Override
    public void run(String... args) {
        if (pagoRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker();

        for (int i = 0; i < TOTAL_PEDIDOS; i++) {
            Pago pago = Pago.builder()
                    .pedidoId((long) (i + 1))
                    .metodo(METODOS[faker.number().numberBetween(0, METODOS.length)])
                    .monto(faker.number().numberBetween(5000, 800000))
                    .estado(ESTADOS[faker.number().numberBetween(0, ESTADOS.length)])
                    .fechaPago(LocalDateTime.now().minusDays(faker.number().numberBetween(0, 30)))
                    .build();
            pagoRepository.save(pago);
        }

        System.out.println("Datafaker: " + TOTAL_PEDIDOS + " pagos de prueba generados.");
    }
}
