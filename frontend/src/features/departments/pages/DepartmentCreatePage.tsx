import {
    type ChangeEvent,
    type FormEvent,
    type ReactNode,
    useEffect,
    useState,
} from 'react'
import {Link, useNavigate} from 'react-router-dom'

import {ApiError} from '../../../api/httpClient'
import {AddressAutocomplete} from '../components/AddressAutocomplete'
import {createDepartment} from '../services/departmentService'
import type {CurrencyCode, DepartmentCreateRequest} from '../types/department'

interface FormState {
    titulo: string
    descripcion: string
    precio: string
    moneda: CurrencyCode
    metrosCuadrados: string
    direccion: string
    latitud: number | null
    longitud: number | null
    disponible: boolean
}

const INITIAL_FORM: FormState = {
    titulo: '',
    descripcion: '',
    precio: '',
    moneda: 'USD',
    metrosCuadrados: '',
    direccion: '',
    latitud: null,
    longitud: null,
    disponible: true,
}

const MAX_IMAGES = 5
const MAX_IMAGE_SIZE = 10 * 1024 * 1024

const ALLOWED_IMAGE_TYPES = [
    'image/jpeg',
    'image/png',
    'image/webp',
]

export function DepartmentCreatePage() {
    const navigate = useNavigate()

    const [form, setForm] = useState<FormState>(INITIAL_FORM)
    const [images, setImages] = useState<File[]>([])
    const [previewUrls, setPreviewUrls] = useState<string[]>([])

    const [saving, setSaving] = useState(false)
    const [saveError, setSaveError] = useState<string | null>(null)

    useEffect(() => {
        const urls = images.map((image) => URL.createObjectURL(image),)

        setPreviewUrls(urls)

        return () => {urls.forEach((url) => URL.revokeObjectURL(url))}}, [images])

    function handleImagesChange(event: ChangeEvent<HTMLInputElement>,) {
        const selectedFiles = Array.from(event.target.files ?? [],)

        setSaveError(null)

        const invalidType = selectedFiles.find((file) => !ALLOWED_IMAGE_TYPES.includes(file.type),)

        if (invalidType) {
            setSaveError('Las imágenes deben ser JPG, PNG o WEBP.',)
            event.target.value = ''
            return
        }

        const oversizedFile = selectedFiles.find((file) => file.size > MAX_IMAGE_SIZE,)

        if (oversizedFile) {
            setSaveError('Cada imagen puede pesar como máximo 10 MB.',)
            event.target.value = ''
            return
        }

        setImages((current) => {

            const newImages = selectedFiles.filter(
                (selectedFile) =>
                    !current.some(
                        (currentFile) =>
                            currentFile.name === selectedFile.name &&
                            currentFile.size === selectedFile.size &&
                            currentFile.lastModified ===
                            selectedFile.lastModified,
                    ),
            )

            const combinedImages = [
                ...current,
                ...newImages,
            ]

            if (combinedImages.length > MAX_IMAGES) {
                setSaveError(`Podés subir como máximo ${MAX_IMAGES} imágenes.`,)

                return current
            }

            return combinedImages
        })

        event.target.value = ''
    }

    function removeImage(index: number) {
        setImages((current) => current.filter((_, currentIndex) => currentIndex !== index))
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>,) {
        event.preventDefault()

        setSaveError(null)

        if (form.latitud === null || form.longitud === null) {
            setSaveError('Seleccioná una dirección de las sugerencias del autocompletado.',)
            return
        }

        const price = Number(form.precio)
        const squareMeters = Number(form.metrosCuadrados)

        if (!Number.isFinite(price) || price <= 0) {
            setSaveError('El precio debe ser mayor a cero.',)
            return
        }

        if (!Number.isFinite(squareMeters) || squareMeters <= 0) {
            setSaveError('Los metros cuadrados deben ser mayores a cero.',)
            return
        }

        const request: DepartmentCreateRequest = {
            titulo: form.titulo.trim(),
            descripcion: form.descripcion.trim(),
            precio: price,
            moneda: form.moneda,
            metrosCuadrados: squareMeters,
            direccion: form.direccion.trim(),
            latitud: form.latitud,
            longitud: form.longitud,
            disponible: form.disponible,
        }

        try {
            setSaving(true)

            const createdDepartment = await createDepartment(request, images)

            navigate(`/departamentos/${createdDepartment.id}`,)
        } catch (error) {
            if (error instanceof ApiError) {
                switch (error.status) {
                    case 400:
                        setSaveError('Hay datos inválidos. Revisá el formulario.',)
                        break

                    case 413:
                        setSaveError('Las imágenes superan el tamaño permitido.',)
                        break

                    case 415:
                        setSaveError('Alguna de las imágenes tiene un formato no soportado.',)
                        break

                    case 503:
                        setSaveError('El almacenamiento de imágenes no está disponible. Intentá nuevamente.',)
                        break

                    default:
                        setSaveError('No se pudo crear el departamento. Intentá nuevamente.',)
                }

                return
            }

            setSaveError('Ocurrió un error inesperado. Intentá nuevamente.',)
        } finally {
            setSaving(false)
        }
    }

    return (
        <div>
            <div className="mb-6">
                <Link
                    to="/departamentos"
                    className="text-sm font-medium text-blue-600 hover:text-blue-700"
                >
                    ← Volver al listado
                </Link>

                <h2 className="mt-3 text-2xl font-bold text-slate-900 sm:text-3xl">
                    Crear departamento
                </h2>

                <p className="mt-2 text-slate-600">
                    Completá los datos de la nueva propiedad.
                </p>
            </div>

            <form onSubmit={handleSubmit}>
                <section className="rounded-xl border border-slate-200 bg-white p-4 sm:p-6 shadow-sm">
                    <h3 className="text-lg font-semibold text-slate-900">
                        Información básica
                    </h3>

                    <div className="mt-6 grid gap-5 md:grid-cols-2">
                        <FormField label="Título">
                            <input
                                type="text"
                                value={form.titulo}
                                maxLength={120}
                                required
                                onChange={(event) =>
                                    setForm((current) => ({
                                        ...current,
                                        titulo: event.target.value,
                                    }))
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            />
                        </FormField>

                        <FormField label="Estado">
                            <select
                                value={form.disponible.toString()}
                                onChange={(event) =>
                                    setForm((current) => ({
                                        ...current,
                                        disponible:
                                            event.target.value === 'true',
                                    }))
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
                                min="0.01"
                                step="0.01"
                                required
                                value={form.precio}
                                onChange={(event) =>
                                    setForm((current) => ({
                                        ...current,
                                        precio: event.target.value,
                                    }))
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            />
                        </FormField>

                        <FormField label="Moneda">
                            <select
                                value={form.moneda}
                                onChange={(event) =>
                                    setForm((current) => ({
                                        ...current,
                                        moneda:
                                            event.target.value as CurrencyCode,
                                    }))
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            >
                                <option value="ARS">
                                    ARS
                                </option>
                                <option value="USD">
                                    USD
                                </option>
                            </select>
                        </FormField>

                        <FormField label="Superficie (m²)">
                            <input
                                type="number"
                                min="0.01"
                                step="0.01"
                                required
                                value={form.metrosCuadrados}
                                onChange={(event) =>
                                    setForm((current) => ({
                                        ...current,
                                        metrosCuadrados:
                                        event.target.value,
                                    }))
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            />
                        </FormField>

                        <FormField label="Dirección">
                            <AddressAutocomplete
                                value={form.direccion}
                                onChange={(direccion) =>
                                    setForm((current) => ({
                                        ...current,
                                        direccion,
                                        latitud: null,
                                        longitud: null,
                                    }))
                                }
                                onSelect={(address) =>
                                    setForm((current) => ({
                                        ...current,
                                        direccion:
                                        address.formatted,
                                        latitud:
                                        address.latitude,
                                        longitud:
                                        address.longitude,
                                    }))
                                }
                            />

                            {form.latitud !== null &&
                                form.longitud !== null && (
                                    <p className="mt-2 text-xs text-slate-400">
                                        Coordenadas:{' '}
                                        {form.latitud},{' '}
                                        {form.longitud}
                                    </p>
                                )}
                        </FormField>
                    </div>

                    <div className="mt-5">
                        <FormField label="Descripción">
                            <textarea
                                rows={5}
                                maxLength={2000}
                                required
                                value={form.descripcion}
                                onChange={(event) =>
                                    setForm((current) => ({
                                        ...current,
                                        descripcion:
                                        event.target.value,
                                    }))
                                }
                                className="w-full rounded-lg border border-slate-300 px-3 py-2.5"
                            />
                        </FormField>
                    </div>
                </section>

                <section className="mt-6 rounded-xl border border-slate-200 bg-white p-4 sm:p-6 shadow-sm">
                    <h3 className="text-lg font-semibold text-slate-900">
                        Imágenes
                    </h3>

                    <p className="mt-1 text-sm text-slate-500">
                        Podés subir hasta 5 imágenes JPG, PNG o WEBP.
                    </p>

                    <div className="mt-5">
                        <input
                            type="file"
                            accept="image/jpeg,image/png,image/webp"
                            multiple
                            onChange={handleImagesChange}
                            className="block w-full text-sm text-slate-600 file:mr-4 file:rounded-lg file:border-0 file:bg-slate-100 file:px-4 file:py-2.5 file:font-medium file:text-slate-700 hover:file:bg-slate-200"
                        />
                    </div>

                    {previewUrls.length > 0 && (
                        <div className="mt-5 grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-5">
                            {previewUrls.map((url, index) => (
                                <div
                                    key={url}
                                    className="relative"
                                >
                                    <img
                                        src={url}
                                        alt={`Vista previa ${index + 1}`}
                                        className="h-32 w-full rounded-lg object-cover"
                                    />

                                    <button
                                        type="button"
                                        onClick={() =>
                                            removeImage(index)
                                        }
                                        className="absolute right-2 top-2 rounded-md bg-white/90 px-2 py-1 text-xs font-medium text-red-600 shadow"
                                    >
                                        Quitar
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </section>

                {saveError && (
                    <div className="mt-5 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                        {saveError}
                    </div>
                )}

                <div className="mt-6 flex justify-end gap-3">
                    <Link
                        to="/departamentos"
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
                            ? 'Creando...'
                            : 'Crear departamento'}
                    </button>
                </div>
            </form>
        </div>
    )
}

type FormFieldProps = {
    label: string
    children: ReactNode
}

function FormField({label, children}: FormFieldProps) {
    return (
        <div>
            <label className="mb-2 block text-sm font-medium text-slate-700">
                {label}
            </label>
            {children}
        </div>
    )
}