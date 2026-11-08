# agrocontrol-frontend

Frontend de **AgroControl** (proyecto 16) con **React + TypeScript + Vite**, creado y ejecutado desde **WebStorm**.
Consumirá más adelante la API REST del backend Spring Boot (`agrocontrol-backend`, puerto 8080).

## Guía 07 - CRUD completo de Parcela + relación Predio 1:N

**Parcela** es la entidad **hija**: cada parcela pertenece a **un** predio (`predioId`) y un predio puede tener **muchas** parcelas.

### Equivalencias con el ejemplo del profesor
| Profesor (ParkFlow360) | AgroControl |
|---|---|
| Vehículo (hija) → Cliente (padre) | Parcela (hija) → Predio (padre) |
| `clienteId` | `predioId` |
| `vehiculo.cliente_id` → `cliente.id` | `parcela.predio_id` → `predio.predio_id` (`fk_parcela_predio`) |
| `vehiculoService` (CRUD) | `parcelaService`: `listar`, `obtenerPorId`, `crear`, `actualizar`, `eliminar` |
| Selector de propietario | Selector de **predio** (`<select name="predioId">` controlado) |
| Cambiar de propietario | **Mover la parcela a otro predio** (PUT con otro `predioId`) |
| Filtro por cliente | **Filtro por predio** (local) |
| Placa duplicada → 409 | Código repetido **en el mismo predio** → 409 (`uq_parcela_predio_codigo`) |
| `anio: string` → `Number()` | `superficie: string` → `Number()` |
| Solo clientes **activos** en el selector | **Todos** los predios (no existe `activo`), ordenados por nombre |

### Endpoints nuevos en el backend
`PUT /api/plots/{id}` y `DELETE /api/plots/{id}`. Detalle en el `README.md` del backend.

### Cómo se representa la relación en cada capa
| Capa | Representación |
|---|---|
| PostgreSQL | Columna `parcela.predio_id` + `fk_parcela_predio` → `predio(predio_id)` |
| JPA | `ParcelaJpaEntity` con `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "predio_id")` |
| Dominio / JSON | `predioId: Long` / `"predioId": 1` |
| React | `<select>` con `value={formData.predioId}` ("1" como texto). La tabla muestra el **nombre** con un `Map` |

`predioId` **no es** lo mismo que `@ManyToOne`: es solo el número que viaja en el JSON. El objeto `@ManyToOne` vive en la entidad JPA, y la FK real vive en PostgreSQL.

### Qué se agregó en el frontend
- **`ParcelaForm`** crea y edita, con la misma técnica de `key` que en la Guía 06. Al editar, el select viene con el predio actual. Si se elige otro, aparece el aviso amarillo *"Al actualizar, la parcela se moverá a este predio."*
- **`validarParcela(data, predios)`** revisa que se haya elegido un predio y que ese predio **exista** en la lista cargada.
- **`ParcelaTable`** arma un `Map(id → Predio)` con los predios que ya cargó la página. **No hace ningún GET por fila** (evita el problema N+1).
- **`ParcelasPage`** tiene:
  - carga en paralelo con `Promise.all`;
  - filtro local por predio (`Number(predioFiltro)`);
  - editar con GET/{id};
  - aviso *"Parcela P-02 movida de San Isidro a La Esperanza"*;
  - DELETE con confirmación;
  - errores sin borrar filas.

### Flujos
| Acción | Flujo |
|---|---|
| **Crear** | Formulario → `validarParcela` (el predio existe) → `POST /api/plots` → Service (predio existe → 404; código → 409) → `INSERT` con `predio_id` → 201 → `[...prev, creada]` |
| **Mover de predio** | Editar → `GET /api/plots/{id}` → el select viene con "1" → elegir La Esperanza ("2") → Actualizar → `PUT` con `predioId: 2` → `UPDATE parcela SET predio_id = 2` → 200 → `map()` reemplaza la fila → la columna Predio dice "La Esperanza" |
| **Mostrar el predio** | `parcelas` + `predios` → `Map` por id → `predioPorId.get(parcela.predioId).nombre` |
| **Filtrar** | `predioFiltro` ("1") → `Number` → `filter(p => p.predioId === 1)` → React vuelve a dibujar, sin backend |
| **Eliminar** | Confirmar → `DELETE /api/plots/{id}` → 204 → `filter()`. Si responde 409, la fila queda |

### Filtro local vs endpoint del backend
| Estrategia | AgroControl |
|---|---|
| Filtro en React (el que se usa) | Las parcelas ya están cargadas: filtra en memoria, sin peticiones |
| Endpoint del backend | Ya existe `GET /api/plots/farm/{predioId}` y **no se inventó** `?predioId=`. Convendría con miles de parcelas o con paginación |

### Práctica evaluada
| Práctica | Evidencia |
|---|---|
| CRUD completo de la hija | `parcelaService` + botones Editar / Eliminar |
| Selector controlado del padre | `<select name="predioId" value=... onChange=...>` con los predios del GET |
| Impedir un padre vacío o inválido | `validarParcela`: "Debe seleccionar un predio" / "El predio seleccionado no es válido" |
| Cambiar de padre al editar | Mover Lote Sur de San Isidro a La Esperanza (PUT con `predioId: 2`) |
| Nombre del padre en la tabla | Columna Predio con el **nombre**, resuelto con un `Map` |
| Filtro por padre | "Filtrar por predio" |
| POST y PUT en Network con el id del padre | El Payload muestra `"predioId": 2` |
| Referencia inexistente | `fetch` con `predioId: 999` desde la Console → **404** "No existe el predio con id: 999" |
| Sin petición por fila | En Network → Fetch/XHR, al abrir Parcelas solo aparecen `plots` y `farms` |
| Recorrido hasta la FK | Flujo "Mover de predio" + DataGrip: `SELECT parcela_id, predio_id, codigo FROM agrocontrol.parcela` |

