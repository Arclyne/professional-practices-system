# Estándar de Codificación

## Introducción

Este documento establece las directrices de codificación para el
proyecto de la experiencia educativa de
**Principios de Construcción de Software**.
El objetivo es asegurar que el código sea profesional, legible y fácil
de mantener por cualquier miembro del equipo.

## Propósito

Este estándar define una colección de prácticas esenciales para el
desarrollo en JAVA/J2EE. Seguir estas reglas no solo mejora la
eficiencia, sino que también demuestra la proficiencia y
profesionalismo del equipo de desarrollo. El enfoque principal es la
mantenibilidad y la claridad a primera vista.

## Reglas de Nombrado

El nombrado debe ser consistente y utilizar palabras descriptivas en
inglés para evitar ambigüedades, asegurando que cada elemento sea
autoexplicativo.

### Variables

Se utiliza **lowerCamelCase** (empezar con minúscula). Se debe
evitar el uso de nombres de un solo carácter, a menos que sea el
contador de un ciclo simple. En ciclos anidados, los contadores deben
tener nombres descriptivos. Los nombres de colecciones deben ser en
plural. No se deben declarar dos variables en la misma línea. Para
booleanos se utilizan prefijos que planteen una pregunta clara
(**is, has, can, should**).

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Cadena de texto | `String customerName;` | `String n, nombre;` |
| Lista o Colección | `List<Order> userOrders;` | `List<Order> orderList;` |
| Valor Booleano | `boolean isActive;` | `boolean seActivo;` |
| Objeto de Dominio | `Practitioner activePractitioner;` | `Practitioner p;` |

### Métodos

El nombre debe describir el propósito del método en
**lowerCamelCase**. Un método debe recibir como máximo **3 parámetros**;
de requerirse más, deben encapsularse en un objeto DTO.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Acción (Cálculo) | `double calculateTotal(double price)` | `double Totales(double p)` |
| Validación Booleana | `boolean isPersistent()` | `boolean getCheck()` |
| Acceso a Datos | `Practitioner getPractitioner(int id)` | `Practitioner fetch(int i)` |

### Constantes

Se utiliza **SCREAMING_SNAKE_CASE** (todo en mayúsculas con
guiones bajos). Deben declararse obligatoriamente con los modificadores
`static final`. Esta convención aplica a todos los campos `static final`,
incluyendo instancias de `Logger`.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Valor Numérico | `static final float VALUE_PI = 3.14f;` | `static final int aceleración = 10;` |
| Cadena de Texto | `static final String DEFAULT_ROLE = "ADMIN";` | `static final String rol = "ADMIN";` |

### Clases

Se utiliza **PascalCase** (empezar con mayúscula). Deben ser
sustantivos en singular. Se debe evitar el uso de acrónimos a menos
que sean más conocidos que la palabra completa
(ej. **URL**, **HTML**).

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Lógica General | `class DataValidator` | `class Dt_valid` |
| Patrón de Diseño | `class VisitorDAO` | `class visitorDt` |
| Entidad de Negocio | `class ProjectAssignment` | `class Assignments` |

### Interfaces

Se utilizan adjetivos descriptivos terminados en **-able** o
**-ible** para indicar capacidades. Es válido usar sustantivos
cuando la interfaz representa un rol específico en la arquitectura.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Comportamiento | `interface Serializable` | `interface CanSerialize` |
| Rol o Entidad | `interface UserRepository` | `interface IUser` |

### Variables No Utilizadas

Dado que el proyecto compila en **Java 25**, queda estrictamente
prohibido usar nombres genéricos para variables que la sintaxis obliga
a declarar pero que no se utilizan. Se debe utilizar la
**variable sin nombre (*unnamed variable*)** `_`.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Captura de Excepción | `catch (SQLException _)` | `catch (SQLException ignored)` |
| Expresión Lambda | `(_, response) -> process(response)` | `(a, response) -> process(response)` |

### Componentes de Interfaz Gráfica (JavaFX)

Todas las variables inyectadas desde un archivo `.fxml` con
`@FXML` deben ser `private` e incluir un sufijo que indique
explícitamente su tipo de control en **camelCase**.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Botón | `private Button saveButton;` | `public Button btnSave;` |
| Campo de Texto | `private TextField nameTextField;` | `private TextField txtName;` |
| Etiqueta | `private Label errorLabel;` | `private Label lblError;` |
| Tabla | `private TableView practitionersTableView;` | `private TableView tabla;` |

