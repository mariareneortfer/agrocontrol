// Lo que el usuario esta escribiendo en el formulario de predio.
// Todo es texto porque asi lo entregan los inputs; limpiarAlGuardar es una opcion
// de la pantalla (checkbox) y NO se envia al backend.
export interface PredioFormData {
  nombre: string
  ubicacion: string
  descripcion: string
  limpiarAlGuardar: boolean
}
