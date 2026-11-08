// Payload: el JSON exacto que espera el backend en POST /api/farms
// (mismo contrato que CrearPredioRequest.java). Se usara en la Guia 05.
export interface CrearPredioRequest {
  nombre: string
  ubicacion: string | null
  descripcion: string | null
}