### Respuestas para la defensa oral
1. **¿Por qué Parcela es la hija?** Porque cada parcela necesita un predio (`predio_id NOT NULL`) y un predio puede tener muchas parcelas.
2. **`predioId` en el frontend:** el número que identifica al predio dueño. Es la referencia que viaja en el JSON.
3. **¿Dónde está la FK de verdad?** En PostgreSQL: `fk_parcela_predio` sobre `parcela.predio_id`.
4. **¿Por qué `predioId` no es igual a `@ManyToOne`?** `predioId` es un número en el JSON y el dominio. `@ManyToOne` es un objeto `PredioJpaEntity` dentro de la entidad JPA. El mapper los traduce.
5. **Endpoints de la hija:** `GET /plots`, `GET /plots/{id}`, `POST /plots`, `PUT /plots/{id}`, `DELETE /plots/{id}`.
6. **¿Por qué también `GET /farms` en Parcelas?** Para el selector, el filtro y mostrar el nombre del predio.
7. **¿Por qué el select es controlado?** Para que el estado de React sea la fuente de verdad: se puede precargar al editar, validar y limpiar.
8. **¿Qué necesita un select controlado?** `value` (del estado) y `onChange` (que actualiza el estado).
9. **¿Por qué no `selected` en cada `option`?** Porque lo decide el `value` del `<select>`. Mezclar las dos cosas da conflictos y React avisa.
10. **¿Por qué `predioId` es texto en `ParcelaFormData`?** El `<select>` entrega siempre texto, y al inicio está vacío ("").
11. **¿Cuándo se convierte a número?** En `handleSubmit`, después de que la validación no encontró errores.
12. **¿Si `Number(predioId)` da `NaN`?** No pasaría la validación, porque ningún predio tiene id `NaN`. Así nunca se envía.
13. **¿Por qué React valida si Spring también lo hace?** Para dar respuesta inmediata al usuario y no enviar peticiones que fallarían seguro.
14. **¿Por qué no basta el frontend?** Cualquiera puede llamar a la API sin el formulario (Console, curl). La integridad la aseguran Spring y PostgreSQL.
15. **Un predio con muchas parcelas:** varias filas de `parcela` con el mismo `predio_id`, como San Isidro con P-01, P-02...
16. **¿Una parcela con dos predios a la vez?** No. `predio_id` es una sola columna con un solo valor.
17. **`Promise.all`:** lanza `GET /plots` y `GET /farms` al mismo tiempo y espera a los dos.
18. **Ventaja de cargarlos en paralelo:** se tarda lo que tarda la más lenta, no la suma de las dos.
19. **Riesgo de `GET /farms/{id}` por fila:** con 100 parcelas serían 100 peticiones. La página se vuelve lenta y el servidor se sobrecarga.
20. **Problema N+1 en la UI:** 1 petición para la lista + N peticiones, una por cada fila.
21. **¿Cómo se resuelve el nombre sin pedir nada por fila?** Con los predios ya cargados y un `Map`.
22. **¿Por qué un `Map`?** `get(id)` encuentra el predio directo, sin recorrer todo el arreglo en cada fila.
23. **¿Si el `predioId` no está en el arreglo?** Muestra "Sin predio asociado (#id)".
24. **¿Cómo se precarga el predio al editar?** `parcelaAFormData` convierte `predioId: 1` en `"1"`, y el select muestra ese predio.
25. **¿Qué pasa al cambiar el predio y actualizar?** Sale `PUT /plots/{id}` con el `predioId` nuevo, el backend valida y hace `UPDATE predio_id`.
26. **¿Qué valida Spring antes de guardar el nuevo predio?** Que la parcela exista, que el predio destino exista y que el código no se repita en ese predio.
27. **¿Qué columna cambia en PostgreSQL?** `parcela.predio_id`.
28. **Crear vs mover:** crear hace `INSERT` de una fila nueva. Mover hace `UPDATE` de `predio_id` en una fila que ya existe (el mismo id).
29. **¿Cómo se sincroniza la fila después del PUT?** `map()` reemplaza la fila con el objeto que devolvió el backend.
30. **¿Por qué usar la respuesta y no el payload?** Es lo que realmente quedó en la base de datos, por ejemplo el código normalizado por el backend.
31. **Filtro local:** `parcelas.filter(p => p.predioId === Number(predioFiltro))` sobre los datos que ya están cargados.
32. **¿Por qué `Number(predioFiltro)`?** El select da `"1"` y `predioId` es `1`. `"1" === 1` es `false`.
33. **¿Cuándo filtrar en el backend?** Con muchos datos, paginación o reglas complejas.
34. **¿Puedo inventar `?predioId=`?** No. El contrato lo define el backend. Hoy existe `/plots/farm/{predioId}`.
35. **409 al crear una parcela:** el código ya existe en ese predio.
36. **404 relacionado con `predioId`:** el predio enviado no existe (o fue borrado mientras tanto).
37. **Un PUT que no cambia el predio, ¿qué mirar en Network?** El Payload (¿lleva el `predioId` nuevo?) y el Response (¿qué `predioId` devolvió?).
38. **POST completo hasta la FK:** select (`"2"`) → `Number` → `POST /api/plots {predioId: 2}` → `@Valid` → `ParcelaService` (predio existe, código libre) → `getReferenceById(2)` → `INSERT parcela(predio_id = 2, ...)` → PostgreSQL revisa `fk_parcela_predio` → 201.
39. **PUT que cambia de predio:** GET/{id} → el select muestra "San Isidro" → elijo "La Esperanza" → `PUT /api/plots/2 {predioId: 2}` → Service valida 404 / 409 → `UPDATE parcela SET predio_id = 2` → 200 → `map()` → la columna Predio cambia.
40. **¿Cómo se muestra el nombre del predio sin copiarlo en la parcela?** La parcela solo guarda `predioId`. La tabla recibe los predios por props y busca el nombre con `predioPorId.get(predioId)`. El nombre vive en un solo lugar: la tabla `predio`.

