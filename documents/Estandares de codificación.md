<div style="text-align: center; font-family: 'Times New Roman', serif; margin-top: 50px;">
  <div style="font-size: 32px;">UNIVERSIDAD VERACRUZANA</div>
<div style="font-size: 20px; font-weight: bold; margin-top: 25px;">Facultad de Estadística e Informática</div>
<div style="margin-top: 60px; margin-bottom: 60px;">
    <img src="logo.png" alt="Logo UV" width="160" style="display: block; margin: 0 auto;">
</div>

<div style="font-size: 30px;">Estándar de Codificación</div>
<div style="font-size: 18px; font-weight: bold; margin-top: 25px;">Proyecto de Principios de Construcción de Software</div>

<div style="font-size: 16px; margin-top: 80px;">
    <b>Integrantes:</b><br>
    Angel Gabriel Aguilar Hernandez<br>
    José Eduardo Prior Hernández
</div>

<div style="font-size: 16px; margin-top: 60px;">
    <b>Fecha:</b><br>
    10 de marzo de 2026
</div>

<div style="page-break-after: always;"></div>

# Índice

[TOC]

# 1. Introducción

Este documento establece las directrices de codificación para el proyecto de la experiencia educativa de **Principios de Contrucción de Software.** El objetivo es asegurar que sea profesional, legible y fácil de mantener por cualquier miembro del equipo.

# 2. Propósito

Este estándar define una colección de prácticas esenciales para el desarrollo en JAVA/J2EE. Seguir estas reglas no solo mejora la eficiencia, sino que también demuestra la proficiencia y profesionalismo del equipo de desarrollo. El enfoque principal es la mantenibilidad y la claridad a primera vista.

# 3. Reglas de Nombrado

El nombrado debe ser consistente y utilizar palabras descriptivas en inglés para evitar ambigüedades, asegurando que cada elemento sea autoexplicativo.

## 3.1 Variables

Se utiliza **lowerCamelCase** (empezar con minúscula). Se debe evitar el uso de nombres de un solo carácter, a menos que sea el contador de un ciclo simple. En ciclos anidados, los contadores deben tener nombres descriptivos. Los nombres de colecciones deben ser en plural. No se deben declarar dos variables en la misma línea. Para booleanos se utilizan prefijos que planteen una pregunta clara (**is, has, can, should**).

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Cadena de texto | `String customerName;` | `String n, nombre;` |
| Lista o Colección | `List<Order> userOrders;` | `List<Order> orderList;` |
| Valor Booleano | `boolean isActive;` | `boolean seActivo;` |
| Objeto de Dominio | `Practitioner activePractitioner;` | `Practitioner p;` |

## 3.2 Métodos

El nombre debe describir el propósito del método en **lowerCamelCase**. Un método debe recibir entre 2 y 3 parámetros como máximo; de requerirse más, deben encapsularse en un objeto DTO.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Acción (Cálculo) | `double calculateTotal(double price)` | `double Totales(double p)` |
| Validación Booleana | `boolean isPersistent()` | `boolean getCheck()` |
| Acceso a Datos | `Practitioner getPractitioner(int id)` | `Practitioner fetch(int i)` |

## 3.3 Constantes

Se utiliza **SCREAMING_SNAKE_CASE** (todo en mayúsculas con guiones bajos). Deben declararse obligatoriamente utilizando los modificadores `static final`.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Valor Numérico | `static final float VALUE_PI = 3.14f;` | `static final int aceleración = 10;` |
| Cadena de Texto | `static final String DEFAULT_ROLE = "ADMIN";` | `static final String rol = "ADMIN";` |

## 3.4 Clases

Se utiliza **PascalCase** (empezar con mayúscula). Deben ser sustantivos en singular. Se debe evitar el uso de acrónimos a menos que sean más conocidos que la palabra compleja (ej. **URL, HTML**).

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Lógica General | `class DataValidator` | `class Dt_valid` |
| Patrón de Diseño | `class VisitorDAO` | `class visitorDt` |
| Entidad de Negocio | `class ProjectAssignment` | `class Assignments` |

## 3.5 Interfaces

Se utilizan adjetivos descriptivos terminados en **-able o -ible** para indicar capacidades. Es válido usar sustantivos cuando la interfaz representa un rol específico en la arquitectura.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Comportamiento | `interface Serializable` | `interface CanSerialize` |
| Rol o Entidad | `interface UserRepository` | `interface IUser` |

