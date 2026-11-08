import { useState, type ChangeEvent, type FormEvent } from 'react'
import { prediosMock } from '../../predios/data/predios.mock'
import type { CrearParcelaRequest } from '../types/CrearParcelaRequest'
import type { ParcelaFormData } from '../types/ParcelaFormData'
import { validarParcela, type ParcelaFormErrors } from '../utils/parcelaValidation'

const initialParcelaForm: ParcelaFormData = {
  predioId: '',
  codigo: '',
  nombre: '',
  superficie: '',
  unidadSuperficie: 'hectarea',
  ubicacionReferencial: '',
  mantenerPredio: false,
}

// El select muestra los predios ordenados por nombre; guarda el id, no el nombre.
const prediosOrdenados = [...prediosMock].sort((a, b) => a.nombre.localeCompare(b.nombre))

export default function ParcelaForm() {
  const [formData, setFormData] = useState<ParcelaFormData>(initialParcelaForm)
  const [errors, setErrors] = useState<ParcelaFormErrors>({})
  const [mensaje, setMensaje] = useState('')
  const [enviando, setEnviando] = useState(false)

  const totalErrores = Object.keys(errors).length

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
  }

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setMensaje('')

    const validationErrors = validarParcela(formData)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    // Recien aqui, con los datos ya validados, se convierte texto -> number.
    const ubicacion = formData.ubicacionReferencial.trim()
    const parcelaPayload: CrearParcelaRequest = {
      predioId: Number(formData.predioId),
      codigo: formData.codigo.trim().toUpperCase(),
      nombre: formData.nombre.trim(),
      superficie: Number(formData.superficie),
      unidadSuperficie: formData.unidadSuperficie,
      ubicacionReferencial: ubicacion ? ubicacion : null,
    }

    // Envio simulado de 500 ms: en la Guia 05 aqui ira la llamada real a POST /api/plots.
    setEnviando(true)
    setTimeout(() => {
      console.log('Parcela lista para API:', parcelaPayload)
      setEnviando(false)
      setMensaje('Datos válidos. Parcela lista para enviarse al backend.')
      // Si se marco la casilla, se limpia todo menos el predio, para cargar otra parcela seguida.
      setFormData((prev) =>
        prev.mantenerPredio
          ? { ...initialParcelaForm, predioId: prev.predioId, mantenerPredio: true }
          : initialParcelaForm,
      )
    }, 500)
  }

  return (
    <form className="entity-form" onSubmit={handleSubmit} noValidate>
      <div className="form-grid">
        <label>
          Predio *
          <select name="predioId" value={formData.predioId} onChange={handleChange}>
            <option value="">Seleccione un predio</option>
            {prediosOrdenados.map((predio) => (
              <option key={predio.id} value={predio.id}>
                {predio.nombre}
              </option>
            ))}
          </select>
          {errors.predioId && <small className="field-error">{errors.predioId}</small>}
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

        <label className="checkbox-field form-span-2">
          <input
            type="checkbox"
            name="mantenerPredio"
            checked={formData.mantenerPredio}
            onChange={handleChange}
          />
          Mantener el predio seleccionado para registrar otra parcela
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
          {enviando ? 'Guardando...' : 'Guardar parcela'}
        </button>
      </div>
    </form>
  )
}