## Guía 06 - CRUD completo de Predio

La entidad padre (**Predio**) tiene el CRUD completo en el frontend y en el backend: listar, buscar por id, crear, editar y eliminar.

### Equivalencias con el ejemplo del profesor
| Profesor (ParkFlow360) | AgroControl |
|---|---|
| `clienteService` (5 métodos) | `predioService`: `listar`, `obtenerPorId`, `crear`, `actualizar`, `eliminar` |
| `ClienteCreateRequest` / `ClienteUpdateRequest` | `CrearPredioRequest` / `ActualizarPredioRequest` |
| `ClienteTable` con `onEdit` / `onDelete` | `PredioTable` con `onEdit`, `onDelete`, `editingId`, `deletingId`, `accionesBloqueadas` |
| `ClienteForm` crear/editar | `PredioForm` crear/editar (prop `predio`) |
| DELETE de un cliente con vehículos → 409 | DELETE de un predio con parcelas → **409** (`PredioConParcelasException`) |
| `GET/PUT/DELETE /api/clientes/{id}` | `GET/PUT/DELETE /api/farms/{id}`, agregados al backend en esta guía |

### Endpoints nuevos en el backend
Antes solo existían `GET` y `POST`. Se agregaron `PUT /api/farms/{id}` y `DELETE /api/farms/{id}`. Detalle en el `README.md` del backend.

### Decisión técnica: `key` en lugar de `useEffect` para cargar el formulario
La guía copia los datos de la prop `cliente` al estado del formulario con un `useEffect`. Aquí se hace lo mismo con la técnica que **recomienda la documentación de React**:
```tsx
<PredioForm key={editingPredio ? `editar-${editingPredio.id}` : 'nuevo'} predio={editingPredio} ... />
```
- Cuando cambia la `key`, React **descarta** el formulario anterior y crea uno nuevo. `useState(() => predioAFormData(predio))` toma los datos del predio elegido.
- Así no hace falta un `useEffect` que haga `setFormData` (ESLint, con `react-hooks/set-state-in-effect`, marca ese patrón), y se evita un render con datos viejos.
- El resultado es el mismo que pide la guía: al tocar Editar se cargan los datos, al Cancelar vuelve a "Nuevo predio" y **ningún** efecto hace un PUT automático.

### Flujos
| Acción | Flujo |
|---|---|
| **Listar** | `PrediosPage` → `predioService.listar` → `GET /api/farms` → Controller → Service → Adapter → `SELECT` → JSON → `setPredios` |
| **Editar** | Clic en Editar → `GET /api/farms/{id}` (versión actual) → `editingPredio` → el formulario se crea con esos datos → Actualizar → `PUT /api/farms/{id}` → `UPDATE` → 200 con el predio → `map()` reemplaza **solo** esa fila |
| **Crear** | Crear predio → validación → `POST /api/farms` → `INSERT` → 201 con `id` → `[...prev, creado]` |
| **Eliminar** | Clic en Eliminar → `window.confirm` → `DELETE /api/farms/{id}` → **204** → `filter()` quita la fila. Si es **409**, la fila **queda** y se muestra el error |
| **Cancelar edición** | `setEditingPredio(null)`. No hay petición HTTP y la fila no cambia |

### Bloqueos (evitar doble PUT o doble DELETE)
- Mientras carga el detalle, guarda o elimina, `accionesBloqueadas = true` y **todos** los botones Editar/Eliminar se deshabilitan.
- El botón del formulario dice **"Guardando..."** y el de la fila **"Eliminando..."**.
- La fila que se está editando se resalta en verde.

### Práctica evaluada
| Práctica | Cómo se demuestra |
|---|---|
| Listar, buscar, crear, actualizar y eliminar en el service | `predioService.ts` |
| Editar y Eliminar por callbacks | `PredioTable` no importa `predioService` |
| Un formulario para crear y editar | `PredioForm` con la prop `predio` |
| GET/{id} antes de editar | `handleEdit` llama a `predioService.obtenerPorId` |
| Sincronizar después de POST, PUT y DELETE | `[...prev, x]`, `map()` y `filter()` con la respuesta del backend |
| Cancelar edición | Botón "Cancelar edición", sin HTTP |
| Bloquear acciones | `accionesBloqueadas`, `saving` y `deletingId` |
| Un 404 real | Abrir dos pestañas, eliminar un predio en una y tocar Editar en la otra → `Error 404: No existe el predio con id: X`. También `curl.exe -i http://localhost:8080/api/farms/999` |
| Eliminar un padre con hijas | Eliminar San Isidro → **409**: "No se puede eliminar el predio San Isidro porque tiene parcelas asociadas". La fila sigue en la tabla y en PostgreSQL |
| Evidencia de Network | GET, GET/{id}, POST (201), PUT (200) y DELETE (204) |

