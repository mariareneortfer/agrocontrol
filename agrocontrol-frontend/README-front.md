# agrocontrol-frontend

Frontend de **AgroControl** (proyecto 16) con **React + TypeScript + Vite**, creado y ejecutado desde **WebStorm**.
Consumirá más adelante la API REST del backend Spring Boot (`agrocontrol-backend`, puerto 8080).

## Guía 03 - Modelos TypeScript, datos simulados y tablas

Las páginas `/predios` y `/parcelas` ya muestran **tablas con datos simulados (mocks)** y tarjetas de estadísticas. Todavía **no** se llama al backend.

### Equivalencias con el ejemplo del profesor
| Profesor (ParkFlow360) | AgroControl |
|---|---|
| `clientes/models/Cliente.ts` | `predios/models/Predio.ts` |
| `vehiculos/models/Vehiculo.ts` | `parcelas/models/Parcela.ts` |
| `clientes.mock.ts` / `vehiculos.mock.ts` | `predios.mock.ts` / `parcelas.mock.ts` |
| `ClienteTable` / `VehiculoTable` | `PredioTable` / `ParcelaTable` |
| `Vehiculo.clienteId` (1:N) | `Parcela.predioId` (1:N) |
| `find()`: nombre del cliente | `find()`: nombre del predio |
| Placa resaltada | Código de parcela resaltado |
| Estado Activo / Inactivo (`activo: boolean`) | Ver la nota siguiente |

### Decisión: ¿por qué no hay campo `activo`?
La guía pide alinear los modelos con el **contrato real** de la API. En AgroControl, las tablas `predio` y `parcela` **no tienen** una columna `activo`, y `PredioResponse` y `ParcelaResponse` tampoco la devuelven. Si se inventara `activo` solo en el frontend, al conectar la API ese campo llegaría como `undefined`.

Por eso, el estado visual se calcula con datos que sí existen:
- **Predios:** badge verde **"Con parcelas"** o rojo **"Sin parcelas"**. Se calcula contando las parcelas del predio con `filter()`, usando la relación 1:N.
- **Parcelas:** si `ubicacionReferencial` es `null`, se muestra un badge rojo **"Sin ubicación"**.
- Las tarjetas de estadísticas usan `filter()` igual que "Activos / Inactivos": Con parcelas / Sin parcelas y Con ubicación / Sin ubicación.

### Modelos (alineados con el contrato de la API, no con la entidad JPA)
```ts
export interface Predio {
  id: number
  nombre: string
  ubicacion: string | null
  descripcion: string | null
}

export interface Parcela {
  id: number
  predioId: number            // relación 1:N con Predio
  codigo: string
  nombre: string
  superficie: number
  unidadSuperficie: string
  ubicacionReferencial: string | null
}
```
`string | null` indica que el dato es opcional en la base de datos (columnas sin `NOT NULL`).

La carpeta `types/` de cada feature (Guía 01) queda reservada para los tipos de formularios y requests (por ejemplo `CrearPredioRequest`, Guía 04). `models/` contiene lo que la API devuelve.

### Flujo de datos
```text
predios.mock.ts      → exporta Predio[]
PrediosPage.tsx      → calcula estadísticas con filter() y pasa predios={prediosMock} parcelas={parcelasMock}
PredioTable.tsx      → recibe Predio[] y Parcela[] por props
predios.map(...)     → cada Predio se convierte en un <tr key={predio.id}>
JSX → React actualiza el DOM → el navegador muestra la tabla
```
Para Parcelas el flujo es el mismo. La diferencia es que `ParcelaTable` usa `find()` sobre los predios para mostrar el **nombre** del predio en lugar del número `predioId`.

### Práctica obligatoria
1. **Dos predios nuevos:** uno con parcelas y otro sin parcelas (en AgroControl esto equivale a activo/inactivo).
2. **Tres parcelas nuevas** asociadas a predios existentes con `predioId`.
3. **Parcela con `predioId: 999`:** en la columna Predio aparece **"Sin predio asociado"**.
4. **Tipo incorrecto:** `superficie: '12.5'` (texto) en vez de `12.5`. WebStorm subraya en rojo y `npm run build` falla con `Type 'string' is not assignable to type 'number'`. Después se corrigió.
5. **Arreglo vacío:** `prediosMock = []` muestra "No hay predios registrados." y las estadísticas en 0. Después se restauró.
6. **Cambiar un código** (por ejemplo `P-02` → `P-02A`): solo cambia esa fila, porque React identifica cada fila por su `key` (`parcela.id`).
7. **`npm run build`** sin errores.

