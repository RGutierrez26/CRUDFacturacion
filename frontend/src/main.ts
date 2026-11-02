import './style.css';
import {
  obtenerFacturas,
  obtenerFacturaPorId,
  eliminarFacturaPorId,
  anularFacturaPorId,
  facturarFacturaPorId,
  actualizarFactura,
  obtenerPuntosVenta,
  obtenerClientes,
  obtenerCondicionesIva,
  obtenerTiposMoneda,
  obtenerListasPrecioArticulos,
  descargarReporte
} from './services/obtenerfacturas';
import type {
  FacturaReporteDTO,
  FacturaReportePageDTO,
  FacturaFiltrosDTO,
  FacturaVentaDTO,
  FacturaEditDTO,
  PuntoVentaDTO,
  ClienteDTO,
  CondicionIvaDTO,
  TipoMonedaDTO,
  ListaPrecioArticuloDTO
} from './models/factura.dto';

// Elementos del DOM Principal
const tbody = document.getElementById('tabla-facturas-body') as HTMLTableSectionElement;
const formFiltros = document.getElementById('form-filtros') as HTMLFormElement;
const btnResetear = document.getElementById('btn-resetear') as HTMLButtonElement;
const contadorFacturas = document.getElementById('contador-facturas') as HTMLSpanElement;

// Elementos de Paginación del DOM (Inferior y Superior)
const pagDesde = document.getElementById('pag-desde') as HTMLElement | null;
const pagHasta = document.getElementById('pag-hasta') as HTMLElement | null;
const pagTotal = document.getElementById('pag-total') as HTMLElement | null;
const pagIndicador = document.getElementById('pag-indicador') as HTMLElement | null;
const btnPagPrimera = document.getElementById('btn-pag-primera') as HTMLButtonElement | null;
const btnPagAnterior = document.getElementById('btn-pag-anterior') as HTMLButtonElement | null;
const btnPagSiguiente = document.getElementById('btn-pag-siguiente') as HTMLButtonElement | null;
const btnPagUltima = document.getElementById('btn-pag-ultima') as HTMLButtonElement | null;

const pagDesdeTop = document.getElementById('pag-desde-top') as HTMLElement | null;
const pagHastaTop = document.getElementById('pag-hasta-top') as HTMLElement | null;
const pagTotalTop = document.getElementById('pag-total-top') as HTMLElement | null;
const pagIndicadorTop = document.getElementById('pag-indicador-top') as HTMLElement | null;
const btnPagPrimeraTop = document.getElementById('btn-pag-primera-top') as HTMLButtonElement | null;
const btnPagAnteriorTop = document.getElementById('btn-pag-anterior-top') as HTMLButtonElement | null;
const btnPagSiguienteTop = document.getElementById('btn-pag-siguiente-top') as HTMLButtonElement | null;
const btnPagUltimaTop = document.getElementById('btn-pag-ultima-top') as HTMLButtonElement | null;

// Estado de paginación y filtros
let paginaActual = 0;
const tamanioPagina = 10;
let totalPaginas = 1;
let totalElementos = 0;
let filtrosActuales: FacturaFiltrosDTO | undefined = undefined;

// Elementos del Modal Revisar Detalle
const modalDetalle = document.getElementById('modal-detalle') as HTMLDivElement;
const modalTitulo = document.getElementById('modal-titulo') as HTMLHeadingElement;
const modalBody = document.getElementById('modal-body') as HTMLDivElement;
const modalBtnCerrar = document.getElementById('modal-btn-cerrar') as HTMLButtonElement;
const modalBtnCerrarFooter = document.getElementById('modal-btn-cerrar-footer') as HTMLButtonElement;

// Elementos del Modal Modificar Factura
const modalEditar = document.getElementById('modal-editar') as HTMLDivElement;
const modalEditarTitulo = document.getElementById('modal-editar-titulo') as HTMLHeadingElement;
const modalEditarBody = document.getElementById('modal-editar-body') as HTMLDivElement;
const modalEditarBtnCerrar = document.getElementById('modal-editar-btn-cerrar') as HTMLButtonElement;
const modalEditarBtnCancelar = document.getElementById('modal-editar-btn-cancelar') as HTMLButtonElement;
const formEditarFactura = document.getElementById('form-editar-factura') as HTMLFormElement;
const btnGuardarEdicion = document.getElementById('modal-editar-btn-guardar') as HTMLButtonElement;
const btnModalAnular = document.getElementById('btn-modal-anular') as HTMLButtonElement | null;
const btnModalBajaLogica = document.getElementById('btn-modal-baja-logica') as HTMLButtonElement | null;

// Interfaz para gestionar ítems en edición
interface DetalleEdicion {
  listaPrecioArticuloId: number;
  codigo: string;
  descripcion: string;
  precioUnitario: number;
  cantidad: number;
  porcentajeBonificacion: number;
  importeNeto: number;
  importeIva: number;
  importeSubtotal: number;
}

// Estado para edición
let facturaEnEdicion: FacturaVentaDTO | null = null;
let detallesEnEdicion: DetalleEdicion[] = [];
let articulosMaestros: ListaPrecioArticuloDTO[] = [];
let modificarDetallesActivo = false;

// Funciones de formateo
function formatearFecha(fechaStr?: string): string {
  if (!fechaStr) return '-';
  const fecha = new Date(fechaStr);
  return isNaN(fecha.getTime()) ? fechaStr : fecha.toLocaleDateString('es-AR');
}

