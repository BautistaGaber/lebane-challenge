import { apiRequest } from '../../../api/httpClient'
import type {DepartmentListItem, PageResponse} from '../types/department'

export async function getDepartments(): Promise<
    PageResponse<DepartmentListItem>> {
    return apiRequest<PageResponse<DepartmentListItem>>('/api/departamentos?pagina=0&cantidad=20')
}