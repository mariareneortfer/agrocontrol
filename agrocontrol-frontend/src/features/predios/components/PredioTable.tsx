import type { Parcela } from '../../parcelas/models/Parcela'
import type { Predio } from '../models/Predio'

interface PredioTableProps {
  predios: Predio[]
  parcelas: Parcela[]
}

// Tabla de predios. Solo MUESTRA lo que recibe por props; no sabe de donde vienen los datos.
// Recibe tambien las parcelas para contar cuantas tiene cada predio (relacion 1:N).
export default function PredioTable({ predios, parcelas }: PredioTableProps) {
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
            </tr>
          </thead>
          <tbody>
            {predios.map((predio) => {
              const totalParcelas = contarParcelas(predio.id)
              return (
                <tr key={predio.id}>
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
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
