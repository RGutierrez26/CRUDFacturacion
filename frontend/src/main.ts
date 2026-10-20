import './style.css';
import {
  obtenerFacturas,
  obtenerFacturaPorNumero,
  eliminarFacturaPorNumero,
  actualizarFactura,
  obtenerPuntosVenta,
  obtenerClientes,
  obtenerCondicionesIva,
  obtenerTiposMoneda,
  obtenerListasPrecioArticulos
} from './services/obtenerfacturas';
import type {
  FacturaReporteDTO,
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
  contadorFacturas.textContent = `${facturas.length} comprobante(s)`;

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
    const estadoStyle = f.estado === 'ANULADA' 
      ? 'background: var(--danger-light); color: var(--danger);' 
      : 'background: var(--info-light); color: var(--info);';

    return `
      <tr>
        <td class="factura-num">#${f.numeroFactura}</td>
        <td>${formatearFecha(f.fechaEmision)}</td>
        <td><strong>${f.ClienteDenominacion || 'Consumidor Final'}</strong></td>
        <td>${f.condicionIva}</td>
        <td>${f.puntoVentaDescripcion}</td>
        <td class="text-right factura-total">${formatearMoneda(f.importeTotal)}</td>
        <td class="text-center">${f.CantidadItemns}</td>
        <td class="text-center">
          <span style="display: inline-block; padding: 0.15rem 0.55rem; border-radius: 4px; font-size: 0.75rem; font-weight: 700; ${estadoStyle}">
            ${f.estado || 'EMITIDA'}
          </span>
        </td>
        <td class="text-center">
          <div class="action-buttons">
            <button class="btn-action btn-review" data-numero="${f.numeroFactura}" title="Revisar detalle">
              Revisar
            </button>
            <button class="btn-action btn-delete" data-numero="${f.numeroFactura}" title="Eliminar/Anular factura">
              Eliminar
            </button>
            <button class="btn-action btn-modify" data-numero="${f.numeroFactura}" title="Modificar factura">
              Modificar
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
  // Botones Revisar
  tbody.querySelectorAll('.btn-review').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const numero = Number((btn as HTMLElement).dataset.numero);
      await abrirModalDetalle(numero);
    });
  });

  // Botones Eliminar
  tbody.querySelectorAll('.btn-delete').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const numero = Number((btn as HTMLElement).dataset.numero);
      await confirmarEliminarFactura(numero);
    });
  });

  // Botones Modificar
  tbody.querySelectorAll('.btn-modify').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const numero = Number((btn as HTMLElement).dataset.numero);
      await abrirModalEditar(numero);
    });
  });
}

// Cargar facturas principales
async function cargarFacturas(filtros?: FacturaFiltrosDTO) {
  try {
    tbody.innerHTML = `
      <tr>
        <td colspan="9" class="text-center py-4" style="color: var(--text-muted);">
          Cargando comprobantes...
        </td>
      </tr>
    `;
    const facturas = await obtenerFacturas(filtros);
    renderizarTabla(facturas);
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
async function abrirModalDetalle(numero: number) {
  modalTitulo.textContent = `Detalle de Factura #${numero}`;
  modalBody.innerHTML = `<p class="text-center py-4">Cargando información del comprobante...</p>`;
  modalDetalle.classList.remove('hidden');

  try {
    const factura: FacturaVentaDTO = await obtenerFacturaPorNumero(numero);

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
          <p class="modal-item-value" style="color: ${factura.estado === 'ANULADA' ? 'var(--danger)' : 'var(--primary)'};">
            ${factura.estado}
          </p>
        </div>
        <div>
          <span class="modal-item-title">Total Comprobante</span>
          <p class="modal-item-value" style="font-size: 1.1rem; color: #0f172a;">
            ${formatearMoneda(factura.importeTotal)}
          </p>
        </div>
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
    modalBody.innerHTML = `<p style="color: var(--danger);">No se pudo cargar el detalle de la factura #${numero}.</p>`;
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
async function abrirModalEditar(numero: number) {
  modalEditarTitulo.textContent = `Modificar Factura #${numero}`;
  modalEditarBody.innerHTML = `<p class="text-center py-4">Cargando datos para edición...</p>`;
  modalEditar.classList.remove('hidden');

  try {
    const [factura, puntosVenta, clientes, condicionesIva, tiposMoneda, articulos] = await Promise.all([
      obtenerFacturaPorNumero(numero),
      obtenerPuntosVenta(),
      obtenerClientes(),
      obtenerCondicionesIva(),
      obtenerTiposMoneda(),
      obtenerListasPrecioArticulos()
    ]);

    facturaEnEdicion = factura;
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
          <label for="edit-numero">Número de Factura *</label>
          <input type="number" id="edit-numero" value="${factura.numero}" min="1" required />
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
    modalEditarBody.innerHTML = `<p style="color: var(--danger);">No se pudo cargar la información para editar la factura #${numero}.</p>`;
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
              <option value="${a.id}">[${a.articuloCodigo}] ${a.articuloDenominacion} - ${formatearMoneda(a.precioVenta)}</option>
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
      codigo: art.articuloCodigo,
      descripcion: art.articuloDenominacion,
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

  // 2. Número de Factura
  const numeroVal = Number((document.getElementById('edit-numero') as HTMLInputElement).value);
  if (numeroVal && numeroVal !== facturaEnEdicion.numero) {
    editPayload.numero = numeroVal;
  }

  // 3. Fecha de Emisión
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
    await cargarFacturas();
  } catch (error) {
    console.error('Error al actualizar factura:', error);
    alert(`Error al actualizar la factura: ${error instanceof Error ? error.message : String(error)}`);
  } finally {
    btnGuardarEdicion.disabled = false;
    btnGuardarEdicion.textContent = originalText;
  }
});

// Confirmar y ejecutar eliminación lógica
async function confirmarEliminarFactura(numero: number) {
  const confirma = confirm(`¿Estás seguro de que deseas anular/eliminar la factura #${numero}?`);
  if (!confirma) return;

  try {
    await eliminarFacturaPorNumero(numero);
    alert(`La factura #${numero} fue dada de baja con éxito.`);
    await cargarFacturas();
  } catch (error) {
    console.error('Error al eliminar factura:', error);
    alert(`No se pudo eliminar la factura #${numero}.`);
  }
}

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

  await cargarFacturas(filtros);
});

// Resetear filtros manualmente
btnResetear.addEventListener('click', async () => {
  formFiltros.reset();
  await cargarFacturas();
});

// Botón Agregar Factura -> Redirección a nueva-factura.html
document.getElementById('btn-nueva-factura')?.addEventListener('click', () => {
  window.location.href = '/nueva-factura.html';
});

// Carga inicial
cargarFacturas();