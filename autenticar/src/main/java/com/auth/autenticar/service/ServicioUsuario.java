package com.auth.autenticar.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth.autenticar.model.ModeloUsuario;
import com.auth.autenticar.repository.IRepositorioUsuario;



@Service
public class ServicioUsuario {
    
    @Autowired
    IRepositorioUsuario repositorioUsuario;

    public ArrayList<ModeloUsuario> listarUsuarios() {
        return (ArrayList<ModeloUsuario>) repositorioUsuario.findAll();
    }

    public ModeloUsuario guardarUsuario(ModeloUsuario usuario) {
        return repositorioUsuario.save(usuario);
    }

    
    public ModeloUsuario actualizarUsuario(ModeloUsuario usuario) {
        return repositorioUsuario.save(usuario);
    }

    public void eliminarUsuario(ModeloUsuario usuario) {
         repositorioUsuario.deleteById(usuario.getIdPersona());
    }
}

