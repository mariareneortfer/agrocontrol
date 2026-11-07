// Datos de una parcela tal como los devuelve el backend (GET /api/plots).
// predioId es la referencia al predio dueño (relacion 1:N: un predio tiene muchas parcelas).
export interface Parcela {
  id: number
  predioId: number
  codigo: string
  nombre: string
  superficie: number
  unidadSuperficie: string
  ubicacionReferencial: string | null
}