**Componentes JavaFX inyectados con @FXML**

```java
public class PractitionerController {

    @FXML
    private TextField enrollmentTextField;

    @FXML
    private Button saveButton;
}
```

### Estructuras de Datos (Clases vs Records)

La elección entre `class` y `record` depende de la
mutabilidad del objeto. Entidades mutables se declaran como
`class`; estados inmutables o eventos como `record`.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Transferencia (Mutable) | `public class Practitioner {}` | `public record Practitioner {}` |
| Evento (Inmutable) | `public record AdminResult(boolean exists) {}` | `public class AdminResult {}` |

### Identidad de los Objetos

La identidad debe definirse mediante una
**Clave de Negocio (*Business Key*)**. Queda prohibido
generar `equals()` y `hashCode()` usando todos los atributos
o únicamente el `id` autoincrementable.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Identificador Único | `String enrollment`, `String email` | `int id`, `String name`, `String password` |
| Identificador Compuesto | `String projectCode` | `int id`, `String description` |

### Valores Mágicos

Queda estrictamente prohibido el uso de valores numéricos o cadenas
literales directamente en la lógica. Todo valor constante debe
gestionarse mediante enumeradores (`enum`) o constantes de clase.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Estado Finito | `if (status == Status.ASSIGNED)` | `if (status.equals("Assigned"))` |
| Límite Numérico | `if (grade > MINIMUM_GRADE)` | `if (grade > 7.0)` |

**Constante de clase en lugar de valor mágico literal**

```java
public class AssignmentManager {

    private static final double MINIMUM_GRADE = 7.0;

    public boolean validateAssignment(Practitioner practitioner) {
        boolean isValid = false;

        if (practitioner.getGrade() >= MINIMUM_GRADE) {
            practitioner.setStatus(UserStatus.ACTIVE);
            isValid = true;
        }

        return isValid;
    }
}
```

### Consultas SQL

Las consultas deben aislarse como constantes de clase con el prefijo
`SQL_`. Queda prohibido instanciar cadenas SQL directamente
dentro del cuerpo de los métodos.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Inserción | `private static final String SQL_INSERT = "...";` | `String insertQuery = "...";` |
| Selección | `private static final String SQL_SELECT_ALL = "...";` | `String sqlSelect = "...";` |

**Constante SQL aislada; variable sin nombre en catch no referenciado**

```java
public class PractitionerDAO {

    private static final String SQL_INSERT =
            "INSERT INTO practitioner (name) VALUES (?)";

    public boolean insert(Practitioner practitioner) {
        boolean isInserted = false;

        try (PreparedStatement statement =
                connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, practitioner.getName());
            isInserted = statement.executeUpdate() > 0;
        } catch (SQLException _) {
            isInserted = false;
        }

        return isInserted;
    }
}
```

## Estilo de Codificación

### Indentación

Una indentación se compone de **4 espacios en blanco exactos**
por nivel.

**Indentación de 4 espacios por nivel**

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

### Espaciado

Se utiliza un espacio entre palabras reservadas y el paréntesis. Se
deja **2 línea en blanco** para separar los imports de la definición de
la clase y para separar métodos entre sí. Se deja un espacio entre
operadores binarios.

**Espaciado entre imports, definición de clase y métodos**

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

### Profundidad de Anidamiento

No se permite anidar más de **3 niveles de profundidad**
(`if`, `for`, `try`). Se deben combinar condiciones
lógicas en una sola línea o extraer fragmentos a métodos privados.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Validación Múltiple | `if (isActive && hasFunds)` — 1 nivel | `if (isActive) { if (hasFunds) {` — 2 niveles |
| Bloque Complejo | Extraer lógica a un método `private` | `try` dentro de `for` dentro de `if` |

