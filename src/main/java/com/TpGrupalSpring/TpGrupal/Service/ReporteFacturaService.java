package com.TpGrupalSpring.TpGrupal.Service;

import com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReporteDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteFacturaService {

    @PersistenceContext
    private EntityManager em;

    // 1. Consulta JPQL optimizada con Constructor Expression
    @Transactional(readOnly = true)
    public List<FacturaReporteDTO> obtenerReporteFacturas() {
        String jpql = "SELECT new com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReporteDTO(" +
                "   f.numero, " +
                "   f.fechaEmision, " +
                "   COALESCE(c.denominacion, 'Consumidor Final'), " +
                "   ci.denominacion, " +
                "   pv.descripcion, " +
                "   f.importeTotal, " +
                "   COUNT(d), " +
                "   f.estado " +
                ") " +
                "FROM FacturaVenta f " +
                "LEFT JOIN f.cliente c " +
                "JOIN f.condicionIva ci " +
                "JOIN f.puntoVenta pv " +
                "JOIN f.detalles d " +
                "GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, ci.denominacion, pv.descripcion, f.importeTotal, f.estado";

        return em.createQuery(jpql, FacturaReporteDTO.class).getResultList();
    }

    // 2. Generación de Archivo PDF (usando OpenPDF / iText)
    public void generarPdf(List<FacturaReporteDTO> facturas, String rutaArchivo) throws Exception {
        Document document = new Document(PageSize.A4.rotate()); // Horizontal para que entren las columnas
        PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
        document.open();

        // Título
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph titulo = new Paragraph("Reporte de Facturación", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" ")); // Espacio en blanco

        // Tabla con 8 columnas (incluye Estado)
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);

        // Encabezados
        String[] columnas = {"N° Factura", "Fecha", "Cliente", "Cond. IVA", "Punto Venta", "Total", "Ítems", "Estado"};
        for (String col : columnas) {
            table.addCell(col);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Filas con datos
        for (FacturaReporteDTO f : facturas) {
            table.addCell(String.valueOf(f.numeroFactura()));
            table.addCell(f.fechaEmision() != null ? sdf.format(f.fechaEmision()) : "-");
            table.addCell(f.ClienteDenominacion());
            table.addCell(f.condicionIva());
            table.addCell(f.puntoVentaDescripcion());
            table.addCell("$" + f.importeTotal());
            table.addCell(String.valueOf(f.CantidadItemns()));
            table.addCell(f.estado() != null ? f.estado() : "-");
        }

        document.add(table);
        document.close();
    }

    // 3. Generación de Archivo Excel (TSV separado por tabulaciones '\t')
    public void generarExcelTSV(List<FacturaReporteDTO> facturas, String rutaArchivo) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            // Cabecera separada por tabulaciones (\t)
            writer.println("N° Factura\tFecha Emisión\tCliente\tCondición IVA\tPunto de Venta\tImporte Total\tCantidad Ítems\tEstado");

            // Filas
            for (FacturaReporteDTO f : facturas) {
                writer.println(
                        f.numeroFactura() + "\t" +
                                (f.fechaEmision() != null ? sdf.format(f.fechaEmision()) : "-") + "\t" +
                                f.ClienteDenominacion() + "\t" +
                                f.condicionIva() + "\t" +
                                f.puntoVentaDescripcion() + "\t" +
                                f.importeTotal() + "\t" +
                                f.CantidadItemns() + "\t" +
                                (f.estado() != null ? f.estado() : "-")
                );
            }
        }
    }

    @Transactional(readOnly = true)
    // Reporte para Método Rest
    public List<FacturaReporteDTO> ObtenerFacturas(Date fechaDesde, Date fechaHasta, String estado, Double montoMinimo) {
        StringBuilder jpql = new StringBuilder("SELECT new com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReporteDTO(" +
                "   f.numero, " +
                "   f.fechaEmision, " +
                "   COALESCE(c.denominacion, 'Consumidor Final'), " +
                "   ci.denominacion, " +
                "   pv.descripcion, " +
                "   f.importeTotal, " +
                "   COUNT(d), " +
                "   f.estado " +
                ") " +
                "FROM FacturaVenta f " +
                "LEFT JOIN f.cliente c " +
                "JOIN f.condicionIva ci " +
                "JOIN f.puntoVenta pv " +
                "JOIN f.detalles d " +
                "WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (fechaDesde != null) {
            jpql.append(" AND f.fechaEmision >= :fechaDesde ");
            params.put("fechaDesde", fechaDesde);
        }
        if (fechaHasta != null) {
            jpql.append(" AND f.fechaEmision <= :fechaHasta ");
            params.put("fechaHasta", fechaHasta);
        }
        if (estado != null && !estado.trim().isEmpty()) {
            jpql.append(" AND f.estado = :estado ");
            params.put("estado", estado);
        }
        if (montoMinimo != null) {
            jpql.append(" AND f.importeTotal >= :montoMinimo ");
            params.put("montoMinimo", montoMinimo);
        }

        jpql.append(" GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, ci.denominacion, pv.descripcion, f.importeTotal, f.estado");

        TypedQuery<FacturaReporteDTO> query = em.createQuery(jpql.toString(), FacturaReporteDTO.class);
        params.forEach(query::setParameter);
        return query.getResultList();
    }
}