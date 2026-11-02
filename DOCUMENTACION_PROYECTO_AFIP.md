# Documentación Integral del Proyecto: Sistema de Facturación y Conexión ARCA / AFIP

> **Destinatario:** Asistente IA / Desarrollador para continuar en nuevo chat.  
> **Fecha de Actualización:** 02/11/2026  
> **Materia:** Desarrollo de Software (3er Año - UTN)  
> **Ubicación del Proyecto:** `C:\Users\renzo\Desktop\UTN\3er Año\Desarrollo\Java\SpringApiRest\Practica\Grupal`

---

## 1. Lineamientos, Reglas y Filosofía de Trabajo

Al continuar el desarrollo en cualquier sesión o nuevo chat, **se deben respetar estrictamente los siguientes lineamientos**:

1. **Saludo Inicial Obligatorio:** Todas las respuestas al usuario deben comenzar siempre con la frase:
   > **`Okey Capitan`**
2. **Validación Previa en Módulos Grandes / Cambios Estructurales:**
   * **NUNCA** arrancar a modificar o implementar módulos completos o cambios grandes de arquitectura sin antes **explicar con claridad qué se va a hacer, qué capas o archivos se van a modificar en cada caso, y pedir confirmación previa al usuario**.
   * El usuario debe tener siempre el control y la noción exacta de lo que se va a alterar antes de tocar código estructural.
   * La autonomía directa queda reservada para correcciones puntuales, ajustes menores o comandos de verificación técnica.
3. **Criterio Visual Frontend:**
   * **Cero emojis** en botones de acción, tablas y encabezados.
   * Estilo limpio, sobrio y profesional (Light Theme, azules corporativos `#2563eb`, grises pizarra `#0f172a`, bordes suaves).
   * Botones deshabilitados (ej. `Modificar` en facturas emitidas) en gris atenuado (`#f1f5f9`, texto `#94a3b8`, `pointer-events: none`).
4. **Reglas de Negocio en Facturación:**
   * **Sin número al crear:** Al crear una factura, **nunca** se ingresa el número de factura manualmente. Nace con `numero = null` y estado `PENDIENTE`. El número oficial correlativo es otorgado exclusivamente por AFIP/ARCA al emitirla.
   * **Baja Lógica (`fechaBaja`):** Soft delete definitivo. La factura deja de listarse en consultas regulares (`fechaBaja IS NULL`). Solo se permite en comprobantes no emitidos.
   * **Anulación Comercial (`fechaAnulacion` + `estado = 'ANULADA'`):** Mantiene `fechaBaja = null` para auditoría y reportes impositivos. Tampoco se permite anular si ya fue autorizada y emitida formalmente con número fiscal.
   * **Inmutabilidad de Factura Emitida:** Una vez que la factura tiene estado `EMITIDA` (con CAE y número fiscal oficial), **no se puede modificar** ni dar de baja lógica ni anular comercialmente.
   * **Gestión por ID:** Toda la navegación, edición, consulta, anulación y facturación en el frontend y backend se realiza mediante la clave primaria `id`.

---

## 2. Arquitectura del Backend (Spring Boot 3 / Java 21)

* **Build Tool:** Gradle (Wrapper `./gradlew.bat`).
* **Base de Datos:** PostgreSQL en Supabase / JPA Hibernate (schema `facturacion`).
* **Migración Jakarta:** Uso exclusivo de dependencias `jakarta.*` (`jakarta.persistence.*`, `jakarta.xml.ws.*`, `jakarta.xml.bind.*`, `jakarta.jws.*`).

### 2.1. Entidades de Dominio (`Entities/Dominio` y `Entities/Abstractas`)
* **`AuditoriaApp` (Clase Abstracta):** Controla `fechaAlta`, `fechaModificacion`, `fechaBaja` (baja lógica `@Temporal(TemporalType.DATE)`), `usuarioCarga`, `usuarioModificacion` y `usuarioBaja`.
* **`FacturaVenta`:** Representa el comprobante de venta.
  * Atributos clave: `id`, `numero` (nullable hasta facturar), `fechaEmision`, `importeTotal`, `importeCobrado`, `importeSaldo`, `estado` (`PENDIENTE`, `EMITIDA`, `ANULADA`), `fechaAnulacion`, `observaciones`.
  * Atributos fiscales AFIP: `cae`, `caeFechaVencimiento`, `resultadoAfip`, `motivoRechazo`.
  * Relaciones: `@ManyToOne` con `PuntoVenta`, `Cliente`, `CondicionIva`, `TipoMoneda`, y `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` con `FacturaVentaDetalle`.