### Respuestas para la defensa oral
1. **CRUD:** Create = POST, Read = GET, Update = PUT, Delete = DELETE.
2. **`GET /farms` vs `GET /farms/{id}`:** el primero devuelve la **colección** (`Predio[]`) y el segundo **un** predio, o 404.
3. **¿Por qué GET/{id} antes de editar?** Para editar la versión **actual** que tiene la base de datos. Si otro usuario lo cambió o lo borró, lo sabemos antes de abrir el formulario.
4. **El id en la URL:** identifica **qué** recurso se lee, actualiza o elimina.
5. **`POST /farms` vs `PUT /farms/7`:** POST crea un recurso **nuevo** en la colección y el servidor le da el id. PUT reemplaza los datos del recurso **7**, que ya existe.
6. **¿Por qué POST no es idempotente?** Enviarlo dos veces intenta crear dos predios. En AgroControl, el segundo daría 409 por el nombre.
7. **¿Por qué PUT es idempotente?** Enviar el mismo PUT 1 o 10 veces deja el predio en el mismo estado final.
8. **¿Por qué DELETE es idempotente?** Después del primero el predio ya no existe. Los siguientes dan 404, pero el estado final es el mismo: borrado.
9. **Responsabilidad de `PredioTable`:** mostrar filas y avisar con `onEdit` / `onDelete` qué pidió el usuario.
10. **¿Por qué la tabla no llama al service?** Para que sea reutilizable y fácil de probar. Quién llama a la API y cómo se actualiza el estado lo decide la página.
11. **`editingPredio`:** es el predio que se está editando (traído con GET/{id}). Si es `null`, el formulario está en modo crear.
12. **¿Cómo sabe el formulario si crea o edita?** Por la prop `predio`: si hay predio con id, edita (PUT); si no, crea (POST).
13. **¿Por qué la guía usa `useEffect` cuando cambia la prop?** Para copiar los datos del predio al estado del formulario. En AgroControl lo resolvemos con `key`: al cambiar la key, React crea un formulario nuevo con esos datos. Es la forma que recomienda React y evita un `setState` dentro de un efecto.
14. **¿Por qué no hacer un PUT dentro de un efecto?** Se ejecutaría solo al montar o al cambiar la prop (y dos veces con `StrictMode`), sin que el usuario lo pida. Una modificación de datos solo debe salir de una acción del usuario.
15. **`CrearPredioRequest` vs `ActualizarPredioRequest`:** hoy tienen los mismos campos, pero se nombran por su intención. Si editar cambia, solo se toca uno.
16. **Al presionar Editar:** `handleEdit(id)` → GET/{id} → `setEditingPredio` → el formulario aparece con los datos y la fila se resalta.
17. **Al presionar Cancelar edición:** `setEditingPredio(null)` → el formulario vuelve a "Nuevo predio". No sale ninguna petición HTTP.
18. **¿Qué envía el PUT?** `{ nombre, ubicacion, descripcion }` en el body, y el id en la URL.
19. **¿Por qué no cambiar la fila antes de que el PUT responda?** Si el backend responde 400, 404 o 409, la pantalla mostraría algo que **no** está en PostgreSQL.
20. **¿Cómo se reemplaza solo el predio editado?** `prev.map(p => p.id === guardado.id ? guardado : p)`.
21. **`map()` y `filter()`:** `map` devuelve un arreglo del mismo tamaño con un elemento cambiado (editar). `filter` devuelve uno sin el elemento (eliminar).
22. **¿Qué devuelve DELETE?** **204 No Content**, sin cuerpo.
23. **¿Por qué `apiClient` maneja el 204?** Porque `response.json()` sobre un cuerpo vacío lanzaría un error, aunque el borrado haya salido bien.
24. **¿Por qué confirmar antes de DELETE?** Es irreversible (borrado físico) y un clic accidental no debería borrar datos.
25. **404 en GET/{id}:** el predio ya no existe, por ejemplo si otro usuario lo eliminó.
26. **400 en PUT:** el JSON no cumple `ActualizarPredioRequest` (nombre vacío o demasiado largo).
27. **409 al eliminar un predio:** todavía tiene parcelas asociadas.
28. **Relación con 1:N:** cada parcela tiene `predio_id`, y `fk_parcela_predio` impide borrar el predio mientras alguna parcela lo apunte.
29. **Borrado físico vs baja lógica:** el físico elimina la fila (DELETE). La baja lógica la conserva con `activo = false` (UPDATE). AgroControl no tiene columna `activo`.
30. **Ventaja de actualizar el estado local:** es instantáneo y no necesita otro GET.
31. **Ventaja de volver a hacer GET:** muestra exactamente lo que tiene el servidor, incluso cambios de otros usuarios.
32. **¿Cómo se evita un doble DELETE o PUT?** Con los botones deshabilitados (`accionesBloqueadas`, `saving`) mientras la petición está en curso.
33. **¿Qué mirar en Network?** Método, URL (con el id), status, Payload (en PUT y POST) y Response (`message` del ApiError).
34. **PUT completo:** Actualizar predio → `validarPredio` → `predioService.actualizar(id, payload)` → `apiFetch` con `PUT /api/farms/{id}` → `PredioController.actualizar` (`@Valid`) → `PredioService.actualizar` (`@Transactional`: existe → 404, nombre de otro → 409) → `guardar` con id → JPA `UPDATE` → 200 con `PredioResponse` → `onSaved(actualizado, 'edit')` → `map()` → React vuelve a dibujar la fila.
35. **DELETE completo:** Eliminar → `confirm` → `predioService.eliminar(id)` → `DELETE /api/farms/{id}` → `PredioController.eliminar` → `PredioService.eliminar` (`@Transactional`, 404 si no existe) → adaptador `deleteById` + `flush` → si **no** tiene parcelas: `DELETE` en PostgreSQL → 204 → `filter()` quita la fila. Si **tiene** parcelas: PostgreSQL rechaza por `fk_parcela_predio` → `PredioConParcelasException` → rollback → **409** → la fila queda y se muestra el mensaje.

