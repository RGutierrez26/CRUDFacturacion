import './style.css';
import {
  obtenerPuntosVenta,
  obtenerClientes,
  obtenerCondicionesIva,
  obtenerTiposMoneda,
  obtenerListasPrecio,
  obtenerArticulosPorListaPrecio,
  crearFactura
} from './services/obtenerfacturas';
import type {
  PuntoVentaDTO,
  ClienteDTO,
  CondicionIvaDTO,
  TipoMonedaDTO,
  ListaPrecioDTO,
  ListaPrecioArticuloDTO,
  FacturaRequestDTO,
  FacturaDetalleRequestDTO
} from './models/factura.dto';

// Interfaz para la representación visual de ítems cargados
interface ItemDetalleDisplay extends FacturaDetalleRequestDTO {
  codigoArticulo: string;
  denominacionArticulo: string;
  importeNeto: number;
  importeIva: number;
  importeSubtotal: number;
}

// ===================================================
// Elementos del DOM Principal
// ===================================================
const formNuevaFactura = document.getElementById('form-nueva-factura') as HTMLFormElement;
const inputFechaEmision = document.getElementById('fecha-emision') as HTMLInputElement;
const inputObservaciones = document.getElementById('observaciones') as HTMLInputElement;
const btnGuardarFactura = document.getElementById('btn-guardar-factura') as HTMLButtonElement;

// Botones y Tarjetas de Selección de Cabecera
const btnSelectPuntoVenta = document.getElementById('btn-select-punto-venta') as HTMLButtonElement;
const cardPuntoVentaDesc = document.getElementById('card-punto-venta-desc') as HTMLDivElement;
const cardPuntoVentaSub = document.getElementById('card-punto-venta-sub') as HTMLDivElement;
const badgePuntoVenta = document.getElementById('badge-punto-venta') as HTMLSpanElement;

const btnSelectCliente = document.getElementById('btn-select-cliente') as HTMLButtonElement;
const cardClienteDesc = document.getElementById('card-cliente-desc') as HTMLDivElement;
const cardClienteSub = document.getElementById('card-cliente-sub') as HTMLDivElement;
const badgeCliente = document.getElementById('badge-cliente') as HTMLSpanElement;

const btnSelectCondicionIva = document.getElementById('btn-select-condicion-iva') as HTMLButtonElement;
const cardCondicionIvaDesc = document.getElementById('card-condicion-iva-desc') as HTMLDivElement;
const cardCondicionIvaSub = document.getElementById('card-condicion-iva-sub') as HTMLDivElement;
const badgeCondicionIva = document.getElementById('badge-condicion-iva') as HTMLSpanElement;

const btnSelectTipoMoneda = document.getElementById('btn-select-tipo-moneda') as HTMLButtonElement;
const cardTipoMonedaDesc = document.getElementById('card-tipo-moneda-desc') as HTMLDivElement;
const cardTipoMonedaSub = document.getElementById('card-tipo-moneda-sub') as HTMLDivElement;
const badgeTipoMoneda = document.getElementById('badge-tipo-moneda') as HTMLSpanElement;

// Sección de Lista de Precios y Detalle
const btnSelectListaPrecio = document.getElementById('btn-select-lista-precio') as HTMLButtonElement;
const cardListaPrecioDesc = document.getElementById('card-lista-precio-desc') as HTMLDivElement;
const cardListaPrecioSub = document.getElementById('card-lista-precio-sub') as HTMLDivElement;
const badgeListaPrecio = document.getElementById('badge-lista-precio') as HTMLSpanElement;
const bannerListaPrecio = document.getElementById('banner-lista-precio') as HTMLDivElement;
const seccionCargaItems = document.getElementById('seccion-carga-items') as HTMLDivElement;

// Carga de Ítems
const inputArticuloPreview = document.getElementById('input-articulo-preview') as HTMLInputElement;
const btnSelectArticulo = document.getElementById('btn-select-articulo') as HTMLButtonElement;
const inputPrecio = document.getElementById('input-precio') as HTMLInputElement;
const inputCantidad = document.getElementById('input-cantidad') as HTMLInputElement;
const inputBonificacion = document.getElementById('input-bonificacion') as HTMLInputElement;
const btnAgregarItem = document.getElementById('btn-agregar-item') as HTMLButtonElement;

// Tabla y Resumen
const tbodyDetalles = document.getElementById('tabla-detalles-body') as HTMLTableSectionElement;
const resumenNeto = document.getElementById('resumen-neto') as HTMLElement;
const resumenIva = document.getElementById('resumen-iva') as HTMLElement;
const resumenTotal = document.getElementById('resumen-total') as HTMLElement;

