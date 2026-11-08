// Payload de PUT /api/plots/{id} (mismo contrato que ActualizarParcelaRequest.java).
// Si predioId es distinto al actual, la parcela se MUEVE a ese predio (cambia la FK predio_id).
// El id de la parcela NO va en el body: viaja en la URL.
export interface ActualizarParcelaRequest {
  predioId: number
  codigo: string
  nombre: string
  superficie: number
  unidadSuperficie: string
  ubicacionReferencial: string | null
}
