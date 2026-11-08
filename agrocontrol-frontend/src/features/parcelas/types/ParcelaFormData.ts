// Lo que el usuario esta escribiendo en el formulario de parcela.
// predioId y superficie son string mientras se escribe: el <select> y el <input>
// entregan texto. Se convierten a number recien despues de validar.
export interface ParcelaFormData {
  predioId: string
  codigo: string
  nombre: string
  superficie: string
  unidadSuperficie: string
  ubicacionReferencial: string
  mantenerPredio: boolean
}