// Elementos del Modal Selector Reutilizable
const modalSelector = document.getElementById('modal-selector') as HTMLDivElement;
const modalSelectorTitulo = document.getElementById('modal-selector-titulo') as HTMLHeadingElement;
const modalSelectorBtnCerrar = document.getElementById('modal-selector-btn-cerrar') as HTMLButtonElement;
const modalSelectorBusqueda = document.getElementById('modal-selector-busqueda') as HTMLInputElement;
const modalSelectorLista = document.getElementById('modal-selector-lista') as HTMLDivElement;
const modalSelectorPreviewContent = document.getElementById('modal-selector-preview-content') as HTMLDivElement;
const modalSelectorPreviewBadge = document.getElementById('modal-selector-preview-badge') as HTMLSpanElement;
const modalSelectorBtnOpcional = document.getElementById('modal-selector-btn-opcional') as HTMLButtonElement;
const modalSelectorBtnCancelar = document.getElementById('modal-selector-btn-cancelar') as HTMLButtonElement;
const modalSelectorBtnAceptar = document.getElementById('modal-selector-btn-aceptar') as HTMLButtonElement;

// ===================================================
// Estado de Selección en Memoria
// ===================================================
let puntoVentaSeleccionado: PuntoVentaDTO | null = null;
let clienteSeleccionado: ClienteDTO | null = null;
let esConsumidorFinal = false; // Flag explícito si se eligió Consumidor Final
let condicionIvaSeleccionada: CondicionIvaDTO | null = null;
let tipoMonedaSeleccionado: TipoMonedaDTO | null = null;
let listaPrecioSeleccionada: ListaPrecioDTO | null = null;
let articuloSeleccionadoParaItem: ListaPrecioArticuloDTO | null = null;
const detallesCargados: ItemDetalleDisplay[] = [];

// ===================================================
// Utilidades de Formateo y Matemáticas
// ===================================================
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

function obtenerArticuloCodigo(item: ListaPrecioArticuloDTO): string {
  return item.articuloDTO?.codigo || item.articuloCodigo || 'ART';
}

function obtenerArticuloDenominacion(item: ListaPrecioArticuloDTO): string {
  return item.articuloDTO?.denominacion || item.articuloDenominacion || 'Artículo';
}

// ===================================================
// Controlador Genérico del Modal Selector en 2 Paneles
// ===================================================
interface OpcionSelector<T> {
  id: string | number;
  titulo: string;
  subtitulo: string;
  tagsParaBuscar: string;
  datos: T;
  renderPreview: (item: T) => string;
}

let opcionesActuales: OpcionSelector<any>[] = [];
let opcionPreseleccionada: OpcionSelector<any> | null = null;
let onAceptarCallback: ((item: any) => void) | null = null;

function abrirModalCargando(titulo: string) {
  modalSelectorTitulo.textContent = titulo;
  modalSelectorBusqueda.value = '';
  modalSelectorBtnAceptar.disabled = true;
  modalSelectorBtnOpcional.classList.add('hidden');
  modalSelectorPreviewBadge.textContent = 'Consultando BD';
  modalSelectorLista.innerHTML = `
    <div style="padding: 3rem 1rem; text-align: center; color: var(--text-muted);">
      <p style="font-weight: 600; color: var(--primary);">Consultando base de datos...</p>
      <small>Obteniendo registros del servidor</small>
    </div>
  `;
  modalSelectorPreviewContent.innerHTML = `
    <p style="color: var(--text-muted); text-align: center; padding: 2rem 0;">
      Aguarde mientras se cargan los datos desde el backend...
    </p>
  `;
  modalSelector.classList.remove('hidden');
}

function configurarModalSelector<T>({
  titulo,
  opciones,
  idSeleccionadoActual,
  textoBotonOpcional,
  onOpcional,
  onAceptar
}: {
  titulo: string;
  opciones: OpcionSelector<T>[];
  idSeleccionadoActual?: string | number | null;
  textoBotonOpcional?: string;
  onOpcional?: () => void;
  onAceptar: (item: T) => void;
}) {
  modalSelectorTitulo.textContent = titulo;
  opcionesActuales = opciones;
  onAceptarCallback = onAceptar;
  opcionPreseleccionada = null;
  modalSelectorBtnAceptar.disabled = true;

  // Botón opcional (ej: Consumidor Final)
  if (textoBotonOpcional && onOpcional) {
    modalSelectorBtnOpcional.textContent = textoBotonOpcional;
    modalSelectorBtnOpcional.classList.remove('hidden');
    modalSelectorBtnOpcional.onclick = () => {
      onOpcional();
      cerrarModalSelector();
    };
  } else {
    modalSelectorBtnOpcional.classList.add('hidden');
    modalSelectorBtnOpcional.onclick = null;
  }

  // Preseleccionar actual si existe
  if (idSeleccionadoActual != null) {
    const encontrada = opciones.find(o => String(o.id) === String(idSeleccionadoActual));
    if (encontrada) {
      opcionPreseleccionada = encontrada;
      modalSelectorBtnAceptar.disabled = false;
    }
  }

  renderizarListaSelector(opcionesActuales);
  renderizarPreviewSelector();
  setTimeout(() => modalSelectorBusqueda.focus(), 100);
}

