package duoc.notificaciones.config;

import duoc.notificaciones.model.ModelNotificacion;
import duoc.notificaciones.repository.RepositoryNotificacion;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final RepositoryNotificacion notificacionRepository;

    public DataLoader(RepositoryNotificacion notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    private static final int TOTAL_USUARIOS = 15;
    private static final String[] TIPOS = {"EMAIL", "SMS", "PUSH"};

    @Override
    public void run(String... args) {
        if (notificacionRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker();

        for (int i = 0; i < 15; i++) {
            ModelNotificacion notificacion = new ModelNotificacion();
            notificacion.setUsuarioDestino("usuario" + faker.number().numberBetween(1, TOTAL_USUARIOS + 1));
            notificacion.setMensaje(faker.lorem().sentence(8));
            notificacion.setTipoNotificacion(TIPOS[faker.number().numberBetween(0, TIPOS.length)]);
            notificacionRepository.save(notificacion);
        }

        System.out.println("Datafaker: 15 notificaciones de prueba generadas.");
    }
}