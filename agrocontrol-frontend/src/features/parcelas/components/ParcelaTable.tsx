import type { Predio } from '../../predios/models/Predio'
import type { Parcela } from '../models/Parcela'

interface ParcelaTableProps {
  parcelas: Parcela[]
  // Los predios ya cargados por la pagina: la tabla NO hace un GET por cada fila.
  predios: Predio[]
  onEdit: (id: number) => void
  onDelete: (parcela: Parcela) => void
  editingId?: number | null
  deletingId?: number | null
  accionesBloqueadas?: boolean
}

export default function ParcelaTable({
  parcelas,
  predios,
  onEdit,
  onDelete,
  editingId,
  deletingId,
  accionesBloqueadas = false,
}: ParcelaTableProps) {
  // Map id -> Predio: buscar el nombre del predio de cada fila es directo, sin recorrer el arreglo.
  const predioPorId = new Map(predios.map((predio) => [predio.id, predio]))

  if (parcelas.length === 0) {
    return <div className="empty-state">No hay parcelas para mostrar.</div>
  }

  return (
    <div className="table-card">
      <div className="table-responsive">
        <table className="data-table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Superficie</th>
              <th>Predio</th>
              <th>Ubicación referencial</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {parcelas.map((parcela) => {
              const predio = predioPorId.get(parcela.predioId)
              return (
                <tr key={parcela.id} className={editingId === parcela.id ? 'row-editing' : undefined}>
                  <td className="code-cell">{parcela.codigo}</td>
                  <td>{parcela.nombre}</td>
                  <td>{parcela.superficie.toFixed(2)} {parcela.unidadSuperficie}</td>
                  <td>{predio ? predio.nombre : `Sin predio asociado (#${parcela.predioId})`}</td>
                  <td>
                    {parcela.ubicacionReferencial ?? (
                      <span className="status-badge status-inactive">Sin ubicación</span>
                    )}
                  </td>
                  <td>
                    <div className="actions-cell">
                      <button
                        type="button"
                        className="btn-small"
                        onClick={() => onEdit(parcela.id)}
                        disabled={accionesBloqueadas}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className="btn-small btn-danger"
                        onClick={() => onDelete(parcela)}
                        disabled={accionesBloqueadas}
                      >
                        {deletingId === parcela.id ? 'Eliminando...' : 'Eliminar'}
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