## 3.6 Variables No Utilizadas

Dado que el proyecto compila en **Java 25**, queda estrictamente prohibido usar nombres genéricos para variables que la sintaxis obliga a declarar pero que no se utilizan. Se debe utilizar la **variable sin nombre (unnamed variable)** `_`.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Captura de Excepción | `catch (SQLException _)` | `catch (SQLException ignored)` |
| Expresión Lambda | `( _, response) -> process(response)` | `(a, response) -> process(response)` |

## 3.7 Componentes de Interfaz Gráfica (JavaFX)

Todas las variables inyectadas desde un archivo `.fxml` con `@FXML` deben ser privadas e incluir un sufijo que indique explícitamente su tipo de control en **camelCase**.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Botón | `private Button saveButton;` | `public Button btnSave;` |
| Campo de Texto | `private TextField nameTextField;` | `private TextField txtName;` |
| Etiqueta | `private Label errorLabel;` | `private Label lblError;` |
| Tabla | `private TableView practitionersTableView;`| `private TableView tabla;` |

```java
public class PractitionerController {

    @FXML
    private TextField enrollmentTextField;

    @FXML
    private Button saveButton;
}
```

## 3.8 Estructuras de Datos (Clases vs Records)

La elección entre utilizar una clase o un record dependerá de la mutabilidad del objeto. Entidades mutables deben declararse como `class`. Estados inmutables o eventos deben declararse como `record`.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Transferencia (Mutable) | `public class Practitioner { }` | `public record Practitioner { }` |
| Evento (Inmutable) | `public record AdminResult(boolean exists) {}`| `public class AdminResult { }` |

## 3.9 Identidad de los Objetos

La identidad debe definirse estrictamente a través de una **Clave de Negocio (Business Key)**. Queda prohibido generar `equals()` y `hashCode()` utilizando todos los atributos o únicamente el `id` autoincrementable.

| Concepto | Ejemplo Correcto (Atributos) | Ejemplo Incorrecto (Atributos) |
| :--- | :--- | :--- |
| Identificador Único | `String enrollment`, `String email` | `int id`, `String name`, `String password` |
| Identificador Compuesto | `String projectCode` | `int id`, `String description` |

## 3.10 Valores Mágicos

Queda estrictamente prohibido el uso de valores numéricos o cadenas literales directamente en la lógica. Todo valor constante debe ser gestionado mediante enumeradores (`enum`) o constantes de clase.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Estado Finito | `if (status == Status.ASSIGNED)` | `if (status.equals("Assigned"))` |
| Límite Numérico | `if (grade > MINIMUM_GRADE)` | `if (grade > 7.0)` |

```java
public class AssignmentManager {
    
    private static final double MINIMUM_GRADE_REQUIRED = 7.0;

    public boolean validateAssignment(Practitioner practitioner) {
        boolean isValid = false;
        
        if (practitioner.getGrade() >= MINIMUM_GRADE_REQUIRED) {
            practitioner.setStatus(UserStatus.ACTIVE);
            isValid = true;
        }
        
        return isValid;
    }
}
```

## 3.11 Consultas SQL

Las consultas a la base de datos deben aislarse como constantes de clase con el prefijo `SQL_`. Queda prohibido instanciar cadenas SQL directamente dentro del cuerpo de los métodos.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Inserción | `private static final String SQL_INSERT = "...";` | `String insertQuery = "...";` |
| Selección | `private static final String SQL_SELECT_ALL = "...";`| `String sqlSelect = "...";` |

```java
public class PractitionerDAO {
    
    private static final String SQL_INSERT = "INSERT INTO practitioner (name) VALUES (?)";

    public boolean insert(Practitioner practitioner) {
        boolean isInserted = false;
        
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, practitioner.getName());
            isInserted = statement.executeUpdate() > 0;
        } catch (SQLException _) {
            isInserted = false;
        }
        
        return isInserted;
    }
}
```

# 4. Estilo de Codificación

## 4.1 Indentación

Una indentación se compone de 4 espacios en blanco exactos por nivel.

```java
public class Calculator {
    
    public int add(int parameterA, int parameterB) {
        int result = parameterB;
        
        if (parameterA > 0) {
            result = parameterA + parameterB;
        }
        
        return result;
    }
}
```

## 4.2 Espaciado

