import type { ParcelaFormData } from '../types/ParcelaFormData'

export type ParcelaFormErrors = Partial<Record<keyof ParcelaFormData, string>>

// Hasta 8 enteros y 2 decimales, como NUMERIC(10,2) y @Digits(integer = 8, fraction = 2).
const FORMATO_SUPERFICIE = /^\d{1,8}(\.\d{1,2})?$/

// Reglas copiadas de CrearParcelaRequest.java y de la tabla parcela.
export function validarParcela(data: ParcelaFormData): ParcelaFormErrors {
  const errors: ParcelaFormErrors = {}
  const codigo = data.codigo.trim()
  const superficieTexto = data.superficie.trim()

  if (!data.predioId) errors.predioId = 'Debe seleccionar un predio.'

  if (!codigo) {
    errors.codigo = 'El código es obligatorio.'
  } else if (codigo.length > 20) {
    errors.codigo = 'El código no puede superar 20 caracteres.'
  }

  if (!data.nombre.trim()) {
    errors.nombre = 'El nombre es obligatorio.'
  } else if (data.nombre.trim().length > 100) {
    errors.nombre = 'El nombre no puede superar 100 caracteres.'
  }

  if (!superficieTexto) {
    errors.superficie = 'La superficie es obligatoria.'
  } else if (!FORMATO_SUPERFICIE.test(superficieTexto)) {
    errors.superficie = 'Use un número con hasta 2 decimales (ejemplo: 12.50).'
  } else if (Number(superficieTexto) <= 0) {
    errors.superficie = 'La superficie debe ser mayor que cero.'
  }

  if (!data.unidadSuperficie) errors.unidadSuperficie = 'Debe elegir la unidad de superficie.'

  if (data.ubicacionReferencial.trim().length > 200) {
    errors.ubicacionReferencial = 'La ubicación referencial no puede superar 200 caracteres.'
  }

  return errors
}
