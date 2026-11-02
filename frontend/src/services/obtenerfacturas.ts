import type { 
  FacturaReportePageDTO,
  FacturaFiltrosDTO, 
  FacturaVentaDTO, 
  FacturaRequestDTO,
  FacturaEditDTO,
  ClienteDTO,
  PuntoVentaDTO,
  CondicionIvaDTO,
  TipoMonedaDTO,
  ListaPrecioDTO,
  ListaPrecioArticuloDTO
} from '../models/factura.dto';
import { getResources } from '../Util/getResources';

const BASE_URL = 'http://localhost:8080/api';

// Facturas (Paginadas desde el Backend)
export async function obtenerFacturas(
  filtros?: FacturaFiltrosDTO,
  page: number = 0,
  size: number = 10
): Promise<FacturaReportePageDTO> {
  const params = new URLSearchParams();

  if (filtros) {
    if (filtros.fechaDesde) params.append('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params.append('fechaHasta', filtros.fechaHasta);
    if (filtros.estado) params.append('estado', filtros.estado);
    if (filtros.montoMinimo != null && !isNaN(filtros.montoMinimo)) {
      params.append('montoMinimo', filtros.montoMinimo.toString());
    }
  }

  params.append('page', page.toString());
  params.append('size', size.toString());

  const query = params.toString() ? `?${params.toString()}` : '';
  return getResources<FacturaReportePageDTO>(`${BASE_URL}/facturareportes${query}`);
}

export async function obtenerFacturaPorNumero(numero: number): Promise<FacturaVentaDTO> {
  return getResources<FacturaVentaDTO>(`${BASE_URL}/facturas/numero/${numero}`);
}

export async function obtenerFacturaPorId(id: number): Promise<FacturaVentaDTO> {
  return getResources<FacturaVentaDTO>(`${BASE_URL}/facturas/${id}`);
}

export async function eliminarFacturaPorId(id: number): Promise<void> {
  const response = await fetch(`${BASE_URL}/facturas/${id}`, {
    method: 'DELETE'
  });
  if (!response.ok && response.status !== 204) {
    throw new Error(`Error al eliminar factura: ${response.statusText}`);
  }
}

export async function anularFacturaPorId(id: number): Promise<FacturaVentaDTO> {
  const response = await fetch(`${BASE_URL}/facturas/${id}/anular`, {
    method: 'POST'
  });
  if (!response.ok) {
    const errorJson = await response.json().catch(() => null);
    const mensaje = errorJson?.message || `Error al anular la factura (${response.status})`;
    throw new Error(mensaje);
  }
  return response.json();
}

export async function eliminarFacturaPorNumero(numero: number): Promise<void> {
  const response = await fetch(`${BASE_URL}/facturas/numero/${numero}`, {
    method: 'DELETE'
  });
  if (!response.ok && response.status !== 204) {
    throw new Error(`Error al eliminar factura: ${response.statusText}`);
  }
}

export async function anularFacturaPorNumero(numero: number): Promise<FacturaVentaDTO> {
  const response = await fetch(`${BASE_URL}/facturas/numero/${numero}/anular`, {
    method: 'POST'
  });
  if (!response.ok) {
    const errorJson = await response.json().catch(() => null);
    const mensaje = errorJson?.message || `Error al anular la factura (${response.status})`;
    throw new Error(mensaje);
  }
  return response.json();
}

export async function facturarFacturaPorNumero(numero: number): Promise<FacturaVentaDTO> {
  const response = await fetch(`${BASE_URL}/facturas/numero/${numero}/facturar`, {
    method: 'POST'
  });
  if (!response.ok) {
    const errorJson = await response.json().catch(() => null);
    const mensaje = errorJson?.message || `Error al facturar en AFIP/ARCA (${response.status})`;
    throw new Error(mensaje);
  }
  return response.json();
}

export async function facturarFacturaPorId(id: number): Promise<FacturaVentaDTO> {
  const response = await fetch(`${BASE_URL}/facturas/${id}/facturar`, {
    method: 'POST'
  });
  if (!response.ok) {
    const errorJson = await response.json().catch(() => null);
    const mensaje = errorJson?.message || `Error al facturar en AFIP/ARCA (${response.status})`;
    throw new Error(mensaje);
  }
  return response.json();
}

export async function crearFactura(factura: FacturaRequestDTO): Promise<FacturaVentaDTO> {
  const response = await fetch(`${BASE_URL}/facturas`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(factura)
  });

  if (!response.ok) {
    const errorJson = await response.json().catch(() => null);
    const mensaje = errorJson?.message || `Error del servidor (${response.status})`;
    throw new Error(mensaje);
  }

  return response.json();
}

