import {Link, useParams} from 'react-router-dom'
import {useDepartment} from '../hooks/useDepartment'
import {ImageWithFallback} from "../../../components/ImageWithFallback.tsx";

export function DepartmentDetailPage() {
    const {id} = useParams()

    const departmentId = Number(id)

    const {department, loading, error} = useDepartment(departmentId)

    if (loading) {
        return (
            <div className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-500">
                Cargando departamento...
            </div>
        )
    }

    if (error || !department) {
        return (
            <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-sm text-red-700">
                {error ?? 'Departamento no encontrado.'}
            </div>
        )
    }

    return (
        <div>
            <div className="mb-6 flex items-start justify-between gap-4">
                <div>
                    <Link
                        to="/departamentos"
                        className="text-sm font-medium text-blue-600 hover:text-blue-700"
                    >
                        ← Volver a departamentos
                    </Link>

                    <h2 className="mt-3 text-3xl font-bold text-slate-900">
                        {department.titulo}
                    </h2>

                    <p className="mt-2 text-slate-600">
                        {department.direccion}
                    </p>
                </div>

                <Link
                    to={`/departamentos/${department.id}/editar`}
                    className="rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700"
                >
                    Editar departamento
                </Link>
            </div>

            <div className="grid gap-6 lg:grid-cols-3">
                <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm lg:col-span-2">
                    <h3 className="text-lg font-semibold text-slate-900">
                        Información
                    </h3>

                    <div className="mt-6 grid gap-6 sm:grid-cols-2">
                        <DetailItem
                            label="Precio"
                            value={`${department.moneda} ${department.precio.toLocaleString()}`}
                        />

                        <DetailItem
                            label="Superficie"
                            value={`${department.metrosCuadrados} m²`}
                        />

                        <DetailItem
                            label="Estado"
                            value={
                                department.disponible
                                    ? 'Disponible'
                                    : 'No disponible'
                            }
                        />

                        <DetailItem
                            label="Dirección"
                            value={department.direccion}
                        />
                    </div>

                    <div className="mt-8">
                        <h4 className="text-sm font-medium text-slate-500">
                            Descripción
                        </h4>

                        <p className="mt-2 whitespace-pre-line text-slate-700">
                            {department.descripcion}
                        </p>
                    </div>
                </section>

                <section className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
                    <h3 className="text-lg font-semibold text-slate-900">
                        Resumen
                    </h3>

                    <div className="mt-5 space-y-4">
                        <DetailItem
                            label="Fotos"
                            value={department.imagenes.length.toString()}
                        />

                        <DetailItem
                            label="Consultas"
                            value={department.consultas.length.toString()}
                        />

                        <DetailItem
                            label="ID"
                            value={department.id.toString()}
                        />
                    </div>
                </section>
            </div>
            <section className="mt-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="flex items-center justify-between">
                    <h3 className="text-lg font-semibold text-slate-900">
                        Galería
                    </h3>

                    <span className="text-sm text-slate-500">
      {department.imagenes.length} fotos
    </span>
                </div>

                {department.imagenes.length === 0 ? (
                    <div
                        className="mt-5 flex h-40 items-center justify-center rounded-xl bg-slate-100 text-sm text-slate-400">
                        Sin imágenes
                    </div>
                ) : (
                    <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                        {department.imagenes.map((image) => (
                            <ImageWithFallback
                                key={image.id}
                                src={image.url}
                                alt={department.titulo}
                                className="h-48 w-full"
                            />
                        ))}
                    </div>
                )}
            </section>

            <section className="mt-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="flex items-center justify-between">
                    <h3 className="text-lg font-semibold text-slate-900">
                        Consultas
                    </h3>

                    <span className="text-sm text-slate-500">
      {department.consultas.length} consultas
    </span>
                </div>

                {department.consultas.length === 0 ? (
                    <p className="mt-5 text-sm text-slate-500">
                        Este departamento todavía no tiene consultas.
                    </p>
                ) : (
                    <div className="mt-5 space-y-4">
                        {department.consultas.map((inquiry) => (
                            <article
                                key={inquiry.id}
                                className="rounded-xl border border-slate-200 p-4"
                            >
                                <div className="flex items-start justify-between gap-4">
                                    <div>
                                        <p className="font-medium text-slate-900">
                                            {inquiry.nombre}
                                        </p>

                                        <p className="text-sm text-slate-500">
                                            {inquiry.email}
                                        </p>
                                    </div>

                                    <time className="text-sm text-slate-400">
                                        {new Date(inquiry.fecha).toLocaleDateString('es-AR')}
                                    </time>
                                </div>

                                <p className="mt-3 text-sm leading-6 text-slate-700">
                                    {inquiry.mensaje}
                                </p>
                            </article>
                        ))}
                    </div>
                )}
            </section>
        </div>
    )
}

type DetailItemProps = {
    label: string
    value: string
}

function DetailItem({label, value}: DetailItemProps) {
    return (
        <div>
            <p className="text-sm text-slate-500">
                {label}
            </p>

            <p className="mt-1 font-medium text-slate-900">
                {value}
            </p>
        </div>
    )
}