> Nota de seguridad: PostgreSQL no permitiría `predioId: 999`, porque `fk_parcela_predio` lo rechaza (el backend responde 404). En los mocks sí es posible porque son datos locales, y por eso la tabla muestra "Sin predio asociado".

### Respuestas para la defensa oral
1. **Entidad JPA vs interface TS:** la entidad vive en el backend y describe cómo se guarda en PostgreSQL (`@Entity`, `@Table`, `@ManyToOne`). La interface solo describe la forma del JSON que usa React.
2. **`Predio[]`:** dice que el arreglo solo puede tener objetos con forma de Predio. Evita, por ejemplo, meter un número o un objeto sin `nombre`.
3. **Mock separado de la página:** así, cuando usemos la API, solo se cambia el origen de datos y no la página ni la tabla.
4. **`import type`:** importa solo el tipo, que se usa para revisar el código. No genera código en el navegador.
5. **Props de `PredioTable`:** `predios: Predio[]` y `parcelas: Parcela[]`.
6. **Si envío un string:** TypeScript marca el error antes de ejecutar y `npm run build` falla.
7. **`map()`:** transforma cada objeto del arreglo en una fila `<tr>`.
8. **¿Por qué `key`?** React la usa para saber qué fila es cuál y actualizar solo la que cambió.
9. **¿Por qué el `id` y no el índice?** El índice cambia si agrego, quito u ordeno elementos. El `id` es siempre el mismo para el mismo predio.
10. **¿`predio.key` dentro del componente?** No. `key` es para React y no llega como prop.
11. **Retorno temprano:** si el arreglo está vacío, muestra un mensaje en vez de una tabla vacía.
12. **`filter()`:** deja solo los elementos que cumplen una condición, por ejemplo los predios que tienen parcelas, y con `.length` se cuentan.
13. **Relación 1:N en los mocks:** cada parcela tiene `predioId`. Varias parcelas pueden tener el mismo `predioId`.
14. **¿Por qué se repite el mismo `predioId`?** Porque un predio puede tener muchas parcelas; San Isidro tiene P-01 y P-02.
15. **`find()`:** busca el predio cuyo `id` es igual al `predioId` de la parcela.
16. **Si `find()` no encuentra:** devuelve `undefined` y la tabla muestra "Sin predio asociado".
17. **¿Por qué `ParcelaTable` recibe dos arreglos?** Necesita los predios para traducir `predioId` a un nombre.
18. **Página vs tabla:** la página elige los datos y calcula estadísticas. La tabla solo los muestra.
19. **Problema de `any`:** desactiva la revisión de TypeScript; los errores aparecen recién en el navegador.
20. **¿La interface existe en runtime?** No. Ni en PostgreSQL ni en el navegador: se borra al compilar y solo existe mientras programo.
21. **Chequeo estático:** TypeScript revisa los tipos leyendo el código, sin ejecutarlo.
22. **¿Cómo cambia al integrar el backend?** La página pedirá los datos a un servicio HTTP (`GET /api/farms`, `GET /api/plots`) en lugar de importar los mocks. La tabla queda igual.
23. **Archivo que deja de ser el origen:** `predios.mock.ts` y `parcelas.mock.ts`.
24. **`table-responsive`:** permite desplazar la tabla de lado a lado en pantallas angostas sin romper el layout.
25. **Flujo completo:** `prediosMock` → `PrediosPage` lo pasa por la prop `predios` → `PredioTable` hace `predios.map()` → cada predio se convierte en un `<tr>` en JSX → React actualiza el DOM → el navegador muestra la fila.

## Guía 02 - Navegación, layout principal y menú

Se instaló **React Router** (`npm install react-router`) y se armó un layout administrativo: menú lateral, cabecera y una zona central que cambia según la URL.

### Equivalencias con el ejemplo del profesor
| Profesor (ParkFlow360) | AgroControl |
|---|---|
| `/clientes` → `ClientesPage.tsx` | `/predios` → `features/predios/pages/PrediosPage.tsx` |
| `/vehiculos` → `VehiculosPage.tsx` | `/parcelas` → `features/parcelas/pages/ParcelasPage.tsx` |
| Marca "P - ParkFlow360" | Marca "A - AgroControl, Gestión agrícola" |
| Colores azules | Colores verdes (tema agrícola) |

