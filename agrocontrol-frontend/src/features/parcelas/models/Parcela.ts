// Modelo del frontend para una parcela (contrato de ParcelaResponse en GET /api/plots).
// predioId representa la relacion 1:N: muchas parcelas pueden apuntar al mismo predio.
export interface Parcela {
  id: number
  predioId: number
  codigo: string
  nombre: string
  superficie: number
  unidadSuperficie: string
  ubicacionReferencial: string | null
}