function formatearMoneda(monto: number): string {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(monto);
}

function calcularValoresDetalle(cantidad: number, precioUnitario: number, bonificacion: number) {
  const importeBruto = cantidad * precioUnitario;
  const descuento = importeBruto * (bonificacion / 100.0);
  const importeNeto = importeBruto - descuento;
  const importeIva = importeNeto * 0.21;
  const importeSubtotal = importeNeto + importeIva;
  return { importeNeto, importeIva, importeSubtotal };
}

// Renderizado de la tabla principal
function renderizarTabla(facturas: FacturaReporteDTO[]) {
  if (!facturas || facturas.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="9" class="text-center py-4" style="color: var(--text-muted);">
          No se encontraron facturas con los filtros seleccionados.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = facturas.map((f) => {
    const estadoUpper = (f.estado || 'PENDIENTE').toUpperCase();
    let badgeClase = 'badge-estado-pendiente';
    if (estadoUpper === 'ANULADA') {
      badgeClase = 'badge-estado-anulada';
    } else if (estadoUpper === 'EMITIDA') {
      badgeClase = 'badge-estado-emitida';
    }

    const esEmitida = estadoUpper === 'EMITIDA';
    const esAnulada = estadoUpper === 'ANULADA';

    const numeroTexto = (f.numeroFactura != null && f.numeroFactura > 0)
      ? `#${f.numeroFactura}`
      : '<span class="badge-estado badge-estado-pendiente" style="font-weight: 600; text-transform: none; padding: 0.1rem 0.45rem;">Pendiente</span>';

    return `
      <tr>
        <td class="factura-num">${numeroTexto}</td>
        <td>${formatearFecha(f.fechaEmision)}</td>
        <td><strong>${f.ClienteDenominacion || 'Consumidor Final'}</strong></td>
        <td>${f.condicionIva}</td>
        <td>${f.puntoVentaDescripcion}</td>
        <td class="text-right factura-total">${formatearMoneda(f.importeTotal)}</td>
        <td class="text-center">${f.CantidadItemns}</td>
        <td class="text-center">
          <span class="badge-estado ${badgeClase}">
            ${f.estado || 'PENDIENTE'}
          </span>
        </td>
        <td class="text-center">
          <div class="action-buttons">
            <button class="btn-action btn-consultar" data-id="${f.id}" title="Consultar detalle de la factura">
              Consultar
            </button>
            <button class="btn-action btn-modify" data-id="${f.id}" title="${esEmitida ? 'No se puede modificar una factura ya emitida' : 'Modificar comprobante y estados'}" ${esEmitida ? 'disabled' : ''}>
              Modificar
            </button>
            <button class="btn-action btn-facturar" data-id="${f.id}" title="${esEmitida ? 'Factura ya autorizada ante AFIP' : (esAnulada ? 'Factura anulada' : 'Facturar con AFIP / ARCA')}" ${esEmitida || esAnulada ? 'disabled' : ''}>
              Facturar
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  vincularEventosTabla();
}

// Vincular clicks en la tabla principal
function vincularEventosTabla() {
  // Botones Consultar
  tbody.querySelectorAll('.btn-consultar').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const id = Number((btn as HTMLElement).dataset.id);
      await abrirModalDetalle(id);
    });
  });

  // Botones Modificar
  tbody.querySelectorAll('.btn-modify').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const id = Number((btn as HTMLElement).dataset.id);
      await abrirModalEditar(id);
    });
  });

  // Botones Facturar (AFIP/ARCA WSFE)
  tbody.querySelectorAll('.btn-facturar').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const buttonEl = btn as HTMLButtonElement;
      const id = Number(buttonEl.dataset.id);

      const confirmar = confirm(`¿Desea enviar la factura a autorizar ante ARCA / AFIP (Entorno de Homologación)?`);
      if (!confirmar) return;

      const textoOriginal = buttonEl.textContent;
      buttonEl.disabled = true;
      buttonEl.textContent = 'Facturando...';

      try {
        const respuesta = await facturarFacturaPorId(id);
        alert(
          `¡Comprobante autorizado con éxito por ARCA / AFIP!\n\n` +
          `• Comprobante N°: ${respuesta.numero}\n` +
          `• CAE Oficial: ${respuesta.cae || 'N/A'}\n` +
          `• Vencimiento CAE: ${respuesta.caeFechaVencimiento ? new Date(respuesta.caeFechaVencimiento).toLocaleDateString() : 'N/A'}\n` +
          `• Estado AFIP: ${respuesta.resultadoAfip || 'Aprobado'}`
        );
        await cargarFacturas(filtrosActuales, paginaActual);
      } catch (err: any) {
        alert(`Error al facturar con AFIP: ${err?.message || err}`);
      } finally {
        buttonEl.disabled = false;
        buttonEl.textContent = textoOriginal;
      }
    });
  });
}

// Cargar facturas principales (Paginación Server-Side)
async function cargarFacturas(filtros?: FacturaFiltrosDTO, page: number = 0) {
  try {
    filtrosActuales = filtros;
    paginaActual = page;

    tbody.innerHTML = `
      <tr>
        <td colspan="9" class="text-center py-4" style="color: var(--text-muted);">
          Cargando comprobantes...
        </td>
      </tr>
    `;

    const data: FacturaReportePageDTO = await obtenerFacturas(filtrosActuales, paginaActual, tamanioPagina);

    totalPaginas = Math.max(1, data.totalPaginas);
    paginaActual = data.paginaActual;
    totalElementos = data.totalElementos;

    // Actualizar badges y contadores
    if (contadorFacturas) {
      contadorFacturas.textContent = `${totalElementos} comprobante(s)`;
    }

    if (totalElementos === 0) {
      if (pagDesde) pagDesde.textContent = '0';
      if (pagHasta) pagHasta.textContent = '0';
      if (pagTotal) pagTotal.textContent = '0';
      if (pagIndicador) pagIndicador.textContent = 'Página 1 de 1';

      if (pagDesdeTop) pagDesdeTop.textContent = '0';
      if (pagHastaTop) pagHastaTop.textContent = '0';
      if (pagTotalTop) pagTotalTop.textContent = '0';
      if (pagIndicadorTop) pagIndicadorTop.textContent = 'Página 1 de 1';
    } else {
      const desde = paginaActual * tamanioPagina + 1;
      const hasta = Math.min((paginaActual + 1) * tamanioPagina, totalElementos);
      const indicadorTexto = `Página ${paginaActual + 1} de ${totalPaginas}`;

      if (pagDesde) pagDesde.textContent = String(desde);
      if (pagHasta) pagHasta.textContent = String(hasta);
      if (pagTotal) pagTotal.textContent = String(totalElementos);
      if (pagIndicador) pagIndicador.textContent = indicadorTexto;

      if (pagDesdeTop) pagDesdeTop.textContent = String(desde);
      if (pagHastaTop) pagHastaTop.textContent = String(hasta);
      if (pagTotalTop) pagTotalTop.textContent = String(totalElementos);
      if (pagIndicadorTop) pagIndicadorTop.textContent = indicadorTexto;
    }

    // Actualizar estados de botones de navegación (Inferior y Superior)
    const esPrimeraPagina = paginaActual <= 0;
    const esUltimaPagina = paginaActual >= totalPaginas - 1;

    if (btnPagPrimera) btnPagPrimera.disabled = esPrimeraPagina;
    if (btnPagAnterior) btnPagAnterior.disabled = esPrimeraPagina;
    if (btnPagSiguiente) btnPagSiguiente.disabled = esUltimaPagina;
    if (btnPagUltima) btnPagUltima.disabled = esUltimaPagina;

    if (btnPagPrimeraTop) btnPagPrimeraTop.disabled = esPrimeraPagina;
    if (btnPagAnteriorTop) btnPagAnteriorTop.disabled = esPrimeraPagina;
    if (btnPagSiguienteTop) btnPagSiguienteTop.disabled = esUltimaPagina;
    if (btnPagUltimaTop) btnPagUltimaTop.disabled = esUltimaPagina;

    renderizarTabla(data.facturas);
  } catch (error) {
    console.error('Error al cargar facturas:', error);
    tbody.innerHTML = `
      <tr>
        <td colspan="9" class="text-center py-4" style="color: var(--danger);">
          Ocurrió un error al cargar las facturas desde el servidor.
        </td>
      </tr>
    `;
  }
}

// ==========================================
// MODAL REVISAR DETALLE
// ==========================================
async function abrirModalDetalle(id: number) {
  modalTitulo.textContent = `Detalle de Factura`;
  modalBody.innerHTML = `<p class="text-center py-4">Cargando información del comprobante...</p>`;
  modalDetalle.classList.remove('hidden');

  try {
    const factura: FacturaVentaDTO = await obtenerFacturaPorId(id);
    modalTitulo.textContent = (factura.numero != null && factura.numero > 0)
      ? `Detalle de Factura #${factura.numero}`
      : `Detalle de Factura (Pendiente AFIP)`;

    modalBody.innerHTML = `
      <div class="modal-grid">
        <div>
          <span class="modal-item-title">Cliente</span>
          <p class="modal-item-value">${factura.cliente?.denominacion || 'Consumidor Final'}</p>
          <small style="color: var(--text-muted);">${factura.cliente?.cuitCuil || ''}</small>
        </div>
        <div>
          <span class="modal-item-title">Fecha Emisión</span>
          <p class="modal-item-value">${formatearFecha(factura.fechaEmision)}</p>
        </div>
        <div>
          <span class="modal-item-title">Punto de Venta</span>
          <p class="modal-item-value">${factura.puntoVenta?.descripcion || '-'}</p>
        </div>
        <div>
          <span class="modal-item-title">Condición IVA / Moneda</span>
          <p class="modal-item-value">${factura.condicionIva?.denominacion || '-'} (${factura.tipoMoneda?.simbolo || '$'})</p>
        </div>
        <div>
          <span class="modal-item-title">Estado</span>
          <p class="modal-item-value" style="color: ${factura.estado === 'ANULADA' ? 'var(--danger)' : (factura.estado === 'PENDIENTE' ? 'var(--text-muted)' : 'var(--primary)')};">
            ${factura.estado}
          </p>
        </div>
        <div>
          <span class="modal-item-title">Total Comprobante</span>
          <p class="modal-item-value" style="font-size: 1.1rem; color: #0f172a;">
            ${formatearMoneda(factura.importeTotal)}
          </p>
        </div>
        ${factura.cae ? `
        <div>
          <span class="modal-item-title">CAE AFIP / ARCA</span>
          <p class="modal-item-value" style="color: #0369a1; font-weight: 700;">${factura.cae}</p>
          <small style="color: var(--text-muted);">Vto: ${factura.caeFechaVencimiento ? new Date(factura.caeFechaVencimiento).toLocaleDateString() : '-'}</small>
        </div>
        ` : ''}
      </div>

      <div>
        <h4 style="margin-bottom: 0.5rem; color: var(--text-main);">Renglones de la Factura:</h4>
        <div class="table-responsive">
          <table class="modal-table">
            <thead>
              <tr>
                <th>Artículo / Descripción</th>
                <th class="text-center">Cant.</th>
                <th class="text-right">P. Unitario</th>
                <th class="text-right">Bonif.</th>
                <th class="text-right">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              ${
                factura.detalles && factura.detalles.length > 0
                  ? factura.detalles.map(d => `
                    <tr>
                      <td>${d.descripcion || d.listaPrecioArticulo?.articuloDenominacion || 'Item'}</td>
                      <td class="text-center">${d.cantidad}</td>
                      <td class="text-right">${formatearMoneda(d.precioUnitario)}</td>
                      <td class="text-right">${d.porcentajeBonificacion}%</td>
                      <td class="text-right"><strong>${formatearMoneda(d.importeSubtotal)}</strong></td>
                    </tr>
                  `).join('')
                  : `<tr><td colspan="5" class="text-center" style="color: var(--text-muted);">Sin ítems detallados.</td></tr>`
              }
            </tbody>
          </table>
        </div>
      </div>

      ${factura.observaciones ? `<p><small><strong>Observaciones:</strong> ${factura.observaciones}</small></p>` : ''}
    `;
  } catch (error) {
    console.error('Error al obtener factura:', error);
    modalBody.innerHTML = `<p style="color: var(--danger);">No se pudo cargar el detalle del comprobante seleccionado.</p>`;
  }
}

function cerrarModalDetalle() {
  modalDetalle.classList.add('hidden');
}

modalBtnCerrar.addEventListener('click', cerrarModalDetalle);
modalBtnCerrarFooter.addEventListener('click', cerrarModalDetalle);
modalDetalle.addEventListener('click', (e) => {
  if (e.target === modalDetalle) cerrarModalDetalle();
});

// ==========================================
// MODAL MODIFICAR FACTURA (EDICIÓN COMPLETA)
// ==========================================
async function abrirModalEditar(id: number) {
  modalEditarTitulo.textContent = `Modificar Factura`;
  modalEditarBody.innerHTML = `<p class="text-center py-4">Cargando datos para edición...</p>`;
  modalEditar.classList.remove('hidden');

  try {
    const [factura, puntosVenta, clientes, condicionesIva, tiposMoneda, articulos] = await Promise.all([
      obtenerFacturaPorId(id),
      obtenerPuntosVenta(),
      obtenerClientes(),
      obtenerCondicionesIva(),
      obtenerTiposMoneda(),
      obtenerListasPrecioArticulos()
    ]);

    facturaEnEdicion = factura;
    modalEditarTitulo.textContent = (factura.numero != null && factura.numero > 0)
      ? `Modificar Factura #${factura.numero}`
      : `Modificar Factura (Pendiente AFIP)`;
    articulosMaestros = articulos;
    modificarDetallesActivo = false;

    // Inicializar copia de los detalles existentes
    detallesEnEdicion = (factura.detalles || []).map(d => {
      const precio = d.precioUnitario || d.listaPrecioArticulo?.precioVenta || 0;
      const calc = calcularValoresDetalle(d.cantidad, precio, d.porcentajeBonificacion);
      return {
        listaPrecioArticuloId: d.listaPrecioArticulo?.id || 0,
        codigo: d.listaPrecioArticulo?.articuloCodigo || '-',
        descripcion: d.descripcion || d.listaPrecioArticulo?.articuloDenominacion || '',
        precioUnitario: precio,
        cantidad: d.cantidad,
        porcentajeBonificacion: d.porcentajeBonificacion,
        importeNeto: d.importeNeto || calc.importeNeto,
        importeIva: d.importeIva || calc.importeIva,
        importeSubtotal: d.importeSubtotal || calc.importeSubtotal
      };
    });

    const fechaFormateada = factura.fechaEmision ? factura.fechaEmision.split('T')[0] : '';

    modalEditarBody.innerHTML = `
      <!-- 1. Cabecera de Atributos -->
      <div class="form-grid-3">
        <div class="form-group">
          <label for="edit-punto-venta">Punto de Venta *</label>
          <select id="edit-punto-venta" required>
            ${puntosVenta.map((pv: PuntoVentaDTO) => `
              <option value="${pv.id}" ${factura.puntoVenta?.id === pv.id ? 'selected' : ''}>
                ${String(pv.numero).padStart(4, '0')} - ${pv.descripcion}
              </option>
            `).join('')}
          </select>
        </div>

        <div class="form-group">
          <label for="edit-fecha">Fecha de Emisión *</label>
          <input type="date" id="edit-fecha" value="${fechaFormateada}" required />
        </div>

        <div class="form-group">
          <label for="edit-cliente">Cliente</label>
          <select id="edit-cliente">
            <option value="">Consumidor Final</option>
            ${clientes.map((c: ClienteDTO) => `
              <option value="${c.id}" ${factura.cliente?.id === c.id ? 'selected' : ''}>
                ${c.denominacion} (${c.cuitCuil})
              </option>
            `).join('')}
          </select>
        </div>

        <div class="form-group">
          <label for="edit-condicion-iva">Condición IVA *</label>
          <select id="edit-condicion-iva" required>
            ${condicionesIva.map((ci: CondicionIvaDTO) => `
              <option value="${ci.id}" ${factura.condicionIva?.id === ci.id ? 'selected' : ''}>
                ${ci.denominacion}
              </option>
            `).join('')}
          </select>
        </div>

        <div class="form-group">
          <label for="edit-tipo-moneda">Tipo de Moneda *</label>
          <select id="edit-tipo-moneda" required>
            ${tiposMoneda.map((tm: TipoMonedaDTO) => `
              <option value="${tm.id}" ${factura.tipoMoneda?.id === tm.id ? 'selected' : ''}>
                ${tm.denominacion} (${tm.simbolo})
              </option>
            `).join('')}
          </select>
        </div>

        <div class="form-group">
          <label for="edit-estado">Estado *</label>
          <select id="edit-estado" required>
            <option value="PENDIENTE" ${factura.estado === 'PENDIENTE' ? 'selected' : ''}>PENDIENTE</option>
            <option value="EMITIDA" ${factura.estado === 'EMITIDA' ? 'selected' : ''}>EMITIDA</option>
            <option value="ANULADA" ${factura.estado === 'ANULADA' ? 'selected' : ''}>ANULADA</option>
          </select>
        </div>

        <div class="form-group span-full">
          <label for="edit-observaciones">Observaciones</label>
          <input type="text" id="edit-observaciones" value="${factura.observaciones || ''}" placeholder="Observaciones opcionales" />
        </div>
      </div>

      <!-- 2. Interruptor / Selector para modificar renglones -->
      <div style="margin-top: 0.5rem;">
        <label class="checkbox-toggle-container">
          <input type="checkbox" id="check-modificar-detalles" />
          <span>Modificar renglones / detalles de la factura</span>
        </label>
      </div>

      <!-- 3. Contenedor Dinámico de Detalles -->
      <div id="contenedor-edicion-detalles" style="margin-top: 0.25rem;">
        <!-- Se renderiza por JS según el estado del checkbox -->
      </div>
    `;

    // Vincular toggle de detalles
    const checkModificarDetalles = document.getElementById('check-modificar-detalles') as HTMLInputElement;
    checkModificarDetalles.addEventListener('change', () => {
      modificarDetallesActivo = checkModificarDetalles.checked;
      renderizarSeccionDetallesEdicion();
    });

    renderizarSeccionDetallesEdicion();

  } catch (error) {
    console.error('Error al abrir modal de edición:', error);
    modalEditarBody.innerHTML = `<p style="color: var(--danger);">No se pudo cargar la información para editar el comprobante seleccionado.</p>`;
  }
}

// Renderizado condicional de la sección de detalles dentro del modal de edición
function renderizarSeccionDetallesEdicion() {
  const contenedor = document.getElementById('contenedor-edicion-detalles');
  if (!contenedor) return;

  if (!modificarDetallesActivo) {
    // Modo solo lectura de detalles (los actuales se conservan intactos)
    contenedor.innerHTML = `
      <div style="background: #f8fafc; border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 0.75rem 1rem; margin-bottom: 0.75rem;">
        <small style="color: var(--text-muted);">
          Los renglones actuales se mantendrán sin cambios al guardar. Active la casilla superior si desea modificar o reemplazar los ítems.
        </small>
      </div>
      <div class="table-responsive">
        <table class="modal-table">
          <thead>
            <tr>
              <th>Artículo</th>
              <th class="text-center">Cant.</th>
              <th class="text-right">Precio</th>
              <th class="text-right">Desc %</th>
              <th class="text-right">Subtotal</th>
            </tr>
          </thead>
          <tbody>
            ${
              detallesEnEdicion.length > 0
                ? detallesEnEdicion.map(d => `
                  <tr>
                    <td><strong>${d.descripcion}</strong></td>
                    <td class="text-center">${d.cantidad}</td>
                    <td class="text-right">${formatearMoneda(d.precioUnitario)}</td>
                    <td class="text-right">${d.porcentajeBonificacion}%</td>
                    <td class="text-right">${formatearMoneda(d.importeSubtotal)}</td>
                  </tr>
                `).join('')
                : `<tr><td colspan="5" class="text-center" style="color: var(--text-muted);">Sin ítems.</td></tr>`
            }
          </tbody>
        </table>
      </div>
    `;
    return;
  }

  // Modo edición de renglones habilitado
  let totalNeto = 0;
  let totalIva = 0;
  let totalFactura = 0;

  detallesEnEdicion.forEach(d => {
    totalNeto += d.importeNeto;
    totalIva += d.importeIva;
    totalFactura += d.importeSubtotal;
  });

  contenedor.innerHTML = `
    <!-- Card para Agregar Renglón (Cómodo y sin desbordes) -->
    <div class="add-item-card" style="margin-bottom: 1rem;">
      <div class="add-item-row-top">
        <div class="form-group">
          <label for="edit-select-producto">Artículo / Producto *</label>
          <select id="edit-select-producto">
            <option value="">Seleccione un artículo...</option>
            ${articulosMaestros.map(a => `
              <option value="${a.id}">[${a.articuloDTO?.codigo || a.articuloCodigo || 'ART'}] ${a.articuloDTO?.denominacion || a.articuloDenominacion || 'Artículo'} - ${formatearMoneda(a.precioVenta)}</option>
            `).join('')}
          </select>
        </div>

        <div class="form-group">
          <label for="edit-input-precio">Precio Unitario ($)</label>
          <input type="number" id="edit-input-precio" placeholder="0.00" readonly tabindex="-1" style="background-color: #ffffff; cursor: not-allowed;" title="Precio fijado automáticamente por la lista de precios" />
        </div>
      </div>

      <div class="add-item-row-bottom">
        <div class="form-group">
          <label for="edit-input-cantidad">Cantidad *</label>
          <input type="number" id="edit-input-cantidad" min="1" step="any" value="1" />
        </div>

        <div class="form-group">
          <label for="edit-input-bonif">% Bonif. (Desc.)</label>
          <input type="number" id="edit-input-bonif" min="0" max="100" step="any" value="0" />
        </div>

        <div class="form-actions-inline">
          <button type="button" id="edit-btn-agregar-item" class="btn btn-primary" style="height: 38px; width: 100%;">
            + Agregar Renglón
          </button>
        </div>
      </div>
    </div>

    <!-- Tabla de ítems con botón de quitar -->
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>Código</th>
            <th>Artículo / Descripción</th>
            <th class="text-right">Precio</th>
            <th class="text-center">Cant.</th>
            <th class="text-right">Desc %</th>
            <th class="text-right">Subtotal</th>
            <th class="text-center">Acción</th>
          </tr>
        </thead>
        <tbody id="edit-tbody-detalles">
          ${
            detallesEnEdicion.length > 0
              ? detallesEnEdicion.map((item, idx) => `
                <tr>
                  <td><code>${item.codigo}</code></td>
                  <td><strong>${item.descripcion}</strong></td>
                  <td class="text-right">${formatearMoneda(item.precioUnitario)}</td>
                  <td class="text-center">${item.cantidad}</td>
                  <td class="text-right">${item.porcentajeBonificacion}%</td>
                  <td class="text-right"><strong>${formatearMoneda(item.importeSubtotal)}</strong></td>
                  <td class="text-center">
                    <button type="button" class="btn-action btn-delete edit-btn-quitar" data-index="${idx}" title="Quitar ítem">
                      Quitar
                    </button>
                  </td>
                </tr>
              `).join('')
              : `<tr><td colspan="7" class="text-center py-3" style="color: var(--text-muted);">No hay ítems en la lista.</td></tr>`
          }
        </tbody>
      </table>
    </div>

    <!-- Resumen de totales en edición -->
    <div class="invoice-summary" style="margin-top: 1rem;">
      <div class="summary-line">
        <span>Subtotal Neto:</span>
        <strong>${formatearMoneda(totalNeto)}</strong>
      </div>
      <div class="summary-line">
        <span>IVA (21%):</span>
        <strong>${formatearMoneda(totalIva)}</strong>
      </div>
      <div class="summary-line total-highlight">
        <span>Nuevo Total:</span>
        <strong>${formatearMoneda(totalFactura)}</strong>
      </div>
    </div>
  `;

  // Autocompletar precio al seleccionar artículo en el modal
  const editSelectProd = document.getElementById('edit-select-producto') as HTMLSelectElement;
  const editInputPrecio = document.getElementById('edit-input-precio') as HTMLInputElement;

  editSelectProd.addEventListener('change', () => {
    const selId = Number(editSelectProd.value);
    const art = articulosMaestros.find(a => a.id === selId);
    if (art) {
      editInputPrecio.value = art.precioVenta.toString();
    } else {
      editInputPrecio.value = '';
    }
  });

  // Agregar ítem a la lista de edición
  const btnAgregar = document.getElementById('edit-btn-agregar-item') as HTMLButtonElement;
  btnAgregar.addEventListener('click', () => {
    const selId = Number(editSelectProd.value);
    if (!selId) {
      alert('Seleccione un artículo de la lista.');
      return;
    }
    const art = articulosMaestros.find(a => a.id === selId);
    if (!art) return;

    const cant = parseFloat((document.getElementById('edit-input-cantidad') as HTMLInputElement).value);
    if (isNaN(cant) || cant <= 0) {
      alert('La cantidad debe ser mayor a 0.');
      return;
    }

    const bonif = parseFloat((document.getElementById('edit-input-bonif') as HTMLInputElement).value) || 0;
    if (bonif < 0 || bonif > 100) {
      alert('La bonificación debe estar entre 0 y 100.');
      return;
    }

    const calc = calcularValoresDetalle(cant, art.precioVenta, bonif);

    detallesEnEdicion.push({
      listaPrecioArticuloId: art.id,
      codigo: art.articuloDTO?.codigo || art.articuloCodigo || 'ART',
      descripcion: art.articuloDTO?.denominacion || art.articuloDenominacion || 'Artículo',
      precioUnitario: art.precioVenta,
      cantidad: cant,
      porcentajeBonificacion: bonif,
      importeNeto: calc.importeNeto,
      importeIva: calc.importeIva,
      importeSubtotal: calc.importeSubtotal
    });

    renderizarSeccionDetallesEdicion();
  });

  // Quitar ítem
  contenedor.querySelectorAll('.edit-btn-quitar').forEach((btn) => {
    btn.addEventListener('click', () => {
      const idx = Number((btn as HTMLElement).dataset.index);
      detallesEnEdicion.splice(idx, 1);
      renderizarSeccionDetallesEdicion();
    });
  });
}

function cerrarModalEditar() {
  modalEditar.classList.add('hidden');
  facturaEnEdicion = null;
  detallesEnEdicion = [];
}

modalEditarBtnCerrar.addEventListener('click', cerrarModalEditar);
modalEditarBtnCancelar.addEventListener('click', cerrarModalEditar);
modalEditar.addEventListener('click', (e) => {
  if (e.target === modalEditar) cerrarModalEditar();
});

// Guardar cambios de edición (PATCH /api/facturas/{id} - Envío diferencial)
formEditarFactura.addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!facturaEnEdicion) return;

  if (modificarDetallesActivo && detallesEnEdicion.length === 0) {
    alert('La factura debe tener al menos un renglón. No se permite dejar una factura sin ítems.');
    return;
  }

  const editPayload: FacturaEditDTO = {};

  // 1. Punto de Venta
  const puntoVentaVal = Number((document.getElementById('edit-punto-venta') as HTMLSelectElement).value);
  if (puntoVentaVal && puntoVentaVal !== facturaEnEdicion.puntoVenta?.id) {
    editPayload.puntoVentaId = puntoVentaVal;
  }

  // 2. Fecha de Emisión
  const fechaVal = (document.getElementById('edit-fecha') as HTMLInputElement).value;
  const originalFechaStr = facturaEnEdicion.fechaEmision ? facturaEnEdicion.fechaEmision.split('T')[0] : '';
  if (fechaVal && fechaVal !== originalFechaStr) {
    editPayload.fechaEmision = new Date(`${fechaVal}T12:00:00`).toISOString();
  }

  // 4. Cliente
  const clienteSelectVal = (document.getElementById('edit-cliente') as HTMLSelectElement).value;
  const clienteVal = clienteSelectVal ? Number(clienteSelectVal) : null;
  const originalClienteVal = facturaEnEdicion.cliente?.id ?? null;
  if (clienteVal !== originalClienteVal) {
    editPayload.clienteId = clienteVal;
  }

  // 5. Condición IVA
  const condicionIvaVal = Number((document.getElementById('edit-condicion-iva') as HTMLSelectElement).value);
  if (condicionIvaVal && condicionIvaVal !== facturaEnEdicion.condicionIva?.id) {
    editPayload.condicionIvaId = condicionIvaVal;
  }

  // 6. Tipo de Moneda
  const tipoMonedaVal = Number((document.getElementById('edit-tipo-moneda') as HTMLSelectElement).value);
  if (tipoMonedaVal && tipoMonedaVal !== facturaEnEdicion.tipoMoneda?.id) {
    editPayload.tipoMonedaId = tipoMonedaVal;
  }

  // 7. Estado
  const estadoVal = (document.getElementById('edit-estado') as HTMLSelectElement).value;
  if (estadoVal && estadoVal !== facturaEnEdicion.estado) {
    editPayload.estado = estadoVal;
  }

  // 8. Observaciones
  const observacionesVal = (document.getElementById('edit-observaciones') as HTMLInputElement).value.trim();
  const originalObservacionesVal = (facturaEnEdicion.observaciones || '').trim();
  if (observacionesVal !== originalObservacionesVal) {
    editPayload.observaciones = observacionesVal;
  }

  // 9. Detalles (solo si se activó explícitamente su modificación)
  if (modificarDetallesActivo) {
    editPayload.detalles = detallesEnEdicion.map(d => ({
      listaPrecioArticuloId: d.listaPrecioArticuloId,
      descripcion: d.descripcion,
      cantidad: d.cantidad,
      precioUnitario: d.precioUnitario,
      porcentajeBonificacion: d.porcentajeBonificacion
    }));
  }

  // Si no se modificó ningún campo
  if (Object.keys(editPayload).length === 0) {
    alert('No se detectaron modificaciones en la factura.');
    return;
  }

  btnGuardarEdicion.disabled = true;
  const originalText = btnGuardarEdicion.textContent;
  btnGuardarEdicion.textContent = 'Guardando cambios...';

  try {
    const actualizada = await actualizarFactura(facturaEnEdicion.id, editPayload);
    alert(`Factura #${actualizada.numero} actualizada con éxito.`);
    cerrarModalEditar();
    await cargarFacturas(filtrosActuales, paginaActual);
  } catch (error) {
    console.error('Error al actualizar factura:', error);
    alert(`Error al actualizar la factura: ${error instanceof Error ? error.message : String(error)}`);
  } finally {
    btnGuardarEdicion.disabled = false;
    btnGuardarEdicion.textContent = originalText;
  }
});

