import type { Predio } from '../models/Predio'

// Datos simulados (temporales) mientras no consumimos la API.
// Respetan exactamente la interface Predio.
export const prediosMock: Predio[] = [
  {
    id: 1,
    nombre: 'San Isidro',
    ubicacion: 'Warnes, Santa Cruz',
    descripcion: 'Predio principal de soya y maíz',
  },
  {
    id: 2,
    nombre: 'La Esperanza',
    ubicacion: 'Cotoca, Santa Cruz',
    descripcion: null,
  },
  {
    id: 3,
    nombre: 'Santa Rosa',
    ubicacion: 'Montero, Santa Cruz',
    descripcion: 'Predio de caña de azúcar',
  },
  {
    id: 4,
    nombre: 'El Porvenir',
    ubicacion: null,
    descripcion: null,
  },
  {
    id: 5,
    nombre: 'Los Tajibos',
    ubicacion: 'Portachuelo, Santa Cruz',
    descripcion: 'Predio de arroz',
  },
  {
    id: 6,
    nombre: 'Buena Vista',
    ubicacion: 'Buena Vista, Santa Cruz',
    descripcion: null,
  },
]
