// Payload: el JSON exacto que espera el backend en POST /api/plots
// (mismo contrato que CrearParcelaRequest.java). Aqui predioId y superficie ya son number.
export interface CrearParcelaRequest {
  predioId: number
  codigo: string
  nombre: string
  superficie: number
  unidadSuperficie: string
  ubicacionReferencial: string | null
}
