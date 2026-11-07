# agrocontrol-backend — Capítulo 03

Primer backend real de AgroControl con Spring Boot, Maven y Java 21. Todavía no usa JPA ni se conecta a PostgreSQL.

## Ficha del capítulo
| Dato | Respuesta |
|---|---|
| Nombre del sistema | AgroControl |
| Nombre del backend | `agrocontrol-backend` |
| Package base | `com.agrocontrol` |
| Entidad padre 1:N | Predio (tabla `predio`) |
| Entidad dependiente N | Parcela (tabla `parcela`) |
| Relación | 1 predio tiene muchas parcelas (FK `parcela.predio_id`) |

## Cómo se creó
IntelliJ IDEA → New Project → Spring Boot, con Maven, Java 21, Jar, Group `com.agrocontrol`, Artifact `agrocontrol-backend`, Package name `com.agrocontrol`. Dependencias: solo **Spring Web** y **Validation**. No se agregó Spring Data JPA ni el driver de PostgreSQL.

## Estructura de packages
```text
com.agrocontrol
├── AgrocontrolBackendApplication.java
├── shared
│   ├── application
│   │   └── ProjectInfoService.java      ← primer Bean (@Service)
│   └── web
│       └── HealthController.java        ← GET /api/health
├── farm                                 ← módulo de Predio (entidad padre)
│   ├── domain
│   ├── application
│   │   ├── PredioDemoResponse.java      ← record de salida
│   │   └── PredioDemoService.java       ← Bean temporal
│   └── infrastructure
│       └── web
│           └── PredioDemoController.java ← GET /api/farms/demo
└── plot                                 ← módulo de Parcela (entidad dependiente)
    ├── domain
    ├── application
    └── infrastructure
        └── web
```

El código se organiza por módulo de negocio (`farm`, `plot`) y no con packages globales `controller`, `service` y `repository`. Las carpetas vacías tienen un `package-info.java` para que Git las conserve.

Se usa `/api/farms` (en inglés) para seguir el estilo de la API de la ficha: `/plots`, `/campaigns`, `/tasks`.

## Endpoints
| Método y URL | Respuesta |
|---|---|
| `GET /api/health` | `{"status":"OK","application":"agrocontrol-backend","project":"AgroControl","stage":"SPRING_BOOT_BASE","timestamp":"..."}` |
| `GET /api/farms/demo` | `{"id":1,"nombre":"San Isidro","ubicacion":"Km 12 carretera al norte","cantidadParcelas":2}` |

Las dos peticiones están escritas en `requests.http`. En la versión gratuita de IntelliJ el cliente HTTP necesita licencia, así que las pruebas se hicieron abriendo las mismas URL en el navegador, con la aplicación encendida:

- `http://localhost:8080/api/health` → HTTP 200 con `"status":"OK"`.
- `http://localhost:8080/api/farms/demo` → HTTP 200 con el predio San Isidro.

Las capturas de la consola (`Started AgrocontrolBackendApplication`, Tomcat en el puerto 8080) y de las dos respuestas son la evidencia del capítulo.

## Flujo de una petición
```text
Cliente HTTP (requests.http)
   ↓
GET /api/farms/demo
   ↓
Spring MVC busca el controller cuyo @RequestMapping + @GetMapping coincide
   ↓
PredioDemoController.demo()
   ↓
PredioDemoService.obtenerDemo()   (Bean creado e inyectado por Spring)
   ↓
PredioDemoResponse (record)
   ↓
Spring lo convierte a JSON → HTTP 200
```

## Conceptos
| Concepto | Explicación |
|---|---|
| Spring Boot | Framework que arma la aplicación con configuración automática y la ejecuta con un servidor web incluido (puerto 8080), sin instalar un servidor aparte. |
| Bean | Objeto que crea y administra Spring. En este proyecto: `ProjectInfoService`, `PredioDemoService` y los controllers. |
| IoC (inversión de control) | Nosotros no creamos los objetos con `new`; Spring los crea y decide cuándo. El control pasa al framework. |
| DI (inyección de dependencias) | Spring entrega a cada clase los Beans que necesita. `HealthController` recibe `ProjectInfoService` ya creado. |
| `@RestController` | Marca una clase que atiende peticiones HTTP y devuelve datos (JSON), no páginas. |
| `@Service` | Marca una clase de lógica de aplicación para que Spring la convierta en Bean. |
| Inyección por constructor | El Bean llega como parámetro del constructor y se guarda en un atributo `final`. La dependencia queda explícita y no puede faltar. |
| `@GetMapping` | Indica qué método responde a una petición GET en una ruta. |
| `@SpringBootApplication` | Marca la clase principal: activa la configuración automática y busca Beans en `com.agrocontrol` y sus subpackages. |

## Por qué todavía no usamos PostgreSQL
Primero hay que entender cómo Spring crea los Beans y atiende peticiones HTTP. La base `agrocontrol` ya existe; la conexión llegará con Spring Data JPA en un capítulo posterior. El script de la base está guardado en `src/main/resources/db/migration/V1__init_core.sql`, la carpeta que usará Flyway.

## Cómo evolucionará hacia hexagonal
`PredioDemoService` se reemplazará por casos de uso en `farm/application`, que dependerán de puertos (interfaces) en `farm/domain`, como `PredioRepository` del laboratorio. La implementación con PostgreSQL irá en `farm/infrastructure/persistence`. El controller seguirá en `farm/infrastructure/web`.
