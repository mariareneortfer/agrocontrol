import type { Parcela } from '../../parcelas/models/Parcela'
import type { Predio } from '../models/Predio'

interface PredioTableProps {
  predios: Predio[]
  parcelas: Parcela[]
  // La tabla solo AVISA que se pidio una accion; no llama al service ni toca la API.
  onEdit: (id: number) => void
  onDelete: (predio: Predio) => void
  editingId?: number | null
  deletingId?: number | null
  // true mientras otra accion esta en curso (cargando detalle, guardando o eliminando).
  accionesBloqueadas?: boolean
}

export default function PredioTable({
  predios,
  parcelas,
  onEdit,
  onDelete,
  editingId,
  deletingId,
  accionesBloqueadas = false,
}: PredioTableProps) {
  const contarParcelas = (predioId: number) =>
    parcelas.filter((parcela) => parcela.predioId === predioId).length

  if (predios.length === 0) {
    return <div className="empty-state">No hay predios registrados.</div>
  }

  return (
    <div className="table-card">
      <div className="table-responsive">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Predio</th>
              <th>Ubicación</th>
              <th>Descripción</th>
              <th>Parcelas</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {predios.map((predio) => {
              const totalParcelas = contarParcelas(predio.id)
              const eliminando = deletingId === predio.id
              return (
                <tr key={predio.id} className={editingId === predio.id ? 'row-editing' : undefined}>
                  <td>{predio.id}</td>
                  <td className="name-cell">{predio.nombre}</td>
                  <td>{predio.ubicacion ?? '—'}</td>
                  <td>{predio.descripcion ?? '—'}</td>
                  <td>{totalParcelas}</td>
                  <td>
                    <span className={`status-badge ${totalParcelas > 0 ? 'status-active' : 'status-inactive'}`}>
                      {totalParcelas > 0 ? 'Con parcelas' : 'Sin parcelas'}
                    </span>
                  </td>
                  <td>
                    <div className="actions-cell">
                      <button
                        type="button"
                        className="btn-small"
                        onClick={() => onEdit(predio.id)}
                        disabled={accionesBloqueadas}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className="btn-small btn-danger"
                        onClick={() => onDelete(predio)}
                        disabled={accionesBloqueadas}
                      >
                        {eliminando ? 'Eliminando...' : 'Eliminar'}
                      </button>
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