## Guía 05 - Conexión React ↔ Spring Boot

Los mocks se eliminaron. Las tablas cargan **datos reales de PostgreSQL** a través del backend, y los formularios **crean** predios y parcelas con `POST`.

### Contrato real del backend (fuente de verdad)
El backend de AgroControl no tiene Swagger instalado. El contrato se tomó de los `@RestController` y los DTO (`record`) reales, y se probó con `curl.exe` en los Capítulos 07 y 08.

| Profesor (ParkFlow360) | AgroControl |
|---|---|
| `GET/POST /api/clientes` | `GET/POST /api/farms` (`PredioController`) |
| `GET/POST /api/vehiculos` | `GET/POST /api/plots` (`ParcelaController`) |
| `ClienteCreateRequest = Omit<Cliente, 'id'>` | `CrearPredioRequest` (interface propia, Guía 04) |
| `VehiculoCreateRequest` con `clienteId` | `CrearParcelaRequest` con `predioId` (así lo espera `CrearParcelaRequest.java`) |
| `src/api/apiClient.ts` | `src/services/http/apiClient.ts` (la carpeta reservada en la Guía 01) |
| Lectura de `VITE_API_URL` en el apiClient | `src/config/env.ts` (la carpeta `config/` de la Guía 01) |

No se usa `Omit<Predio, 'id'>`, porque el request no es "el modelo menos el id": `ubicacion` puede ser `null` y la parcela no manda `id`. Por eso cada DTO tiene su interface exacta.

### Cambios en el backend
- `CorsConfig.java`: permite el origen `http://localhost:5173`.
- Nuevo `GET /api/plots`: lista todas las parcelas. Antes solo existía `/api/plots/farm/{id}`.

Detalle en el `README.md` de `agrocontrol-backend`.

### Archivos nuevos
| Archivo | Responsabilidad |
|---|---|
| `.env.development` | `VITE_API_URL=http://localhost:8080/api`, solo datos públicos |
| `src/vite-env.d.ts` | Tipado de `import.meta.env.VITE_API_URL` |
| `src/config/env.ts` | Único lugar donde se lee la variable |
| `src/services/http/apiClient.ts` | `apiFetch`: URL base, `Content-Type`, `response.ok`, `ApiError` con `status`, `message` y `fieldErrors` |
| `features/predios/services/predioService.ts` | `listar()` → GET `/farms`, `crear()` → POST `/farms` |
| `features/parcelas/services/parcelaService.ts` | `listar()` → GET `/plots`, `crear()` → POST `/plots` |

### Detalle técnico: `ApiError` sin "parameter properties"
La guía escribe `constructor(public status: number, ...)`. El `tsconfig.app.json` de Vite tiene `erasableSyntaxOnly: true`, y con esa opción esa sintaxis **no compila**: `npm run build` da el error `TS1294`. Por eso, en `apiClient.ts`, los campos se declaran aparte (`readonly status: number`) y se asignan en el constructor.

### Cómo se conecta cada pantalla
- **PrediosPage:** `useEffect` → `Promise.all([predioService.listar(), parcelaService.listar()])`. Hace falta pedir las dos para contar las parcelas de cada predio. Estados: `loading`, `error` y datos.
- **ParcelasPage:** lo mismo. Los predios se usan para mostrar el nombre en la tabla y para el `<select>` del formulario (la prop `predios`).
- **PredioForm / ParcelaForm:** el `setTimeout` de la Guía 04 se reemplazó por `await predioService.crear(payload)`. El botón queda en "Guardando..." mientras espera (`submitting`). `onCreated` agrega a la tabla **el objeto que devolvió el backend**, con su `id` real.
- **Errores del backend:** se muestra `Error 409: Ya existe la parcela P-01 en el predio 1`. Si el backend responde 400 con `fieldErrors`, cada mensaje aparece junto a su campo.
- **Backend apagado o CORS mal configurado:** "No se pudo conectar con el backend (...)". `fetch` no recibe ninguna respuesta HTTP.

### La relación 1:N al crear una parcela
React **no crea** la relación: elige un predio en el `<select>` y envía su id como `predioId: 1`. El backend verifica que el predio exista (`PredioNoEncontradoException` → 404) y que el código no se repita (`CodigoParcelaDuplicadoException` → 409). PostgreSQL la protege con `fk_parcela_predio` y `uq_parcela_predio_codigo`.

### Pruebas y evidencias (DevTools → Network)
| # | Acción | Qué se ve en Network |
|---|---|---|
| 1 | Abrir `/predios` | `GET /api/farms` y `GET /api/plots` → **200**, con el JSON en Response |
| 2 | Crear el predio "Los Algarrobos" | `POST /api/farms` → **201**. El Payload lleva `{nombre, ubicacion, descripcion}` y la fila aparece en la tabla |
| 3 | Crear la parcela `p-08` en San Isidro | `POST /api/plots` → **201**. El Payload lleva `predioId: 1`, `codigo: "P-08"` y `superficie` como número |
| 4 | Repetir el código `P-01` en San Isidro | `POST /api/plots` → **409**. El mensaje del backend aparece en rojo en el formulario |
| 5 | Repetir un nombre de predio | `POST /api/farms` → **409** |

En desarrollo, cada GET aparece **dos veces** y la primera queda **cancelada**. `StrictMode` monta el componente dos veces y el `AbortController` cancela la primera petición. Es el comportamiento esperado.

