import { useState, type ChangeEvent, type FormEvent } from 'react'
import { ApiError, mensajeDeError } from '../../../services/http/apiClient'
import type { Predio } from '../../predios/models/Predio'
import type { Parcela } from '../models/Parcela'
import { parcelaService } from '../services/parcelaService'
import type { CrearParcelaRequest } from '../types/CrearParcelaRequest'
import type { ParcelaFormData } from '../types/ParcelaFormData'
import { validarParcela, type ParcelaFormErrors } from '../utils/parcelaValidation'

export type ModoFormulario = 'create' | 'edit'

interface ParcelaFormProps {
  // Predios REALES (GET /api/farms) para el selector de predio.
  predios: Predio[]
  // null o ausente = modo CREAR; una Parcela (cargada con GET /plots/{id}) = modo EDITAR.
  parcela?: Parcela | null
  onSaved: (parcela: Parcela, modo: ModoFormulario) => void
  onCancelEdit?: () => void
  onBusyChange?: (ocupado: boolean) => void
}

const initialParcelaForm: ParcelaFormData = {
  predioId: '',
  codigo: '',
  nombre: '',
  superficie: '',
  unidadSuperficie: 'hectarea',
  ubicacionReferencial: '',
  mantenerPredio: false,
}

// La Parcela del backend tiene numeros y null; el formulario trabaja con texto.
const parcelaAFormData = (parcela: Parcela): ParcelaFormData => ({
  predioId: String(parcela.predioId),
  codigo: parcela.codigo,
  nombre: parcela.nombre,
  superficie: String(parcela.superficie),
  unidadSuperficie: parcela.unidadSuperficie,
  ubicacionReferencial: parcela.ubicacionReferencial ?? '',
  mantenerPredio: false,
})

// Mismo formulario para CREAR y EDITAR. La pagina le da una key distinta por cada parcela
// que se edita, asi useState arranca con los datos de esa parcela.
export default function ParcelaForm({ predios, parcela, onSaved, onCancelEdit, onBusyChange }: ParcelaFormProps) {
  const [formData, setFormData] = useState<ParcelaFormData>(() =>
    parcela ? parcelaAFormData(parcela) : initialParcelaForm,
  )
  const [errors, setErrors] = useState<ParcelaFormErrors>({})
  const [mensaje, setMensaje] = useState('')
  const [apiError, setApiError] = useState('')
  const [saving, setSaving] = useState(false)

  const isEditing = parcela != null
  const totalErrores = Object.keys(errors).length
  // El select muestra los predios ordenados por nombre; guarda el id, no el nombre.
  const prediosOrdenados = [...predios].sort((a, b) => a.nombre.localeCompare(b.nombre))
  // Aviso visual cuando, al editar, se elige un predio distinto al original.
  const cambiaDePredio = parcela != null && formData.predioId !== String(parcela.predioId)

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    const nuevoValor =
      e.target instanceof HTMLInputElement && e.target.type === 'checkbox' ? e.target.checked : value
    setFormData((prev) => ({ ...prev, [name]: nuevoValor }))
  }

  const limpiar = () => {
    setFormData(initialParcelaForm)
    setErrors({})
    setMensaje('')
    setApiError('')
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setMensaje('')
    setApiError('')

    const validationErrors = validarParcela(formData, predios)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    // Recien aqui, con los datos ya validados, se convierte texto -> number.
    const ubicacion = formData.ubicacionReferencial.trim()
    const payload: CrearParcelaRequest = {
      predioId: Number(formData.predioId),
      codigo: formData.codigo.trim().toUpperCase(),
      nombre: formData.nombre.trim(),
      superficie: Number(formData.superficie),
      unidadSuperficie: formData.unidadSuperficie,
      ubicacionReferencial: ubicacion ? ubicacion : null,
    }

    try {
      setSaving(true)
      onBusyChange?.(true)
      if (parcela) {
        // PUT: si predioId cambio, el backend mueve la parcela a ese predio (UPDATE predio_id).
        const actualizada = await parcelaService.actualizar(parcela.id, payload)
        onSaved(actualizada, 'edit')
      } else {
        const creada = await parcelaService.crear(payload)
        onSaved(creada, 'create')
        setMensaje(`Parcela ${creada.codigo} creada correctamente (id ${creada.id}).`)
        setFormData((prev) =>
          prev.mantenerPredio
            ? { ...initialParcelaForm, predioId: prev.predioId, mantenerPredio: true }
            : initialParcelaForm,
        )
      }
    } catch (err) {
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
      <h3 className="form-title">
        {parcela ? `Editar parcela ${parcela.codigo} (#${parcela.id})` : 'Nueva parcela'}
      </h3>

      <div className="form-grid">
        <label>
          Predio *
          {/* Select controlado: value + onChange. No se usa "selected" en cada option. */}
          <select name="predioId" value={formData.predioId} onChange={handleChange}>
            <option value="">Seleccione un predio</option>
            {prediosOrdenados.map((predio) => (
              <option key={predio.id} value={String(predio.id)}>
                {predio.nombre}
              </option>
            ))}
          </select>
          {errors.predioId && <small className="field-error">{errors.predioId}</small>}
          {cambiaDePredio && !errors.predioId && (
            <small className="field-hint">Al actualizar, la parcela se moverá a este predio.</small>
          )}
        </label>

        <label>
          Código *
          <input name="codigo" placeholder="P-01" value={formData.codigo} onChange={handleChange} />
          {errors.codigo && <small className="field-error">{errors.codigo}</small>}
        </label>

        <label>
          Nombre *
          <input name="nombre" placeholder="Lote Norte" value={formData.nombre} onChange={handleChange} />
          {errors.nombre && <small className="field-error">{errors.nombre}</small>}
        </label>

        <label>
          Superficie *
          <input
            type="number"
            name="superficie"
            min="0"
            step="0.01"
            value={formData.superficie}
            onChange={handleChange}
          />
          {errors.superficie && <small className="field-error">{errors.superficie}</small>}
        </label>

        <label>
          Unidad de superficie *
          <select name="unidadSuperficie" value={formData.unidadSuperficie} onChange={handleChange}>
            <option value="">Seleccione una unidad</option>
            <option value="hectarea">Hectárea</option>
            <option value="metro_cuadrado">Metro cuadrado</option>
          </select>
          {errors.unidadSuperficie && <small className="field-error">{errors.unidadSuperficie}</small>}
        </label>

        <label>
          Ubicación referencial
          <input
            name="ubicacionReferencial"
            value={formData.ubicacionReferencial}
            onChange={handleChange}
          />
          {errors.ubicacionReferencial && (
            <small className="field-error">{errors.ubicacionReferencial}</small>
          )}
        </label>

        {!isEditing && (
          <label className="checkbox-field form-span-2">
            <input
              type="checkbox"
              name="mantenerPredio"
              checked={formData.mantenerPredio}
              onChange={handleChange}
            />
            Mantener el predio seleccionado para registrar otra parcela
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
          <button type="button" className="btn-secondary" onClick={onCancelEdit} disabled={saving}>
            Cancelar edición
          </button>
        ) : (
          <button type="button" className="btn-secondary" onClick={limpiar} disabled={saving}>
            Limpiar
          </button>
        )}
        <button type="submit" className="btn-primary" disabled={saving}>
          {saving ? 'Guardando...' : isEditing ? 'Actualizar parcela' : 'Crear parcela'}
        </button>
      </div>
    </form>
  )
}
