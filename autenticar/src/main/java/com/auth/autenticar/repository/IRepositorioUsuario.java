package com.auth.autenticar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.autenticar.model.ModeloUsuario;


@Repository
public interface IRepositorioUsuario extends JpaRepository<ModeloUsuario, Long> {
    ModeloUsuario findByCorreo(String correo);
}