### Tres fallos intencionales y su diagnóstico
| Fallo | Cómo se provoca | Qué se ve | Diagnóstico |
|---|---|---|---|
| **URL incorrecta** | En `.env.development`, cambiar `/api` por `/apix` y reiniciar `npm run dev` | **404**: "La ruta solicitada no existe" | El backend respondió, pero la ruta no coincide con el `@RequestMapping` |
| **CORS** | En `CorsConfig`, cambiar `5173` por `5174` y reiniciar el backend | "No se pudo conectar..." y en Console: *blocked by CORS policy* | La petición sí llega (`curl` funciona), pero el navegador no deja leer la respuesta |
| **Body inválido** | En la consola del navegador, enviar un POST sin campos (ver abajo) | **400** con `fieldErrors` por campo | El JSON no cumple `CrearParcelaRequest.java` (`@NotNull`, `@NotBlank`) |

Body inválido (pegar en DevTools → Console):
```js
fetch('http://localhost:8080/api/plots', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{"codigo":""}' }).then(r => r.json()).then(console.log)
```

### Respuestas para la defensa oral
1. **`VITE_API_URL`:** la URL del backend está en un solo lugar. Si cambia el puerto o el servidor, se cambia `.env.development` y no 10 componentes.
2. **¿Por qué no una contraseña en `VITE_`?** Vite la copia dentro del JavaScript que se descarga en el navegador, y cualquiera puede leerla con F12.
3. **Origen:** es protocolo + dominio + puerto. `http://localhost:5173` y `http://localhost:8080` son orígenes distintos.
4. **¿Por qué Postman sí funciona y React no?** CORS lo aplica **el navegador**. Postman y `curl` no son navegadores, así que no lo aplican.
5. **¿Qué controla CORS?** Solo qué orígenes del navegador pueden leer la respuesta. No autentica ni protege la base de datos.
6. **¿Qué devuelve `fetch`?** Una `Promise`, que más adelante se resuelve con un `Response`.
7. **`await`:** pausa la función `async` hasta que la `Promise` termine, sin bloquear la página.
8. **¿Por qué un 404 no entra al `catch`?** Para `fetch`, el servidor sí respondió. Solo falla si no hay respuesta (red caída, CORS, backend apagado).
9. **`response.ok`:** es `true` si el status está entre 200 y 299.
10. **¿Por qué `response.json()` también es asíncrono?** El cuerpo puede seguir llegando por la red y hay que leerlo completo antes de convertirlo.
11. **¿Para qué `apiClient`?** Para no repetir en cada componente la URL base, el `Content-Type`, la revisión de `ok` y la lectura de errores.
12. **Responsabilidad de `predioService`:** conocer los endpoints de predios (`/farms`) y qué tipos envían y reciben.
13. **Modelo vs request:** `Predio` es lo que llega, con `id`. `CrearPredioRequest` es lo que se envía, sin `id`.
14. **¿Por qué el POST no envía `id`?** Lo genera PostgreSQL (`GENERATED BY DEFAULT AS IDENTITY`).
15. **`JSON.stringify`:** convierte el objeto de JavaScript en el texto JSON que viaja en el body.
16. **`Content-Type: application/json`:** se agrega cuando hay body, así Spring sabe que debe leerlo como JSON. Sin él responde **415**.
17. **`loading` vs `submitting`:** `loading` es la carga inicial de la tabla (GET). `submitting` es el envío del formulario (POST).
18. **¿Por qué se deshabilita el botón?** Para evitar doble clic: dos POST crearían dos registros, o el segundo daría 409.
19. **Del clic a PostgreSQL:** `handleSubmit` → validación → `parcelaService.crear` → `apiFetch` → `POST /api/plots` → `ParcelaController` (`@Valid`) → `ParcelaService` (`@Transactional`, verifica predio y código) → `ParcelaPersistenceAdapter` → `SpringDataParcelaRepository.save` → `INSERT` en `agrocontrol.parcela`.
20. **De PostgreSQL a la pantalla:** PostgreSQL devuelve el `id` → entidad → dominio → `ParcelaResponse` → JSON con **201** → `apiFetch` hace `response.json()` → `onCreated(creada)` → `setParcelas([...prev, creada])` → React vuelve a dibujar la tabla.
21. **¿Por qué usar el objeto devuelto?** El `id` real lo da la base de datos. Si React inventara uno, no coincidiría con PostgreSQL.
22. **Evidencia ante un 400:** en Network → Payload, comparar el JSON enviado con `CrearParcelaRequest.java`. En Response, mirar `fieldErrors`.
23. **Evidencia ante un 404:** la Request URL. Comparar la ruta con el `@RequestMapping`, o revisar si el `predioId` existe.
24. **Evidencia ante un 500:** la consola de IntelliJ del backend (el log de `GlobalExceptionHandler`). React solo recibe un mensaje genérico.
25. **CORS vs error JPA:** CORS no tiene status en Network ("CORS error") y el backend no muestra excepción. Un error JPA llega como 409 o 500, con log en IntelliJ.
26. **`AbortController`:** cancela el GET si el componente se desmonta (cambio de página), así no se actualiza el estado de un componente que ya no existe.
27. **POST dentro de un Effect:** se ejecutaría solo al montar (y dos veces con `StrictMode`), creando registros duplicados sin que el usuario lo pida.
28. **Relación 1:N al crear una parcela:** el `<select>` elige un predio y se envía `predioId`. El backend la valida y PostgreSQL la guarda en la FK `predio_id`.
29. **¿Si el backend esperara `predio: { id: 1 }`?** Se cambiaría solo el payload y `CrearParcelaRequest.ts`. El contrato lo define el backend.
30. **Flujo completo:** formulario → `parcelaService.crear` → `apiFetch` → `POST /api/plots` → `ParcelaController` → `ParcelaService` → `ParcelaRepositoryPort` / Adapter → Spring Data JPA → PostgreSQL → `ParcelaResponse` en JSON (201) → `apiFetch` → `onCreated` → `setParcelas` → la tabla muestra la fila nueva.

