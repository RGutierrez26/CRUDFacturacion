// ==========================================
// DTOs auxiliares para combos/selects del Front
// ==========================================

export interface ClienteDTO {
  id: number;
  cuitCuil: string;
  denominacion: string;
  email?: string | null;
  telefono?: string | null;
  celular?: string | null;
  domicilio?: string | null;
}

export interface PuntoVentaDTO {
  id: number;
  numero: number;
  descripcion: string;
  tipoEmision: string;
  domicilioComercial: string;
}

export interface CondicionIvaDTO {
  id: number;
  codigoAfip: number;
  denominacion: string;
}

export interface TipoMonedaDTO {
  id: number;
  codigoAfip: string;
  denominacion: string;
  simbolo: string;
}

export interface MarcaDTO {
  id: number;
  codigo: number;
  denominacion: string;
}

export interface RubroDTO {
  id: number;
  codigo: number;
  denominacion: string;
}

export interface ArticuloDTO {
  id: number;
  codigo: string;
  denominacion: string;
  marcaDTO?: MarcaDTO | null;
  rubroDTO?: RubroDTO | null;
}

export interface ListaPrecioDTO {
  id: number;
  codigo: string;
  denominacion: string;
}

export interface ListaPrecioArticuloDTO {
  id: number;
  precioVenta: number;
  listaPrecioDTO?: ListaPrecioDTO | null;
  articuloDTO?: ArticuloDTO | null;
  // Campos heredados o aplanados para compatibilidad
  listaPrecioId?: number;
  listaPrecioDenominacion?: string;
  articuloId?: number;
  articuloCodigo?: string;
  articuloDenominacion?: string;
}

// ==========================================
// DTOs de Factura y Detalle (Salida completa)
// ==========================================

export interface FacturaVentaDetalleDTO {
  id?: number;
  listaPrecioArticulo: ListaPrecioArticuloDTO;
  descripcion: string;
  cantidad: number;
  precioUnitario: number;
  porcentajeBonificacion: number;
  importeNeto: number;
  importeIva: number;
  importeSubtotal: number;
}

export interface FacturaVentaDTO {
  id: number;
  numero: number;
  fechaEmision: string; // ISO Date string desde el backend
  puntoVenta: PuntoVentaDTO;
  cliente: ClienteDTO | null;
  condicionIva: CondicionIvaDTO;
  tipoMoneda: TipoMonedaDTO;
  importeTotal: number;
  importeCobrado: number;
  importeSaldo: number;
  cae?: string | null;
  caeFechaVencimiento?: string | null;
  resultadoAfip?: string | null;
  motivoRechazo?: string | null;
  estado: string;
  fechaAnulacion?: string | null;
  observaciones?: string | null;
  detalles: FacturaVentaDetalleDTO[];
}

// ==========================================
// DTOs Especiales de Entrada (Creación y Edición)
// ==========================================

export interface FacturaDetalleRequestDTO {
  listaPrecioArticuloId: number;
  descripcion?: string;
  cantidad: number;
  precioUnitario: number;
  porcentajeBonificacion: number;
}

export interface FacturaRequestDTO {
  puntoVentaId: number;
  clienteId?: number | null;
  condicionIvaId: number;
  tipoMonedaId: number;
  numero?: number;
  fechaEmision?: string;
  observaciones?: string;
  usuarioCargaId?: number | null;
  detalles: FacturaDetalleRequestDTO[];
}

export interface FacturaEditDTO {
  puntoVentaId?: number;
  clienteId?: number | null;
  condicionIvaId?: number;
  tipoMonedaId?: number;
  numero?: number;
  fechaEmision?: string;
  estado?: string;
  observaciones?: string;
  usuarioModificacionId?: number | null;
  detalles?: FacturaDetalleRequestDTO[];
}

// ==========================================
// DTO para reporte/grilla y filtros
// ==========================================

export interface FacturaReporteDTO {
  id: number;
  numeroFactura?: number | null;
  fechaEmision: string;
  ClienteDenominacion: string;
  condicionIva: string;
  puntoVentaDescripcion: string;
  importeTotal: number;
  CantidadItemns: number;
  estado: string;
  cae?: string | null;
  caeFechaVencimiento?: string | null;
}

export interface FacturaReportePageDTO {
  facturas: FacturaReporteDTO[];
  paginaActual: number;
  totalPaginas: number;
  totalElementos: number;
  tamanioPagina: number;
}

export interface FacturaFiltrosDTO {
  fechaDesde?: string;
  fechaHasta?: string;
  estado?: string;
  montoMinimo?: number;
}

export interface ErrorResponseDTO {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
