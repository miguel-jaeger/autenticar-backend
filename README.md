# autenticar-backend

# Migración a FireBase
1 Agregar en el fichero POM
<dependency>
  <groupId>com.google.cloud</groupId>
  <artifactId>spring-cloud-gcp-data-firestore</artifactId>
  <version>6.2.3</version> <!-- o la versión compatible con tu Spring Boot -->
</dependency>

Paso 2: Configurar Firebase en tu Proyecto

2.1 Obtén las credenciales de Firebase:
Ve a Firebase Console
Selecciona o crea tu proyecto
Ve a Configuración del proyecto → Cuentas de servicio
Genera una nueva clave privada (descarga el archivo JSON)

2.2 Coloca el archivo JSON en tu proyecto:
Guárdalo en src/main/resources/ con nombre firebase-service-account.json
Importante: Agrega este archivo a .gitignore para no subirlo a GitHub

Paso 3: Modificar application.properties
Elimina o comenta las configuraciones de SQLite/JPA:
firebase.enabled=true

