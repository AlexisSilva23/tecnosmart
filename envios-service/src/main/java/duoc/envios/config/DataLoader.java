package duoc.envios.config;

import duoc.envios.model.ModelEnvio;
import duoc.envios.repository.RepositoryEnvio;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DataLoader implements CommandLineRunner {

    private final RepositoryEnvio envioRepository;

    public DataLoader(RepositoryEnvio envioRepository) {
        this.envioRepository = envioRepository;
    }

    private static final String[] EMPRESAS = {"Chilexpress", "Starken", "Correos de Chile", "Blue Express"};
    private static final String[] ESTADOS = {"En preparación", "Despachado", "En tránsito", "Entregado"};

    @Override
    public void run(String... args) {
        if (envioRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker(new Locale("es"));

        for (int i = 0; i < 15; i++) {
            ModelEnvio envio = new ModelEnvio();
            envio.setDestinatario(faker.name().fullName());
            envio.setDireccionDestino(faker.address().fullAddress());
            envio.setEmpresaTransporte(EMPRESAS[faker.number().numberBetween(0, EMPRESAS.length)]);
            envio.setEstado(ESTADOS[faker.number().numberBetween(0, ESTADOS.length)]);
            envioRepository.save(envio);
        }

        System.out.println("Datafaker: 15 envíos de prueba generados.");
    }
}