# Sistema de Gestión de Prácticas Profesionales (SPP)

Aplicación de escritorio para la gestión integral de prácticas profesionales de la Facultad de Estadística e Informática (FEI) de la UV. Administra el flujo completo: registro de practicantes, asignación de empresas, seguimiento de reportes, evaluaciones y generación de documentos.

---

## Tabla de contenidos

- [Requisitos previos](#requisitos-previos)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Configuración](#configuración)
  - [Base de datos](#base-de-datos)
  - [Correo electrónico](#correo-electrónico)
- [Perfiles de inyección de dependencias](#perfiles-de-inyección-de-dependencias)
- [Instalación y compilación](#instalación-y-compilación)
- [Cómo correr la aplicación](#cómo-correr-la-aplicación)
- [Cómo correr las pruebas](#cómo-correr-las-pruebas)
- [Roles de usuario](#roles-de-usuario)

---

## Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| JDK | 25 |
| Maven | 3.9+ |
| MySQL | 8.0+ (para perfil `local`) |

> Para el perfil `net` no se requiere MySQL local; la base de datos está alojada en DigitalOcean.

---

## Estructura del proyecto

El proyecto es multi-módulo Maven:

```
professional-practices-system/
├── app/                    # Módulo principal: UI (JavaFX) + lógica de negocio
│   └── src/main/
│       ├── java/mx/uv/fei/
│       │   ├── app/                  # Punto de entrada (MainApplication)
│       │   ├── appconfiguration/     # Configuración de DI y base de datos
│       │   ├── dataaccess/           # DAOs, interfaces, conexión a BD
│       │   ├── domain/               # DTOs, managers, state machine, validadores
│       │   └── presentation/         # Controladores JavaFX por rol
│       └── resources/
│           ├── database.properties   # Configuración multi-perfil de BD
│           ├── mail.properties       # Configuración SMTP
│           └── mx/uv/fei/presentation/  # Archivos FXML (49 vistas)
├── etiquette-core/         # Framework de inyección de dependencias propio
├── etiquette-test/         # Utilidades de prueba para Etiquette
├── statemachine-core/      # Máquina de estados para el flujo de prácticas
├── database/               # Respaldo SQL de la base de datos
└── documents/              # Estándares de codificación
```

---

## Configuración

### Base de datos

Edita `app/src/main/resources/database.properties`. El archivo tiene una sección por perfil:

```properties
# Perfil: net (producción — base de datos remota, requiere SSL)
net.db.url=jdbc:mysql://<host>:<puerto>/professional-practices-system?sslMode=REQUIRED
net.db.user=spp_app_user
net.db.password=<contraseña>

# Perfil: ssh (túnel SSH al servidor remoto)
ssh.db.url=jdbc:mysql://localhost:3307/profesionalPractice
ssh.db.user=spp_app_user
ssh.db.password=<contraseña>

# Perfil: local (MySQL local estándar)
local.db.url=jdbc:mysql://localhost:3306/professional-practices-system
local.db.user=spp_app_user
local.db.password=<contraseña>

# Perfil: test (H2 en memoria, modo MySQL — solo pruebas automáticas)
test.db.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;...
test.db.user=sa
test.db.password=
```

Para el perfil `local`, importa el respaldo incluido:

```bash
mysql -u root -p < database/<archivo>.sql
```

### Correo electrónico

Edita `app/src/main/resources/mail.properties`:

```properties
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.host=smtp.office365.com
mail.smtp.port=587
mail.system.email=correo_institucional@uv.mx
mail.system.password=contraseña_de_aplicación
```

---

## Perfiles de inyección de dependencias

La aplicación usa **Etiquette**, un framework de DI propio basado en anotaciones. El perfil activo determina qué implementación de `IDatabaseConnection` se inyecta.

### Perfiles disponibles

| Perfil | Descripción | Cuándo usarlo |
|---|---|---|
| `net` | Base de datos remota en la nube con SSL | Producción o cualquier servidor remoto accesible por red |
| `ssh` | Túnel SSH al servidor, BD en `localhost:3307` | Acceso remoto sin exponer el puerto directo |
| `local` | MySQL local en `localhost:3306` | Desarrollo local |
| `local2` | Variante local alternativa | Segundo entorno de desarrollo |
| `test` | H2 en memoria modo MySQL | Pruebas automatizadas (se activa automáticamente) |

### Cómo se selecciona el perfil

El perfil se declara con la anotación `@Profile` en `MainApplication`:

```java
@StartEtiquette
@Profile("net")          // <-- cambia este valor
public class MainApplication extends Application { ... }
```

`DatabaseConnectionConfiguration` lee el perfil activo de `ApplicationConfiguration` y carga el prefijo correspondiente de `database.properties` (`net.db.*`, `local.db.*`, etc.).

Para cambiar de perfil sin recompilar, modifica el valor de `@Profile` en el código fuente antes de compilar, o pasa la propiedad por línea de comandos (ver sección siguiente).

---

## Instalación y compilación

```bash
# Clonar el repositorio
git clone <url-del-repo>
cd professional-practices-system

# Compilar todos los módulos
mvn clean install
```

---

## Cómo correr la aplicación

```bash
# Con el perfil predeterminado definido en @Profile (recomendado)
mvn -pl app javafx:run

# Sobreescribir el perfil desde la línea de comandos
mvn -pl app javafx:run -Dprofile=local
mvn -pl app javafx:run -Dprofile=ssh
mvn -pl app javafx:run -Dprofile=net
```

La ventana principal se abre en **450 × 600 px** con el título _"Sistema de Gestión de Prácticas Profesionales - FEI"_.

---

## Cómo correr las pruebas

```bash
# Todas las pruebas (usa automáticamente el perfil test con H2)
mvn test

# Solo un módulo
mvn -pl app test
```

---

## Roles de usuario

| Rol | Acceso principal |
|---|---|
| Administrador | Alta de coordinadores, configuración general |
| Coordinador | Gestión de periodos, empresas, practicantes, plantillas y reportes |
| Profesor | Evaluación y calificación de practicantes |
| Practicante | Envío de reportes, bitácoras y documentos propios |
