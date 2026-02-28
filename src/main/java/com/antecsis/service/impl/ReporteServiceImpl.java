package com.antecsis.service.impl;

import com.antecsis.entity.Usuario;
import com.antecsis.entity.Venta;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public byte[] exportarVentasExcel(LocalDate fechaInicio, LocalDate fechaFin, Long sectorId) throws Exception {
        Long effectiveId = resolverSectorId(sectorId);
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        List<Venta> ventas = effectiveId != null
                ? ventaRepository.findByFechaBetweenAndSectorId(inicio, fin, effectiveId)
                : ventaRepository.findByFechaBetween(inicio, fin);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Ventas");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Fecha");
            header.createCell(2).setCellValue("Sector");
            header.createCell(3).setCellValue("Cliente");
            header.createCell(4).setCellValue("Usuario");
            header.createCell(5).setCellValue("Total");
            header.createCell(6).setCellValue("Estado");
            header.createCell(7).setCellValue("Tipo Doc");
            header.createCell(8).setCellValue("Nº Doc");

            int rowNum = 1;
            for (Venta v : ventas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().toString() : "");
                row.createCell(2).setCellValue(v.getSector() != null ? v.getSector().getNombreSector() : "Sin sector");
                row.createCell(3).setCellValue(v.getCliente() != null ? v.getCliente().getNombre() : "");
                row.createCell(4).setCellValue(v.getUsuario() != null ? v.getUsuario().getUsername() : "");
                row.createCell(5).setCellValue(v.getTotal() != null ? v.getTotal().doubleValue() : 0);
                row.createCell(6).setCellValue(v.getEstado() != null ? v.getEstado().name() : "");
                row.createCell(7).setCellValue(v.getTipoDocumento() != null ? v.getTipoDocumento().name() : "");
                row.createCell(8).setCellValue(v.getNumeroDocumento() != null ? v.getNumeroDocumento() : "");
            }

            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] exportarVentasPdf(LocalDate fechaInicio, LocalDate fechaFin, Long sectorId) throws Exception {
        Long effectiveId = resolverSectorId(sectorId);
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        List<Venta> ventas = effectiveId != null
                ? ventaRepository.findByFechaBetweenAndSectorId(inicio, fin, effectiveId)
                : ventaRepository.findByFechaBetween(inicio, fin);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Reporte de Ventas"));
            document.add(new Paragraph("Periodo: " + fechaInicio + " a " + fechaFin));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100f);
            table.addCell(new PdfPCell(new Phrase("ID")));
            table.addCell(new PdfPCell(new Phrase("Fecha")));
            table.addCell(new PdfPCell(new Phrase("Sector")));
            table.addCell(new PdfPCell(new Phrase("Cliente")));
            table.addCell(new PdfPCell(new Phrase("Usuario")));
            table.addCell(new PdfPCell(new Phrase("Total")));
            table.addCell(new PdfPCell(new Phrase("Estado")));
            table.addCell(new PdfPCell(new Phrase("Tipo Doc")));
            table.addCell(new PdfPCell(new Phrase("Nº Doc")));

            for (Venta v : ventas) {
                table.addCell(String.valueOf(v.getId()));
                table.addCell(v.getFecha() != null ? v.getFecha().toString() : "");
                table.addCell(v.getSector() != null ? v.getSector().getNombreSector() : "Sin sector");
                table.addCell(v.getCliente() != null ? v.getCliente().getNombre() : "");
                table.addCell(v.getUsuario() != null ? v.getUsuario().getUsername() : "");
                table.addCell(v.getTotal() != null ? v.getTotal().toString() : "0");
                table.addCell(v.getEstado() != null ? v.getEstado().name() : "");
                table.addCell(v.getTipoDocumento() != null ? v.getTipoDocumento().name() : "");
                table.addCell(v.getNumeroDocumento() != null ? v.getNumeroDocumento() : "");
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    private Long resolverSectorId(Long sectorIdParam) {
        Long sectorIdUsuario = obtenerSectorIdAutenticado();
        if (sectorIdParam != null) {
            if (sectorIdUsuario != null && !sectorIdUsuario.equals(sectorIdParam)) {
                throw new BusinessException("No tiene acceso a este sector");
            }
            return sectorIdParam;
        }
        return sectorIdUsuario;
    }

    private Long obtenerSectorIdAutenticado() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return usuario.getSede() != null ? usuario.getSede().getId() : null;
    }

    private Usuario obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }
}
