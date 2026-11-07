package com.agrocontrol;

import com.agrocontrol.farm.application.PredioService;
import com.agrocontrol.farm.domain.Predio;
import com.agrocontrol.farm.domain.port.PredioRepository;
import com.agrocontrol.farm.infrastructure.memory.PredioRepositoryEnMemoria;
import com.agrocontrol.plot.application.command.RegistrarParcelaCommand;
import com.agrocontrol.plot.domain.Parcela;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {
        // El servicio recibe el contrato; aqui elegimos la implementacion en memoria.
        PredioRepository repository = new PredioRepositoryEnMemoria();
        PredioService service = new PredioService(repository);

        System.out.println("=== Pruebas positivas ===");

        // 1. Registrar dos predios.
        service.registrar(new Predio(1L, "San Isidro", "Km 12 carretera al norte"));
        service.registrar(new Predio(2L, "La Esperanza", null));

        // 2. Listar.
        System.out.println("Predios registrados: " + service.listar().size());

        // 3. Obtener un id existente.
        System.out.println("Obtener(1): " + service.obtener(1L));

        // 4. Registrar parcelas con el record RegistrarParcelaCommand.
        service.registrarParcela(new RegistrarParcelaCommand(
                1L, "P-01", "Lote Norte", new BigDecimal("4.50"), "hectarea"));
        service.registrarParcela(new RegistrarParcelaCommand(
                1L, "P-02", "Lote Sur", new BigDecimal("3.25"), "hectarea"));
        // El mismo codigo P-01 SI se permite en otro predio (unicidad contextual).
        service.registrarParcela(new RegistrarParcelaCommand(
                2L, "P-01", "Lote Unico", new BigDecimal("2.00"), "hectarea"));

        for (Predio predio : service.listar()) {
            System.out.println(predio);
            for (Parcela parcela : predio.getParcelas()) {
                System.out.println("  - " + parcela);
            }
        }

        System.out.println();
        System.out.println("=== Pruebas negativas (errores controlados) ===");

        try {
            service.obtener(999L);
        } catch (RuntimeException ex) {
            System.out.println("ERROR CONTROLADO: " + ex.getMessage());
        }

        try {
            service.registrar(new Predio(3L, "san isidro", null));
        } catch (RuntimeException ex) {
            System.out.println("ERROR CONTROLADO: " + ex.getMessage());
        }

        try {
            service.registrarParcela(new RegistrarParcelaCommand(
                    1L, "P-01", "Lote repetido", new BigDecimal("1.00"), "hectarea"));
        } catch (RuntimeException ex) {
            System.out.println("ERROR CONTROLADO: " + ex.getMessage());
        }

        try {
            service.registrarParcela(new RegistrarParcelaCommand(
                    999L, "P-09", "Lote huerfano", new BigDecimal("1.00"), "hectarea"));
        } catch (RuntimeException ex) {
            System.out.println("ERROR CONTROLADO: " + ex.getMessage());
        }

        try {
            service.registrarParcela(new RegistrarParcelaCommand(
                    1L, "P-04", "Lote sin superficie", BigDecimal.ZERO, "hectarea"));
        } catch (RuntimeException ex) {
            System.out.println("ERROR CONTROLADO: " + ex.getMessage());
        }

        System.out.println();
        System.out.println("Parcelas en San Isidro: " + service.obtener(1L).cantidadParcelas());
    }
}
