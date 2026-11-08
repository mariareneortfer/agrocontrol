import { useEffect, useState, type ChangeEvent } from 'react'
import { mensajeDeError } from '../../../services/http/apiClient'
import type { Predio } from '../../predios/models/Predio'
import { predioService } from '../../predios/services/predioService'
import ParcelaForm, { type ModoFormulario } from '../components/ParcelaForm'
import ParcelaTable from '../components/ParcelaTable'
import type { Parcela } from '../models/Parcela'
import { parcelaService } from '../services/parcelaService'

// Guia 07: CRUD completo de Parcela + relacion Predio 1:N
// (selector de predio, cambio de predio con PUT y filtro por predio).
export default function ParcelasPage() {
  const [parcelas, setParcelas] = useState<Parcela[]>([])
  const [predios, setPredios] = useState<Predio[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [editingParcela, setEditingParcela] = useState<Parcela | null>(null)
  const [loadingDetailId, setLoadingDetailId] = useState<number | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [guardando, setGuardando] = useState(false)
  const [aviso, setAviso] = useState('')
  const [actionError, setActionError] = useState('')
  // Filtro por predio: '' = todos. Se guarda como texto porque viene de un <select>.
  const [predioFiltro, setPredioFiltro] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    const cargarDatos = async () => {
      try {
        // Dos consultas independientes en paralelo: parcelas para la tabla y predios para
        // el selector, el filtro y el nombre del predio en cada fila.
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

  const limpiarMensajes = () => {
    setAviso('')
    setActionError('')
  }

  // EDITAR paso 1: GET /api/plots/{id} con la version actual de la parcela.
  const handleEdit = async (id: number) => {
    limpiarMensajes()
    try {
      setLoadingDetailId(id)
      const parcela = await parcelaService.obtenerPorId(id)
      setEditingParcela(parcela)
      setMostrarFormulario(true)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } catch (err) {
      setActionError(mensajeDeError(err))
    } finally {
      setLoadingDetailId(null)
    }
  }

  const nombrePredio = (predioId: number) =>
    predios.find((predio) => predio.id === predioId)?.nombre ?? `#${predioId}`

  const handleSaved = (guardada: Parcela, modo: ModoFormulario) => {
    if (modo === 'create') {
      setParcelas((prev) => [...prev, guardada])
      return
    }
    // Se compara con la version anterior para avisar si cambio de predio.
    const anterior = parcelas.find((parcela) => parcela.id === guardada.id)
    setParcelas((prev) => prev.map((parcela) => (parcela.id === guardada.id ? guardada : parcela)))
    setEditingParcela(null)
    setAviso(
      anterior && anterior.predioId !== guardada.predioId
        ? `Parcela ${guardada.codigo} movida de ${nombrePredio(anterior.predioId)} a ${nombrePredio(guardada.predioId)}.`
        : `Parcela ${guardada.codigo} actualizada correctamente.`,
    )
  }

  const handleCancelEdit = () => {
    setEditingParcela(null)
    limpiarMensajes()
  }

  // ELIMINAR: confirmar -> DELETE /api/plots/{id} -> 204 -> filter(). Si falla, la fila queda.
  const handleDelete = async (parcela: Parcela) => {
    limpiarMensajes()
    const confirmado = window.confirm(
      `¿Eliminar la parcela ${parcela.codigo} "${parcela.nombre}" de ${nombrePredio(parcela.predioId)}?`,
    )
    if (!confirmado) return

    try {
      setDeletingId(parcela.id)
      await parcelaService.eliminar(parcela.id)
      setParcelas((prev) => prev.filter((item) => item.id !== parcela.id))
      if (editingParcela?.id === parcela.id) {
        setEditingParcela(null)
      }
      setAviso(`Parcela ${parcela.codigo} eliminada.`)
    } catch (err) {
      setActionError(mensajeDeError(err))
    } finally {
      setDeletingId(null)
    }
  }

  const alternarFormulario = () => {
    if (mostrarFormulario) {
      setEditingParcela(null)
    }
    setMostrarFormulario((prev) => !prev)
  }

  // Filtro LOCAL: no pide nada al backend; filtra el arreglo que ya esta en memoria.
  // Number() es clave: el select entrega "1" (texto) y predioId es 1 (numero).
  const parcelasVisibles =
    predioFiltro === ''
      ? parcelas
      : parcelas.filter((parcela) => parcela.predioId === Number(predioFiltro))

  const total = parcelasVisibles.length
  const conUbicacion = parcelasVisibles.filter((parcela) => parcela.ubicacionReferencial !== null).length
  const accionesBloqueadas = loadingDetailId !== null || deletingId !== null || guardando

  return (
    <section className="feature-page">
      <div className="page-heading">
        <div>
          <p className="page-heading__eyebrow">Gestión de parcelas</p>
          <h2>Parcelas</h2>
          <p>CRUD completo + relación Predio 1:N (cada parcela pertenece a un predio).</p>
        </div>
        <button
          type="button"
          className="btn-primary"
          onClick={alternarFormulario}
          disabled={loading || error !== ''}
        >
          {mostrarFormulario ? 'Cerrar formulario' : '+ Nueva parcela'}
        </button>
      </div>

      {mostrarFormulario && (
        <ParcelaForm
          key={editingParcela ? `editar-${editingParcela.id}` : 'nueva'}
          predios={predios}
          parcela={editingParcela}
          onSaved={handleSaved}
          onCancelEdit={handleCancelEdit}
          onBusyChange={setGuardando}
        />
      )}

      {loadingDetailId !== null && <div className="state-card">Cargando detalle de la parcela...</div>}
      {aviso && <div className="state-card success">{aviso}</div>}
      {actionError && <div className="state-card error">{actionError}</div>}

      {loading && <div className="state-card">Cargando parcelas...</div>}
      {error && <div className="state-card error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="filter-bar">
            <label>
              Filtrar por predio
              <select value={predioFiltro} onChange={(e: ChangeEvent<HTMLSelectElement>) => setPredioFiltro(e.target.value)}>
                <option value="">Todos los predios</option>
                {predios.map((predio) => (
                  <option key={predio.id} value={String(predio.id)}>
                    {predio.nombre}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div className="stats-grid">
            <article className="stat-card"><span>Total</span><strong>{total}</strong></article>
            <article className="stat-card"><span>Con ubicación</span><strong>{conUbicacion}</strong></article>
            <article className="stat-card"><span>Sin ubicación</span><strong>{total - conUbicacion}</strong></article>
          </div>

          <ParcelaTable
            parcelas={parcelasVisibles}
            predios={predios}
            onEdit={handleEdit}
            onDelete={handleDelete}
            editingId={editingParcela?.id ?? null}
            deletingId={deletingId}
            accionesBloqueadas={accionesBloqueadas}
          />
        </>
      )}
    </section>
  )
}
