package com.agrocontrol;

import com.agrocontrol.farm.domain.Predio;
import com.agrocontrol.plot.domain.Parcela;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {
        // 1. Crear un objeto padre.
        Predio sanIsidro = new Predio(1L, "San Isidro", "Km 12 carretera al norte");

        // 2. Crear dos objetos dependientes.
        Parcela loteNorte = new Parcela(1L, "P-01", "Lote Norte", new BigDecimal("4.50"), "hectarea");
        Parcela loteSur = new Parcela(2L, "P-02", "Lote Sur", new BigDecimal("3.25"), "hectarea");

        // 3. Relacionarlos mediante el metodo de negocio.
        sanIsidro.agregarParcela(loteNorte);
        sanIsidro.agregarParcela(loteSur);

        // 4. Imprimir la evidencia.
        System.out.println("=== Relacion 1:N Predio -> Parcela ===");
        System.out.println(sanIsidro);
        for (Parcela parcela : sanIsidro.getParcelas()) {
            System.out.println("  - " + parcela);
        }

        // 5. Comprobar las reglas.
        System.out.println();
        System.out.println("=== Pruebas de reglas ===");

        try {
            Parcela repetida = new Parcela(3L, "P-01", "Lote repetido", new BigDecimal("1.00"), "hectarea");
            sanIsidro.agregarParcela(repetida);
        } catch (IllegalStateException e) {
            System.out.println("Rechazado (codigo repetido): " + e.getMessage());
        }

        try {
            new Parcela(4L, "P-04", "Lote sin superficie", BigDecimal.ZERO, "hectarea");
        } catch (IllegalArgumentException e) {
            System.out.println("Rechazado (superficie): " + e.getMessage());
        }

        try {
            sanIsidro.getParcelas().add(loteNorte);
        } catch (UnsupportedOperationException e) {
            System.out.println("Rechazado (lista de solo lectura): no se puede modificar desde fuera");
        }

        System.out.println();
        System.out.println("Total de parcelas en " + sanIsidro.getNombre() + ": " + sanIsidro.cantidadParcelas());
    }
}