### Archivos y responsabilidades
| Archivo | Responsabilidad |
|---|---|
| `src/main.tsx` | Monta React en `#root` y envuelve todo con `BrowserRouter` |
| `src/app/App.tsx` | Solo delega en `AppRouter` |
| `src/routes/AppRouter.tsx` | Declara qué página corresponde a cada URL |
| `src/layouts/MainLayout.tsx` | Estructura fija: `Sidebar` + `Header` + `Outlet` |
| `src/components/common/Sidebar.tsx` | Menú con `NavLink` y marca la opción activa |
| `src/components/common/Header.tsx` | Cabecera común |
| `src/pages/DashboardPage.tsx` | Inicio (`/`) |
| `src/pages/AcercaPage.tsx` | Acerca (`/acerca`), actividad práctica |
| `src/pages/NotFoundPage.tsx` | Cualquier ruta inexistente (`*`) |
| `src/features/predios/pages/PrediosPage.tsx` | `/predios` |
| `src/features/parcelas/pages/ParcelasPage.tsx` | `/parcelas` |

### Rutas
| URL | Route | Página |
|---|---|---|
| `/` | `index` | `DashboardPage` |
| `/predios` | `path="predios"` | `PrediosPage` |
| `/parcelas` | `path="parcelas"` | `ParcelasPage` |
| `/acerca` | `path="acerca"` | `AcercaPage` |
| cualquier otra | `path="*"` | `NotFoundPage` |

### Pruebas
| # | Acción | Resultado esperado |
|---|---|---|
| 1 | Abrir `/` | Se ve el panel principal e "Inicio" está activo |
| 2 | Clic en Predios | Se ve `PrediosPage` y "Predios" está activo |
| 3 | Clic en Parcelas | Se ve `ParcelasPage` y "Parcelas" está activo |
| 4 | Escribir `/ruta-inexistente` | Se ve la página 404 dentro del mismo layout |
| 5 | Botones Atrás / Adelante | Cambia la página sin perder el menú |
| 6 | Achicar la ventana (menos de 820 px) | El menú se acomoda en una fila horizontal |

### Actividad práctica
1. **AcercaPage.tsx** creada en `src/pages`, porque es una página global y no de un módulo.
2. **Ruta `/acerca`** agregada dentro de `MainLayout` en `AppRouter.tsx`.
3. **"Acerca" en el menú**: se agregó `{ to: '/acerca', label: 'Acerca' }` a `menuItems` en `Sidebar.tsx`.
4. **Opción activa visible**: `.sidebar__link--active` usa fondo blanco, texto verde, letra más gruesa y una barra verde a la izquierda. Las demás opciones no cambian.
5. **`path="predios"` → `path="predios2"`**: al hacer clic en Predios (`/predios`), ninguna ruta coincide y aparece la página 404. Escribiendo `/predios2` sí aparece `PrediosPage`, pero el menú ya no la marca como activa. Después se restauró el código.
6. **Sin `<Outlet />`**: el menú y la cabecera se siguen viendo, pero la zona central queda vacía en todas las rutas. El layout no tiene dónde dibujar la página hija. Después se restauró el código.
7. **Flujo desde un clic en Predios hasta PrediosPage (8 líneas):**
   1. El usuario hace clic en "Predios" del `Sidebar`.
   2. `NavLink` cambia la URL a `/predios` sin pedir una página nueva al servidor.
   3. `BrowserRouter` detecta que cambió la dirección.
   4. `Routes` en `AppRouter` busca la `Route` que coincide: `path="predios"`.
   5. Esa ruta está dentro de la ruta de `MainLayout`, así que el layout se mantiene.
   6. `MainLayout` sigue mostrando `Sidebar` y `Header`.
   7. `<Outlet />` dibuja `PrediosPage` en la zona central.
   8. `NavLink` recibe `isActive = true` y pinta "Predios" como opción activa.