// Acciones dentro del modal de edición: Anulación (Negocio) y Baja Lógica (Definitiva)
btnModalAnular?.addEventListener('click', async () => {
  if (!facturaEnEdicion) return;
  const numDisplay = (facturaEnEdicion.numero != null && facturaEnEdicion.numero > 0)
    ? `#${facturaEnEdicion.numero}`
    : `(Pendiente)`;

  const confirma = confirm(
    `¿Desea anular la factura ${numDisplay}?\n\n` +
    `Regla de negocio: Su estado cambiará a 'ANULADA' y se registrará la fecha/hora de anulación comercial. ` +
    `El comprobante seguirá existiendo en el sistema para auditoría contable.`
  );
  if (!confirma) return;

  btnModalAnular.disabled = true;
  const textoOriginal = btnModalAnular.textContent;
  btnModalAnular.textContent = 'Anulando...';

  try {
    await anularFacturaPorId(facturaEnEdicion.id);
    alert(`Factura ${numDisplay} anulada con éxito.`);
    cerrarModalEditar();
    await cargarFacturas(filtrosActuales, paginaActual);
  } catch (error) {
    console.error('Error al anular factura:', error);
    alert(`Error al anular la factura: ${error instanceof Error ? error.message : String(error)}`);
  } finally {
    btnModalAnular.disabled = false;
    btnModalAnular.textContent = textoOriginal;
  }
});

