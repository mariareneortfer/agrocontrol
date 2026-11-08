// Payload de PUT /api/farms/{id} (mismo contrato que ActualizarPredioRequest.java).
// Hoy tiene la misma forma que CrearPredioRequest, pero se nombra por su INTENCION:
// si manana editar exige otros campos, el cambio queda solo aqui.
// El id NO va en el body: viaja en la URL.
export interface ActualizarPredioRequest {
  nombre: string
  ubicacion: string | null
  descripcion: string | null
}
