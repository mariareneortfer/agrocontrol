import { useEffect, useState } from 'react'
import { mensajeDeError } from '../../../services/http/apiClient'
import type { Parcela } from '../../parcelas/models/Parcela'
import { parcelaService } from '../../parcelas/services/parcelaService'
import PredioForm from '../components/PredioForm'
import PredioTable from '../components/PredioTable'
import type { Predio } from '../models/Predio'
import { predioService } from '../services/predioService'

// Guia 05: los datos ya no salen de los mocks sino del backend (GET /api/farms y GET /api/plots).
export default function PrediosPage() {
  const [predios, setPredios] = useState<Predio[]>([])
  const [parcelas, setParcelas] = useState<Parcela[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [mostrarFormulario, setMostrarFormulario] = useState(false)

  useEffect(() => {
    // AbortController cancela la peticion si el usuario sale de la pagina antes de que responda.
    const controller = new AbortController()

    const cargarDatos = async () => {
      try {
        // Las dos peticiones salen al mismo tiempo; se espera a que terminen ambas.
        const [prediosApi, parcelasApi] = await Promise.all([
          predioService.listar(controller.signal),
          parcelaService.listar(controller.signal),
        ])
        setPredios(prediosApi)
        setParcelas(parcelasApi)
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

  const total = predios.length
  const conParcelas = predios.filter((predio) =>
    parcelas.some((parcela) => parcela.predioId === predio.id),
  ).length

  return (
    <section className="feature-page">
      <div className="page-heading">
        <div>
          <p className="page-heading__eyebrow">Gestión de predios</p>
          <h2>Predios</h2>
          <p>Datos reales del backend: GET /api/farms.</p>
        </div>
        <button
          type="button"
          className="btn-primary"
          onClick={() => setMostrarFormulario((prev) => !prev)}
          disabled={loading || error !== ''}
        >
          {mostrarFormulario ? 'Cerrar formulario' : '+ Nuevo predio'}
        </button>
      </div>

      {mostrarFormulario && (
        <PredioForm
          // Se agrega a la tabla el objeto que devolvio el backend, con su id real.
          onCreated={(nuevo) => setPredios((prev) => [...prev, nuevo])}
        />
      )}

      {loading && <div className="state-card">Cargando predios...</div>}
      {error && <div className="state-card error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="stats-grid">
            <article className="stat-card"><span>Total</span><strong>{total}</strong></article>
            <article className="stat-card"><span>Con parcelas</span><strong>{conParcelas}</strong></article>
            <article className="stat-card"><span>Sin parcelas</span><strong>{total - conParcelas}</strong></article>
          </div>
          <PredioTable predios={predios} parcelas={parcelas} />
        </>
      )}
    </section>
  )
}
