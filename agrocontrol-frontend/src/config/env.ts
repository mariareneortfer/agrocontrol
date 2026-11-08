// Unico lugar donde se leen las variables de entorno (Guia 01: carpeta config/).
// Si cambia el puerto del backend, se cambia .env.development y nada mas.
export const API_URL: string = import.meta.env.VITE_API_URL ?? ''
