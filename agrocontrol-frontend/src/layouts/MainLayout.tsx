import { Outlet } from 'react-router'
import Header from '../components/common/Header'
import Sidebar from '../components/common/Sidebar'

// MainLayout.tsx: estructura permanente de la pantalla.
// Sidebar y Header siempre se ven; Outlet es el hueco donde aparece la pagina de la ruta actual.
export default function MainLayout() {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-shell__main">
        <Header />
        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
