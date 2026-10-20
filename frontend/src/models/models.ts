export interface FacturaReporteDTO  {
  numeroFactura: number
  fechaEmision: string
  ClienteDenominacion: string
  condicionIva: string
  puntoVentaDescripcion: string
  importeTotal: number
  CantidadItemns: number
  estado: string
}