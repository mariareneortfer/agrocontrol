import type { Parcela } from '../models/Parcela'

// Datos simulados de parcelas. predioId dice a que predio pertenece cada una:
// el predio 1 tiene dos parcelas (relacion 1:N) y el predio 4 todavia no tiene ninguna.
export const parcelasMock: Parcela[] = [
  { id: 1, predioId: 1, codigo: 'P-01', nombre: 'Lote Norte',    superficie: 12.5, unidadSuperficie: 'hectarea', ubicacionReferencial: 'Junto al camino principal' },
  { id: 2, predioId: 1, codigo: 'P-02', nombre: 'Lote Sur', superficie: 8.75, unidadSuperficie: 'hectarea', ubicacionReferencial: null },
  { id: 3, predioId: 2, codigo: 'P-01', nombre: 'Lote Central', superficie: 20, unidadSuperficie: 'hectarea', ubicacionReferencial: 'Frente a la casa' },
  { id: 4, predioId: 3, codigo: 'P-01', nombre: 'Lote Río', superficie: 5.3, unidadSuperficie: 'hectarea', ubicacionReferencial: null },
  { id: 5, predioId: 5, codigo: 'P-01', nombre: 'Lote Arrocero', superficie: 15, unidadSuperficie: 'hectarea', ubicacionReferencial: 'Cerca del canal' },
  { id: 6, predioId: 5, codigo: 'P-02', nombre: 'Lote Bajo', superficie: 9.4, unidadSuperficie: 'hectarea', ubicacionReferencial: null },
  { id: 7, predioId: 2, codigo: 'P-02', nombre: 'Lote Este', superficie: 6.25, unidadSuperficie: 'hectarea', ubicacionReferencial: 'Detrás del galpón' },
  { id: 8, predioId: 999, codigo: 'P-99', nombre: 'Lote Huérfano', superficie: 1, unidadSuperficie: 'hectarea', ubicacionReferencial: null },
]