function cerrarModalSelector() {
  modalSelector.classList.add('hidden');
  opcionesActuales = [];
  opcionPreseleccionada = null;
  onAceptarCallback = null;
}

function renderizarListaSelector(lista: OpcionSelector<any>[]) {
  if (lista.length === 0) {
    modalSelectorLista.innerHTML = `
      <p style="padding: 1.5rem; text-align: center; color: var(--text-muted); font-size: 0.88rem;">
        No se encontraron registros coincidentes.
      </p>
    `;
    return;
  }

  modalSelectorLista.innerHTML = lista.map((opcion) => {
    const isSelected = opcionPreseleccionada && String(opcionPreseleccionada.id) === String(opcion.id);
    return `
      <div class="selector-item ${isSelected ? 'selected' : ''}" data-id="${opcion.id}">
        <div class="selector-item-title">${opcion.titulo}</div>
        <div class="selector-item-subtitle">${opcion.subtitulo}</div>
      </div>
    `;
  }).join('');

  // Vincular clics y doble clics de selección en cada ítem
  modalSelectorLista.querySelectorAll('.selector-item').forEach((elem) => {
    elem.addEventListener('click', () => {
      const id = (elem as HTMLElement).dataset.id;
      const seleccion = opcionesActuales.find(o => String(o.id) === String(id));
      if (!seleccion) return;

      opcionPreseleccionada = seleccion;
      modalSelectorBtnAceptar.disabled = false;

      // Actualizar clase visual en la lista
      modalSelectorLista.querySelectorAll('.selector-item').forEach(el => el.classList.remove('selected'));
      elem.classList.add('selected');

      renderizarPreviewSelector();
    });

    // Doble clic: selecciona y confirma inmediatamente
    elem.addEventListener('dblclick', () => {
      const id = (elem as HTMLElement).dataset.id;
      const seleccion = opcionesActuales.find(o => String(o.id) === String(id));
      if (!seleccion) return;

      opcionPreseleccionada = seleccion;
      if (onAceptarCallback) {
        onAceptarCallback(seleccion.datos);
      }
      cerrarModalSelector();
    });
  });
}

function renderizarPreviewSelector() {
  if (!opcionPreseleccionada) {
    modalSelectorPreviewBadge.textContent = 'Pendiente';
    modalSelectorPreviewContent.innerHTML = `
      <div style="padding: 3rem 1rem; text-align: center; color: var(--text-muted);">
        <p>Haga clic en un elemento de la lista para inspeccionar todos sus datos antes de aceptar (o haga doble clic para seleccionarlo de inmediato).</p>
      </div>
    `;
    return;
  }

  modalSelectorPreviewBadge.textContent = 'Seleccionado';
  modalSelectorPreviewContent.innerHTML = opcionPreseleccionada.renderPreview(opcionPreseleccionada.datos);
}

// Búsqueda en vivo dentro del selector
modalSelectorBusqueda.addEventListener('input', () => {
  const termino = modalSelectorBusqueda.value.toLowerCase().trim();
  if (!termino) {
    renderizarListaSelector(opcionesActuales);
    return;
  }

  const filtradas = opcionesActuales.filter(op => 
    op.titulo.toLowerCase().includes(termino) ||
    op.subtitulo.toLowerCase().includes(termino) ||
    op.tagsParaBuscar.toLowerCase().includes(termino)
  );

  renderizarListaSelector(filtradas);
});

// Botón Aceptar en el modal
modalSelectorBtnAceptar.addEventListener('click', () => {
  if (opcionPreseleccionada && onAceptarCallback) {
    onAceptarCallback(opcionPreseleccionada.datos);
  }
  cerrarModalSelector();
});

modalSelectorBtnCerrar.addEventListener('click', cerrarModalSelector);
modalSelectorBtnCancelar.addEventListener('click', cerrarModalSelector);
modalSelector.addEventListener('click', (e) => {
  if (e.target === modalSelector) cerrarModalSelector();
});

// ===================================================
// Flujo On-Demand (Búsqueda en BD al hacer clic)
// ===================================================

