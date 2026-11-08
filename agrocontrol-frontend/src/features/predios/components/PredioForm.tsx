import { useState, type ChangeEvent, type FormEvent } from 'react'
import type { CrearPredioRequest } from '../types/CrearPredioRequest'
import type { PredioFormData } from '../types/PredioFormData'
import { validarPredio, type PredioFormErrors } from '../utils/predioValidation'

const initialPredioForm: PredioFormData = {
  nombre: '',
  ubicacion: '',
  descripcion: '',
  limpiarAlGuardar: true,
}

// Texto vacio -> null, porque en la base de datos la columna queda sin valor.
const textoONull = (valor: string) => (valor.trim() ? valor.trim() : null)

export default function PredioForm() {
  const [formData, setFormData] = useState<PredioFormData>(initialPredioForm)
  const [errors, setErrors] = useState<PredioFormErrors>({})
  const [mensaje, setMensaje] = useState('')
  const [enviando, setEnviando] = useState(false)

  const totalErrores = Object.keys(errors).length

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    // El checkbox se lee con checked (true/false); los demas campos con value (texto).
    const nuevoValor =
      e.target instanceof HTMLInputElement && e.target.type === 'checkbox' ? e.target.checked : value
    setFormData((prev) => ({ ...prev, [name]: nuevoValor }))
  }

  const limpiar = () => {
    setFormData(initialPredioForm)
    setErrors({})
    setMensaje('')
  }

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setMensaje('')

    const validationErrors = validarPredio(formData)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    const predioPayload: CrearPredioRequest = {
      nombre: formData.nombre.trim(),
      ubicacion: textoONull(formData.ubicacion),
      descripcion: textoONull(formData.descripcion),
    }

    // Envio simulado de 500 ms: en la Guia 05 aqui ira la llamada real a POST /api/farms.
    setEnviando(true)
    setTimeout(() => {
      console.log('Predio listo para API:', predioPayload)
      setEnviando(false)
      setMensaje('Datos válidos. Predio listo para enviarse al backend.')
      if (formData.limpiarAlGuardar) {
        setFormData(initialPredioForm)
      }
    }, 500)
  }

  return (
    <form className="entity-form" onSubmit={handleSubmit} noValidate>
      <div className="form-grid">
        <label>
          Nombre del predio *
          <input name="nombre" value={formData.nombre} onChange={handleChange} />
          {errors.nombre && <small className="field-error">{errors.nombre}</small>}
        </label>

        <label>
          Ubicación
          <input name="ubicacion" value={formData.ubicacion} onChange={handleChange} />
          {errors.ubicacion && <small className="field-error">{errors.ubicacion}</small>}
        </label>

        <label className="form-span-2">
          Descripción
          <textarea name="descripcion" rows={3} value={formData.descripcion} onChange={handleChange} />
          {errors.descripcion && <small className="field-error">{errors.descripcion}</small>}
        </label>

        <label className="checkbox-field form-span-2">
          <input
            type="checkbox"
            name="limpiarAlGuardar"
            checked={formData.limpiarAlGuardar}
            onChange={handleChange}
          />
          Limpiar el formulario después de guardar
        </label>
      </div>

      {totalErrores > 0 && (
        <div className="form-error-summary">
          El formulario tiene {totalErrores} {totalErrores === 1 ? 'error' : 'errores'}.
        </div>
      )}
      {mensaje && <div className="form-success">{mensaje}</div>}

      <div className="form-actions">
        <button type="button" className="btn-secondary" onClick={limpiar} disabled={enviando}>
          Limpiar
        </button>
        <button type="submit" className="btn-primary" disabled={enviando}>
          {enviando ? 'Guardando...' : 'Guardar predio'}
        </button>
      </div>
    </form>
  )
}
