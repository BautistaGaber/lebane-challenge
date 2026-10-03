import {type FormEvent, useEffect, useState} from 'react'
import {Link, useNavigate, useParams} from 'react-router-dom'
import {useDepartment} from '../hooks/useDepartment'
import {updateDepartment} from '../services/departmentService'
import type {CurrencyCode, DepartmentUpdateRequest,} from '../types/department'
import {ApiError} from "../../../api/httpClient.ts";

export function DepartmentEditPage() {
    const {id} = useParams()
    const navigate = useNavigate()

    const departmentId = Number(id)

    const {department,loading,error} = useDepartment(departmentId)

    const [form, setForm] = useState<DepartmentUpdateRequest | null>(null)

    const [saving, setSaving] = useState(false)
    const [saveError, setSaveError] = useState<string | null>(null)

    useEffect(() => {
        if (!department) {
            return
        }

        setForm({
            titulo: department.titulo,
            descripcion: department.descripcion,
            precio: department.precio,
            moneda: department.moneda,
            metrosCuadrados: department.metrosCuadrados,
            direccion: department.direccion,
            latitud: department.latitud,
            longitud: department.longitud,
            disponible: department.disponible,
            version: department.version,
        })
    }, [department])

    async function handleSubmit(event: FormEvent) {
        event.preventDefault()

        if (!form) {
            return
        }

        try {
            setSaving(true)
            setSaveError(null)

            await updateDepartment(departmentId,form)

            navigate(`/departamentos/${departmentId}`)
        } catch(error) {
            if (error instanceof ApiError) {
                switch (error.status) {
                    case 400:
                        setSaveError(
                            'Hay datos inválidos. Revisá los campos e intentá nuevamente.')
                        break

                    case 404:
                        setSaveError('El departamento ya no existe.')
                        break

                    case 409:
                        setSaveError('El departamento fue modificado por otra operación. Volvé a cargar la página antes de guardar nuevamente.')
                        break

                    default:
                        setSaveError('No se pudieron guardar los cambios. Intentá nuevamente.')
                }

                return
            }
            setSaveError('Ocurrió un error inesperado. Intentá nuevamente.',)
        } finally {
            setSaving(false)
        }
    }

    if (loading) {
        return (
            <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500">
                Cargando departamento...
            </div>
        )
    }

    if (error || !department || !form) {
        return (
            <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-sm text-red-700">
                {error ?? 'No se pudo cargar el departamento.'}
            </div>
        )
    }

    return (
        <div>
            <div className="mb-6 flex items-start justify-between gap-4">
                <div>
                    <Link
                        to={`/departamentos/${departmentId}`}
                        className="text-sm font-medium text-blue-600 hover:text-blue-700"
                    >
                        ← Volver al detalle
                    </Link>

                    <h2 className="mt-3 text-3xl font-bold text-slate-900">
                        Editar departamento #{departmentId}
                    </h2>

                    <p className="mt-2 text-slate-600">
                        {department.direccion}
                    </p>
                </div>
            </div>

            <form onSubmit={handleSubmit}>
                <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
                    <h3 className="text-lg font-semibold text-slate-900">
                        Información básica
                    </h3>

                    <div className="mt-6 grid gap-5 md:grid-cols-2">
                        <FormField label="Título">
                            <input
                                type="text"
                                value={form.titulo}
                                onChange={(event) =>
                                    setForm({
                                        ...form,
                                        titulo: event.target.value,
                                    })
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            />
                        </FormField>

                        <FormField label="Estado">
                            <select
                                value={form.disponible.toString()}
                                onChange={(event) =>
                                    setForm({
                                        ...form,
                                        disponible: event.target.value === 'true',
                                    })
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            >
                                <option value="true">
                                    Disponible
                                </option>

                                <option value="false">
                                    No disponible
                                </option>
                            </select>
                        </FormField>

                        <FormField label="Precio">
                            <input
                                type="number"
                                min="0"
                                value={form.precio}
                                onChange={(event) =>
                                    setForm({
                                        ...form, precio: Number(event.target.value),
                                    })
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            />
                        </FormField>

                        <FormField label="Moneda">
                            <select
                                value={form.moneda}
                                onChange={(event) =>
                                    setForm({
                                        ...form,
                                        moneda: event.target.value as CurrencyCode,
                                    })
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            >
                                <option value="ARS">ARS</option>
                                <option value="USD">USD</option>
                            </select>
                        </FormField>

                        <FormField label="Superficie (m²)">
                            <input
                                type="number"
                                min="0"
                                step="0.01"
                                value={form.metrosCuadrados}
                                onChange={(event) =>
                                    setForm({
                                        ...form, metrosCuadrados: Number(event.target.value),
                                    })
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            />
                        </FormField>

                        <FormField label="Dirección">
                            <input
                                type="text"
                                value={form.direccion}
                                onChange={(event) =>
                                    setForm({...form, direccion: event.target.value,
                                    })
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            />
                        </FormField>
                    </div>

                    <div className="mt-5">
                        <FormField label="Descripción">
              <textarea
                  rows={5}
                  value={form.descripcion}
                  onChange={(event) =>
                      setForm({...form, descripcion: event.target.value,
                      })
                  }
                  className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
              />
                        </FormField>
                    </div>
                </section>

                {saveError && (
                    <div className="mt-5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                        {saveError}
                    </div>
                )}

                <div className="mt-6 flex justify-end gap-3">
                    <Link
                        to={`/departamentos/${departmentId}`}
                        className="rounded-lg border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
                    >
                        Cancelar
                    </Link>

                    <button
                        type="submit"
                        disabled={saving}
                        className="rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                        {saving
                            ? 'Guardando...'
                            : 'Guardar cambios'}
                    </button>
                </div>
            </form>
        </div>
    )
}

type FormFieldProps = {
    label: string
    children: React.ReactNode
}

function FormField({label, children,}: FormFieldProps) {
    return (
        <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">
                {label}
            </label>
            {children}
        </div>
    )
}