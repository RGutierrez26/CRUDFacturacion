import './style.css';
import {
  obtenerPuntosVenta,
  obtenerClientes,
  obtenerCondicionesIva,
  obtenerTiposMoneda,
  obtenerListasPrecioArticulos,
  crearFactura
} from './services/obtenerfacturas';
import type {
  PuntoVentaDTO,
  ClienteDTO,
  CondicionIvaDTO,
  TipoMonedaDTO,
  ListaPrecioArticuloDTO,
  FacturaRequestDTO,
  FacturaDetalleRequestDTO
} from './models/factura.dto';

// Interfaz extendida para el manejo en pantalla de los ítems agregados
interface ItemDetalleDisplay extends FacturaDetalleRequestDTO {
  codigoArticulo: string;
  denominacionArticulo: string;
  importeNeto: number;
  importeIva: number;
  importeSubtotal: number;
}

// Elementos de la Cabecera
const formNuevaFactura = document.getElementById('form-nueva-factura') as HTMLFormElement;
const selectPuntoVenta = document.getElementById('select-punto-venta') as HTMLSelectElement;
const inputNumeroFactura = document.getElementById('numero-factura') as HTMLInputElement;
const inputFechaEmision = document.getElementById('fecha-emision') as HTMLInputElement;
const selectCliente = document.getElementById('select-cliente') as HTMLSelectElement;
const selectCondicionIva = document.getElementById('select-condicion-iva') as HTMLSelectElement;
const selectTipoMoneda = document.getElementById('select-tipo-moneda') as HTMLSelectElement;
const inputObservaciones = document.getElementById('observaciones') as HTMLInputElement;
const btnGuardar = document.getElementById('btn-guardar-factura') as HTMLButtonElement;

// Elementos de Carga de Ítems
const selectProducto = document.getElementById('select-producto') as HTMLSelectElement;
const inputPrecio = document.getElementById('input-precio') as HTMLInputElement;
const inputCantidad = document.getElementById('input-cantidad') as HTMLInputElement;
const inputBonificacion = document.getElementById('input-bonificacion') as HTMLInputElement;
const btnAgregarItem = document.getElementById('btn-agregar-item') as HTMLButtonElement;

// Elementos de Tabla y Resumen
const tbodyDetalles = document.getElementById('tabla-detalles-body') as HTMLTableSectionElement;
const resumenNeto = document.getElementById('resumen-neto') as HTMLElement;
const resumenIva = document.getElementById('resumen-iva') as HTMLElement;
const resumenTotal = document.getElementById('resumen-total') as HTMLElement;

// Estado en memoria
let listasPrecioArticulos: ListaPrecioArticuloDTO[] = [];
const detallesCargados: ItemDetalleDisplay[] = [];

// Formateador de moneda
function formatearMoneda(monto: number): string {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(monto);
}

// Cálculo matemático según la lógica del backend
function calcularValoresDetalle(cantidad: number, precioUnitario: number, bonificacion: number) {
  const importeBruto = cantidad * precioUnitario;
  const descuento = importeBruto * (bonificacion / 100.0);
  const importeNeto = importeBruto - descuento;
  const importeIva = importeNeto * 0.21;
  const importeSubtotal = importeNeto + importeIva;
  return { importeNeto, importeIva, importeSubtotal };
}

