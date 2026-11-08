import { apiFetch } from '../../../services/http/apiClient'
import type { Parcela } from '../models/Parcela'
import type { ActualizarParcelaRequest } from '../types/ActualizarParcelaRequest'
import type { CrearParcelaRequest } from '../types/CrearParcelaRequest'

// Endpoints de parcelas (ParcelaController: /api/plots).
// Los predios del selector se piden con predioService.listar(): no se duplica un service de predios aqui.
export const parcelaService = {
  listar(signal?: AbortSignal): Promise<Parcela[]> {
    return apiFetch<Parcela[]>('/plots', { signal })
  },

  obtenerPorId(id: number, signal?: AbortSignal): Promise<Parcela> {
    return apiFetch<Parcela>(`/plots/${id}`, { signal })
  },

  crear(data: CrearParcelaRequest): Promise<Parcela> {
    return apiFetch<Parcela>('/plots', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  actualizar(id: number, data: ActualizarParcelaRequest): Promise<Parcela> {
    return apiFetch<Parcela>(`/plots/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
  },

  eliminar(id: number): Promise<void> {
    return apiFetch<void>(`/plots/${id}`, { method: 'DELETE' })
  },
}