btnModalBajaLogica?.addEventListener('click', async () => {
  if (!facturaEnEdicion) return;
  const numDisplay = (facturaEnEdicion.numero != null && facturaEnEdicion.numero > 0)
    ? `#${facturaEnEdicion.numero}`
    : `(Pendiente)`;

  const confirma = confirm(
    `¿Desea dar de BAJA LÓGICA definitiva a la factura ${numDisplay}?\n\n` +
    `Baja definitiva: Se registrará la fechaHoraBaja del registro y la factura quedará eliminada de las listas activas.`
  );
  if (!confirma) return;

  btnModalBajaLogica.disabled = true;
  const textoOriginal = btnModalBajaLogica.textContent;
  btnModalBajaLogica.textContent = 'Dando de baja...';

  try {
    await eliminarFacturaPorId(facturaEnEdicion.id);
    alert(`Factura ${numDisplay} dada de baja lógica con éxito.`);
    cerrarModalEditar();
    await cargarFacturas(filtrosActuales, paginaActual);
  } catch (error) {
    console.error('Error al dar de baja lógica:', error);
    alert(`Error al dar de baja lógica: ${error instanceof Error ? error.message : String(error)}`);
  } finally {
    btnModalBajaLogica.disabled = false;
    btnModalBajaLogica.textContent = textoOriginal;
  }
});