## Guía 04 - Formularios controlados con React + TypeScript

Las páginas de Predios y Parcelas tienen un botón **"+ Nuevo predio"** / **"+ Nueva parcela"** que muestra un formulario. Al guardar, el formulario **valida** los datos y arma el **payload**, el objeto listo para enviar al backend, que por ahora solo se muestra en la consola. Todavía **no** se llama a la API.

### Equivalencias con el ejemplo del profesor
| Profesor (ParkFlow360) | AgroControl |
|---|---|
| `ClienteFormData` / `VehiculoFormData` | `PredioFormData` / `ParcelaFormData` (en `types/`) |
| `validarCliente` / `validarVehiculo` (en `utils/`) | `validarPredio` / `validarParcela` (en `utils/`) |
| `ClienteForm` / `VehiculoForm` | `PredioForm` / `ParcelaForm` |
| `anio: string` → `Number()` al validar | `superficie: string` → `Number()` al validar |
| `<select>` de `clienteId` | `<select>` de `predioId`, que muestra el nombre y guarda el id |
| `clientePayload` / `vehiculoPayload` sin tipo | Tipados como `CrearPredioRequest` / `CrearParcelaRequest` |
| Checkbox `activo` | Checkbox de opciones de pantalla (ver la nota) |

### Tres tipos para la misma entidad
| Tipo | Archivo | Para qué sirve |
|---|---|---|
| Modelo | `models/Parcela.ts` | Lo que la API **devuelve**: tiene `id` y `superficie: number` |
| Datos del formulario | `types/ParcelaFormData.ts` | Lo que el usuario **está escribiendo**: todo es texto (`superficie: string`, `predioId: string`) |
| Payload | `types/CrearParcelaRequest.ts` | Lo que se **enviará** a `POST /api/plots`: sin `id`, ya convertido a `number`, igual que `CrearParcelaRequest.java` |

Tipar el payload hace que TypeScript avise si olvido convertir: `superficie: formData.superficie` da el error `Type 'string' is not assignable to type 'number'`.

### Nota: el checkbox
El profesor usa el checkbox `activo`, pero en AgroControl **no existe la columna `activo`** (ver la Guía 03). Para practicar `checked` igual, cada formulario tiene un checkbox que es **una opción de la pantalla** y **no se envía** en el payload:
- **Predio:** "Limpiar el formulario después de guardar" (`limpiarAlGuardar`, marcado por defecto).
- **Parcela:** "Mantener el predio seleccionado para registrar otra parcela" (`mantenerPredio`). Después de guardar, se limpia todo **menos** el predio elegido, para cargar varias parcelas del mismo predio seguidas.

Esto también muestra la diferencia entre **datos del formulario** y **payload**: `ParcelaFormData` tiene `mantenerPredio`, pero `CrearParcelaRequest` no.

### Validaciones (las mismas reglas que el backend)
| Campo | Regla en el frontend | De dónde sale |
|---|---|---|
| Predio: nombre | Obligatorio, de 3 a 100 caracteres | `@NotBlank @Size(max=100)` + regla propia de 3 caracteres (práctica) |
| Predio: ubicación | Hasta 200 caracteres | `@Size(max=200)` |
| Predio: descripción | Hasta 1000 caracteres | `@Size(max=1000)` |
| Parcela: predio | Obligatorio | `@NotNull predioId` |
| Parcela: código | Obligatorio, hasta 20 caracteres | `@NotBlank @Size(max=20)` |
| Parcela: nombre | Obligatorio, hasta 100 caracteres | `@NotBlank @Size(max=100)` |
| Parcela: superficie | Obligatoria, número > 0 con hasta 8 enteros y 2 decimales | `@Positive @Digits(8,2)` + `ck_parcela_superficie_positiva` |
| Parcela: unidad | Obligatoria | `@NotBlank unidadSuperficie` |
| Parcela: ubicación referencial | Hasta 200 caracteres | `@Size(max=200)` |

### Práctica evaluada (cómo se adaptó)
| Práctica del profesor | En AgroControl |
|---|---|
| Campo Dirección obligatorio en Cliente | No hay dirección. La ubicación del predio es **opcional en la base de datos** (El Porvenir no tiene). El campo nuevo obligatorio es la **unidad de superficie** de la parcela (`unidad_superficie NOT NULL`), un select que viene con "Hectárea" elegida por defecto. Si se cambia a "Seleccione una unidad", aparece el error. |
| Documento con al menos 5 caracteres | **Nombre del predio con al menos 3 caracteres** |
| Placa a mayúsculas en el payload | **Código de parcela a mayúsculas**: se escribe `p-07` y el payload lleva `P-07`. El backend también lo normaliza (`Parcela.normalizarCodigo`). |
| Solo clientes activos en el select | No hay campo `activo`. El select muestra **todos los predios ordenados por nombre** (`sort` + `localeCompare`). |
| Botón Limpiar en Vehículo | Botón **Limpiar** en los dos formularios |
| Botón Guardar deshabilitado 500 ms (reto) | Estado `enviando`: el botón dice **"Guardando..."** y queda deshabilitado durante 500 ms (`setTimeout`) |
| Contador de errores | Mensaje rojo **"El formulario tiene N errores."** |
| ¿Por qué el frontend no reemplaza al backend? | Ver el texto siguiente |