Se utiliza un espacio en blanco entre palabras reservadas y el paréntesis. Se dejan 2 líneas en blanco para separar los imports de la definición de la clase, y 1 línea para separar métodos. Se deja un espacio entre operadores binarios.

```java
import java.util.List;


public class UserSession {
    
    private String sessionId;
  
    public UserSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
```

## 4.3 Profundidad de Anidamiento

Para mantener la legibilidad, no se permite anidar más de **3 niveles de profundidad** (`if`, `for`, `try`). Se deben combinar condiciones lógicas en una sola línea o extraer fragmentos a métodos privados.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Validación Múltiple | `if (isActive && hasFunds)` (1 Nivel) | `if (isActive) { if (hasFunds) {` (2 Niveles) |
| Bloque Complejo | Extraer lógica a un método privado | Declarar un `try` dentro de un `for` dentro de un `if` |

```java
public void processPayment(User user) {
    if (user != null && user.isActive()) {
        executeTransaction(user); 
    }
}

private void executeTransaction(User user) {
    try {
        database.save(user);
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
```

## 4.4 Punto de Salida (SESE)

El código debe seguir el principio de **Entrada Única, Salida Única (SESE)**. Todo método que devuelva un valor debe tener **un solo `return`** al final de su ejecución.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Puntos de Retorno | Una única variable retornada al final | Múltiples `return` incrustados en condiciones `if` |

```java
public boolean validateUser(User user) {
    boolean isValid = false; 

    if (user.getAge() >= 18) {
        isValid = true; 
    }

    return isValid;
}
```

## 4.5 Estructuras de Control

Es obligatorio el uso de llaves `{}` para todos los bloques, incluso para una sola instrucción. Los bloques `if/else` comparten la línea de la llave de cierre. La declaración `switch` debe incluir siempre un caso `default`. 

## 4.6 Reutilización de Código

