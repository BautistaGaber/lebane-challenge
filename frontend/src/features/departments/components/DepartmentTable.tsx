import {Link} from 'react-router-dom'
import type {DepartmentListItem} from '../types/department'
import {ImageWithFallback} from "../../../components/ImageWithFallback.tsx";

type DepartmentTableProps = {
    departments: DepartmentListItem[]
}

export function DepartmentTable({departments}: DepartmentTableProps) {
    return (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
            <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                    <thead className="border-b border-slate-200 bg-slate-50">
                    <tr className="text-slate-600">
                        <th className="px-5 py-4 font-semibold">Imagen</th>
                        <th className="px-5 py-4 font-semibold">ID</th>
                        <th className="px-5 py-4 font-semibold">Título</th>
                        <th className="px-5 py-4 font-semibold">Precio</th>
                        <th className="px-5 py-4 font-semibold">m²</th>
                        <th className="px-5 py-4 font-semibold">Estado</th>
                        <th className="px-5 py-4 font-semibold">Fotos</th>
                        <th className="px-5 py-4 font-semibold">Consultas</th>
                        <th className="px-5 py-4 font-semibold">Acciones</th>
                    </tr>
                    </thead>

                    <tbody className="divide-y divide-slate-100">
                    {departments.map((department) => (
                        <tr
                            key={department.id}
                            className="transition hover:bg-slate-50"
                        >
                            <td className="px-5 py-4">
                                {department.imagenPrincipal ? (
                                    <td className="h-12 w-16 rounded-lg object-cover">
                                        <ImageWithFallback
                                            src={department.imagenPrincipal}
                                            alt={department.titulo}
                                        />
                                    </td>
                                ) : (
                                    <div
                                        className="flex h-12 w-16 items-center justify-center rounded-lg bg-slate-100 text-xs text-slate-400">
                                        Sin foto
                                    </div>
                                )}
                            </td>

                            <td className="px-5 py-4 text-slate-500">
                                {department.id}
                            </td>

                            <td className="px-5 py-4">
                                <Link
                                    to={`/departamentos/${department.id}`}
                                    className="font-medium text-slate-900 hover:text-blue-600"
                                >
                                    {department.titulo}
                                </Link>
                            </td>

                            <td className="px-5 py-4 font-medium text-slate-900">
                                {department.moneda}{' '}
                                {department.precio.toLocaleString('es-AR')}
                            </td>

                            <td className="px-5 py-4 text-slate-600">
                                {department.metrosCuadrados}
                            </td>

                            <td className="px-5 py-4">
                  <span
                      className={[
                          'inline-flex rounded-full px-2.5 py-1 text-xs font-semibold',
                          department.disponible
                              ? 'bg-emerald-50 text-emerald-700'
                              : 'bg-slate-100 text-slate-600',
                      ].join(' ')}
                  >
                    {department.disponible
                        ? 'Disponible'
                        : 'No disponible'}
                  </span>
                            </td>

                            <td className="px-5 py-4 text-slate-600">
                                {department.cantidadImagenes}
                            </td>

                            <td className="px-5 py-4 text-slate-600">
                                {department.cantidadConsultas}
                            </td>

                            <td className="px-5 py-4">
                                <Link
                                    to={`/departamentos/${department.id}`}
                                    className="font-medium text-blue-600 hover:text-blue-700"
                                >
                                    Ver detalle
                                </Link>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    )
}