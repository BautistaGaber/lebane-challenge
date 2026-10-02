import {useEffect, useState} from 'react'
import {getDepartments} from '../services/departmentService'
import type {DepartmentFilters, DepartmentListItem, PageResponse,} from '../types/department'

export function useDepartments(filters: DepartmentFilters, page: number, size: 10) {
    const [data, setData] = useState<PageResponse<DepartmentListItem> | null>(null)

    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        async function loadDepartments() {
            try {
                setLoading(true)
                setError(null)

                const response = await getDepartments({filters, page, size})

                setData(response)
            } catch {
                setError('No se pudieron cargar los departamentos.')
            } finally {
                setLoading(false)
            }
        }

        loadDepartments()
    }, [filters, page, size])

    return {
        departments: data?.contenido ?? [],
        pageInfo: data,
        loading,
        error,
    }
}