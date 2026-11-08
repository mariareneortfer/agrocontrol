import { API_URL } from '../../config/env'

// Error HTTP con el formato del GlobalExceptionHandler del backend:
// { timestamp, status, error, message, path, fieldErrors }.
// status 0 = no hubo respuesta (backend apagado, URL/puerto incorrecto o CORS).
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

interface BackendErrorBody {
  message?: string
  fieldErrors?: Record<string, string> | null
}

// Unica puerta HTTP de la aplicacion: arma la URL, pone el Content-Type,
// revisa response.ok y convierte los errores del backend en ApiError.
export async function apiFetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  if (!API_URL) {
    throw new ApiError(0, 'Falta configurar VITE_API_URL en .env.development.')
  }

  const headers = new Headers(options.headers)
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  let response: Response
  try {
    response = await fetch(`${API_URL}${endpoint}`, { ...options, headers })
  } catch (err) {
    // Si el componente cancelo la peticion (AbortController), se deja pasar tal cual.
    if (err instanceof DOMException && err.name === 'AbortError') throw err
    // fetch solo falla asi cuando NO hubo respuesta HTTP.
    throw new ApiError(
      0,
      `No se pudo conectar con el backend (${API_URL}). Revise que Spring Boot esté encendido y que CORS permita este origen.`,
    )
  }

  // fetch NO lanza error por 400, 404, 409 o 500: hay que revisar response.ok.
  if (!response.ok) {
    const texto = await response.text()
    let body: BackendErrorBody = {}
    try {
      body = JSON.parse(texto) as BackendErrorBody
    } catch {
      body = { message: texto }
    }
    throw new ApiError(
      response.status,
      body.message || response.statusText || `Error HTTP ${response.status}`,
      body.fieldErrors ?? {},
    )
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

// Mensaje legible para mostrar en pantalla a partir de cualquier error.
export function mensajeDeError(err: unknown): string {
  if (err instanceof ApiError) {
    return err.status ? `Error ${err.status}: ${err.message}` : err.message
  }
  return err instanceof Error ? err.message : 'Error inesperado.'
}