### Respuestas para la defensa oral
1. **SPA:** es una aplicación de una sola página. El navegador la carga una vez y después solo cambia la parte que hace falta, así que la navegación es rápida.
2. **React Router:** relaciona cada URL con un componente y permite navegar sin recargar.
3. **BrowserRouter cerca de la raíz:** así todos los componentes que están dentro (rutas, `NavLink`, `Link`) pueden usar la navegación.
4. **main.tsx:** es el punto de entrada. Monta React y configura `BrowserRouter`.
5. **App.tsx:** es la raíz de la aplicación. Solo delega en `AppRouter`.
6. **AppRouter separado:** si mañana agrego rutas, solo cambio ese archivo. `App.tsx` queda limpio.
7. **Routes:** mira la URL actual y elige la `Route` que coincide.
8. **Route:** une un `path` con el componente (`element`) que se debe mostrar.
9. **Route index:** es la ruta por defecto de su padre; aquí representa `/`.
10. **`path="*"`:** atrapa cualquier dirección que no coincidió con las demás (404).
11. **Rutas sin `/` al inicio:** son relativas a su ruta padre. `predios` dentro de la ruta del layout (que está en `/`) se convierte en `/predios`.
12. **Layout:** es la estructura fija de la pantalla. `MainLayout` lo usan todas las páginas, por eso no pertenece a `features/predios`.
13. **Outlet:** es el lugar donde se dibuja la ruta hija que coincide.
14. **Sin Outlet:** se ve el menú y la cabecera, pero ninguna página.
15. **Link vs NavLink:** los dos navegan sin recargar. `NavLink` además sabe si su ruta es la actual y se puede pintar diferente.
16. **isActive:** vale `true` cuando la URL actual coincide con el `to` del `NavLink`.
17. **¿Por qué no `<a href>`?** Un `<a href>` normal pide la página de nuevo al servidor y React se reinicia. `NavLink` cambia la URL sin recargar.
18. **Flujo completo:** ver la actividad 7.
19. **Agregar `/reportes`:** se modifica `AppRouter.tsx` (nueva `Route`) y se crea la página `ReportesPage.tsx`.
20. **Reportes en el menú:** además de lo anterior, se modifica `Sidebar.tsx` (agregar el ítem a `menuItems`).
21. **¿Por qué todavía no el backend?** Primero hay que resolver la navegación sola. Si algo falla, así es más fácil saber si es el router o la API.
22. **¿Cómo sé que no recargó?** El ícono de recargar del navegador no gira, la pestaña Network de las DevTools no pide un nuevo documento HTML y el menú no parpadea.
23. **URL vs archivo:** `/predios` es la dirección que ve el usuario. `PrediosPage.tsx` es el componente. La unión entre los dos la hace la `Route` en `AppRouter`.
24. **¿Por qué un 404 explícito?** Sin él, una URL mal escrita mostraría una pantalla vacía sin explicación. Con él, el usuario entiende qué pasó y puede volver al inicio.
25. **CSS responsive:** en pantallas angostas (menos de 820 px) el menú pasa de columna a fila horizontal, así la aplicación se puede usar en celular o en una ventana pequeña.

## Guía 01 - Estructuración del frontend

### Equivalencias con el ejemplo del profesor
| Profesor (ParkFlow360) | AgroControl |
|---|---|
| `parkflow360-frontend` | `agrocontrol-frontend` |
| Cliente (padre) | Predio (padre) - backend `/api/farms` |
| Vehículo (dependiente) | Parcela (dependiente) - backend `/api/plots` |
| `features/clientes/` | `features/predios/` |
| `features/vehiculos/` | `features/parcelas/` |
| `ClienteCard.tsx` / `Cliente.ts` | `PredioCard.tsx` / `Predio.ts` |
| `VehiculoCard.tsx` / `Vehiculo.ts` | `ParcelaCard.tsx` / `Parcela.ts` |

### Versiones usadas
| Herramienta | Versión |
|---|---|
| Node.js | v26.13.0 |
| npm | 11.20.0 |
| create-vite | 9.3.4 (plantilla `react-ts`, linter ESLint) |
| Vite | 8.3.6 |
| WebStorm | 2026.2.4 (licencia no comercial) |

### Comandos
```bash
npm install      # instala las dependencias de package.json en node_modules/
npm run dev      # servidor de desarrollo en http://localhost:5173/ (con HMR)
npm run build    # revisa TypeScript y genera la versión de producción en dist/
```
El servidor se detiene con `Ctrl + C`. Para volver a levantarlo basta con `npm run dev`; no hace falta repetir `npm create vite`.

### Estructura de carpetas
```text
src/
├── app/App.tsx
├── assets/
├── components/common/
├── config/
├── features/
│   ├── predios/
│   │   ├── components/PredioCard.tsx
│   │   ├── pages/
│   │   ├── services/
│   │   └── types/Predio.ts
│   └── parcelas/
│       ├── components/ParcelaCard.tsx
│       ├── pages/
│       ├── services/
│       └── types/Parcela.ts
├── hooks/
├── layouts/
├── pages/
├── routes/
├── services/http/
├── styles/global.css
├── types/
├── utils/
└── main.tsx
```
Las carpetas que todavía están vacías tienen un archivo `.gitkeep`, porque Git no guarda carpetas vacías.