// Carga de todos los datos maestros para combos y selects
async function inicializarFormulario() {
  // Establecer fecha de hoy por defecto
  const hoy = new Date().toISOString().split('T')[0];
  inputFechaEmision.value = hoy;

  try {
    const [puntosVenta, clientes, condicionesIva, tiposMoneda, articulos] = await Promise.all([
      obtenerPuntosVenta(),
      obtenerClientes(),
      obtenerCondicionesIva(),
      obtenerTiposMoneda(),
      obtenerListasPrecioArticulos()
    ]);

    listasPrecioArticulos = articulos;

    // 1. Puntos de Venta
    selectPuntoVenta.innerHTML = `
      <option value="">Seleccione un punto de venta...</option>
      ${puntosVenta.map((pv: PuntoVentaDTO) => `
        <option value="${pv.id}">${String(pv.numero).padStart(4, '0')} - ${pv.descripcion}</option>
      `).join('')}
    `;
    if (puntosVenta.length === 1) {
      selectPuntoVenta.value = puntosVenta[0].id.toString();
    }

    // 2. Clientes
    selectCliente.innerHTML = `
      <option value="">Consumidor Final</option>
      ${clientes.map((c: ClienteDTO) => `
        <option value="${c.id}">${c.denominacion} (${c.cuitCuil})</option>
      `).join('')}
    `;

    // 3. Condiciones IVA
    selectCondicionIva.innerHTML = `
      <option value="">Seleccione condición IVA...</option>
      ${condicionesIva.map((ci: CondicionIvaDTO) => `
        <option value="${ci.id}">${ci.denominacion}</option>
      `).join('')}
    `;
    if (condicionesIva.length > 0) {
      selectCondicionIva.value = condicionesIva[0].id.toString();
    }

    // 4. Tipos de Moneda
    selectTipoMoneda.innerHTML = `
      <option value="">Seleccione moneda...</option>
      ${tiposMoneda.map((tm: TipoMonedaDTO) => `
        <option value="${tm.id}">${tm.denominacion} (${tm.simbolo})</option>
      `).join('')}
    `;
    const monedaDefault = tiposMoneda.find(m => m.simbolo === '$' || m.codigoAfip === 'PES') || tiposMoneda[0];
    if (monedaDefault) {
      selectTipoMoneda.value = monedaDefault.id.toString();
    }

    // 5. Lista de Precios de Artículos
    selectProducto.innerHTML = `
      <option value="">Seleccione un artículo...</option>
      ${articulos.map((a: ListaPrecioArticuloDTO) => `
        <option value="${a.id}">[${a.articuloCodigo}] ${a.articuloDenominacion} - ${formatearMoneda(a.precioVenta)}</option>
      `).join('')}
    `;

  } catch (error) {
    console.error('Error al inicializar datos del formulario:', error);
    alert('Error al cargar datos maestros desde el servidor. Verifique que el backend esté activo.');
  }
}

// Al seleccionar un artículo, autocompletar su precio unitario
selectProducto.addEventListener('change', () => {
  const selectedId = Number(selectProducto.value);
  const articulo = listasPrecioArticulos.find(a => a.id === selectedId);
  if (articulo) {
    inputPrecio.value = articulo.precioVenta.toString();
  } else {
    inputPrecio.value = '';
  }
});

// Renderizar la tabla de ítems y actualizar el resumen totalizador
function actualizarTablaYResumen() {
  if (detallesCargados.length === 0) {
    tbodyDetalles.innerHTML = `
      <tr>
        <td colspan="9" class="text-center py-4" style="color: var(--text-muted);">
          Aún no agregaste productos a la factura.
        </td>
      </tr>
    `;
    resumenNeto.textContent = formatearMoneda(0);
    resumenIva.textContent = formatearMoneda(0);
    resumenTotal.textContent = formatearMoneda(0);
    return;
  }

  let totalNeto = 0;
  let totalIva = 0;
  let totalFactura = 0;

  tbodyDetalles.innerHTML = detallesCargados.map((item, index) => {
    totalNeto += item.importeNeto;
    totalIva += item.importeIva;
    totalFactura += item.importeSubtotal;

    return `
      <tr>
        <td><code>${item.codigoArticulo}</code></td>
        <td><strong>${item.denominacionArticulo}</strong></td>
        <td class="text-right">${formatearMoneda(item.precioUnitario)}</td>
        <td class="text-center">${item.cantidad}</td>
        <td class="text-right">${item.porcentajeBonificacion}%</td>
        <td class="text-right">${formatearMoneda(item.importeNeto)}</td>
        <td class="text-right">${formatearMoneda(item.importeIva)}</td>
        <td class="text-right"><strong>${formatearMoneda(item.importeSubtotal)}</strong></td>
        <td class="text-center">
          <button type="button" class="btn-action btn-delete btn-quitar-item" data-index="${index}" title="Quitar ítem">
            Quitar
          </button>
        </td>
      </tr>
    `;
  }).join('');

  // Actualizar totales en el resumen
  resumenNeto.textContent = formatearMoneda(totalNeto);
  resumenIva.textContent = formatearMoneda(totalIva);
  resumenTotal.textContent = formatearMoneda(totalFactura);

  // Vincular eventos de quitar
  tbodyDetalles.querySelectorAll('.btn-quitar-item').forEach((btn) => {
    btn.addEventListener('click', () => {
      const idx = Number((btn as HTMLElement).dataset.index);
      detallesCargados.splice(idx, 1);
      actualizarTablaYResumen();
    });
  });
}