* **Otras entidades maestras:** `PuntoVenta`, `Cliente`, `CondicionIva`, `TipoMoneda`, `ListaPrecio`, `ListaPrecioArticulo`, `Articulo`, `Marca`, `Rubro`, `Usuario`. Todas heredan de `AuditoriaApp`.

### 2.2. DTOs (Java Records inmutables en `DTO/`)
* **`FacturaReporteDTO`:** `(Long id, Long numeroFactura, Date fechaEmision, String ClienteDenominacion, String condicionIva, String puntoVentaDescripcion, double importeTotal, Long CantidadItemns, String estado, String cae, Date caeFechaVencimiento)`. Utilizado para alimentar la grilla paginada y reportes fiscales impresos.
* **`FacturaReportePageDTO`:** DTO contenedor para paginación:
  ```java
  public record FacturaReportePageDTO(
      List<FacturaReporteDTO> facturas,
      int paginaActual,
      int totalPaginas,
      long totalElementos,
      int tamanioPagina
  ) {}
  ```
* **`FacturaVentaDTO`:** DTO completo de lectura. Incluye `cae`, `caeFechaVencimiento`, `resultadoAfip` y `motivoRechazo`.
* **`FacturaRequestDTO`:** DTO de alta. No contiene `numero` (generación delegada a AFIP). Valida que exista al menos un renglón/detalle.
* **`FacturaEditDTO`:** DTO de edición diferencial (PATCH). No permite alterar el número de comprobante. Redondea matemáticamente subtotales y saldos a 2 decimales.

### 2.3. Capa de Repositorios (`Repository/`)
* **`AudotoriaRepository<T, ID>`:** Interfaz genérica base:
  * `List<T> findByFechaBajaIsNull()`
  * `Optional<T> findByIdAndFechaBajaIsNull(ID id)`
* **`FacturaVentaRepository`:**
  * `findByNumeroAndFechaBajaIsNull(Long numero)`
  * `findByFechaBajaIsNull()`
  * `findByIdActiva(@Param("id") Long id)`: `SELECT f FROM FacturaVenta f WHERE f.id = :id AND f.fechaBaja IS NULL`
* **`ListaPrecioArticuloRepository`:**
  * `findByListaPrecioIdAndFechaBajaIsNull(Long listaPrecioId)`

### 2.4. Capa de Servicios (`Service/`)
* **`FacturaVentaService` / `FacturaVentaServiceImpl`:**
  * `save(FacturaRequestDTO)`: Valida existencia de detalles, calcula y redondea a 2 decimales importes brutos, descuentos, netos, IVA (21%) y total acumulado. Estado inicial: `PENDIENTE`.
  * `update(Long id, FacturaEditDTO)`: Rechaza modificaciones si la factura está en `EMITIDA`, `ANULADA` o con `fechaBaja != null`.
  * `deleteById(Long id)`: Aplica baja lógica asignando `fechaBaja = new Date()`. Bloquea la baja si la factura ya está emitida (`numero > 0` o `estado == "EMITIDA"`).
  * `anularFacturaPorId(Long id)`: Asigna `fechaAnulacion = new Date()` y `estado = "ANULADA"`. Bloquea si ya está emitida ante AFIP o si fue dada de baja lógica.
* **`ReporteFacturaService`:**
  * Paginación eficiente con `ObtenerFacturas(Date fechaDesde, Date fechaHasta, String estado, Double montoMinimo, int page, int size)`.
  * Solo cuenta para la paginación los registros activos (`WHERE f.fechaBaja IS NULL`).
  * Ejecuta `COUNT(f)` sobre JPQL para calcular el total de páginas y `setFirstResult(page * size).setMaxResults(size)` con ordenamiento estable `ORDER BY f.id DESC`.
  * Exportación a PDF (OpenPDF) y TSV (Excel).