### Propósito de cinco carpetas (explicado con mis palabras)
1. **`features/predios/`**: guarda todo lo que es solo de los predios: su tarjeta, sus páginas, la forma de pedirlos al backend y su tipo `Predio`. Si cambia algo de los predios, sé que tengo que buscar aquí.
2. **`features/parcelas/`**: lo mismo, pero para las parcelas. Va separada de predios porque en el backend también son módulos distintos (`farm` y `plot`), aunque una parcela pertenece a un predio.
3. **`components/common/`**: piezas que sirven en cualquier pantalla, como un botón o un mensaje de error. Si algo solo lo usan las parcelas, no va aquí sino en `features/parcelas/components/`.
4. **`services/http/`**: aquí estará la configuración para hablar con el backend (la URL `http://localhost:8080` y el manejo de errores 400, 404, 409 y 500). Así los componentes no llaman a la API cada uno por su cuenta.
5. **`config/`**: lee las variables de entorno, como `VITE_API_BASE_URL`, en un solo lugar. Si cambia el puerto del backend, solo se cambia el archivo `.env.local`.

### Limpieza realizada
- Se eliminaron `src/App.tsx`, `src/App.css`, `src/index.css` y las imágenes de ejemplo de `src/assets/`.
- `App.tsx` ahora está en `src/app/App.tsx` y solo muestra "AgroControl - Frontend en construcción".
- `main.tsx` sigue siendo el punto de entrada; ahora importa `./app/App.tsx` y `./styles/global.css`.

### Configuración preparada para el backend
- `.env.example` contiene `VITE_API_BASE_URL=http://localhost:8080`, sin contraseñas.
- Para usarlo se copia como `.env.local`, que Git no sube (`*.local` está en `.gitignore`).
- Nunca se guardan contraseñas en variables `VITE_`: todo lo que empieza con `VITE_` termina en el navegador del usuario. La contraseña de PostgreSQL solo la conoce el backend (`config/db-local.yml`).

### Preguntas de defensa oral
1. **Node.js, npm, Vite, React y TypeScript.** Node.js ejecuta JavaScript fuera del navegador, en mi computadora. npm instala librerías y ejecuta los scripts del proyecto. Vite es el servidor de desarrollo y el empaquetador. React es la librería para construir la interfaz con componentes. TypeScript es JavaScript con tipos.
2. **¿Qué resuelve Vite?** Levanta un servidor rápido que actualiza el navegador apenas guardo un archivo (HMR), sin recargar todo. Además, prepara la versión de producción.
3. **Punto de entrada.** `src/main.tsx`: busca el `<div id="root">` de `index.html` y dibuja `<App />` dentro.
4. **`package.json`.** Describe el proyecto: nombre, scripts (`dev`, `build`, `lint`) y las dependencias con sus versiones.
5. **¿Por qué no se sube `node_modules`?** Es muy pesado y se puede regenerar con `npm install` a partir de `package.json` y `package-lock.json`.
6. **`npm run dev`.** Inicia el servidor de Vite en `http://localhost:5173/` y vigila los cambios en los archivos.
7. **`npm run build`.** Primero revisa los tipos con TypeScript (`tsc -b`). Si no hay errores, genera la carpeta `dist/` con archivos optimizados para producción.
8. **¿Por qué Predio y Parcela están en features distintas?** Son funcionalidades distintas del negocio. Separarlas evita mezclar archivos y sigue la misma división del backend (`farm` y `plot`).
9. **`components/common` vs `features/predios/components`.** `common` es para piezas que sirven en todo el sistema. `features/predios/components` es para piezas que solo tienen sentido con predios, como `PredioCard`.
10. **¿Por qué el frontend no se conecta a PostgreSQL?** El código del frontend corre en el navegador del usuario. Si se conectara a la base de datos, la contraseña quedaría expuesta y se saltarían las validaciones del backend (400, 404, 409). Por eso el frontend solo habla con la API REST.
11. **¿Por qué no guardar una contraseña en `VITE_`?** Vite mete esas variables dentro del código que se descarga en el navegador, así que cualquiera podría leerla.
12. **¿Qué aporta TypeScript?** Avisa de los errores antes de ejecutar. Por ejemplo, si escribo `predioId: numbr` en `Parcela.ts`, el editor y `npm run build` marcan el error.
13. **Si muevo `App.tsx` y deja de compilar...** Primero revisaría el `import` en `main.tsx`, que debe apuntar a `./app/App.tsx`.
14. **HMR.** Significa Hot Module Replacement. Cambié el texto de `App.tsx`, guardé con `Ctrl + S` y el navegador mostró el cambio al instante, sin recargar la página.
15. **¿Base de datos o API HTTP?** La API HTTP (`/api/farms`, `/api/plots`). El frontend nunca toca la base de datos: llama al controller REST, que pasa por el servicio y el puerto/adaptador hasta llegar a PostgreSQL.

### Evidencias
- Captura del navegador en `http://localhost:5173/`.
- Captura del árbol de carpetas en WebStorm.
- Salida de `npm run dev` y de `npm run build`.
