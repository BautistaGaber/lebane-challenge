import {apiRequest} from '../../../api/httpClient'
import type {
    DepartmentCreateRequest,
    DepartmentDetail,
    DepartmentFilters,
    DepartmentListItem,
    DepartmentUpdateRequest,
    PageResponse
} from '../types/department'

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
        params.set('metrosCuadradosMin', filters.metrosCuadradosMin)
    }

    if (filters.metrosCuadradosMax) {
        params.set('metrosCuadradosMax', filters.metrosCuadradosMax)
    }

    return apiRequest<PageResponse<DepartmentListItem>>(`/api/departamentos?${params.toString()}`)
}

export async function getDepartmentById(id: number): Promise<DepartmentDetail> {
    return apiRequest<DepartmentDetail>(`/api/departamentos/${id}`)
}

export async function updateDepartment(id: number, data: DepartmentUpdateRequest): Promise<DepartmentDetail>{
    return apiRequest<DepartmentDetail>(`/api/departamentos/${id}`,{
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
}

export async function createDepartment(data: DepartmentCreateRequest, images: File[],): Promise<DepartmentDetail> {
    const formData = new FormData()

    formData.append(
        'departamento',
        new Blob([JSON.stringify(data)],
            {
                type: 'application/json'
            },
        ),
    )

    images.forEach((image) => {formData.append('imagenes', image)})

    return apiRequest<DepartmentDetail>(
        '/api/departamentos',
        {
            method: 'POST',
            body: formData,
        },
    )
}
