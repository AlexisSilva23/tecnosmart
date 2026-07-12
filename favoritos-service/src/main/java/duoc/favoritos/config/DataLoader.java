package duoc.favoritos.config;

import duoc.favoritos.model.ModelFavorito;
import duoc.favoritos.repository.RepositoryFavorito;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final RepositoryFavorito favoritoRepository;

    public DataLoader(RepositoryFavorito favoritoRepository) {
        this.favoritoRepository = favoritoRepository;
    }

    private static final int TOTAL_USUARIOS = 15;
    private static final String[] TIPOS = {"Producto", "Categoria", "Empresa"};

    @Override
    public void run(String... args) {
        if (favoritoRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker();

        for (int i = 0; i < 15; i++) {
            ModelFavorito favorito = new ModelFavorito();
            favorito.setUsuario("usuario" + faker.number().numberBetween(1, TOTAL_USUARIOS + 1));
            favorito.setTipoItem(TIPOS[faker.number().numberBetween(0, TIPOS.length)]);
            favorito.setNombreItem(faker.commerce().productName());
            favoritoRepository.save(favorito);
        }

        System.out.println("Datafaker: 15 favoritos de prueba generados.");
    }
}