// 1. Selector de Punto de Venta (Consulta a BD on-click)
btnSelectPuntoVenta.addEventListener('click', async () => {
  abrirModalCargando('Seleccionar Punto de Venta');

  try {
    const puntos = await obtenerPuntosVenta();
    const opciones: OpcionSelector<PuntoVentaDTO>[] = puntos.map(pv => ({
      id: pv.id,
      titulo: `[${String(pv.numero).padStart(4, '0')}] ${pv.descripcion}`,
      subtitulo: `Emisión: ${pv.tipoEmision} | Domicilio: ${pv.domicilioComercial}`,
      tagsParaBuscar: `${pv.numero} ${pv.descripcion} ${pv.tipoEmision} ${pv.domicilioComercial}`,
      datos: pv,
      renderPreview: (p) => `
        <div class="preview-field">
          <span class="preview-field-label">ID Base de Datos</span>
          <span class="preview-field-val" style="color: var(--primary);">#${p.id}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Número de Punto de Venta</span>
          <span class="preview-field-val">${String(p.numero).padStart(4, '0')}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Descripción / Sucursal</span>
          <span class="preview-field-val">${p.descripcion}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Tipo de Emisión</span>
          <span class="preview-field-val">${p.tipoEmision}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Domicilio Comercial</span>
          <span class="preview-field-val">${p.domicilioComercial}</span>
        </div>
      `
    }));

    configurarModalSelector({
      titulo: 'Seleccionar Punto de Venta',
      opciones,
      idSeleccionadoActual: puntoVentaSeleccionado?.id,
      onAceptar: (pv: PuntoVentaDTO) => {
        puntoVentaSeleccionado = pv;
        cardPuntoVentaDesc.textContent = `[${String(pv.numero).padStart(4, '0')}] ${pv.descripcion}`;
        cardPuntoVentaSub.textContent = `ID #${pv.id} - ${pv.domicilioComercial}`;
        badgePuntoVenta.textContent = 'Seleccionado';
        badgePuntoVenta.className = 'entity-card-badge badge-selected';
      }
    });

  } catch (error) {
    console.error('Error al obtener puntos de venta:', error);
    alert('No se pudieron obtener los puntos de venta desde la base de datos.');
    cerrarModalSelector();
  }
});

// 2. Selector de Cliente (Consulta a BD on-click)
btnSelectCliente.addEventListener('click', async () => {
  abrirModalCargando('Seleccionar Cliente');

  try {
    const clientes = await obtenerClientes();
    const opciones: OpcionSelector<ClienteDTO>[] = clientes.map(c => ({
      id: c.id,
      titulo: c.denominacion,
      subtitulo: `CUIT/CUIL: ${c.cuitCuil} | Email: ${c.email || 'No posee'}`,
      tagsParaBuscar: `${c.denominacion} ${c.cuitCuil} ${c.email || ''} ${c.telefono || ''}`,
      datos: c,
      renderPreview: (cli) => `
        <div class="preview-field">
          <span class="preview-field-label">ID Base de Datos</span>
          <span class="preview-field-val" style="color: var(--primary);">#${cli.id}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Razón Social / Denominación</span>
          <span class="preview-field-val">${cli.denominacion}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">CUIT / CUIL</span>
          <span class="preview-field-val">${cli.cuitCuil}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Correo Electrónico</span>
          <span class="preview-field-val">${cli.email || 'No registrado'}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Teléfono / Celular</span>
          <span class="preview-field-val">${cli.telefono || '-'} / ${cli.celular || '-'}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Domicilio Fiscal</span>
          <span class="preview-field-val">${cli.domicilio || 'No especificado'}</span>
        </div>
      `
    }));

    configurarModalSelector({
      titulo: 'Seleccionar Cliente',
      opciones,
      idSeleccionadoActual: clienteSeleccionado?.id,
      textoBotonOpcional: 'Consumidor Final (Sin Cliente)',
      onOpcional: () => {
        clienteSeleccionado = null;
        esConsumidorFinal = true;
        cardClienteDesc.textContent = 'Consumidor Final';
        cardClienteSub.textContent = 'Sin CUIT/CUIL asociado (Venta a Consumidor Final)';
        badgeCliente.textContent = 'Consumidor Final';
        badgeCliente.className = 'entity-card-badge badge-selected';
      },
      onAceptar: (cli: ClienteDTO) => {
        clienteSeleccionado = cli;
        esConsumidorFinal = false;
        cardClienteDesc.textContent = cli.denominacion;
        cardClienteSub.textContent = `ID #${cli.id} - CUIT/CUIL: ${cli.cuitCuil}`;
        badgeCliente.textContent = 'Cliente Asignado';
        badgeCliente.className = 'entity-card-badge badge-selected';
      }
    });

  } catch (error) {
    console.error('Error al obtener clientes:', error);
    alert('No se pudieron obtener los clientes desde la base de datos.');
    cerrarModalSelector();
  }
});