Se debe cumplir estrictamente el principio **DRY (Don't Repeat Yourself)**. Queda prohibido duplicar bloques de código. Si una lógica se repite en dos o más métodos, debe extraerse a un método privado genérico.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Lógica Repetida | Invocación de un método privado centralizado | Copiar y pegar el mismo bloque de 10 líneas de código |

## 4.7 Mapeo de Datos

En la capa de persistencia, la lectura y extracción de datos de un `ResultSet` hacia un objeto de transferencia (DTO) nunca debe realizarse dentro del método de consulta principal. Debe encapsularse siempre en un método privado.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Mapeo de Entidades | `user = mapResultSetToUser(resultSet);` | `user.setName(resultSet.getString("name"));` incrustado en el método `getAll` |

```java
public User getUser(int targetId) {
    User user = new User();
    
    try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
            user = mapResultSetToUser(resultSet); 
        }
    }
    
    return user;
}

private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
    User user = new User();
    user.setId(resultSet.getInt("user_id"));
    user.setName(resultSet.getString("name"));
    user.setEmail(resultSet.getString("email"));
    
    return user;
}
```

## 4.8 Expresiones Lambda

El uso de expresiones Lambda (`->`) está permitido exclusivamente para operaciones concisas y de una sola línea. Queda estrictamente prohibido utilizar Lambdas para bloques lógicos que requieran llaves `{ }`.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Filtro Simple | `list.removeIf(u -> u.getStatus() == 0);` | `list.removeIf(u -> { return u.getStatus() == 0; });` |

## 4.9 Referencias a Métodos

Siempre que sea posible, se debe preferir el uso de Referencias a Métodos (`::`) sobre las Expresiones Lambda, ya que favorecen un código más declarativo y fácil de depurar.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Invocación Directa | `list.forEach(this::processUser);` | `list.forEach(u -> processUser(u));` |

```java
public List<Practitioner> filterActivePractitioners(List<Practitioner> practitioners) {
    return practitioners.stream()
            .filter(Practitioner::isActive)
            .toList();
}
```

# 5. Estructura de Archivos

Cada archivo fuente debe contener **exactamente una clase principal (top-level class)** y sus elementos deben ordenarse estrictamente de la siguiente manera:

1. Información de Licencia (si aplica).
2. Declaración del paquete (`package`).
3. Importaciones (`import`): 
   * Queda estrictamente **prohibido el uso de comodines** (`import java.util.*`).
   * Los imports deben separarse en dos bloques: primero las importaciones estáticas (`import static ...`) y luego las normales (`import ...`), separados por una línea en blanco.
4. Javadoc a nivel de clase (si aplica).
5. Definición de la clase.
6. Variables estáticas (`static`).
7. Variables de instancia.
8. Constructores.
9. Métodos.

```java
package mx.uv.fei.logic;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el ciclo de vida y almacenamiento en memoria de los usuarios.
 * 
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public class UserManager {
    
    public static final int MAX_USERS = 100;

    private List<String> userNames;

    public UserManager() {
        this.userNames = new ArrayList<>();
    }

    public void addUser(String userName) { 
        userNames.add(userName); 
    }
}
```

# 6. Documentación Javadoc

El uso de Javadoc (`/** ... */`) está reservado **únicamente** para clases e interfaces de alta importancia, APIs públicas o contratos arquitectónicos. 

Está estrictamente prohibido generar Javadocs para métodos autoexplicativos como *getters*, *setters* o métodos que sobrescriben una interfaz (`@Override`).

## 6.1 A Nivel de Clase

Cuando se documenta la cabecera de una clase principal, el bloque debe contener:
1. **Resumen de Responsabilidad:** Una descripción clara de lo que hace o representa la clase.
2. **Etiqueta @author:** Obligatoria para rastrear la responsabilidad del código en el proyecto.
3. **Etiqueta @version (opcional):** Para indicar la versión o fecha de la iteración.

| Etiqueta | Descripción | Ejemplo de Uso |
| :--- | :--- | :--- |
| `@author` | Nombre(s) de los desarrolladores responsables. | `@author José Eduardo Prior Hernández` |
| `@version` | Versión actual del componente. | `@version 1.0` |

## 6.2 A Nivel de Método

Cuando un método requiera Javadoc por su complejidad, debe cumplir con las siguientes reglas de formato:
1. **Resumen breve:** El primer párrafo debe ser un fragmento resumen corto, no una oración completa, seguido de un punto (ej. "Calcula el impuesto base.", en lugar de "Este método calcula el impuesto base de la entidad.").
2. **Formato de Párrafos:** Si hay múltiples párrafos de explicación, se separan por una línea en blanco con un asterisco alineado.
3. **Orden de Etiquetas:** Las anotaciones de bloque obligatorias deben aparecer exactamente en este orden: `@param`, `@return`, `@throws`. 

```java
/**
 * Procesa y calcula los impuestos tributarios a partir del monto de facturación.
 *
 * El factor de multiplicación estático está justificado por el ticket de deuda 
 * técnica #405 debido a limitaciones de la API externa.
 *
 * @param baseAmount El monto de facturación neto (sin IVA)
 * @return El total calculado incluyendo el impuesto retenido
 * @throws IllegalArgumentException si el monto base es negativo
 */
public double calculateTax(double baseAmount) throws IllegalArgumentException {
    if (baseAmount < 0) {
        throw new IllegalArgumentException("Amount cannot be negative");
    }
    
    double finalTax = baseAmount * 1.16;
    return finalTax;
}
```

# 7. Comentarios Internos

El código debe ser estrictamente autoexplicativo a través de la firma de los métodos y el nombrado descriptivo de las variables. 

Queda estrictamente prohibido incluir comentarios simples (`//`) que expliquen "qué" hace el código. Solo se permite comentar en escenarios de alta complejidad, y el comentario debe explicar obligatoriamente el **"por qué"** (la justificación de negocio o motivo técnico), nunca el "qué".

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Justificación | `// Se divide entre 16 por la ley fiscal 2026` | `// Divide el valor entre 16` |

# 8. Manejo de Excepciones

## 8.1 Convenciones

En los bloques `catch`, la variable que representa la excepción debe nombrarse siempre como **`e`**. Esto se debe a que las capturas ocurren en un contexto encapsulado e inmediato; utilizar la letra `e` mantiene el código ágil, evitando alargar las líneas con la palabra completa.

## 8.2 Excepciones Encadenadas

Al relanzar una excepción personalizada, es obligatorio pasar la excepción original como causa en el constructor para no perder la traza del error (*stack trace*).

## 8.3 Restricciones

Está estrictamente prohibido usar `Exception`, `RuntimeException` o `Throwable` genéricos para captura o lanzamiento. 

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Captura (Catch) | `catch (NumberFormatException e)` | `catch (Exception e)` |
| Lanzamiento (Throw) | `throw new ServiceException("Error", e);` | `throw new Exception("Error");` |
| Firma de Método | `public void connect() throws SQLException` | `public void connect() throws Exception` |

```java
public void establishConnection() throws ServiceException {
    try {
        database.connect();
    } catch (SQLException e) {
        throw new ServiceException("Connection failed", e);
    }
}
```

# 9. Inyección de Dependencias

El proyecto utiliza un módulo propio ubicado en `mx.uv.fei.config.annotation.etiquette`.

* **@Component:** A nivel de clase para declarar la gestión por el contenedor.
* **@Inject:** Preferentemente en el constructor para inyectar dependencias.
* **@Provide:** A nivel de método en clases de configuración para instanciar objetos complejos o interfaces.

# 10. Bitácoras

Queda estrictamente prohibido el uso de `System.out.println()`. Todo registro debe usar el Logger del proyecto respetando los niveles `INFO`, `WARN` y `ERROR`.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Flujo de Negocio | `log.info("Practitioner registered");` | `System.out.println("Practitioner registered");` |
| Evento Anómalo | `log.warn("Missing parameter");` | `System.err.println("Warning: parameter");` |
| Fallo Crítico | `log.error("Database failure", e);` | `e.printStackTrace();` |

```java
public class PractitionerDAO {

    private static final Logger log = LoggerFactory.getLogger(PractitionerDAO.class);

    public void savePractitioner(Practitioner practitioner) throws DAOException {
        try {
            database.insert(practitioner);
        } catch (SQLException e) {
            log.error("Insertion failed", e);
            throw new DAOException("Storage error", e);
        }
    }
}
```

# 11. Pruebas Unitarias

Las pruebas deben ser independientes y autoexplicativas, sirviendo como documentación viva.

## 11.1 Nomenclatura de las Clases de Prueba

Debe incluir obligatoriamente el sufijo **Test** para ser reconocida por Maven.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Clase DAO | `class PractitionerDAOTest` | `class TestPractitionerDAO` |
| Clase Reducer | `class AuthenticatorReducerTest`| `class ReducerTest` |

## 11.2 Nomenclatura de Métodos de Prueba

Se utiliza el estándar `nombreDelMetodo_EstadoBajoPrueba_ComportamientoEsperado`.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Inserción Exitosa | `insert_ValidPractitioner_ReturnsId()` | `insertValidPractitionerReturnsId()` |
| Validación Fallida | `login_InvalidPassword_ReturnsFalse()` | `testLoginWrongPassword()` |
| Excepción | `recover_NullId_ThrowsDAOException()` | `testRecoverError()` |

## 11.3 Estructura Interna (Patrón AAA)

Se debe separar lógicamente en tres bloques (Arrange, Act, Assert) utilizando únicamente **líneas en blanco** entre cada bloque.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PractitionerDAOTest {

    @Test
    public void insert_ValidPractitioner_ReturnsGeneratedId() {
        PractitionerDAO dao = new PractitionerDAO(new DatabaseConnectionMock());
        Practitioner validPractitioner = new Practitioner();
        validPractitioner.setName("Angel");
        validPractitioner.setGrade(9.5);
        
        int resultId = dao.insertPractitioner(validPractitioner);
        
        assertTrue(resultId > 0, "ID must be positive");
    }
}
```

# 12. Módulos Independientes

Para garantizar que los submódulos de infraestructura sean testeables y escalables, se rigen por el **Agnosticismo de Dominio**.

* **Aislamiento Físico:** Cada herramienta reside en su propio submódulo con su `pom.xml`.
* **Agnosticismo Estricto:** Prohibido importar DTOs, DAOs o Entidades del negocio principal.
* **Uso de Genéricos:** Las interfaces deben operar sobre tipos genéricos `<T>`.

| Concepto | Ejemplo Correcto | Ejemplo Incorrecto |
| :--- | :--- | :--- |
| Parámetros | `interface Reducer<S, A>` | `interface Reducer<User, Action>` |
| Retornos | `Object getInstance(Class<?> type)` | `PractitionerDAO getDAO()` |
| Paquetería | `mx.uv.fei.etiquette.core.*` | `mx.uv.fei.domain.etiquette.*` |

```java
package mx.uv.fei.infrastructure.core;

public interface StateReducer<State, Action> {
    
    State reduce(State currentState, Action targetAction);
}
```
