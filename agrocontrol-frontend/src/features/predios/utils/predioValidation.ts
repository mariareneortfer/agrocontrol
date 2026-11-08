import type { PredioFormData } from '../types/PredioFormData'

export type PredioFormErrors = Partial<Record<keyof PredioFormData, string>>

// Reglas copiadas de CrearPredioRequest.java y de la tabla predio,
// mas una regla propia del frontend (nombre de al menos 3 caracteres).
export function validarPredio(data: PredioFormData): PredioFormErrors {
  const errors: PredioFormErrors = {}
  const nombre = data.nombre.trim()

  if (!nombre) {
    errors.nombre = 'El nombre del predio es obligatorio.'
  } else if (nombre.length < 3) {
    errors.nombre = 'El nombre debe tener al menos 3 caracteres.'
  } else if (nombre.length > 100) {
    errors.nombre = 'El nombre no puede superar 100 caracteres.'
  }

  if (data.ubicacion.trim().length > 200) {
    errors.ubicacion = 'La ubicación no puede superar 200 caracteres.'
  }

  if (data.descripcion.trim().length > 1000) {
    errors.descripcion = 'La descripción no puede superar 1000 caracteres.'
  }

  return errors
}
