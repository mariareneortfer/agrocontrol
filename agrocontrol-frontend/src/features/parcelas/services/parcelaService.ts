import { apiFetch } from '../../../services/http/apiClient'
import type { Parcela } from '../models/Parcela'
import type { CrearParcelaRequest } from '../types/CrearParcelaRequest'

// Endpoints de parcelas (ParcelaController: /api/plots).
export const parcelaService = {
  listar(signal?: AbortSignal): Promise<Parcela[]> {
    return apiFetch<Parcela[]>('/plots', { signal })
  },

  crear(data: CrearParcelaRequest): Promise<Parcela> {
    return apiFetch<Parcela>('/plots', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },
}
