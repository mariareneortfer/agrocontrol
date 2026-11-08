import type { Predio } from '../../predios/models/Predio'
import type { Parcela } from '../models/Parcela'

interface ParcelaTableProps {
  parcelas: Parcela[]
  predios: Predio[]
}

// Tabla de parcelas. Recibe dos arreglos: las parcelas a mostrar y los predios,
// para convertir predioId (un numero) en el nombre del predio con find().
export default function ParcelaTable({ parcelas, predios }: ParcelaTableProps) {
  const obtenerNombrePredio = (predioId: number) => {
    const predio = predios.find((item) => item.id === predioId)
    return predio ? predio.nombre : 'Sin predio asociado'
  }

  if (parcelas.length === 0) {
    return <div className="empty-state">No hay parcelas registradas.</div>
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
            </tr>
          </thead>
          <tbody>
            {parcelas.map((parcela) => (
              <tr key={parcela.id}>
                <td className="code-cell">{parcela.codigo}</td>
                <td>{parcela.nombre}</td>
                <td>{parcela.superficie.toFixed(2)} {parcela.unidadSuperficie}</td>
                <td>{obtenerNombrePredio(parcela.predioId)}</td>
                <td>
                  {parcela.ubicacionReferencial ?? (
                    <span className="status-badge status-inactive">Sin ubicación</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