// Botón Agregar Renglón
btnAgregarItem.addEventListener('click', () => {
  const selectedId = Number(selectProducto.value);
  if (!selectedId) {
    alert('Seleccione un artículo o producto de la lista.');
    selectProducto.focus();
    return;
  }

  const articulo = listasPrecioArticulos.find(a => a.id === selectedId);
  if (!articulo) {
    alert('Artículo seleccionado no encontrado.');
    return;
  }

  const cantidad = parseFloat(inputCantidad.value);
  if (isNaN(cantidad) || cantidad <= 0) {
    alert('La cantidad debe ser mayor a 0.');
    inputCantidad.focus();
    return;
  }

  // El precio unitario proviene directamente de la Lista de Precios del artículo
  const precioUnitario = articulo.precioVenta;

  const bonificacion = parseFloat(inputBonificacion.value) || 0;
  if (bonificacion < 0 || bonificacion > 100) {
    alert('El porcentaje de bonificación debe estar comprendido entre 0 y 100.');
    inputBonificacion.focus();
    return;
  }

  const { importeNeto, importeIva, importeSubtotal } = calcularValoresDetalle(
    cantidad,
    precioUnitario,
    bonificacion
  );

  detallesCargados.push({
    listaPrecioArticuloId: articulo.id,
    codigoArticulo: articulo.articuloCodigo,
    denominacionArticulo: articulo.articuloDenominacion,
    descripcion: articulo.articuloDenominacion,
    cantidad,
    precioUnitario,
    porcentajeBonificacion: bonificacion,
    importeNeto,
    importeIva,
    importeSubtotal
  });

  // Limpiar campos del ítem
  selectProducto.value = '';
  inputPrecio.value = '';
  inputCantidad.value = '1';
  inputBonificacion.value = '0';

  actualizarTablaYResumen();
});

// Envío del Formulario Completo (POST /api/facturas)
formNuevaFactura.addEventListener('submit', async (e) => {
  e.preventDefault();

  if (detallesCargados.length === 0) {
    alert('Debe agregar al menos un ítem o producto al detalle de la factura antes de emitirla.');
    return;
  }

  const puntoVentaId = Number(selectPuntoVenta.value);
  const numero = Number(inputNumeroFactura.value);
  const fechaEmisionVal = inputFechaEmision.value;
  const condicionIvaId = Number(selectCondicionIva.value);
  const tipoMonedaId = Number(selectTipoMoneda.value);
  const clienteId = selectCliente.value ? Number(selectCliente.value) : null;
  const observaciones = inputObservaciones.value.trim() || undefined;

  if (!puntoVentaId) {
    alert('Seleccione un punto de venta.');
    selectPuntoVenta.focus();
    return;
  }

  if (!numero || isNaN(numero) || numero <= 0) {
    alert('Ingrese un número de factura válido.');
    inputNumeroFactura.focus();
    return;
  }

  if (!condicionIvaId) {
    alert('Seleccione una condición de IVA.');
    selectCondicionIva.focus();
    return;
  }

  if (!tipoMonedaId) {
    alert('Seleccione un tipo de moneda.');
    selectTipoMoneda.focus();
    return;
  }

  // Formatear fecha para el backend (ISO Date)
  const fechaEmision = fechaEmisionVal ? new Date(`${fechaEmisionVal}T12:00:00`).toISOString() : undefined;

  // Preparar payload DTO
  const payload: FacturaRequestDTO = {
    puntoVentaId,
    clienteId,
    condicionIvaId,
    tipoMonedaId,
    numero,
    fechaEmision,
    estado: 'EMITIDA',
    observaciones,
    usuarioCargaId: null,
    detalles: detallesCargados.map(d => ({
      listaPrecioArticuloId: d.listaPrecioArticuloId,
      descripcion: d.descripcion,
      cantidad: d.cantidad,
      precioUnitario: d.precioUnitario,
      porcentajeBonificacion: d.porcentajeBonificacion
    }))
  };

  btnGuardar.disabled = true;
  const originalBtnText = btnGuardar.textContent;
  btnGuardar.textContent = 'Emitiendo factura...';

  try {
    const facturaCreada = await crearFactura(payload);
    alert(`Factura #${facturaCreada.numero} emitida y registrada con éxito.`);
    window.location.href = '/index.html';
  } catch (error) {
    console.error('Error al emitir factura:', error);
    alert(`Error al registrar factura: ${error instanceof Error ? error.message : String(error)}`);
  } finally {
    btnGuardar.disabled = false;
    btnGuardar.textContent = originalBtnText;
  }
});

// Inicialización
inicializarFormulario();
