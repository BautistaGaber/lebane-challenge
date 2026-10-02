import {useState} from 'react'
import {Link} from 'react-router-dom'
import {DepartmentFilters as DepartmentFiltersComponent} from '../components/DepartmentFilters'
import {DepartmentTable} from '../components/DepartmentTable'
import {useDepartments} from '../hooks/useDepartments'
import type {DepartmentFilters} from '../types/department'

export function DepartmentsListPage() {
    const [draftFilters, setDraftFilters] = useState<DepartmentFilters>({})

    const [appliedFilters, setAppliedFilters] = useState<DepartmentFilters>({})

    const [page, setPage] = useState(0)

    const {
        departments,
        loading,
        pageInfo,
        error
    } = useDepartments(appliedFilters, page, 10,)

    return (
        <div>
            <div className="flex items-start justify-between gap-4">
                <div>
                    <h2 className="text-3xl font-bold text-slate-900">
                        Departamentos
                    </h2>

                    <p className="mt-2 text-slate-600">
                        Gestioná los departamentos de la inmobiliaria.
                    </p>
                </div>

                <Link
                    to="/departamentos/nuevo"
                    className="rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700"
                >
                    Nuevo departamento
                </Link>
            </div>

            <div className="mt-8">
                <DepartmentFiltersComponent
                    filters={draftFilters}
                    onChange={setDraftFilters}
                    onApply={() => {
                        setAppliedFilters(draftFilters)
                        setPage(0)
                    }}
                    onClear={() => {
                        setDraftFilters({})
                        setAppliedFilters({})
                        setPage(0)
                    }}
                />
            </div>

            <section className="mt-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
                {loading && (
                    <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500">
                        Cargando departamentos...
                    </div>
                )}

                {error && (
                    <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-sm text-red-700">
                        {error}
                    </div>
                )}

                {!loading && !error && (
                    <DepartmentTable departments={departments}/>
                )}
                {pageInfo && pageInfo.totalPaginas > 0 && (
                    <div className="mt-6 flex items-center justify-between">
                        <p className="text-sm text-slate-600">
                            Página {pageInfo.pagina + 1} de {pageInfo.totalPaginas}
                            {' · '}
                            {pageInfo.totalElementos} departamentos
                        </p>

                        <div className="flex gap-2">
                            <button
                                type="button"
                                disabled={pageInfo.primera}
                                onClick={() => setPage((currentPage) => currentPage - 1)}
                                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                                Anterior
                            </button>

                            <button
                                type="button"
                                disabled={pageInfo.ultima}
                                onClick={() => setPage((currentPage) => currentPage + 1)}
                                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                                Siguiente
                            </button>
                        </div>
                    </div>
                )}
            </section>
        </div>
    )
}