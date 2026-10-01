export type CurrencyCode = 'USD' | 'ARS'

export interface DepartmentListItem {
    id: number
    titulo: string
    precio: number
    moneda: CurrencyCode
    metrosCuadrados: number
    disponible: boolean
    imagenPrincipal: string | null
    cantidadImagenes: number
    cantidadConsultas: number
}

export interface PageResponse<T> {
    contenido: T[]
    pagina: number
    cantidad: number
    totalElementos: number
    totalPaginas: number
    primera: boolean
    ultima: boolean
}