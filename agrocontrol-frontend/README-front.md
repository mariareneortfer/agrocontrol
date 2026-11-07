# agrocontrol-frontend

Frontend de **AgroControl** (proyecto 16) con **React + TypeScript + Vite**, creado y ejecutado desde **WebStorm**.
Consumirá más adelante la API REST del backend Spring Boot (`agrocontrol-backend`, puerto 8080).

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
