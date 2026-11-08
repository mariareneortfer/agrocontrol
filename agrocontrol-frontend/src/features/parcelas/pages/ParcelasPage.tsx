import { useState } from 'react'
import { prediosMock } from '../../predios/data/predios.mock'
import ParcelaForm from '../components/ParcelaForm'
import ParcelaTable from '../components/ParcelaTable'
import { parcelasMock } from '../data/parcelas.mock'

export default function ParcelasPage() {
  const [mostrarFormulario, setMostrarFormulario] = useState(false)

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
        <button
          type="button"
          className="btn-primary"
          onClick={() => setMostrarFormulario((prev) => !prev)}
        >
          {mostrarFormulario ? 'Cerrar formulario' : '+ Nueva parcela'}
        </button>
      </div>

      {mostrarFormulario && <ParcelaForm />}

      <div className="stats-grid">
        <article className="stat-card"><span>Total</span><strong>{total}</strong></article>
        <article className="stat-card"><span>Con ubicación</span><strong>{conUbicacion}</strong></article>
        <article className="stat-card"><span>Sin ubicación</span><strong>{total - conUbicacion}</strong></article>
      </div>

      <ParcelaTable parcelas={parcelasMock} predios={prediosMock} />
    </section>
  )
}