* **Servicios Maestros (`Cliente`, `PuntoVenta`, `CondicionIva`, etc.):**
  * Implementan `findByFechaBajaIsNull()` en sus consultas `findAll()` para excluir registros eliminados de los modales de selección.

### 2.5. Controladores REST (`Controller/`)
* **`FacturaVentaController` (`/api/facturas`):**
  * `GET /`: Listar todas las facturas activas.
  * `GET /{id}`: Obtener detalle completo por ID.
  * `POST /`: Crear factura en estado `PENDIENTE` (201 Created).
  * `PATCH /{id}`: Modificación diferencial de cabecera o renglones.
  * `DELETE /{id}`: Baja lógica por ID (204 No Content).
  * `POST /{id}/anular`: Anulación comercial por ID.
  * `POST /{id}/facturar`: **Emisión electrónica ante ARCA / AFIP.**
  * `GET /afip/test-wsaa`: Diagnóstico de autenticación WSAA en vivo.
* **`FacturaRestController` (`/api/facturareportes`):**
  * `GET /?page={0}&size={10}&fechaDesde=...&fechaHasta=...&estado=...&montoMinimo=...`: Consulta paginada y filtrada para la grilla.
  * `GET /exportar/pdf?fechaDesde=...&fechaHasta=...&estado=...&montoMinimo=...`: Descarga de archivo binario PDF con cabecera `Content-Disposition: attachment; filename="reporte_facturas.pdf"` (incluye CAE y Vto. CAE de ARCA).
  * `GET /exportar/excel?fechaDesde=...&fechaHasta=...&estado=...&montoMinimo=...`: Descarga de archivo Excel TSV con cabecera `Content-Disposition: attachment; filename="reporte_facturas.xls"` (incluye CAE y Vto. CAE de ARCA).
* **`GlobalExceptionHandler`:**
  * Manejo unificado de excepciones `RuntimeException` (404 si contiene "no encontrad", 400 para reglas de negocio) y `Exception` (500). Seguro contra valores nulos en mensajes.

---

## 3. Arquitectura del Frontend (Vite + TypeScript)

* **Ubicación:** Subdirectorio `frontend/`.
* **Configuración Vite:** Multi-página (`index.html` y `nueva-factura.html`) utilizando `import.meta.dirname` para resolución de rutas.
* **Servicio Base (`getResources.ts`):** Evalúa rigurosamente `if (!response.ok)` y captura el mensaje del backend antes de parsear, previniendo fallos en silencio.

### 3.1. Pantalla Principal (`index.html` / `src/main.ts`)
* **Exportación de Reportes con CAE y Vto. CAE:**
  * Botones `Exportar PDF` y `Exportar Excel` en la cabecera principal (`.header-right`).
  * Integración con la **File System Access API (`window.showSaveFilePicker`)**: abre el cuadro de diálogo nativo del sistema operativo ("Guardar como...") permitiendo al usuario seleccionar la carpeta destino y el nombre del archivo. Cuenta con fallback automático a descarga por Blob URL en navegadores sin soporte.
  * Respeta los filtros activos de búsqueda (`fechaDesde`, `fechaHasta`, `estado`, `montoMinimo`).
* **Paginación Dual Sincronizada:**
  * Controles de paginación tanto en la parte superior (`#paginacion-contenedor-top`) como en la inferior (`#paginacion-contenedor`).
  * Botones `Anterior` y `Siguiente`, selector dinámico de tamaño de página (5, 10, 20, 50) y etiqueta de estado ("Página X de Y (Z facturas)").
  * Disparadores de filtros que reinician la vista a la página 0.
