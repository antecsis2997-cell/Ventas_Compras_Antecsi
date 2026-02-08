package com.antecsis.controller;

import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/ventas-dia")
    public DashboardVentasDTO ventasDia(
            @RequestParam String fecha) {

        return service.ventasPorDia(LocalDate.parse(fecha));
    }

    @GetMapping("/ventas-mes")
    public DashboardVentasDTO ventasMes(
            @RequestParam int year,
            @RequestParam int month) {

        return service.ventasPorMes(year, month);
    }
}
