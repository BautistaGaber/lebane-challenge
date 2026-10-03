import {Link} from 'react-router-dom'
import type {DepartmentListItem} from '../types/department'
import {ImageWithFallback} from '../../../components/ImageWithFallback'

type DepartmentCardProps = {
    department: DepartmentListItem
}

export function DepartmentCard({department}: DepartmentCardProps) {
    return (
        <article className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <div className="flex gap-4">
                <ImageWithFallback
                    src={department.imagenPrincipal}
                    alt={department.titulo}
                    className="h-20 w-24 shrink-0"
                />

                <div className="min-w-0 flex-1">
                    <p className="text-xs text-slate-400">
                        #{department.id}
                    </p>

                    <Link
                        to={`/departamentos/${department.id}`}
                        className="mt-1 block font-semibold text-slate-900 hover:text-blue-600"
                    >
                        {department.titulo}
                    </Link>

                    <p className="mt-2 font-semibold text-slate-900">
                        {department.moneda}{' '}
                        {department.precio.toLocaleString('es-AR')}
                    </p>
                </div>
            </div>

            <div className="mt-4 grid grid-cols-2 gap-3 border-t border-slate-100 pt-4">
                <InfoItem
                    label="Superficie"
                    value={`${department.metrosCuadrados} m²`}
                />

                <div>
                    <p className="text-xs text-slate-400">
                        Estado
                    </p>

                    <span
                        className={[
                            'mt-1 inline-flex rounded-full px-2.5 py-1 text-xs font-semibold',
                            department.disponible
                                ? 'bg-emerald-50 text-emerald-700'
                                : 'bg-slate-100 text-slate-600',
                        ].join(' ')}
                    >
                        {department.disponible
                            ? 'Disponible'
                            : 'No disponible'}
                    </span>
                </div>

                <InfoItem
                    label="Fotos"
                    value={department.cantidadImagenes.toString()}
                />

                <InfoItem
                    label="Consultas"
                    value={department.cantidadConsultas.toString()}
                />
            </div>

            <Link
                to={`/departamentos/${department.id}`}
                className="mt-4 block w-full rounded-lg border border-blue-200 bg-blue-50 px-4 py-2.5 text-center text-sm font-semibold text-blue-700 transition hover:bg-blue-100"
            >
                Ver detalle
            </Link>
        </article>
    )
}

type InfoItemProps = {
    label: string
    value: string
}

function InfoItem({label, value}: InfoItemProps) {
    return (
        <div>
            <p className="text-xs text-slate-400">
                {label}
            </p>

            <p className="mt-1 text-sm font-medium text-slate-700">
                {value}
            </p>
        </div>
    )
}