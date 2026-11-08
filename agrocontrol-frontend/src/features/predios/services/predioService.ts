import { apiFetch } from '../../../services/http/apiClient'
import type { Predio } from '../models/Predio'
import type { ActualizarPredioRequest } from '../types/ActualizarPredioRequest'
import type { CrearPredioRequest } from '../types/CrearPredioRequest'

// El service es el unico que conoce los endpoints de predios (PredioController: /api/farms).
// Los componentes expresan intenciones: predioService.actualizar(7, datos), predioService.eliminar(7).
export const predioService = {
  listar(signal?: AbortSignal): Promise<Predio[]> {
    return apiFetch<Predio[]>('/farms', { signal })
  },

  obtenerPorId(id: number, signal?: AbortSignal): Promise<Predio> {
    return apiFetch<Predio>(`/farms/${id}`, { signal })
  },

  crear(data: CrearPredioRequest): Promise<Predio> {
    return apiFetch<Predio>('/farms', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  actualizar(id: number, data: ActualizarPredioRequest): Promise<Predio> {
    return apiFetch<Predio>(`/farms/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
  },

  // DELETE responde 204 sin cuerpo; apiFetch devuelve undefined en ese caso.
  eliminar(id: number): Promise<void> {
    return apiFetch<void>(`/farms/${id}`, { method: 'DELETE' })
  },
}
