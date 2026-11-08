import { useEffect, useState } from 'react'
import { mensajeDeError } from '../../../services/http/apiClient'
import type { Parcela } from '../../parcelas/models/Parcela'
import { parcelaService } from '../../parcelas/services/parcelaService'
import PredioForm, { type ModoFormulario } from '../components/PredioForm'
import PredioTable from '../components/PredioTable'
import type { Predio } from '../models/Predio'
import { predioService } from '../services/predioService'

// Guia 06: CRUD completo de Predio (GET, GET/{id}, POST, PUT y DELETE).
// La pagina coordina: llama al service, guarda el estado y decide que se muestra.
export default function PrediosPage() {
  const [predios, setPredios] = useState<Predio[]>([])
  const [parcelas, setParcelas] = useState<Parcela[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [editingPredio, setEditingPredio] = useState<Predio | null>(null)
  const [loadingDetailId, setLoadingDetailId] = useState<number | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [guardando, setGuardando] = useState(false)
  const [aviso, setAviso] = useState('')
  const [actionError, setActionError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    const cargarDatos = async () => {
      try {
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

  const limpiarMensajes = () => {
    setAviso('')
    setActionError('')
  }

  // EDITAR paso 1: GET /api/farms/{id} trae la version ACTUAL del predio antes de editar.
  // Si alguien lo borro mientras tanto, el backend responde 404 y no se abre el formulario.
  const handleEdit = async (id: number) => {
    limpiarMensajes()
    try {
      setLoadingDetailId(id)
      const predio = await predioService.obtenerPorId(id)
      setEditingPredio(predio)
      setMostrarFormulario(true)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } catch (err) {
      setActionError(mensajeDeError(err))
    } finally {
      setLoadingDetailId(null)
    }
  }

  // Se usa el objeto que devolvio el backend (POST 201 / PUT 200), nunca datos inventados.
  const handleSaved = (guardado: Predio, modo: ModoFormulario) => {
    if (modo === 'create') {
      setPredios((prev) => [...prev, guardado])
      return
    }
    // PUT: map() reemplaza SOLO la fila con ese id; las demas quedan iguales.
    setPredios((prev) => prev.map((predio) => (predio.id === guardado.id ? guardado : predio)))
    setEditingPredio(null)
    setAviso(`Predio "${guardado.nombre}" actualizado correctamente.`)
  }

  const handleCancelEdit = () => {
    setEditingPredio(null)
    limpiarMensajes()
  }

  // ELIMINAR: confirmar -> DELETE /api/farms/{id} -> 204 -> filter() quita la fila.
  // Si el backend responde 409 (tiene parcelas), la fila NO se quita y se muestra el error.
  const handleDelete = async (predio: Predio) => {
    limpiarMensajes()
    const confirmado = window.confirm(
      `¿Eliminar el predio "${predio.nombre}"? Esta acción no se puede deshacer.`,
    )
    if (!confirmado) return

    try {
      setDeletingId(predio.id)
      await predioService.eliminar(predio.id)
      setPredios((prev) => prev.filter((item) => item.id !== predio.id))
      if (editingPredio?.id === predio.id) {
        setEditingPredio(null)
      }
      setAviso(`Predio "${predio.nombre}" eliminado.`)
    } catch (err) {
      setActionError(mensajeDeError(err))
    } finally {
      setDeletingId(null)
    }
  }

  const alternarFormulario = () => {
    if (mostrarFormulario) {
      setEditingPredio(null)
    }
    setMostrarFormulario((prev) => !prev)
  }

  const total = predios.length
  const conParcelas = predios.filter((predio) =>
    parcelas.some((parcela) => parcela.predioId === predio.id),
  ).length
  const accionesBloqueadas = loadingDetailId !== null || deletingId !== null || guardando

  return (
    <section className="feature-page">
      <div className="page-heading">
        <div>
          <p className="page-heading__eyebrow">Gestión de predios</p>
          <h2>Predios</h2>
          <p>CRUD completo conectado con Spring Boot: GET, GET/{'{id}'}, POST, PUT y DELETE.</p>
        </div>
        <button
          type="button"
          className="btn-primary"
          onClick={alternarFormulario}
          disabled={loading || error !== ''}
        >
          {mostrarFormulario ? 'Cerrar formulario' : '+ Nuevo predio'}
        </button>
      </div>

      {mostrarFormulario && (
        <PredioForm
          // key distinta = formulario nuevo con los datos de ESE predio (o vacio para crear).
          key={editingPredio ? `editar-${editingPredio.id}` : 'nuevo'}
          predio={editingPredio}
          onSaved={handleSaved}
          onCancelEdit={handleCancelEdit}
          onBusyChange={setGuardando}
        />
      )}

      {loadingDetailId !== null && <div className="state-card">Cargando detalle del predio...</div>}
      {aviso && <div className="state-card success">{aviso}</div>}
      {actionError && <div className="state-card error">{actionError}</div>}

      {loading && <div className="state-card">Cargando predios...</div>}
      {error && <div className="state-card error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="stats-grid">
            <article className="stat-card"><span>Total</span><strong>{total}</strong></article>
            <article className="stat-card"><span>Con parcelas</span><strong>{conParcelas}</strong></article>
            <article className="stat-card"><span>Sin parcelas</span><strong>{total - conParcelas}</strong></article>
          </div>
          <PredioTable
            predios={predios}
            parcelas={parcelas}
            onEdit={handleEdit}
            onDelete={handleDelete}
            editingId={editingPredio?.id ?? null}
            deletingId={deletingId}
            accionesBloqueadas={accionesBloqueadas}
          />
        </>
      )}
    </section>
  )
}
