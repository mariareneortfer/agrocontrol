import { apiFetch } from '../../../services/http/apiClient'
import type { Predio } from '../models/Predio'
import type { CrearPredioRequest } from '../types/CrearPredioRequest'

// El service es el unico que conoce los endpoints de predios (PredioController: /api/farms).
export const predioService = {
  listar(signal?: AbortSignal): Promise<Predio[]> {
    return apiFetch<Predio[]>('/farms', { signal })
  },

  crear(data: CrearPredioRequest): Promise<Predio> {
    return apiFetch<Predio>('/farms', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },
}
