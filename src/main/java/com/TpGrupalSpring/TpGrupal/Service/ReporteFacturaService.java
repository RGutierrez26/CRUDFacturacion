package com.TpGrupalSpring.TpGrupal.Service;

import com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReporteDTO;
import com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReportePageDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
                "   f.id, " +
                "   f.numero, " +
                "   f.fechaEmision, " +
                "   COALESCE(c.denominacion, 'Consumidor Final'), " +
                "   COALESCE(ci.denominacion, '-'), " +
                "   COALESCE(pv.descripcion, '-'), " +
                "   f.importeTotal, " +
                "   COUNT(d), " +
                "   f.estado, " +
                "   f.cae, " +
                "   f.caeFechaVencimiento " +
                ") " +
                "FROM FacturaVenta f " +
                "LEFT JOIN f.cliente c " +
                "LEFT JOIN f.condicionIva ci " +
                "LEFT JOIN f.puntoVenta pv " +
                "LEFT JOIN f.detalles d " +
                "WHERE f.fechaBaja IS NULL " +
                "GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, ci.denominacion, pv.descripcion, f.importeTotal, f.estado, f.cae, f.caeFechaVencimiento";

        return em.createQuery(jpql, FacturaReporteDTO.class).getResultList();
    }

    // 2. Generación de Archivo PDF (usando OpenPDF / iText con CAE y Vto. CAE)
    public void generarPdf(List<FacturaReporteDTO> facturas, OutputStream outputStream) throws Exception {
        Document document = new Document(PageSize.A4.rotate()); // Horizontal para que entren las columnas
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Título
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph titulo = new Paragraph("Reporte de Facturación", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" ")); // Espacio en blanco

        // Tabla con 10 columnas (incluye Estado, CAE y Vto. CAE)
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);

        // Encabezados
        String[] columnas = {"N° Factura", "Fecha", "Cliente", "Cond. IVA", "Punto Venta", "Total", "Ítems", "Estado", "CAE", "Vto. CAE"};
        for (String col : columnas) {
            table.addCell(col);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Filas con datos
        for (FacturaReporteDTO f : facturas) {
            table.addCell(f.numeroFactura() != null ? String.valueOf(f.numeroFactura()) : "Pendiente");
            table.addCell(f.fechaEmision() != null ? sdf.format(f.fechaEmision()) : "-");
            table.addCell(f.ClienteDenominacion() != null ? f.ClienteDenominacion() : "Consumidor Final");
            table.addCell(f.condicionIva() != null ? f.condicionIva() : "-");
            table.addCell(f.puntoVentaDescripcion() != null ? f.puntoVentaDescripcion() : "-");
            table.addCell("$" + f.importeTotal());
            table.addCell(String.valueOf(f.CantidadItemns()));
            table.addCell(f.estado() != null ? f.estado() : "-");
            table.addCell(f.cae() != null ? f.cae() : "-");
            table.addCell(f.caeFechaVencimiento() != null ? sdf.format(f.caeFechaVencimiento()) : "-");
        }

        document.add(table);
        document.close();
    }

    public void generarPdf(List<FacturaReporteDTO> facturas, String rutaArchivo) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(rutaArchivo)) {
            generarPdf(facturas, fos);
        }
    }

    public byte[] generarPdfBytes(List<FacturaReporteDTO> facturas) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generarPdf(facturas, baos);
        return baos.toByteArray();
    }

    // 3. Generación de Archivo Excel (TSV separado por tabulaciones '\t' con CAE y Vto. CAE)
    public void generarExcelTSV(List<FacturaReporteDTO> facturas, OutputStream outputStream) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            // Cabecera separada por tabulaciones (\t)
            writer.println("N° Factura\tFecha Emisión\tCliente\tCondición IVA\tPunto de Venta\tImporte Total\tCantidad Ítems\tEstado\tCAE\tVto. CAE");

            // Filas
            for (FacturaReporteDTO f : facturas) {
                writer.println(
                        (f.numeroFactura() != null ? f.numeroFactura() : "Pendiente") + "\t" +
                                (f.fechaEmision() != null ? sdf.format(f.fechaEmision()) : "-") + "\t" +
                                (f.ClienteDenominacion() != null ? f.ClienteDenominacion() : "Consumidor Final") + "\t" +
                                (f.condicionIva() != null ? f.condicionIva() : "-") + "\t" +
                                (f.puntoVentaDescripcion() != null ? f.puntoVentaDescripcion() : "-") + "\t" +
                                f.importeTotal() + "\t" +
                                f.CantidadItemns() + "\t" +
                                (f.estado() != null ? f.estado() : "-") + "\t" +
                                (f.cae() != null ? f.cae() : "-") + "\t" +
                                (f.caeFechaVencimiento() != null ? sdf.format(f.caeFechaVencimiento()) : "-")
                );
            }
            writer.flush();
        }
    }

    public void generarExcelTSV(List<FacturaReporteDTO> facturas, String rutaArchivo) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(rutaArchivo)) {
            generarExcelTSV(facturas, fos);
        }
    }

    public byte[] generarExcelTSVBytes(List<FacturaReporteDTO> facturas) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generarExcelTSV(facturas, baos);
        return baos.toByteArray();
    }

    @Transactional(readOnly = true)
    // Reporte Paginado para Método Rest (solo cuenta registros donde fechaBaja IS NULL)
    public FacturaReportePageDTO ObtenerFacturas(Date fechaDesde, Date fechaHasta, String estado, Double montoMinimo, int page, int size) {
        if (size <= 0) size = 10;
        if (page < 0) page = 0;

        StringBuilder whereClause = new StringBuilder(" WHERE f.fechaBaja IS NULL ");
        Map<String, Object> params = new HashMap<>();

        if (fechaDesde != null) {
            whereClause.append(" AND f.fechaEmision >= :fechaDesde ");
            params.put("fechaDesde", fechaDesde);
        }
        if (fechaHasta != null) {
            whereClause.append(" AND f.fechaEmision <= :fechaHasta ");
            params.put("fechaHasta", fechaHasta);
        }
        if (estado != null && !estado.trim().isEmpty()) {
            whereClause.append(" AND f.estado = :estado ");
            params.put("estado", estado);
        }
        if (montoMinimo != null) {
            whereClause.append(" AND f.importeTotal >= :montoMinimo ");
            params.put("montoMinimo", montoMinimo);
        }

        // 1. Conteo total exacto respetando f.fechaBaja IS NULL y filtros
        String countJpql = "SELECT COUNT(f) FROM FacturaVenta f " + whereClause;
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        params.forEach(countQuery::setParameter);
        long totalElementos = countQuery.getSingleResult();

        int totalPaginas = (int) Math.ceil((double) totalElementos / size);
        if (totalPaginas == 0) {
            totalPaginas = 1;
        }

        // 2. Consulta paginada con ordenamiento estable descendente
        String dataJpql = "SELECT new com.TpGrupalSpring.TpGrupal.DTO.Consultas.FacturaReporteDTO(" +
                "   f.id, " +
                "   f.numero, " +
                "   f.fechaEmision, " +
                "   COALESCE(c.denominacion, 'Consumidor Final'), " +
                "   COALESCE(ci.denominacion, '-'), " +
                "   COALESCE(pv.descripcion, '-'), " +
                "   f.importeTotal, " +
                "   COUNT(d), " +
                "   f.estado, " +
                "   f.cae, " +
                "   f.caeFechaVencimiento " +
                ") " +
                "FROM FacturaVenta f " +
                "LEFT JOIN f.cliente c " +
                "LEFT JOIN f.condicionIva ci " +
                "LEFT JOIN f.puntoVenta pv " +
                "LEFT JOIN f.detalles d " +
                whereClause +
                " GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, ci.denominacion, pv.descripcion, f.importeTotal, f.estado, f.cae, f.caeFechaVencimiento " +
                " ORDER BY f.id DESC";

        TypedQuery<FacturaReporteDTO> query = em.createQuery(dataJpql, FacturaReporteDTO.class);
        params.forEach(query::setParameter);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        List<FacturaReporteDTO> facturas = query.getResultList();
        return new FacturaReportePageDTO(facturas, page, totalPaginas, totalElementos, size);
    }

    @Transactional(readOnly = true)
    // Sobrecarga para mantener compatibilidad sin paginación si se requiere
    public List<FacturaReporteDTO> ObtenerFacturas(Date fechaDesde, Date fechaHasta, String estado, Double montoMinimo) {
        return ObtenerFacturas(fechaDesde, fechaHasta, estado, montoMinimo, 0, Integer.MAX_VALUE).facturas();
    }
}