**Lógica compleja extraída a método privado**

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
        throw new ServiceException("Transaction failed", e);
    }
}
```

### Punto de Salida (SESE)

El código debe seguir el principio de
**Entrada Única, Salida Única (SESE)**. Todo método que devuelva
un valor debe tener **un solo `return`** al final.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Puntos de Retorno | Una única variable retornada al final | Múltiples `return` incrustados en condiciones `if` |

**Único punto de retorno al final del método**

```java
public boolean validateUser(User user) {
    boolean isValid = false;

    if (user.getAge() >= MINIMUM_AGE) {
        isValid = true;
    }

    return isValid;
}
```

### Operaciones Booleanas

Las expresiones booleanas deben ser directas y libres de operaciones
redundantes. Queda estrictamente prohibido comparar una variable o
expresión booleana con los literales `true` o `false` mediante
el operador `==` o `!=`. El valor booleano se evalúa directamente
en la condición.

Cuando el valor de una variable booleana se deriva íntegramente de
una expresión en una sola instrucción, queda prohibido usar un
bloque `if/else` auxiliar que asigne `true` o `false` de
forma literal; la expresión se asigna directamente a la variable.
Del mismo modo, queda prohibido usar el operador ternario
exclusivamente para devolver o asignar uno de estos dos literales.

Queda prohibida la **doble negación**. Si una condición requiere
negarse, el nombre de la variable debe reformularse con un prefijo
positivo para evitar `!isNotActive` o construcciones equivalentes.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Condición afirmativa | `if (isActive)` | `if (isActive == true)` |
| Condición negativa | `if (!isActive)` | `if (isActive == false)` |
| Ternario redundante | `boolean isEligible = grade >= MINIMUM_GRADE;` | `boolean isEligible = grade >= MINIMUM_GRADE ? true : false;` |
| Asignación desde expresión | `boolean hasEnrollment = enrollment != null;` | `if (enrollment != null) { hasEnrollment = true; } else { hasEnrollment = false; }` |
| Doble negación | `if (!isActive)` | `if (!isNotActive)` |

**Asignación directa y condición sin comparación redundante**

```java
public class EligibilityValidator {

    private static final double MINIMUM_GRADE = 7.0;

    public boolean isPractitionerEligible(Practitioner practitioner) {
        boolean hasValidGrade = practitioner.getGrade() >= MINIMUM_GRADE;
        boolean hasEnrollment = practitioner.getEnrollment() != null;

        boolean isEligible = hasValidGrade && hasEnrollment;

        return isEligible;
    }
}
```

**Antipatrón: comparaciones redundantes y ternario innecesario**

```java
public class EligibilityValidator {