// 3. Selector de Condición IVA (Consulta a BD on-click)
btnSelectCondicionIva.addEventListener('click', async () => {
  abrirModalCargando('Seleccionar Condición IVA');

  try {
    const condiciones = await obtenerCondicionesIva();
    const opciones: OpcionSelector<CondicionIvaDTO>[] = condiciones.map(ci => ({
      id: ci.id,
      titulo: ci.denominacion,
      subtitulo: `Código AFIP: ${ci.codigoAfip}`,
      tagsParaBuscar: `${ci.denominacion} ${ci.codigoAfip}`,
      datos: ci,
      renderPreview: (c) => `
        <div class="preview-field">
          <span class="preview-field-label">ID Base de Datos</span>
          <span class="preview-field-val" style="color: var(--primary);">#${c.id}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Condición Tributaria IVA</span>
          <span class="preview-field-val">${c.denominacion}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Código Oficial AFIP / ARCA</span>
          <span class="preview-field-val">${c.codigoAfip}</span>
        </div>
      `
    }));

    configurarModalSelector({
      titulo: 'Seleccionar Condición IVA',
      opciones,
      idSeleccionadoActual: condicionIvaSeleccionada?.id,
      onAceptar: (ci: CondicionIvaDTO) => {
        condicionIvaSeleccionada = ci;
        cardCondicionIvaDesc.textContent = ci.denominacion;
        cardCondicionIvaSub.textContent = `ID #${ci.id} - Código AFIP: ${ci.codigoAfip}`;
        badgeCondicionIva.textContent = 'Seleccionado';
        badgeCondicionIva.className = 'entity-card-badge badge-selected';
      }
    });

  } catch (error) {
    console.error('Error al obtener condiciones IVA:', error);
    alert('No se pudieron obtener las condiciones IVA desde la base de datos.');
    cerrarModalSelector();
  }
});

// 4. Selector de Tipo de Moneda (Consulta a BD on-click)
btnSelectTipoMoneda.addEventListener('click', async () => {
  abrirModalCargando('Seleccionar Tipo de Moneda');

  try {
    const monedas = await obtenerTiposMoneda();
    const opciones: OpcionSelector<TipoMonedaDTO>[] = monedas.map(tm => ({
      id: tm.id,
      titulo: `${tm.denominacion} (${tm.simbolo})`,
      subtitulo: `Código AFIP: ${tm.codigoAfip}`,
      tagsParaBuscar: `${tm.denominacion} ${tm.simbolo} ${tm.codigoAfip}`,
      datos: tm,
      renderPreview: (m) => `
        <div class="preview-field">
          <span class="preview-field-label">ID Base de Datos</span>
          <span class="preview-field-val" style="color: var(--primary);">#${m.id}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Denominación</span>
          <span class="preview-field-val">${m.denominacion}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Símbolo</span>
          <span class="preview-field-val">${m.simbolo}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Código Oficial AFIP</span>
          <span class="preview-field-val">${m.codigoAfip}</span>
        </div>
      `
    }));

    configurarModalSelector({
      titulo: 'Seleccionar Tipo de Moneda',
      opciones,
      idSeleccionadoActual: tipoMonedaSeleccionado?.id,
      onAceptar: (tm: TipoMonedaDTO) => {
        tipoMonedaSeleccionado = tm;
        cardTipoMonedaDesc.textContent = `${tm.denominacion} (${tm.simbolo})`;
        cardTipoMonedaSub.textContent = `ID #${tm.id} - Código AFIP: ${tm.codigoAfip}`;
        badgeTipoMoneda.textContent = 'Seleccionado';
        badgeTipoMoneda.className = 'entity-card-badge badge-selected';
      }
    });

  } catch (error) {
    console.error('Error al obtener tipos de moneda:', error);
    alert('No se pudieron obtener los tipos de moneda desde la base de datos.');
    cerrarModalSelector();
  }
});

