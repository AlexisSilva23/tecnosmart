package duoc.catalogo.controller;

import duoc.catalogo.dto.CategoriaRequestDTO;
import duoc.catalogo.model.Categoria;
import duoc.catalogo.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/categorias") // ruta exclusiva para categorías
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // crea una categoria
    @PostMapping
    public ResponseEntity<EntityModel<Categoria>> crear(@Valid @RequestBody CategoriaRequestDTO dto) {
        Categoria nueva = categoriaService.guardarCategoria(dto);
        return new ResponseEntity<>(toModel(nueva), HttpStatus.CREATED);
    }

    // lista todas las categorías
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Categoria>>> listar() {
        List<EntityModel<Categoria>> categorias = categoriaService.listarLasCategorias().stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Categoria>> collection = CollectionModel.of(categorias,
                linkTo(methodOn(CategoriaController.class).listar()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    // busca una categoria por su ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> obtenerPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.buscarCategoriasPorId(id);
        return ResponseEntity.ok(toModel(categoria));
    }

    // método auxiliar: arma el EntityModel con sus enlaces HATEOAS
    private EntityModel<Categoria> toModel(Categoria categoria) {
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class).obtenerPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listar()).withRel("todas-las-categorias")
        );
    }
}