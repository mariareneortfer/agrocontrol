// AcercaPage.tsx: actividad practica de la Guia 02 (ruta /acerca).
// Es una pagina global, no de un modulo, por eso vive en src/pages.
export default function AcercaPage() {
  return (
    <section>
      <div className="page-heading">
        <div>
          <p className="page-heading__eyebrow">Información</p>
          <h2>Acerca de AgroControl</h2>
        </div>
      </div>

      <div className="placeholder-card">
        <h3>Proyecto 16 - Programación Aplicada 2026-2</h3>
        <p>
          AgroControl registra predios, sus parcelas, los cultivos, las campañas
          agrícolas y las labores realizadas en cada parcela.
        </p>
        <p>
          Backend: Spring Boot + PostgreSQL. Frontend: React + TypeScript + Vite.
        </p>
      </div>
    </section>
  )
}