// 5. Selector de Lista de Precios (Consulta a BD on-click)
btnSelectListaPrecio.addEventListener('click', async () => {
  abrirModalCargando('Seleccionar Lista de Precios');

  try {
    const listas = await obtenerListasPrecio();
    const opciones: OpcionSelector<ListaPrecioDTO>[] = listas.map(lp => ({
      id: lp.id,
      titulo: `[${lp.codigo}] ${lp.denominacion}`,
      subtitulo: `Identificador: #${lp.id}`,
      tagsParaBuscar: `${lp.codigo} ${lp.denominacion} ${lp.id}`,
      datos: lp,
      renderPreview: (l) => `
        <div class="preview-field">
          <span class="preview-field-label">ID Base de Datos</span>
          <span class="preview-field-val" style="color: var(--primary);">#${l.id}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Código de Lista</span>
          <span class="preview-field-val">${l.codigo}</span>
        </div>
        <div class="preview-field">
          <span class="preview-field-label">Denominación</span>
          <span class="preview-field-val">${l.denominacion}</span>
        </div>
      `
    }));

    configurarModalSelector({
      titulo: 'Seleccionar Lista de Precios',
      opciones,
      idSeleccionadoActual: listaPrecioSeleccionada?.id,
      onAceptar: async (lp: ListaPrecioDTO) => {
        // Si ya habían ítems y se cambia la lista de precios, advertir y limpiar
        if (detallesCargados.length > 0 && listaPrecioSeleccionada && listaPrecioSeleccionada.id !== lp.id) {
          const confirma = confirm(
            'Al cambiar la Lista de Precios se removerán los artículos agregados con la lista anterior.\n¿Desea continuar?'
          );
          if (!confirma) return;
          detallesCargados.length = 0;
          actualizarTablaYResumen();
        }

        listaPrecioSeleccionada = lp;
        cardListaPrecioDesc.textContent = `[${lp.codigo}] ${lp.denominacion}`;
        cardListaPrecioSub.textContent = `ID #${lp.id} - Lista activa para cotización de artículos`;
        badgeListaPrecio.textContent = 'Activa';
        badgeListaPrecio.className = 'entity-card-badge badge-selected';

        // Habilitar sección de agregar ítems
        bannerListaPrecio.classList.add('hidden');
        seccionCargaItems.classList.remove('hidden');

        // Limpiar selección de artículo pendiente
        articuloSeleccionadoParaItem = null;
        inputArticuloPreview.value = '';
        inputPrecio.value = '';
      }
    });

  } catch (error) {
    console.error('Error al obtener listas de precios:', error);
    alert('No se pudieron obtener las listas de precios desde la base de datos.');
    cerrarModalSelector();
  }
});

// 6. Selector de Artículo dentro de la Lista de Precios elegida (Consulta a BD on-click)
async function abrirSelectorArticulo() {
  if (!listaPrecioSeleccionada) {
    alert('Primero debe seleccionar una Lista de Precios en el botón de arriba.');
    return;
  }

  abrirModalCargando(`Artículos de ${listaPrecioSeleccionada.denominacion}`);

  try {
    const articulos = await obtenerArticulosPorListaPrecio(listaPrecioSeleccionada.id);

    if (articulos.length === 0) {
      alert(`No hay artículos cargados en la base de datos para la lista [${listaPrecioSeleccionada.codigo}] ${listaPrecioSeleccionada.denominacion}.`);
      cerrarModalSelector();
      return;
    }

    const opciones: OpcionSelector<ListaPrecioArticuloDTO>[] = articulos.map(art => {
      const cod = obtenerArticuloCodigo(art);
      const desc = obtenerArticuloDenominacion(art);
      const rubro = art.articuloDTO?.rubroDTO?.denominacion || '-';
      const marca = art.articuloDTO?.marcaDTO?.denominacion || '-';

      return {
        id: art.id,
        titulo: `[${cod}] ${desc}`,
        subtitulo: `Precio: ${formatearMoneda(art.precioVenta)} | Rubro: ${rubro} | Marca: ${marca}`,
        tagsParaBuscar: `${cod} ${desc} ${rubro} ${marca} ${art.precioVenta}`,
        datos: art,
        renderPreview: (a) => `
          <div class="preview-field">
            <span class="preview-field-label">ID ListaPrecioArticulo</span>
            <span class="preview-field-val" style="color: var(--primary);">#${a.id}</span>
          </div>
          <div class="preview-field">
            <span class="preview-field-label">Código de Artículo</span>
            <span class="preview-field-val">${obtenerArticuloCodigo(a)}</span>
          </div>
          <div class="preview-field">
            <span class="preview-field-label">Descripción / Denominación</span>
            <span class="preview-field-val">${obtenerArticuloDenominacion(a)}</span>
          </div>
          <div class="preview-field">
            <span class="preview-field-label">Rubro</span>
            <span class="preview-field-val">${a.articuloDTO?.rubroDTO?.denominacion || '-'}</span>
          </div>
          <div class="preview-field">
            <span class="preview-field-label">Marca</span>
            <span class="preview-field-val">${a.articuloDTO?.marcaDTO?.denominacion || '-'}</span>
          </div>
          <div class="preview-field">
            <span class="preview-field-label">Precio Unitario Fijado</span>
            <span class="preview-field-val" style="font-size: 1.15rem; color: var(--primary);">
              ${formatearMoneda(a.precioVenta)}
            </span>
          </div>
        `
      };
    });

    configurarModalSelector({
      titulo: `Artículos en ${listaPrecioSeleccionada.denominacion}`,
      opciones,
      idSeleccionadoActual: articuloSeleccionadoParaItem?.id,
      onAceptar: (art: ListaPrecioArticuloDTO) => {
        articuloSeleccionadoParaItem = art;
        const cod = obtenerArticuloCodigo(art);
        const desc = obtenerArticuloDenominacion(art);
        inputArticuloPreview.value = `[${cod}] ${desc} (${formatearMoneda(art.precioVenta)})`;
        inputPrecio.value = art.precioVenta.toString();
        inputCantidad.focus();
      }
    });

  } catch (error) {
    console.error('Error al obtener artículos de la lista de precios:', error);
    alert('No se pudieron obtener los artículos de la lista de precios desde la base de datos.');
    cerrarModalSelector();
  }
}

