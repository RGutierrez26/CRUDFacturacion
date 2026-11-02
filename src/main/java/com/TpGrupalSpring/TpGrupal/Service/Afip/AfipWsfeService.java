package com.TpGrupalSpring.TpGrupal.Service.Afip;

import ar.gov.afip.wsfe.*;
import com.TpGrupalSpring.TpGrupal.DTO.FacturaVenta.FacturaVentaDTO;
import com.TpGrupalSpring.TpGrupal.Entities.Dominio.FacturaVenta;
import com.TpGrupalSpring.TpGrupal.Repository.FacturaVentaRepository;
import jakarta.xml.ws.BindingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.namespace.QName;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AfipWsfeService {

    private final AfipWsaaService afipWsaaService;
    private final FacturaVentaRepository facturaVentaRepository;

    @Value("${afip.cuit:}")
    private String cuitEmisor;

    @Value("${afip.wsfe-url:https://wswhomo.afip.gov.ar/wsfev1/service.asmx}")
    private String wsfeUrl;

    public AfipWsfeService(AfipWsaaService afipWsaaService, FacturaVentaRepository facturaVentaRepository) {
        this.afipWsaaService = afipWsaaService;
        this.facturaVentaRepository = facturaVentaRepository;
    }

    /**
     * Consulta a AFIP el último número emitido para un Punto de Venta y Tipo de Comprobante
     */
    public int obtenerUltimoComprobante(int ptoVta, int cbteTipo) throws Exception {
        String[] auth = afipWsaaService.obtenerTokenYSign("wsfe");
        String token = auth[0];
        String sign = auth[1];

        String soapXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "  <FECompUltimoAutorizado xmlns=\"http://ar.gov.afip.dif.FEV1/\">" +
                "<Auth><Token>" + token + "</Token><Sign>" + sign + "</Sign><Cuit>" + cuitEmisor + "</Cuit></Auth>" +
                "<PtoVta>" + ptoVta + "</PtoVta>" +
                "<CbteTipo>" + cbteTipo + "</CbteTipo>" +
                "  </FECompUltimoAutorizado>" +
                "</soap:Body></soap:Envelope>";

        String response = enviarSoap(wsfeUrl, soapXml, "http://ar.gov.afip.dif.FEV1/FECompUltimoAutorizado");

        if (response.contains("<Err>")) {
            String errorMsg = extraerTag(response, "Err", "Msg");
            throw new RuntimeException("Error AFIP al consultar último comprobante: " + errorMsg);
        }

        if (!response.contains("<CbteNro>")) {
            throw new RuntimeException("Respuesta inesperada de AFIP: " + response);
        }

        String nro = response.substring(response.indexOf("<CbteNro>") + 9, response.indexOf("</CbteNro>"));
        return Integer.parseInt(nro);
    }

    /**
     * Emite la factura ante ARCA / AFIP y le asigna el CAE y el Número oficial
     */
    public FacturaVentaDTO facturar(Long facturaId) {
        FacturaVenta factura = facturaVentaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura con ID " + facturaId + " no encontrada"));

        if (factura.getFechaBaja() != null) {
            throw new RuntimeException("No se puede emitir ante AFIP una factura que ha sido dada de baja lógica.");
        }

        if ("ANULADA".equalsIgnoreCase(factura.getEstado())) {
            throw new RuntimeException("No se puede emitir ante AFIP una factura que se encuentra en estado ANULADA.");
        }

        if (factura.getCae() != null && !factura.getCae().isBlank()) {
            throw new RuntimeException("La factura ya se encuentra autorizada por AFIP. CAE: " + factura.getCae());
        }

        try {
            // 1. Obtener Token y Sign de WSAA
            String[] authWsaa = afipWsaaService.obtenerTokenYSign("wsfe");
            String token = authWsaa[0];
            String sign = authWsaa[1];

            // 2. Determinar Parámetros Fiscales
            int ptoVta = factura.getPuntoVenta() != null ? factura.getPuntoVenta().getNumero() : 1;
            
            // Tipo de Comprobante: Factura B (6) si es consumidor final / exento / monotributo; Factura A (1) si receptor es RI
            int cbteTipo = 6; 
            int docTipo = 99; // 99 = Consumidor Final
            long docNro = 0L;
            int condicionIvaReceptor = 5; // 5 = Consumidor Final por defecto

            if (factura.getCondicionIva() != null && factura.getCondicionIva().getCodigoAfip() > 0) {
                condicionIvaReceptor = factura.getCondicionIva().getCodigoAfip();
                if (condicionIvaReceptor == 1) {
                    cbteTipo = 1; // Factura A
                }
            }

            if (factura.getCliente() != null && factura.getCliente().getCuitCuil() != null) {
                String cuitLimpio = factura.getCliente().getCuitCuil().replaceAll("[^0-9]", "");
                if (cuitLimpio.length() == 11) {
                    docTipo = 80; // CUIT
                    docNro = Long.parseLong(cuitLimpio);
                } else if (cuitLimpio.length() == 8) {
                    docTipo = 96; // DNI
                    docNro = Long.parseLong(cuitLimpio);
                }
            }

            // 3. Obtener el siguiente número de comprobante desde AFIP
            int ultimoNro = obtenerUltimoComprobante(ptoVta, cbteTipo);
            int nuevoNro = ultimoNro + 1;
            System.out.println("-> [AFIP WSFE] Punto de Venta: " + ptoVta + ", Tipo: " + cbteTipo + ". Ultimo Nro: " + ultimoNro + " -> Siguiente a emitir: " + nuevoNro);

            // 4. Instanciar Port SOAP JAX-WS
            ar.gov.afip.wsfe.Service service = new ar.gov.afip.wsfe.Service();
            ServiceSoap port = service.getServiceSoap();

            BindingProvider bp = (BindingProvider) port;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsfeUrl);

            // Autenticación
            FEAuthRequest auth = new FEAuthRequest();
            auth.setToken(token.trim());
            auth.setSign(sign.trim());
            auth.setCuit(Long.parseLong(cuitEmisor));

            // Cabecera
            FECAECabRequest cab = new FECAECabRequest();
            cab.setCantReg(1);
            cab.setPtoVta(ptoVta);
            cab.setCbteTipo(cbteTipo);

            // Detalle
            FECAEDetRequest det = new FECAEDetRequest();
            det.setConcepto(1); // 1 = Productos
            det.setDocTipo(docTipo);
            det.setDocNro(docNro);
            det.setCbteDesde(nuevoNro);
            det.setCbteHasta(nuevoNro);
            det.setCbteFch(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            double impTotal = Math.round(factura.getImporteTotal() * 100.0) / 100.0;
            det.setImpTotal(impTotal);
            det.setImpTotConc(0.00);
            det.setImpOpEx(0.00);
            det.setImpTrib(0.00);
            det.setMonId("PES");
            det.setMonCotiz(1.0);
            det.setCondicionIVAReceptorId(condicionIvaReceptor);

            // Desglose de IVA para Factura A / B
            if (cbteTipo == 1 || cbteTipo == 6) {
                double impNeto = Math.round((impTotal / 1.21) * 100.0) / 100.0;
                double impIva = Math.round((impTotal - impNeto) * 100.0) / 100.0;

                det.setImpNeto(impNeto);
                det.setImpIVA(impIva);

                AlicIva alicIva = new AlicIva();
                alicIva.setId(5); // 5 = 21%
                alicIva.setBaseImp(impNeto);
                alicIva.setImporte(impIva);

                ArrayOfAlicIva arrayIva = new ArrayOfAlicIva();
                arrayIva.getAlicIva().add(alicIva);
                det.setIva(arrayIva);
            } else {
                // Factura C
                det.setImpNeto(impTotal);
                det.setImpIVA(0.00);
            }

            ArrayOfFECAEDetRequest detArray = new ArrayOfFECAEDetRequest();
            detArray.getFECAEDetRequest().add(det);

            FECAERequest feCAEReq = new FECAERequest();
            feCAEReq.setFeCabReq(cab);
            feCAEReq.setFeDetReq(detArray);

            System.out.println("-> [AFIP WSFE] Enviando solicitud feCAESolicitar a AFIP...");
            FECAEResponse response = port.fecaeSolicitar(auth, feCAEReq);

            if (response == null) {
                throw new RuntimeException("La respuesta de AFIP fue nula.");
            }

            // Validar errores a nivel de cabecera
            if (response.getErrors() != null && response.getErrors().getErr() != null && !response.getErrors().getErr().isEmpty()) {
                StringBuilder errSb = new StringBuilder();
                for (Err err : response.getErrors().getErr()) {
                    errSb.append("[").append(err.getCode()).append("] ").append(err.getMsg()).append(" ");
                }
                throw new RuntimeException("AFIP rechazó la solicitud (Cabecera): " + errSb);
            }

            if (response.getFeDetResp() == null || response.getFeDetResp().getFECAEDetResponse() == null || response.getFeDetResp().getFECAEDetResponse().isEmpty()) {
                throw new RuntimeException("AFIP no devolvió detalles en la respuesta de autorización.");
            }

            FECAEDetResponse detResp = response.getFeDetResp().getFECAEDetResponse().get(0);
            String resultado = detResp.getResultado();

            if ("A".equalsIgnoreCase(resultado)) {
                String cae = detResp.getCAE();
                String fchVto = detResp.getCAEFchVto();

                System.out.println("-> [AFIP WSFE] ¡Comprobante AUTORIZADO! CAE: " + cae + " - Vto: " + fchVto);

                factura.setNumero((long) nuevoNro);
                factura.setCae(cae);
                factura.setResultadoAfip("A");
                factura.setMotivoRechazo(null);
                factura.setEstado("EMITIDA");

                if (fchVto != null && fchVto.length() == 8) {
                    try {
                        Date vtoDate = new SimpleDateFormat("yyyyMMdd").parse(fchVto);
                        factura.setCaeFechaVencimiento(vtoDate);
                    } catch (Exception ignored) {}
                }

                factura = facturaVentaRepository.save(factura);
                return FacturaVentaDTO.toDTO(factura);

            } else {
                StringBuilder obsSb = new StringBuilder();
                if (detResp.getObservaciones() != null && detResp.getObservaciones().getObs() != null) {
                    for (Obs obs : detResp.getObservaciones().getObs()) {
                        obsSb.append("[").append(obs.getCode()).append("] ").append(obs.getMsg()).append(" ");
                    }
                }

                String motivo = obsSb.length() > 0 ? obsSb.toString() : "Rechazo sin observaciones";
                System.err.println("-> [AFIP WSFE] Comprobante RECHAZADO: " + motivo);

                factura.setResultadoAfip("R");
                factura.setMotivoRechazo(motivo);
                facturaVentaRepository.save(factura);

                throw new RuntimeException("AFIP rechazó la emisión del comprobante: " + motivo);
            }

        } catch (Exception e) {
            System.err.println("-> [AFIP WSFE] Error al facturar: " + e.getMessage());
            throw new RuntimeException("Error en proceso de facturación con AFIP: " + e.getMessage(), e);
        }
    }

    private String enviarSoap(String endpointUrl, String soapXml, String soapAction) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");

        if (soapAction != null && !soapAction.isEmpty()) {
            conn.setRequestProperty("SOAPAction", soapAction);
        }

        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = soapXml.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        InputStream is = (conn.getResponseCode() >= 400) ? conn.getErrorStream() : conn.getInputStream();
        StringBuilder response = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
        }

        return response.toString();
    }

    private String extraerTag(String xml, String parentTag, String childTag) {
        try {
            int parentIdx = xml.indexOf("<" + parentTag);
            if (parentIdx != -1) {
                int childIdx = xml.indexOf("<" + childTag + ">", parentIdx);
                int endIdx = xml.indexOf("</" + childTag + ">", childIdx);
                if (childIdx != -1 && endIdx != -1) {
                    return xml.substring(childIdx + childTag.length() + 2, endIdx);
                }
            }
        } catch (Exception ignored) {}
        return "Desconocido";
    }
}