**¿Por qué la validación del frontend NO reemplaza a la del backend?**
El código del frontend corre en el navegador del usuario. Cualquiera puede saltarse el formulario y enviar un JSON directo a la API con `curl` o Postman, como hicimos en el Capítulo 08. Además, hay reglas que el frontend no puede verificar solo: si el código `P-01` ya existe en ese predio (409) o si el predio todavía existe (404). Eso lo saben el backend y PostgreSQL (`uq_parcela_predio_codigo`, `fk_parcela_predio`). La validación del frontend sirve para dar **respuesta rápida al usuario**; la del backend es la que **protege los datos**.

### Pruebas manuales
1. **Predio vacío:** "Guardar predio" muestra "El nombre del predio es obligatorio." y "El formulario tiene 1 error."
2. **Nombre "AB":** "El nombre debe tener al menos 3 caracteres."
3. **Predio válido:** el botón dice "Guardando..." medio segundo, luego aparece el mensaje verde y la consola muestra `Predio listo para API: {nombre: 'Los Algarrobos', ubicacion: '...', descripcion: null}`.
4. **Parcela vacía:** con la unidad en "Seleccione una unidad" aparecen 5 errores.
5. **Superficie `3.456`:** "Use un número con hasta 2 decimales."
6. **Superficie `0`:** "La superficie debe ser mayor que cero."
7. **Parcela válida con código `p-07`:** la consola muestra `codigo: 'P-07'`, `predioId: 2` y `superficie: 7.5`, todos `number` salvo el código.
8. **"Mantener el predio" marcado:** después de guardar, el predio sigue elegido y los demás campos se vacían.
9. **Pantalla angosta (menos de 720 px):** el formulario pasa a una sola columna.

### Respuestas para la defensa oral
1. **Input controlado:** su valor sale del estado de React (`value={formData.nombre}`) y solo cambia con `onChange`.
2. **Fuente de verdad:** el estado `formData`, no el DOM.
3. **`useState`:** guarda lo que el usuario escribe, los errores, el mensaje y si se está enviando. Cada cambio vuelve a dibujar la pantalla.
4. **¿Por qué no `formData.nombre = 'Juan'`?** Eso cambia el objeto sin avisar a React, así que no vuelve a dibujar. Hay que usar `setFormData` con un objeto nuevo.
5. **`...prev`:** copia todos los campos anteriores al objeto nuevo y solo se reemplaza el que cambió.
6. **Atributo `name`:** indica qué campo de `formData` se actualiza. Por eso tiene que llamarse igual que la propiedad (`name="superficie"`).
7. **`[name]`:** es una propiedad calculada. Si `name` vale `"codigo"`, `{ [name]: valor }` es `{ codigo: valor }`.
8. **`value` vs `checked`:** `value` es el texto del input. `checked` es `true` o `false` en un checkbox.
9. **Tipo del evento del input:** `ChangeEvent<HTMLInputElement | HTMLSelectElement>`, y en Predio también `HTMLTextAreaElement`.
10. **Tipo del submit:** `FormEvent<HTMLFormElement>`.
11. **`preventDefault()`:** evita que el navegador recargue la página al enviar el form; así no se pierde el estado de la SPA.
12. **`value` sin `onChange`:** el input queda de solo lectura. El usuario escribe pero no cambia nada, y React avisa en la consola.
13. **¿Por qué `superficie` es string mientras se escribe?** El input la entrega como texto. Si se convirtiera en cada tecla, `"12."` se transformaría en `12` y el usuario no podría escribir decimales.
14. **¿Cuándo se convierte a number?** En `handleSubmit`, **después** de que `validarParcela` no encontró errores.
15. **Riesgo de `Number()` sin validar:** `Number('abc')` da `NaN` y `Number('')` da `0`; se enviaría un dato inválido al backend.
16. **Relación 1:N en el select:** cada parcela elige **un** predio, y muchas parcelas pueden elegir el mismo.
17. **¿Por qué el option muestra el nombre y usa el id?** El usuario reconoce "San Isidro", pero la API necesita `predioId: 1`.
18. **`Object.keys(errors).length > 0`:** cuenta cuántos campos tienen error. Si hay alguno, no se arma el payload.
19. **¿Por qué los errores son estado?** Porque deben aparecer y desaparecer en pantalla, y para eso React tiene que volver a dibujar.
20. **Ventaja de un solo `formData`:** un solo `handleChange` sirve para todos los campos, y limpiar el formulario es una sola línea.
21. **Desventaja de un `handleChange` genérico:** si un `name` está mal escrito, TypeScript no lo detecta y se crea una propiedad que no existe. Además, todos los campos se tratan igual.
22. **Frontend vs Spring Boot:** ver el texto de la práctica evaluada.
23. **Modelo vs tipo de formulario:** el modelo (`Parcela`) describe datos ya válidos que vienen de la API. `ParcelaFormData` describe lo que se está escribiendo, aunque todavía esté incompleto o mal.
24. **Payload:** es el objeto que se enviará a la API. Aquí es un `CrearParcelaRequest`: sin `id`, con textos limpios, código en mayúsculas y números convertidos.
25. **Recorrido completo:** escribo una letra → el input dispara `onChange` → `handleChange` lee `name` y `value` → `setFormData` crea un objeto nuevo → React vuelve a dibujar y el input muestra la letra. Al presionar Guardar → `onSubmit` → `preventDefault()` → `validarParcela` → si no hay errores, se arma `parcelaPayload` con `Number()`, `trim()` y `toUpperCase()` → `console.log` (en la Guía 05 será `POST /api/plots`).

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
