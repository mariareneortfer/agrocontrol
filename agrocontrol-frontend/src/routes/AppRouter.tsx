import { Route, Routes } from 'react-router'
import MainLayout from '../layouts/MainLayout'
import DashboardPage from '../pages/DashboardPage'
import AcercaPage from '../pages/AcercaPage'
import NotFoundPage from '../pages/NotFoundPage'
import PrediosPage from '../features/predios/pages/PrediosPage'
import ParcelasPage from '../features/parcelas/pages/ParcelasPage'

// AppRouter.tsx: declara que pagina corresponde a cada URL.
// Todas las rutas estan anidadas dentro de MainLayout, asi el menu y la cabecera no cambian.
export default function AppRouter() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route index element={<DashboardPage />} />
        <Route path="predios" element={<PrediosPage />} />
        <Route path="parcelas" element={<ParcelasPage />} />
        <Route path="acerca" element={<AcercaPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