* **Tabla de Facturas:**
  * Columna número: `#${f.numeroFactura}` o badge gris `Pendiente`.
  * Acciones por fila:
    1. **Consultar:** Modal con desglose de ítems, cliente, totales y estado de autorización AFIP (CAE, vencimiento y motivo de rechazo si aplica).
    2. **Modificar:** Abre modal de edición. **Deshabilitado en gris** si `estado === 'EMITIDA'`. No incluye campo de edición de número.
    3. **Facturar:** Envía `POST /api/facturas/{id}/facturar`, obtiene CAE y número fiscal, y refresca la tabla automáticamente.

### 3.2. Pantalla de Creación (`nueva-factura.html` / `src/nuevaFactura.ts`)
* **Sin campo de número de factura:** Se eliminó por completo el input manual del número.
* **Cero Latencia / Carga Bajo Demanda:** Los catálogos de Clientes, Puntos de Venta, IVA y Monedas se consultan vía API únicamente al abrir sus respectivos modales de selección.
* **Selección de Artículos:** Condicionada a la elección previa de una Lista de Precios activa.
* **Cálculo reactivo:** Actualización en tiempo real de subtotales, IVA 21% y total acumulado a medida que se ingresan cantidades o descuentos.

---

## 4. Módulo de Integración ARCA / AFIP (Facturación Homologación)

### 4.1. Parámetros de Homologación y Credenciales
* **CUIT Representado / Computador:** Variable de entorno `AFIP_CUIT` o configurado en `application-local.yaml`.
* **Alias del Computador Fiscal:** Definido en portal WSASS (ej. `HomologacionTest`).
* **Servicio Autorizado:** `wsfe` (Facturación Electrónica).
* **Entorno:** Homologación (Testing con WSASS).
* **Archivos Criptográficos en la raíz del proyecto:**
  * `privada_test.key`: Clave privada RSA 2048-bit.
  * `solicitud_test.csr`: Solicitud CSR firmada.
  * `certificado_test.crt`: Certificado X.509 homologado otorgado por AFIP.

### 4.2. Servicio de Autenticación WSAA (`AfipWsaaService.java`)
* **URL:** `https://wsaahomo.afip.gov.ar/ws/services/LoginCms`
* **Flujo:**
  1. Genera XML TRA con timestamps ISO-8601 en segundos (`yyyy-MM-dd'T'HH:mm:ssXXX`).
  2. Firma digitalmente en formato CMS / PKCS#7 con BouncyCastle.
  3. Invoca SOAP `LoginCms` y extrae `token` y `sign`.
  4. **Caché de Ticket (12 Horas):** Almacena token y sign en `ticket-homo-wsfe.properties` para reutilización segura.

### 4.3. Servicio de Facturación WSFE (`AfipWsfeService.java`)
* **Blindaje de Emisión:** Valida que la factura no esté anulada, ni dada de baja, ni cuente previamente con CAE.
* **Determinación del Próximo Número:** Invoca `FECompUltimoAutorizado` por Punto de Venta y Tipo de Comprobante, asignando `ultimoNumero + 1`.
* **Emisión Electrónica (`FECAESolicitar`):**
  * Envía cabecera `FECAECabRequest` y detalle `FECAEDetRequest`.
  * Configura `condicionIVAReceptorId` conforme RG 5616.
  * Desglose de IVA en alícuota 21% (ID 5) calculando base imponible e importe neto.
* **Persistencia:** Si el resultado es `A` (Aprobado), guarda CAE, vencimiento, nuevo número y estado `EMITIDA`. Si es `R` (Rechazado), registra `motivoRechazo` y `resultadoAfip = "R"`.

---

## 5. Guía Rápida de Ejecución y Pruebas

1. **Compilar Backend:**
   ```powershell
   ./gradlew.bat compileJava --console=plain
   ```
2. **Iniciar Backend:**
   Ejecutar `TpGrupalApplication.java` desde IntelliJ IDEA o por terminal:
   ```powershell
   ./gradlew.bat bootRun
   ```
3. **Probar Diagnóstico AFIP en Vivo:**
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8080/api/facturas/afip/test-wsaa" -Method Get
   ```
4. **Compilar y Probar Frontend:**
   ```powershell
   cd frontend
   pnpm run build
   pnpm dev
   ```
   Abrir `http://localhost:5173/` en el navegador.
