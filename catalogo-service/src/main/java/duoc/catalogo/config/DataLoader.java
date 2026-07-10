package duoc.catalogo.config;

import duoc.catalogo.model.Categoria;
import duoc.catalogo.model.Producto;
import duoc.catalogo.repository.CategoriaRepository;
import duoc.catalogo.repository.ProductoRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class DataLoader implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public DataLoader(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    private static final String[][] CATEGORIAS_BASE = {
            {"Notebooks", "Laptops y ultrabooks para trabajo y gaming"},
            {"Smartphones", "Celulares de última generación"},
            {"Accesorios", "Cables, cargadores, fundas y periféricos"},
            {"Audio", "Audífonos, parlantes y equipos de sonido"},
            {"Gaming", "Consolas, controles y accesorios gamer"},
            {"Smart Home", "Dispositivos inteligentes para el hogar"}
    };

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0 || productoRepository.count() > 0) {
            return;
        }

        Faker faker = new Faker(new Locale("es"));

        // 1. Crear categorías primero (Producto depende de Categoria por FK)
        List<Categoria> categorias = new java.util.ArrayList<>();
        for (String[] datos : CATEGORIAS_BASE) {
            Categoria categoria = new Categoria();
            categoria.setNombre(datos[0]);
            categoria.setDescripcion(datos[1]);
            categorias.add(categoriaRepository.save(categoria));
        }

        // 2. Crear productos con Datafaker, asignados aleatoriamente a una categoría existente
        for (int i = 0; i < 20; i++) {
            Producto producto = new Producto();
            producto.setNombre(faker.commerce().productName());
            producto.setDescripcion(faker.lorem().sentence(10));
            producto.setPrecio(faker.number().randomDouble(2, 5000, 800000)); // rango en pesos
            producto.setCategoria(categorias.get(faker.random().nextInt(categorias.size())));
            productoRepository.save(producto);
        }

        System.out.println("Datafaker: " + categorias.size() + " categorías y 20 productos de prueba generados.");
    }
}