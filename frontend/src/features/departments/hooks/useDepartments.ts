import { useEffect, useState } from 'react'
import { getDepartments } from '../services/departmentService'
import type {DepartmentListItem, PageResponse,} from '../types/department'

export function useDepartments() {
    const [data, setData] = useState<PageResponse<DepartmentListItem> | null>(null)

    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        async function loadDepartments() {
            try {
                setLoading(true)

                const response = await getDepartments()

                setData(response)
                setError(null)
            } catch {
                setError('No se pudieron cargar los departamentos.')
            } finally {
                setLoading(false)
            }
        }

        loadDepartments()
    }, [])

    return {
        departments: data?.contenido ?? [],
        page: data,
        loading,
        error,
    }
}