import { NavLink } from 'react-router'

// Opciones del menu. "end" en Inicio evita que "/" quede activo en todas las rutas.
const menuItems = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/predios', label: 'Predios' },
  { to: '/parcelas', label: 'Parcelas' },
  { to: '/acerca', label: 'Acerca' },
]

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar__brand">
        <span className="sidebar__brand-mark">A</span>
        <div>
          <strong>AgroControl</strong>
          <small>Gestión agrícola</small>
        </div>
      </div>

      <nav className="sidebar__nav" aria-label="Navegación principal">
        {menuItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
