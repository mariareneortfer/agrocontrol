import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router'
import App from './app/App'
import './styles/global.css'

// main.tsx: punto de entrada. Monta React en <div id="root"> y envuelve
// toda la aplicacion con BrowserRouter para que cualquier componente pueda navegar.
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
