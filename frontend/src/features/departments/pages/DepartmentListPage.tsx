import {Link} from 'react-router-dom'
import {DepartmentTable} from "../components/DepartmentTable.tsx";
import {useDepartments} from "../hooks/useDepartments.ts";

export function DepartmentsListPage() {
    const {departments, loading, error} = useDepartments()

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

            <section className="mt-8 rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
                    <div className="xl:col-span-2">
                        <label
                            htmlFor="search"
                            className="mb-2 block text-sm font-medium text-slate-700"
                        >
                            Buscar
                        </label>

                        <input
                            id="search"
                            type="text"
                            placeholder="Título o dirección..."
                            className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                        />
                    </div>

                    <div>
                        <label
                            htmlFor="availability"
                            className="mb-2 block text-sm font-medium text-slate-700"
                        >
                            Estado
                        </label>

                        <select
                            id="availability"
                            className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                        >
                            <option value="">Todos</option>
                            <option value="true">Disponibles</option>
                            <option value="false">No disponibles</option>
                        </select>
                    </div>

                    <div>
                        <label
                            htmlFor="minPrice"
                            className="mb-2 block text-sm font-medium text-slate-700"
                        >
                            Precio mínimo
                        </label>

                        <input
                            id="minPrice"
                            type="number"
                            min="0"
                            placeholder="0"
                            className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                        />
                    </div>

                    <div>
                        <label
                            htmlFor="maxPrice"
                            className="mb-2 block text-sm font-medium text-slate-700"
                        >
                            Precio máximo
                        </label>

                        <input
                            id="maxPrice"
                            type="number"
                            min="0"
                            placeholder="Sin límite"
                            className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                        />
                    </div>
                </div>
            </section>

            <section className="mt-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mt-6">
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
                </div>
            </section>
        </div>
    )
}