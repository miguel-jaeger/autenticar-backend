package com.auth.autenticar.service;

import com.auth.autenticar.model.ModeloUsuarioFireBase;
import com.auth.autenticar.repository.IRepositorioUsuarioFireBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final IRepositorioUsuarioFireBase usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(IRepositorioUsuarioFireBase usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registrar un nuevo usuario
     */
    public ModeloUsuarioFireBase registrarUsuario(ModeloUsuarioFireBase usuario) {
        try {
            // Validar que el correo no exista
            if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
                throw new RuntimeException("El correo ya está registrado");
            }

            // Encriptar contraseña
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

            // Asignar rol por defecto si no tiene
            if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
                usuario.setRol("USER");
            }

            // Guardar usuario
            ModeloUsuarioFireBase usuarioGuardado = usuarioRepository.save(usuario);
            logger.info("Usuario registrado exitosamente: {}", usuarioGuardado.getCorreo());

            return usuarioGuardado;

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al registrar usuario: {}", e.getMessage());
            throw new RuntimeException("Error al registrar usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Autenticar usuario (login)
     */
    public Optional<ModeloUsuarioFireBase> autenticar(String correo, String contrasena) {
        try {
            Optional<ModeloUsuarioFireBase> usuarioOpt = usuarioRepository.findByCorreo(correo);

            if (usuarioOpt.isPresent()) {
                ModeloUsuarioFireBase usuario = usuarioOpt.get();

                // Verificar contraseña
                if (passwordEncoder.matches(contrasena, usuario.getContrasena())) {
                    logger.info("Usuario autenticado: {}", correo);
                    return Optional.of(usuario);
                } else {
                    logger.warn("Contraseña incorrecta para: {}", correo);
                }
            } else {
                logger.warn("Usuario no encontrado: {}", correo);
            }

            return Optional.empty();

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al autenticar usuario: {}", e.getMessage());
            throw new RuntimeException("Error al autenticar: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener usuario por ID
     */
    public Optional<ModeloUsuarioFireBase> obtenerUsuarioPorId(String id) {
        try {
            return usuarioRepository.findById(id);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al buscar usuario por ID: {}", e.getMessage());
            throw new RuntimeException("Error al buscar usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener usuario por correo
     */
    public Optional<ModeloUsuarioFireBase> obtenerUsuarioPorCorreo(String correo) {
        try {
            return usuarioRepository.findByCorreo(correo);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al buscar usuario por correo: {}", e.getMessage());
            throw new RuntimeException("Error al buscar usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Listar todos los usuarios
     */
    public List<ModeloUsuarioFireBase> obtenerTodosLosUsuarios() {
        try {
            return usuarioRepository.findAll();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al listar usuarios: {}", e.getMessage());
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener usuarios por rol
     */
    public List<ModeloUsuarioFireBase> obtenerUsuariosPorRol(String rol) {
        try {
            return usuarioRepository.findByRol(rol);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al buscar usuarios por rol: {}", e.getMessage());
            throw new RuntimeException("Error al buscar usuarios: " + e.getMessage(), e);
        }
    }

    /**
     * Actualizar usuario
     */
    public ModeloUsuarioFireBase actualizarUsuario(String id, ModeloUsuarioFireBase usuarioActualizado) {
        try {
            // Verificar que el usuario existe
            Optional<ModeloUsuarioFireBase> usuarioExistente = usuarioRepository.findById(id);

            if (usuarioExistente.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado con ID: " + id);
            }

            // Mantener el ID original
            usuarioActualizado.setIdPersona(id);

            // Si se está actualizando la contraseña, encriptarla
            if (usuarioActualizado.getContrasena() != null &&
                !usuarioActualizado.getContrasena().isEmpty()) {
                usuarioActualizado.setContrasena(
                        passwordEncoder.encode(usuarioActualizado.getContrasena())
                );
            } else {
                // Mantener la contraseña anterior si no se proporciona una nueva
                usuarioActualizado.setContrasena(usuarioExistente.get().getContrasena());
            }

            ModeloUsuarioFireBase usuarioGuardado = usuarioRepository.save(usuarioActualizado);
            logger.info("Usuario actualizado: {}", id);

            return usuarioGuardado;

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al actualizar usuario: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Cambiar contraseña
     */
    public void cambiarContrasena(String id, String contrasenaActual, String contrasenaNueva) {
        try {
            Optional<ModeloUsuarioFireBase> usuarioOpt = usuarioRepository.findById(id);

            if (usuarioOpt.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            ModeloUsuarioFireBase usuario = usuarioOpt.get();

            // Verificar contraseña actual
            if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
                throw new RuntimeException("Contraseña actual incorrecta");
            }

            // Actualizar con nueva contraseña encriptada
            usuario.setContrasena(passwordEncoder.encode(contrasenaNueva));
            usuarioRepository.save(usuario);

            logger.info("Contraseña cambiada para usuario: {}", id);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al cambiar contraseña: {}", e.getMessage());
            throw new RuntimeException("Error al cambiar contraseña: " + e.getMessage(), e);
        }
    }

    /**
     * Eliminar usuario
     */
    public void eliminarUsuario(String id) {
        try {
            if (!usuarioRepository.existsById(id)) {
                throw new RuntimeException("Usuario no encontrado con ID: " + id);
            }

            usuarioRepository.deleteById(id);
            logger.info("Usuario eliminado: {}", id);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al eliminar usuario: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Verificar si existe un usuario por correo
     */
    public boolean existeCorreo(String correo) {
        try {
            return usuarioRepository.existsByCorreo(correo);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al verificar correo: {}", e.getMessage());
            throw new RuntimeException("Error al verificar correo: " + e.getMessage(), e);
        }
    }

    /**
     * Contar total de usuarios
     */
    public long contarUsuarios() {
        try {
            return usuarioRepository.count();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al contar usuarios: {}", e.getMessage());
            throw new RuntimeException("Error al contar usuarios: " + e.getMessage(), e);
        }
    }
}
