import { prediosMock } from '../../predios/data/predios.mock'
import ParcelaTable from '../components/ParcelaTable'
import { parcelasMock } from '../data/parcelas.mock'

export default function ParcelasPage() {
  const total = parcelasMock.length
  const conUbicacion = parcelasMock.filter((parcela) => parcela.ubicacionReferencial !== null).length

  return (
    <section className="feature-page">
      <div className="page-heading">
        <div>
          <p className="page-heading__eyebrow">Gestión de parcelas</p>
          <h2>Parcelas</h2>
          <p>Parcelas asociadas a predios mediante predioId.</p>
        </div>
      </div>

      <div className="stats-grid">
        <article className="stat-card"><span>Total</span><strong>{total}</strong></article>
        <article className="stat-card"><span>Con ubicación</span><strong>{conUbicacion}</strong></article>
        <article className="stat-card"><span>Sin ubicación</span><strong>{total - conUbicacion}</strong></article>
      </div>

      <ParcelaTable parcelas={parcelasMock} predios={prediosMock} />
    </section>
  )
}
