// Header.tsx: cabecera comun que se muestra encima de todas las paginas.
export default function Header() {
  return (
    <header className="topbar">
      <div>
        <p className="topbar__eyebrow">Frontend administrativo</p>
        <h1>AgroControl</h1>
      </div>
      <span className="topbar__status">React + TypeScript</span>
    </header>
  )
}