btnSelectArticulo.addEventListener('click', abrirSelectorArticulo);
inputArticuloPreview.addEventListener('click', abrirSelectorArticulo);

// ===================================================
// Agregar Renglón / Detalle a la Factura
// ===================================================
btnAgregarItem.addEventListener('click', () => {
  if (!articuloSeleccionadoParaItem) {
    alert('Por favor seleccione un artículo de la lista de precios.');
    return;
  }

  const cantidad = parseFloat(inputCantidad.value);
  if (isNaN(cantidad) || cantidad <= 0) {
    alert('La cantidad debe ser mayor a 0.');
    return;
  }

  const bonificacion = parseFloat(inputBonificacion.value) || 0;
  if (bonificacion < 0 || bonificacion > 100) {
    alert('La bonificación debe estar comprendida entre 0% y 100%.');
    return;
  }

  const precioUnitario = articuloSeleccionadoParaItem.precioVenta;
  const calculos = calcularValoresDetalle(cantidad, precioUnitario, bonificacion);
  const cod = obtenerArticuloCodigo(articuloSeleccionadoParaItem);
  const desc = obtenerArticuloDenominacion(articuloSeleccionadoParaItem);

  detallesCargados.push({
    listaPrecioArticuloId: articuloSeleccionadoParaItem.id,
    codigoArticulo: cod,
    denominacionArticulo: desc,
    descripcion: desc,
    precioUnitario: precioUnitario,
    cantidad: cantidad,
    porcentajeBonificacion: bonificacion,
    importeNeto: calculos.importeNeto,
    importeIva: calculos.importeIva,
    importeSubtotal: calculos.importeSubtotal
  });

  // Limpiar campos de carga de ítem
  articuloSeleccionadoParaItem = null;
  inputArticuloPreview.value = '';
  inputPrecio.value = '';
  inputCantidad.value = '1';
  inputBonificacion.value = '0';

  actualizarTablaYResumen();
});

// Renderizar la tabla de renglones y totales
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
  let totalGeneral = 0;

  tbodyDetalles.innerHTML = detallesCargados.map((item, index) => {
    totalNeto += item.importeNeto;
    totalIva += item.importeIva;
    totalGeneral += item.importeSubtotal;

    return `
      <tr>
        <td><strong>${item.codigoArticulo}</strong></td>
        <td>${item.denominacionArticulo}</td>
        <td class="text-right">${formatearMoneda(item.precioUnitario)}</td>
        <td class="text-center">${item.cantidad}</td>
        <td class="text-right">${item.porcentajeBonificacion}%</td>
        <td class="text-right">${formatearMoneda(item.importeNeto)}</td>
        <td class="text-right">${formatearMoneda(item.importeIva)}</td>
        <td class="text-right" style="font-weight: 700;">${formatearMoneda(item.importeSubtotal)}</td>
        <td class="text-center">
          <button type="button" class="btn-action btn-delete btn-quitar-item" data-index="${index}" title="Quitar ítem">
            Quitar
          </button>
        </td>
      </tr>
    `;
  }).join('');

  resumenNeto.textContent = formatearMoneda(totalNeto);
  resumenIva.textContent = formatearMoneda(totalIva);
  resumenTotal.textContent = formatearMoneda(totalGeneral);

  // Vincular eventos de quitar
  tbodyDetalles.querySelectorAll('.btn-quitar-item').forEach((btn) => {
    btn.addEventListener('click', () => {
      const idx = Number((btn as HTMLElement).dataset.index);
      detallesCargados.splice(idx, 1);
      actualizarTablaYResumen();
    });
  });
}

