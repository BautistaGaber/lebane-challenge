import {useEffect, useState} from 'react'
import {getDepartmentById} from '../services/departmentService'
import type {DepartmentDetail} from '../types/department'

export function useDepartment(id: number) {
    const [department, setDepartment] = useState<DepartmentDetail | null>(null)

    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        async function loadDepartment() {
            try {
                setLoading(true)
                setError(null)

                const response = await getDepartmentById(id)

                setDepartment(response)
            } catch {
                setError('No se pudo cargar el departamento.')
            } finally {
                setLoading(false)
            }
        }

        loadDepartment()
    }, [id])

    return {
        department, loading, error
    }
}