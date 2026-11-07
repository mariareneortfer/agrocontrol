// Datos de un predio tal como los devuelve el backend (GET /api/farms).
// Escrito a mano a partir de la respuesta JSON, no copiado de la clase Java.
export interface Predio {
  id: number
  nombre: string
  ubicacion: string | null
  descripcion: string | null
}
