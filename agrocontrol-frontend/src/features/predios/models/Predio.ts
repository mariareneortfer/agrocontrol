// Modelo del frontend para un predio.
// Sigue el contrato REAL que expone la API (PredioResponse en GET /api/farms),
// no la entidad JPA: no tiene anotaciones, ni created_at, ni nada que React no use.
export interface Predio {
  id: number
  nombre: string
  ubicacion: string | null
  descripcion: string | null
}