// Búsqueda y Filtros
formFiltros.addEventListener('submit', async (e) => {
  e.preventDefault();

  const fechaDesdeInput = (document.getElementById('filtro-fecha-desde') as HTMLInputElement).value;
  const fechaHastaInput = (document.getElementById('filtro-fecha-hasta') as HTMLInputElement).value;
  const estadoSelect = (document.getElementById('filtro-estado') as HTMLSelectElement).value;
  const montoMinimoInput = (document.getElementById('filtro-monto-minimo') as HTMLInputElement).value;

  const filtros: FacturaFiltrosDTO = {
    fechaDesde: fechaDesdeInput || undefined,
    fechaHasta: fechaHastaInput || undefined,
    estado: estadoSelect || undefined,
    montoMinimo: montoMinimoInput ? Number(montoMinimoInput) : undefined
  };

  paginaActual = 0;
  await cargarFacturas(filtros, 0);
});

// Resetear filtros manualmente
btnResetear.addEventListener('click', async () => {
  formFiltros.reset();
  filtrosActuales = undefined;
  paginaActual = 0;
  await cargarFacturas(undefined, 0);
});

// Controles de Paginación (Inferior y Superior)
btnPagPrimera?.addEventListener('click', async () => {
  if (paginaActual > 0) {
    await cargarFacturas(filtrosActuales, 0);
  }
});
btnPagPrimeraTop?.addEventListener('click', async () => {
  if (paginaActual > 0) {
    await cargarFacturas(filtrosActuales, 0);
  }
});

