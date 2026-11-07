package com.agrocontrol.shared.web;

import java.time.Instant;
import java.util.Map;

/**
 * Contrato UNICO de error de toda la API de AgroControl.
 * Cualquier error (400, 404, 409, 500...) se devuelve con esta misma forma.
 *
 * @param timestamp   momento del error
 * @param status      codigo HTTP (400, 404, 409, 500...)
 * @param error       nombre del codigo HTTP ("Bad Request", "Not Found"...)
 * @param message     explicacion para quien consume la API (nunca detalles internos)
 * @param path        URL que se llamo
 * @param fieldErrors errores por campo; vacio si el error no es de validacion
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