// ===================================================
// Emisión y Envío del Formulario
// ===================================================
formNuevaFactura.addEventListener('submit', async (e) => {
  e.preventDefault();

  if (!puntoVentaSeleccionado || !puntoVentaSeleccionado.id) {
    alert('Debe seleccionar un Punto de Venta haciendo clic en su botón.');
    return;
  }

  if (!clienteSeleccionado && !esConsumidorFinal) {
    alert('Debe seleccionar un Cliente o indicar "Consumidor Final" en el botón de Cliente.');
    return;
  }

  if (!condicionIvaSeleccionada || !condicionIvaSeleccionada.id) {
    alert('Debe seleccionar una Condición IVA haciendo clic en su botón.');
    return;
  }

  if (!tipoMonedaSeleccionado || !tipoMonedaSeleccionado.id) {
    alert('Debe seleccionar un Tipo de Moneda haciendo clic en su botón.');
    return;
  }

  if (!listaPrecioSeleccionada || !listaPrecioSeleccionada.id) {
    alert('Debe seleccionar una Lista de Precios para asociar los renglones.');
    return;
  }

  if (detallesCargados.length === 0) {
    alert('Debe cargar al menos un producto en el detalle de la factura.');
    return;
  }

  // Armar payload garantizando los IDs de los objetos subyacentes
  const payload: FacturaRequestDTO = {
    puntoVentaId: Number(puntoVentaSeleccionado.id),
    clienteId: clienteSeleccionado ? Number(clienteSeleccionado.id) : null,
    condicionIvaId: Number(condicionIvaSeleccionada.id),
    tipoMonedaId: Number(tipoMonedaSeleccionado.id),
    fechaEmision: new Date(`${inputFechaEmision.value}T12:00:00`).toISOString(),
    observaciones: inputObservaciones.value.trim() || undefined,
    detalles: detallesCargados.map(d => ({
      listaPrecioArticuloId: Number(d.listaPrecioArticuloId),
      descripcion: d.descripcion,
      cantidad: Number(d.cantidad),
      precioUnitario: Number(d.precioUnitario),
      porcentajeBonificacion: Number(d.porcentajeBonificacion)
    }))
  };

  console.log('Enviando FacturaRequestDTO al Backend:', JSON.stringify(payload, null, 2));

  btnGuardarFactura.disabled = true;
  const textoOriginal = btnGuardarFactura.textContent;
  btnGuardarFactura.textContent = 'Guardando comprobante en servidor...';

  try {
    const creada = await crearFactura(payload);
    alert(`Factura registrada con éxito en estado PENDIENTE. (ID #${creada.id}${creada.numero ? ` - Número: ${creada.numero}` : ''})`);
    window.location.href = '/index.html';
  } catch (error) {
    console.error('Error al emitir factura:', error);
    alert(`Error al guardar la factura: ${error instanceof Error ? error.message : String(error)}`);
  } finally {
    btnGuardarFactura.disabled = false;
    btnGuardarFactura.textContent = textoOriginal;
  }
});

// ===================================================
// Inicialización Básica de UI (Sin peticiones a la BD)
// ===================================================
function inicializarFormulario() {
  const hoy = new Date().toISOString().split('T')[0];
  inputFechaEmision.value = hoy;

  // Tarjetas inician en estado limpio, sin consultas previas a la BD
  cardPuntoVentaDesc.textContent = 'Sin seleccionar';
  cardPuntoVentaSub.textContent = 'Haga clic para consultar y elegir Punto de Venta';
  badgePuntoVenta.textContent = 'Pendiente';
  badgePuntoVenta.className = 'entity-card-badge badge-empty';

  cardClienteDesc.textContent = 'Sin seleccionar';
  cardClienteSub.textContent = 'Haga clic para elegir Cliente o Consumidor Final';
  badgeCliente.textContent = 'Pendiente';
  badgeCliente.className = 'entity-card-badge badge-empty';

  cardCondicionIvaDesc.textContent = 'Sin seleccionar';
  cardCondicionIvaSub.textContent = 'Haga clic para consultar y elegir Condición IVA';
  badgeCondicionIva.textContent = 'Pendiente';
  badgeCondicionIva.className = 'entity-card-badge badge-empty';

  cardTipoMonedaDesc.textContent = 'Sin seleccionar';
  cardTipoMonedaSub.textContent = 'Haga clic para consultar y elegir Tipo de Moneda';
  badgeTipoMoneda.textContent = 'Pendiente';
  badgeTipoMoneda.className = 'entity-card-badge badge-empty';

  cardListaPrecioDesc.textContent = 'Ninguna lista seleccionada';
  cardListaPrecioSub.textContent = 'Haga clic para consultar y elegir Lista de Precios';
  badgeListaPrecio.textContent = 'Requerida';
  badgeListaPrecio.className = 'entity-card-badge badge-empty';
}

inicializarFormulario();
