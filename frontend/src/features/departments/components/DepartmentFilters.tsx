import type {DepartmentFilters as Filters} from '../types/department'

type DepartmentFiltersProps = {
    filters: Filters
    onChange: (filters: Filters) => void
    onApply: () => void
    onClear: () => void
}

export function DepartmentFilters({filters, onChange, onApply, onClear,}: DepartmentFiltersProps) {
    return (
        <section className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
                <div>
                    <label
                        htmlFor="availability"
                        className="mb-2 block text-sm font-medium text-slate-700"
                    >
                        Estado
                    </label>

                    <select
                        id="availability"
                        value={
                            filters.disponible === undefined
                                ? ''
                                : filters.disponible.toString()
                        }
                        onChange={(event) => {
                            const value = event.target.value

                            onChange({
                                ...filters,
                                disponible:
                                    value === ''
                                        ? undefined
                                        : value === 'true',
                            })
                        }}
                        className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                    >
                        <option value="">Todos</option>
                        <option value="true">Disponibles</option>
                        <option value="false">No disponibles</option>
                    </select>
                </div>

                <FilterInput
                    id="minPrice"
                    label="Precio mínimo"
                    value={filters.precioMin ?? ''}
                    onChange={(value) =>
                        onChange({
                            ...filters,
                            precioMin: value,
                        })
                    }
                />

                <FilterInput
                    id="maxPrice"
                    label="Precio máximo"
                    value={filters.precioMax ?? ''}
                    onChange={(value) =>
                        onChange({
                            ...filters,
                            precioMax: value,
                        })
                    }
                />

                <FilterInput
                    id="minSquareMeters"
                    label="m² mínimos"
                    value={filters.metrosCuadradosMin ?? ''}
                    onChange={(value) =>
                        onChange({
                            ...filters,
                            metrosCuadradosMin: value,
                        })
                    }
                />

                <FilterInput
                    id="maxSquareMeters"
                    label="m² máximos"
                    value={filters.metrosCuadradosMax ?? ''}
                    onChange={(value) =>
                        onChange({
                            ...filters,
                            metrosCuadradosMax: value,
                        })
                    }
                />
            </div>

            <div className="mt-5 grid grid-cols-2 gap-3 sm:flex sm:justify-end">
                <button
                    type="button"
                    onClick={onClear}
                    className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                >
                    Limpiar
                </button>

                <button
                    type="button"
                    onClick={onApply}
                    className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
                >
                    Aplicar filtros
                </button>
            </div>
        </section>
    )
}

type FilterInputProps = {
    id: string
    label: string
    value: string
    onChange: (value: string) => void
}

function FilterInput({id, label, value, onChange}: FilterInputProps) {
    return (
        <div>
            <label
                htmlFor={id}
                className="mb-2 block text-sm font-medium text-slate-700"
            >
                {label}
            </label>

            <input
                id={id}
                type="number"
                min="0"
                value={value}
                onChange={(event) => onChange(event.target.value)}
                className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
        </div>
    )
}