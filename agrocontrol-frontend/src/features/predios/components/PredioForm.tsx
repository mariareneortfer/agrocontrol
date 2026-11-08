import { useState, type ChangeEvent, type FormEvent } from 'react'
import { ApiError, mensajeDeError } from '../../../services/http/apiClient'
import type { Predio } from '../models/Predio'
import { predioService } from '../services/predioService'
import type { PredioFormData } from '../types/PredioFormData'
import { validarPredio, type PredioFormErrors } from '../utils/predioValidation'

export type ModoFormulario = 'create' | 'edit'

interface PredioFormProps {
  // null o ausente = modo CREAR; un Predio (cargado con GET /farms/{id}) = modo EDITAR.
  predio?: Predio | null
  onSaved: (predio: Predio, modo: ModoFormulario) => void
  onCancelEdit?: () => void
  // Avisa a la pagina que hay un POST/PUT en curso, para bloquear Editar/Eliminar.
  onBusyChange?: (ocupado: boolean) => void
}

const initialPredioForm: PredioFormData = {
  nombre: '',
  ubicacion: '',
  descripcion: '',
  limpiarAlGuardar: true,
}

// Convierte el Predio que llego del backend (con null) en datos de formulario (texto).
const predioAFormData = (predio: Predio): PredioFormData => ({
  nombre: predio.nombre,
  ubicacion: predio.ubicacion ?? '',
  descripcion: predio.descripcion ?? '',
  limpiarAlGuardar: true,
})

// Texto vacio -> null, porque en la base de datos la columna queda sin valor.
const textoONull = (valor: string) => (valor.trim() ? valor.trim() : null)

// El mismo formulario sirve para CREAR y EDITAR.
// La pagina le pone una "key" distinta por cada predio que se edita: al cambiar la key,
// React crea el formulario de nuevo y useState toma los datos de ese predio.
export default function PredioForm({ predio, onSaved, onCancelEdit, onBusyChange }: PredioFormProps) {
  const [formData, setFormData] = useState<PredioFormData>(() =>
    predio ? predioAFormData(predio) : initialPredioForm,
  )
  const [errors, setErrors] = useState<PredioFormErrors>({})
  const [mensaje, setMensaje] = useState('')
  const [apiError, setApiError] = useState('')
  const [saving, setSaving] = useState(false)

  const isEditing = predio != null
  const totalErrores = Object.keys(errors).length

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
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

    // Mismo payload para crear y actualizar (CrearPredioRequest y ActualizarPredioRequest).
    const payload = {
      nombre: formData.nombre.trim(),
      ubicacion: textoONull(formData.ubicacion),
      descripcion: textoONull(formData.descripcion),
    }

    try {
      setSaving(true)
      onBusyChange?.(true)
      // POST o PUT NO depende del texto del boton: depende de si existe un predio con id.
      if (predio) {
        const actualizado = await predioService.actualizar(predio.id, payload)
        onSaved(actualizado, 'edit')
      } else {
        const creado = await predioService.crear(payload)
        onSaved(creado, 'create')
        setMensaje(`Predio "${creado.nombre}" creado correctamente (id ${creado.id}).`)
        if (formData.limpiarAlGuardar) {
          setFormData(initialPredioForm)
        }
      }
    } catch (err) {
      // 400 trae fieldErrors del backend: se muestran junto a cada campo.
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setErrors(err.fieldErrors)
      }
      setApiError(mensajeDeError(err))
    } finally {
      setSaving(false)
      onBusyChange?.(false)
    }
  }

  return (
    <form className={`entity-form${isEditing ? ' entity-form--editing' : ''}`} onSubmit={handleSubmit} noValidate>
      <h3 className="form-title">{predio ? `Editar predio #${predio.id}` : 'Nuevo predio'}</h3>

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

        {!isEditing && (
          <label className="checkbox-field form-span-2">
            <input
              type="checkbox"
              name="limpiarAlGuardar"
              checked={formData.limpiarAlGuardar}
              onChange={handleChange}
            />
            Limpiar el formulario después de guardar
          </label>
        )}
      </div>

      {totalErrores > 0 && (
        <div className="form-error-summary">
          El formulario tiene {totalErrores} {totalErrores === 1 ? 'error' : 'errores'}.
        </div>
      )}
      {apiError && <div className="form-error">{apiError}</div>}
      {mensaje && <div className="form-success">{mensaje}</div>}

      <div className="form-actions">
        {isEditing ? (
          // Cancelar NO hace ninguna peticion HTTP: solo vuelve al modo crear.
          <button type="button" className="btn-secondary" onClick={onCancelEdit} disabled={saving}>
            Cancelar edición
          </button>
        ) : (
          <button type="button" className="btn-secondary" onClick={limpiar} disabled={saving}>
            Limpiar
          </button>
        )}
        <button type="submit" className="btn-primary" disabled={saving}>
          {saving ? 'Guardando...' : isEditing ? 'Actualizar predio' : 'Crear predio'}
        </button>
      </div>
    </form>
  )
}
