import { useEffect, useState } from 'react'
import { mensajeDeError } from '../../../services/http/apiClient'
import type { Predio } from '../../predios/models/Predio'
import { predioService } from '../../predios/services/predioService'
import ParcelaForm from '../components/ParcelaForm'
import ParcelaTable from '../components/ParcelaTable'
import type { Parcela } from '../models/Parcela'
import { parcelaService } from '../services/parcelaService'

// Guia 05: parcelas y predios reales. Los predios sirven para la tabla (nombre del predio)
// y para el select del formulario.
export default function ParcelasPage() {
  const [parcelas, setParcelas] = useState<Parcela[]>([])
  const [predios, setPredios] = useState<Predio[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [mostrarFormulario, setMostrarFormulario] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    const cargarDatos = async () => {
      try {
        const [parcelasApi, prediosApi] = await Promise.all([
          parcelaService.listar(controller.signal),
          predioService.listar(controller.signal),
        ])
        setParcelas(parcelasApi)
        setPredios(prediosApi)
      } catch (err) {
        if (err instanceof DOMException && err.name === 'AbortError') return
        setError(mensajeDeError(err))
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }

    void cargarDatos()
    return () => controller.abort()
  }, [])

  const total = parcelas.length
  const conUbicacion = parcelas.filter((parcela) => parcela.ubicacionReferencial !== null).length

  return (
    <section className="feature-page">
      <div className="page-heading">
        <div>
          <p className="page-heading__eyebrow">Gestión de parcelas</p>
          <h2>Parcelas</h2>
          <p>Datos reales del backend: GET /api/plots, asociadas a predios mediante predioId.</p>
        </div>
        <button
          type="button"
          className="btn-primary"
          onClick={() => setMostrarFormulario((prev) => !prev)}
          disabled={loading || error !== ''}
        >
          {mostrarFormulario ? 'Cerrar formulario' : '+ Nueva parcela'}
        </button>
      </div>

      {mostrarFormulario && (
        <ParcelaForm
          predios={predios}
          onCreated={(nueva) => setParcelas((prev) => [...prev, nueva])}
        />
      )}

      {loading && <div className="state-card">Cargando parcelas...</div>}
      {error && <div className="state-card error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="stats-grid">
            <article className="stat-card"><span>Total</span><strong>{total}</strong></article>
            <article className="stat-card"><span>Con ubicación</span><strong>{conUbicacion}</strong></article>
            <article className="stat-card"><span>Sin ubicación</span><strong>{total - conUbicacion}</strong></article>
          </div>
          <ParcelaTable parcelas={parcelas} predios={predios} />
        </>
      )}
    </section>
  )
}
