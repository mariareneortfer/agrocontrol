import PredioTable from '../components/PredioTable'
import { prediosMock } from '../data/predios.mock'
import { parcelasMock } from '../../parcelas/data/parcelas.mock'

// La pagina coordina: elige los datos (por ahora los mocks), calcula las estadisticas
// y le pasa los arreglos a la tabla por props.
export default function PrediosPage() {
  const total = prediosMock.length
  const conParcelas = prediosMock.filter((predio) =>
    parcelasMock.some((parcela) => parcela.predioId === predio.id),
  ).length

  return (
    <section className="feature-page">
      <div className="page-heading">
        <div>
          <p className="page-heading__eyebrow">Gestión de predios</p>
          <h2>Predios</h2>
          <p>Listado local preparado para la futura integración con la API REST.</p>
        </div>
      </div>

      <div className="stats-grid">
        <article className="stat-card"><span>Total</span><strong>{total}</strong></article>
        <article className="stat-card"><span>Con parcelas</span><strong>{conParcelas}</strong></article>
        <article className="stat-card"><span>Sin parcelas</span><strong>{total - conParcelas}</strong></article>
      </div>

      <PredioTable predios={prediosMock} parcelas={parcelasMock} />
    </section>
  )
}