btnPagAnterior?.addEventListener('click', async () => {
  if (paginaActual > 0) {
    await cargarFacturas(filtrosActuales, paginaActual - 1);
  }
});
btnPagAnteriorTop?.addEventListener('click', async () => {
  if (paginaActual > 0) {
    await cargarFacturas(filtrosActuales, paginaActual - 1);
  }
});

btnPagSiguiente?.addEventListener('click', async () => {
  if (paginaActual < totalPaginas - 1) {
    await cargarFacturas(filtrosActuales, paginaActual + 1);
  }
});
btnPagSiguienteTop?.addEventListener('click', async () => {
  if (paginaActual < totalPaginas - 1) {
    await cargarFacturas(filtrosActuales, paginaActual + 1);
  }
});

btnPagUltima?.addEventListener('click', async () => {
  if (paginaActual < totalPaginas - 1) {
    await cargarFacturas(filtrosActuales, totalPaginas - 1);
  }
});
btnPagUltimaTop?.addEventListener('click', async () => {
  if (paginaActual < totalPaginas - 1) {
    await cargarFacturas(filtrosActuales, totalPaginas - 1);
  }
});

// Botones de Exportación (PDF y Excel) con selección de directorio
const btnExportarPdf = document.getElementById('btn-exportar-pdf') as HTMLButtonElement | null;
const btnExportarExcel = document.getElementById('btn-exportar-excel') as HTMLButtonElement | null;

async function ejecutarExportacion(tipo: 'pdf' | 'excel', btn: HTMLButtonElement | null) {
  if (!btn) return;
  const textoOriginal = btn.textContent || '';
  try {
    btn.disabled = true;
    btn.textContent = 'Descargando...';
    await descargarReporte(tipo, filtrosActuales);
  } catch (error: any) {
    console.error(`Error al exportar reporte ${tipo}:`, error);
    alert(`Error al exportar a ${tipo.toUpperCase()}: ${error.message || error}`);
  } finally {
    btn.disabled = false;
    btn.textContent = textoOriginal;
  }
}

btnExportarPdf?.addEventListener('click', () => ejecutarExportacion('pdf', btnExportarPdf));
btnExportarExcel?.addEventListener('click', () => ejecutarExportacion('excel', btnExportarExcel));

// Botón Agregar Factura -> Redirección a nueva-factura.html
document.getElementById('btn-nueva-factura')?.addEventListener('click', () => {
  window.location.href = '/nueva-factura.html';
});

// Carga inicial
cargarFacturas();