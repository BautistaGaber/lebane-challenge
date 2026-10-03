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

export interface DepartmentFilters {
    disponible?: boolean
    precioMin?: string
    precioMax?: string
    metrosCuadradosMin?: string
    metrosCuadradosMax?: string
}

export interface DepartmentImage {
    id: number
    url: string
    principal: boolean
    orden: number
}

export interface DepartmentInquiry {
    id: number
    nombre: string
    email: string
    mensaje: string
    fecha: string
}

export interface DepartmentDetail {
    id: number
    titulo: string
    descripcion: string
    precio: number
    moneda: CurrencyCode
    metrosCuadrados: number
    direccion: string
    latitud: number | null
    longitud: number | null
    disponible: boolean
    version: number
    imagenes: DepartmentImage[]
    consultas: DepartmentInquiry[]
}

export interface DepartmentUpdateRequest {
    titulo: string
    descripcion: string
    precio: number
    moneda: CurrencyCode
    metrosCuadrados: number
    direccion: string
    latitud: number | null
    longitud: number | null
    disponible: boolean
    version: number
}