    public boolean isPractitionerEligible(Practitioner practitioner) {
        boolean hasValidGrade = practitioner.getGrade() >= 7.0
                ? true : false;
        boolean hasEnrollment = practitioner.getEnrollment() != null
                ? true : false;
        boolean isEligible = false;

        if (hasValidGrade == true && hasEnrollment == true) {
            isEligible = true;
        } else {
            isEligible = false;
        }

        return isEligible;
    }
}
```

### Estructuras de Control

Es obligatorio el uso de llaves `{}` para todos los bloques,
incluso para una sola instrucción. Los bloques `if/else` comparten
la línea de la llave de cierre. La declaración `switch` debe
incluir siempre un caso `default`.

### Reutilización de Código

Se debe cumplir el principio **DRY (*Don't Repeat Yourself*)**.
Queda prohibido duplicar bloques de código. Si una lógica se repite en
dos o más métodos, debe extraerse a un método privado genérico.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Lógica Repetida | Invocación de un método privado centralizado | Copiar y pegar el mismo bloque de código |

### Mapeo de Datos

En la capa de persistencia, la lectura de un `ResultSet` hacia un
DTO nunca debe realizarse dentro del método de consulta principal.
Debe encapsularse en un método privado.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Mapeo de Entidades | `user = mapResultSetToUser(resultSet);` | `user.setName(resultSet.getString("name"));` incrustado en `getAll` |

**Mapeo de ResultSet delegado a método privado**

```java
public User getUser(int targetId) {
    User user = new User();

    try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
            user = mapResultSetToUser(resultSet);
        }
    } catch (SQLException e) {
        throw new ServiceException("Query failed", e);
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

### Expresiones Lambda

El uso de expresiones Lambda (`->`) está permitido exclusivamente
para operaciones concisas de una sola línea. Queda estrictamente
prohibido usar Lambdas para bloques que requieran llaves `{}`.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Filtro Simple | `list.removeIf(u -> !u.isActive());` | `list.removeIf(u -> { return !u.isActive(); });` |

### Referencias a Métodos

Se debe preferir el uso de Referencias a Métodos (`::`) sobre
Expresiones Lambda siempre que sea posible, ya que favorecen un código
más declarativo y fácil de depurar.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Invocación Directa | `list.forEach(this::processUser);` | `list.forEach(u -> processUser(u));` |

**Referencia a método como alternativa preferida a lambda**

```java
public List<Practitioner> filterActivePractitioners(
        List<Practitioner> practitioners) {
    return practitioners.stream()
            .filter(Practitioner::isActive)
            .toList();
}
```

## Estructura de Archivos

Cada archivo fuente debe contener exactamente una clase
principal (*top-level class*) y sus elementos deben ordenarse
estrictamente así:

1. Información de Licencia (si aplica).
1. Declaración del paquete (`package`).
1. Importaciones (`import`):
  - Queda estrictamente **prohibido el uso de comodines**
(`import java.util.*`).
  - Primero importaciones estáticas (`import static ...`),
luego las normales (`import ...`), separadas por una
línea en blanco.

1. Javadoc a nivel de clase (si aplica).
1. Definición de la clase.
1. Variables estáticas (`static`).
1. Variables de instancia.
1. Constructores.
1. Métodos.

**Estructura completa de un archivo fuente Java**

```java
package mx.uv.fei.logic;

import static java.util.Collections.unmodifiableList;

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

    public List<String> getUserNames() {
        return unmodifiableList(userNames);
    }
}
```

## Documentación Javadoc

El uso de Javadoc (`/** ... */`) está reservado
**únicamente** para clases DAO. Está estrictamente prohibido
generar Javadocs para métodos autoexplicativos como *getters*,
*setters* o métodos marcados con `@Override`.

### A Nivel de Clase

El bloque debe contener:
1. **Resumen de Responsabilidad:** descripción clara de lo
que hace o representa la clase.
1. **`@author`:** obligatorio para rastrear
responsabilidad del código.
1. **`@version`** (opcional): versión o fecha de la
iteración.

| Etiqueta | Descripción | Ejemplo de uso |
|---|---|---|
| `@author` | Nombre(s) de los desarrolladores responsables. | `@author José Eduardo Prior Hernández` |
| `@version` | Versión actual del componente. | `@version 1.0` |

### A Nivel de Método

Cuando un método requiera Javadoc, debe cumplir:
1. **Resumen breve:** fragmento corto terminado en punto.
1. **Párrafos adicionales** separados por línea en blanco
con asterisco alineado.
1. **Orden de etiquetas:** `@param`, `@return`,
`@throws`.

**Javadoc de método con etiquetas en orden correcto**

```java
public class TaxCalculator {

    private static final double TAX_RATE = 1.16;

    /**
     * Procesa y calcula los impuestos tributarios a partir del monto de
     * facturación.
     *
     * El factor de multiplicación estático está justificado por el ticket
     * de deuda técnica #405 debido a limitaciones de la API externa.
     *
     * @param baseAmount El monto de facturación neto (sin IVA)
     * @return El total calculado incluyendo el impuesto retenido
     * @throws IllegalArgumentException si el monto base es negativo
     */
    public double calculateTax(double baseAmount) throws IllegalArgumentException {
        if (baseAmount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        double finalTax = baseAmount * TAX_RATE;

        return finalTax;
    }
}
```

## Comentarios Internos

El código debe ser estrictamente autoexplicativo a través de la firma
de los métodos y el nombrado descriptivo de las variables. Queda
prohibido incluir comentarios (`//`) que expliquen
*qué* hace el código; solo se permite comentar el
**por qué** en escenarios de alta complejidad.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Justificación | `// Se divide entre 16 por la ley fiscal 2026` | `// Divide el valor entre 16` |

## Manejo de Excepciones

### Convenciones

En los bloques `catch`, la variable de excepción debe nombrarse
siempre **`e`** cuando sea referenciada en el cuerpo del bloque
(para registrar o relanzar la excepción). Cuando la excepción no se
referencia dentro del bloque `catch`, se aplica la variable sin nombre
`_` conforme a la regla de Variables No Utilizadas.

### Excepciones Encadenadas

Al relanzar una excepción personalizada, es obligatorio pasar la
excepción original como causa en el constructor para preservar el
*stack trace*.

### Restricciones

Está estrictamente prohibido usar `Exception`,
`RuntimeException` o `Throwable` genéricos.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Captura referenciada | `catch (NumberFormatException e)` | `catch (Exception e)` |
| Captura no referenciada | `catch (SQLException _)` | `catch (SQLException ignored)` |
| Lanzamiento (Throw) | `throw new ServiceException("Error", e);` | `throw new Exception("Error");` |
| Firma de Método | `public void connect() throws SQLException` | `public void connect() throws Exception` |

**Excepción específica encadenada con la causa original**

```java
public void establishConnection() throws ServiceException {
    try {
        database.connect();
    } catch (SQLException e) {
        throw new ServiceException("Connection failed", e);
    }
}
```

## Inyección de Dependencias

El proyecto utiliza un módulo propio en
`mx.uv.fei.config.annotation.etiquette`.

- **`@Component`:** a nivel de clase para declarar
la gestión por el contenedor.
- **`@Inject`:** preferentemente en el constructor
para inyectar dependencias.
- **`@Provide`:** a nivel de método en clases de
configuración para instanciar objetos complejos o interfaces.

## Bitácoras

Queda estrictamente prohibido el uso de `System.out.println()`.
Todo registro debe usar el Logger del proyecto respetando los niveles
`INFO`, `WARN` y `ERROR`.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Flujo de Negocio | `LOG.info("Practitioner registered");` | `System.out.println("Practitioner registered");` |
| Evento Anómalo | `LOG.warn("Missing parameter");` | `System.err.println("Warning: parameter");` |
| Fallo Crítico | `LOG.error("Database failure", e);` | `e.printStackTrace();` |

**Logger con niveles correctos y excepción encadenada**

```java
public class PractitionerDAO {

    private static final Logger LOG =
            LoggerFactory.getLogger(PractitionerDAO.class);

    public void savePractitioner(Practitioner practitioner)
            throws DAOException {
        try {
            database.insert(practitioner);
        } catch (SQLException e) {
            LOG.error("Insertion failed", e);
            throw new DAOException("Storage error", e);
        }
    }
}
```

## Pruebas Unitarias

Las pruebas deben ser independientes y autoexplicativas, sirviendo
como documentación viva.

### Nomenclatura de las Clases de Prueba

Debe incluir obligatoriamente el sufijo **Test** para ser
reconocida por Maven.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Clase DAO | `class PractitionerDAOTest` | `class TestPractitionerDAO` |
| Clase Reducer | `class AuthenticatorReducerTest` | `class ReducerTest` |

### Nomenclatura de Métodos de Prueba

Se utiliza el estándar
`nombreDelMetodo_EstadoBajoPrueba_ComportamientoEsperado`.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Inserción Exitosa | `insert_ValidPractitioner_ReturnsGeneratedId()` | `insertValidPractitionerReturnsId()` |
| Validación Fallida | `login_InvalidPassword_ReturnsFalse()` | `testLoginWrongPassword()` |
| Excepción | `recover_NullId_ThrowsDAOException()` | `testRecoverError()` |

### Estructura Interna (Patrón AAA)

Se debe separar lógicamente en tres bloques (Arrange, Act, Assert)
utilizando únicamente **líneas en blanco** entre cada bloque.

**Prueba unitaria con patrón AAA y nomenclatura estándar**

```java
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PractitionerDAOTest {

    @Test
    public void insert_ValidPractitioner_ReturnsGeneratedId() {
        PractitionerDAO dao =
                new PractitionerDAO(new DatabaseConnectionMock());
        Practitioner validPractitioner = new Practitioner();
        validPractitioner.setName("Angel");
        validPractitioner.setGrade(9.5);

        int resultId = dao.insertPractitioner(validPractitioner);

        assertTrue(resultId > 0, "ID must be positive");
    }
}
```

## Módulos Independientes

Para garantizar que los submódulos de infraestructura sean testeables
y escalables, se rigen por el **Agnosticismo de Dominio**.

- **Aislamiento Físico:** cada herramienta reside en su
propio submódulo con su `pom.xml`.
- **Agnosticismo Estricto:** prohibido importar DTOs, DAOs
o Entidades del negocio principal.
- **Uso de Genéricos:** las interfaces deben operar sobre
tipos genéricos.

| Concepto | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|
| Parámetros | `interface Reducer<S, A>` | `interface Reducer<User, Action>` |
| Retornos | `Object getInstance(Class<?> type)` | `PractitionerDAO getDAO()` |
| Paquetería | `mx.uv.fei.etiquette.core.*` | `mx.uv.fei.domain.etiquette.*` |

**Interfaz genérica agnóstica del dominio**

```java
package mx.uv.fei.infrastructure.core;

public interface StateReducer<State, Action> {

    State reduce(State currentState, Action targetAction);
}
```
