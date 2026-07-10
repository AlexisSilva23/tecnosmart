package duoc.usuarios.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import duoc.usuarios.model.Usuario;
import duoc.usuarios.dto.UsuarioRequestDTO;
import duoc.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController // esta clase manejará peticiones HTTP y devolverá JSON
@RequestMapping("/api/usuarios")// @RequestMapping define la (URL) para todos los endpoints de este archivo
public class UsuarioController {
    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    // @Autowired inyecta el servicio para delegarle la lógica de negocio
    @Autowired
    private UsuarioService usuarioService;

    // ejemplo de testing
    @GetMapping("/test")
    public String test() {
        log.info("Se ha recibido una petición en el endpoint de prueba /test");
        return "El Microservicio de Usuarios está funcionando correctamente.";
    }

    @PostMapping
    // @RequestBody convierte el JSON al DTO y @Valid activa las validaciones del DTO
    public ResponseEntity<EntityModel<Usuario>> registrarUsuario(@Valid @RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        // llama al servicio para que haga el registro
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuarioRequestDTO);
        // retorna el usuario creado con un código HTTP 201 (Created) + enlaces HATEOAS
        EntityModel<Usuario> resource = toModel(nuevoUsuario);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @GetMapping
    // lista a todos los usuarios guardados
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listarUsuarios() {
        List<EntityModel<Usuario>> usuarios = usuarioService.listarUsuarios().stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Usuario>> collection = CollectionModel.of(usuarios,
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{id}")
    // busca al usuario por su ID
    public ResponseEntity<EntityModel<Usuario>> buscarUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(toModel(usuario));
    }

    @PutMapping("/{id}")
    // actualiza al usuario usando su ID
    public ResponseEntity<EntityModel<Usuario>> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO usuarioRequestDTO) {
        Usuario actualizado = usuarioService.actualizarUsuario(id, usuarioRequestDTO);
        return ResponseEntity.ok(toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    // elimina al usuario junto a su ID
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // retorna 204 no content
    }

    // método auxiliar: arma el EntityModel con sus enlaces HATEOAS para no repetir código
    private EntityModel<Usuario> toModel(Usuario usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarUsuarioPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("todos-los-usuarios"),
                linkTo(methodOn(UsuarioController.class).actualizarUsuario(usuario.getId(), null)).withRel("actualizar"),
                linkTo(methodOn(UsuarioController.class).eliminarUsuario(usuario.getId())).withRel("eliminar")
        );
    }
}

/* Explicación: Controller gestiona el protocolo HTTP extrayendo la información del JSON y
validando que sea correcto para pasar a 'Service' o ser rechazado, este tiene 4 métodos:
POST(create), GET(read), PUT(update) y DELETE (CRUD) */