package com.auth.autenticar.repository;

import com.auth.autenticar.model.ModeloUsuarioFireBase;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class IRepositorioUsuarioFireBase {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "usuario";

    public IRepositorioUsuarioFireBase(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Guardar o actualizar un usuario
     */
    public ModeloUsuarioFireBase save(ModeloUsuarioFireBase usuario) throws ExecutionException, InterruptedException {
        // Si no tiene ID, generar uno nuevo
        if (usuario.getIdPersona() == null || usuario.getIdPersona().isEmpty()) {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            usuario.setIdPersona(docRef.getId());
        }
        
        // Guardar en Firestore
        firestore.collection(COLLECTION_NAME)
                .document(usuario.getIdPersona())
                .set(usuario)
                .get();
        
        return usuario;
    }

    /**
     * Buscar usuario por ID
     */
    public Optional<ModeloUsuarioFireBase> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        
        if (document.exists()) {
            ModeloUsuarioFireBase usuario = document.toObject(ModeloUsuarioFireBase.class);
            if (usuario != null) {
                usuario.setIdPersona(document.getId());
            }
            return Optional.ofNullable(usuario);
        }
        return Optional.empty();
    }

    /**
     * Buscar todos los usuarios
     */
    public List<ModeloUsuarioFireBase> findAll() throws ExecutionException, InterruptedException {
        List<ModeloUsuarioFireBase> usuarios = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        for (QueryDocumentSnapshot document : documents) {
            ModeloUsuarioFireBase usuario = document.toObject(ModeloUsuarioFireBase.class);
            usuario.setIdPersona(document.getId());
            usuarios.add(usuario);
        }
        
        return usuarios;
    }

    /**
     * Buscar usuario por correo (importante para autenticación)
     */
    public Optional<ModeloUsuarioFireBase> findByCorreo(String correo) throws ExecutionException, InterruptedException {
        Query query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("correo", correo)
                .limit(1);
        
        QuerySnapshot querySnapshot = query.get().get();
        
        if (!querySnapshot.isEmpty()) {
            QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
            ModeloUsuarioFireBase usuario = document.toObject(ModeloUsuarioFireBase.class);
            usuario.setIdPersona(document.getId());
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    /**
     * Buscar usuarios por nombre
     */
    public List<ModeloUsuarioFireBase> findByNombre(String nombre) throws ExecutionException, InterruptedException {
        List<ModeloUsuarioFireBase> usuarios = new ArrayList<>();
        Query query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("nombre", nombre);
        
        QuerySnapshot querySnapshot = query.get().get();
        
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            ModeloUsuarioFireBase usuario = document.toObject(ModeloUsuarioFireBase.class);
            usuario.setIdPersona(document.getId());
            usuarios.add(usuario);
        }
        
        return usuarios;
    }

    /**
     * Buscar usuarios por rol
     */
    public List<ModeloUsuarioFireBase> findByRol(String rol) throws ExecutionException, InterruptedException {
        List<ModeloUsuarioFireBase> usuarios = new ArrayList<>();
        Query query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("rol", rol);
        
        QuerySnapshot querySnapshot = query.get().get();
        
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            ModeloUsuarioFireBase usuario = document.toObject(ModeloUsuarioFireBase.class);
            usuario.setIdPersona(document.getId());
            usuarios.add(usuario);
        }
        
        return usuarios;
    }

    /**
     * Verificar si existe un usuario por correo
     */
    public boolean existsByCorreo(String correo) throws ExecutionException, InterruptedException {
        Query query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("correo", correo)
                .limit(1);
        
        QuerySnapshot querySnapshot = query.get().get();
        return !querySnapshot.isEmpty();
    }

    /**
     * Verificar si existe un usuario por ID
     */
    public boolean existsById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .get();
        return document.exists();
    }

    /**
     * Eliminar usuario por ID
     */
    public void deleteById(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .get();
    }

    /**
     * Eliminar un usuario
     */
    public void delete(ModeloUsuarioFireBase usuario) throws ExecutionException, InterruptedException {
        if (usuario.getIdPersona() != null) {
            deleteById(usuario.getIdPersona());
        }
    }

    /**
     * Actualizar solo campos específicos de un usuario
     */
    public void updateFields(String id, java.util.Map<String, Object> updates) 
            throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_NAME)
                .document(id)
                .update(updates)
                .get();
    }

    /**
     * Contar total de usuarios
     */
    public long count() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        return future.get().size();
    }

    /**
     * Buscar usuarios con paginación
     */
    public List<ModeloUsuarioFireBase> findAllPaginated(int pageSize, DocumentSnapshot lastDocument) 
            throws ExecutionException, InterruptedException {
        List<ModeloUsuarioFireBase> usuarios = new ArrayList<>();
        Query query = firestore.collection(COLLECTION_NAME).limit(pageSize);
        
        if (lastDocument != null) {
            query = query.startAfter(lastDocument);
        }
        
        QuerySnapshot querySnapshot = query.get().get();
        
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            ModeloUsuarioFireBase usuario = document.toObject(ModeloUsuarioFireBase.class);
            usuario.setIdPersona(document.getId());
            usuarios.add(usuario);
        }
        
        return usuarios;
    }
}