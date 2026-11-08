import { useState, type ChangeEvent, type FormEvent } from 'react'
import { ApiError, mensajeDeError } from '../../../services/http/apiClient'
import type { Predio } from '../models/Predio'
import { predioService } from '../services/predioService'
import type { CrearPredioRequest } from '../types/CrearPredioRequest'
import type { PredioFormData } from '../types/PredioFormData'
import { validarPredio, type PredioFormErrors } from '../utils/predioValidation'

interface PredioFormProps {
  // La pagina recibe el predio que devolvio el backend (con su id real).
  onCreated?: (predio: Predio) => void
}

const initialPredioForm: PredioFormData = {
  nombre: '',
  ubicacion: '',
  descripcion: '',
  limpiarAlGuardar: true,
}

// Texto vacio -> null, porque en la base de datos la columna queda sin valor.
const textoONull = (valor: string) => (valor.trim() ? valor.trim() : null)

export default function PredioForm({ onCreated }: PredioFormProps) {
  const [formData, setFormData] = useState<PredioFormData>(initialPredioForm)
  const [errors, setErrors] = useState<PredioFormErrors>({})
  const [mensaje, setMensaje] = useState('')
  const [apiError, setApiError] = useState('')
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
    setApiError('')
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setMensaje('')
    setApiError('')

    const validationErrors = validarPredio(formData)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    const predioPayload: CrearPredioRequest = {
      nombre: formData.nombre.trim(),
      ubicacion: textoONull(formData.ubicacion),
      descripcion: textoONull(formData.descripcion),
    }

    try {
      setEnviando(true)
      // POST /api/farms: el backend valida, guarda en PostgreSQL y responde 201 con el predio creado.
      const creado = await predioService.crear(predioPayload)
      onCreated?.(creado)
      setMensaje(`Predio "${creado.nombre}" creado correctamente (id ${creado.id}).`)
      if (formData.limpiarAlGuardar) {
        setFormData(initialPredioForm)
      }
    } catch (err) {
      // 400 trae fieldErrors del backend: se muestran junto a cada campo.
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setErrors(err.fieldErrors)
      }
      setApiError(mensajeDeError(err))
    } finally {
      setEnviando(false)
    }
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
      {apiError && <div className="form-error">{apiError}</div>}
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
