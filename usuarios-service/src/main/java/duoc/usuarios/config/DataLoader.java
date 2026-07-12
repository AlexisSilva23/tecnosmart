package duoc.usuarios.config;

import duoc.usuarios.model.Usuario;
import duoc.usuarios.repository.UsuarioRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public DataLoader(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker(new Locale("es"));

        for (int i = 0; i < 15; i++) {
            Usuario usuario = new Usuario();
            usuario.setNombre(faker.name().fullName());
            usuario.setEmail(faker.internet().emailAddress());
            usuario.setPassword(faker.internet().password(8, 16));
            usuario.setDireccion(faker.address().fullAddress());
            usuarioRepository.save(usuario);
        }

        System.out.println("Datafaker: 15 usuarios de prueba generados.");
    }
}