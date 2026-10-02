import {apiRequest} from '../../../api/httpClient'
import type {DepartmentFilters, DepartmentListItem, PageResponse} from '../types/department'

type GetDepartmentsParams = {
    filters: DepartmentFilters
    page: number
    size: number
}

export async function getDepartments({filters, page, size}: GetDepartmentsParams): Promise<PageResponse<DepartmentListItem>> {

    const params = new URLSearchParams()

    params.set('pagina', page.toString())
    params.set('cantidad', size.toString())


    if (filters.disponible !== undefined) {
        params.set('disponible', filters.disponible.toString())
    }

    if (filters.precioMin) {
        params.set('precioMin', filters.precioMin)
    }

    if (filters.precioMax) {
        params.set('precioMax', filters.precioMax)
    }

    if (filters.metrosCuadradosMin) {
        params.set('metrosCuadradosMin',filters.metrosCuadradosMin)
    }

    if (filters.metrosCuadradosMax) {
        params.set('metrosCuadradosMax',filters.metrosCuadradosMax)
    }

    return apiRequest<PageResponse<DepartmentListItem>>(`/api/departamentos?${params.toString()}`)
}