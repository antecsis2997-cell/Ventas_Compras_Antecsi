package com.antecsis.service.impl;

import com.antecsis.entity.Venta;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

    @Override
    public byte[] exportarVentasExcel(LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        List<Venta> ventas = ventaRepository.findByFechaBetween(inicio, fin);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Ventas");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Fecha");
            header.createCell(2).setCellValue("Cliente");
            header.createCell(3).setCellValue("Usuario");
            header.createCell(4).setCellValue("Total");
            header.createCell(5).setCellValue("Estado");
            header.createCell(6).setCellValue("Tipo Doc");
            header.createCell(7).setCellValue("Nº Doc");

            int rowNum = 1;
            for (Venta v : ventas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().toString() : "");
                row.createCell(2).setCellValue(v.getCliente() != null ? v.getCliente().getNombre() : "");
                row.createCell(3).setCellValue(v.getUsuario() != null ? v.getUsuario().getUsername() : "");
                row.createCell(4).setCellValue(v.getTotal() != null ? v.getTotal().doubleValue() : 0);
                row.createCell(5).setCellValue(v.getEstado() != null ? v.getEstado().name() : "");
                row.createCell(6).setCellValue(v.getTipoDocumento() != null ? v.getTipoDocumento().name() : "");
                row.createCell(7).setCellValue(v.getNumeroDocumento() != null ? v.getNumeroDocumento() : "");
            }

            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] exportarVentasPdf(LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        List<Venta> ventas = ventaRepository.findByFechaBetween(inicio, fin);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Reporte de Ventas"));
            document.add(new Paragraph("Periodo: " + fechaInicio + " a " + fechaFin));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100f);
            table.addCell(new PdfPCell(new Phrase("ID")));
            table.addCell(new PdfPCell(new Phrase("Fecha")));
            table.addCell(new PdfPCell(new Phrase("Cliente")));
            table.addCell(new PdfPCell(new Phrase("Usuario")));
            table.addCell(new PdfPCell(new Phrase("Total")));
            table.addCell(new PdfPCell(new Phrase("Estado")));
            table.addCell(new PdfPCell(new Phrase("Tipo Doc")));
            table.addCell(new PdfPCell(new Phrase("Nº Doc")));

            for (Venta v : ventas) {
                table.addCell(String.valueOf(v.getId()));
                table.addCell(v.getFecha() != null ? v.getFecha().toString() : "");
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
}