export async function actualizarFactura(id: number, factura: FacturaEditDTO): Promise<FacturaVentaDTO> {
  const response = await fetch(`${BASE_URL}/facturas/${id}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(factura)
  });

  if (!response.ok) {
    const errorJson = await response.json().catch(() => null);
    const mensaje = errorJson?.message || `Error al actualizar la factura (${response.status})`;
    throw new Error(mensaje);
  }

  return response.json();
}

// Entidades auxiliares para selects
export async function obtenerClientes(): Promise<ClienteDTO[]> {
  return getResources<ClienteDTO[]>(`${BASE_URL}/clientes`);
}

export async function obtenerPuntosVenta(): Promise<PuntoVentaDTO[]> {
  return getResources<PuntoVentaDTO[]>(`${BASE_URL}/puntos-venta`);
}

export async function obtenerCondicionesIva(): Promise<CondicionIvaDTO[]> {
  return getResources<CondicionIvaDTO[]>(`${BASE_URL}/condiciones-iva`);
}

export async function obtenerTiposMoneda(): Promise<TipoMonedaDTO[]> {
  return getResources<TipoMonedaDTO[]>(`${BASE_URL}/tipos-moneda`);
}

export async function obtenerListasPrecioArticulos(): Promise<ListaPrecioArticuloDTO[]> {
  return getResources<ListaPrecioArticuloDTO[]>(`${BASE_URL}/listas-precio-articulos`);
}

export async function obtenerListasPrecio(): Promise<ListaPrecioDTO[]> {
  return getResources<ListaPrecioDTO[]>(`${BASE_URL}/listaprecios`);
}

export async function obtenerArticulosPorListaPrecio(listaPrecioId: number): Promise<ListaPrecioArticuloDTO[]> {
  return getResources<ListaPrecioArticuloDTO[]>(`${BASE_URL}/listas-precio-articulos/listas-precio/${listaPrecioId}`);
}

// Exportación y descarga de reportes (con selección de directorio mediante File System Access API)
export async function descargarReporte(
  tipo: 'pdf' | 'excel',
  filtros?: FacturaFiltrosDTO
): Promise<void> {
  const params = new URLSearchParams();

  if (filtros) {
    if (filtros.fechaDesde) params.append('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params.append('fechaHasta', filtros.fechaHasta);
    if (filtros.estado) params.append('estado', filtros.estado);
    if (filtros.montoMinimo != null && !isNaN(filtros.montoMinimo)) {
      params.append('montoMinimo', filtros.montoMinimo.toString());
    }
  }

  const query = params.toString() ? `?${params.toString()}` : '';
  const url = `${BASE_URL}/facturareportes/exportar/${tipo}${query}`;

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Error al generar el reporte ${tipo.toUpperCase()} (${response.status})`);
  }

  const blob = await response.blob();
  const nombreSugerido = tipo === 'pdf' ? 'reporte_facturas.pdf' : 'reporte_facturas.xls';

  // Si el navegador soporta File System Access API, abre la ventana nativa "Guardar como" para elegir ubicación
  if ('showSaveFilePicker' in window) {
    try {
      const handle = await (window as any).showSaveFilePicker({
        suggestedName: nombreSugerido,
        types: tipo === 'pdf' ? [
          {
            description: 'Documento PDF (*.pdf)',
            accept: { 'application/pdf': ['.pdf'] }
          }
        ] : [
          {
            description: 'Archivo Excel (*.xls)',
            accept: { 'application/vnd.ms-excel': ['.xls'] }
          }
        ]
      });

      const writable = await handle.createWritable();
      await writable.write(blob);
      await writable.close();
      return;
    } catch (err: any) {
      // Si el usuario canceló el selector de archivos, se ignora
      if (err.name === 'AbortError') {
        return;
      }
      console.warn('showSaveFilePicker no disponible o cancelado, usando descarga estándar:', err);
    }
  }

  // Fallback estándar en navegadores sin soporte de File System Access API
  const downloadUrl = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = downloadUrl;
  a.download = nombreSugerido;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(downloadUrl);
}
