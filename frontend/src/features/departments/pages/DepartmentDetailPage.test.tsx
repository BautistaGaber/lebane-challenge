import {
    render,
    screen,
} from '@testing-library/react'
import {
    MemoryRouter,
    Route,
    Routes,
} from 'react-router-dom'

import {DepartmentDetailPage} from './DepartmentDetailPage'
import {useDepartment} from '../hooks/useDepartment'

jest.mock('../hooks/useDepartment')

const mockUseDepartment =
    useDepartment as jest.MockedFunction<
        typeof useDepartment
    >

const department = {
    id: 1,
    titulo: 'Departamento Palermo',
    descripcion: 'Departamento amplio y luminoso',
    precio: 120000,
    moneda: 'USD',
    metrosCuadrados: 80,
    direccion: 'Av. Santa Fe 3200',
    latitud: -34.5,
    longitud: -58.4,
    disponible: true,
    version: 1,

    imagenes: [
        {
            id: 10,
            url: 'https://example.com/image.jpg',
        },
    ],

    consultas: [
        {
            id: 20,
            nombre: 'Juan Pérez',
            email: 'juan@test.com',
            mensaje: '¿Sigue disponible?',
            fecha: '2026-10-01T10:00:00',
        },
    ],
}

function renderPage() {
    return render(
        <MemoryRouter
            initialEntries={[
                '/departamentos/1',
            ]}
        >
            <Routes>
                <Route
                    path="/departamentos/:id"
                    element={
                        <DepartmentDetailPage />
                    }
                />
            </Routes>
        </MemoryRouter>,
    )
}

describe('DepartmentDetailPage', () => {
    beforeEach(() => {
        jest.clearAllMocks()
    })

    it('muestra loading', () => {
        mockUseDepartment.mockReturnValue({
            department: null,
            loading: true,
            error: null,
        })

        renderPage()

        expect(
            screen.getByText(
                /cargando departamento/i,
            ),
        ).toBeInTheDocument()
    })

    it('muestra error', () => {
        mockUseDepartment.mockReturnValue({
            department: null,
            loading: false,
            error: 'No encontrado',
        })

        renderPage()

        expect(
            screen.getByText(
                'No encontrado',
            ),
        ).toBeInTheDocument()
    })

    it('renderiza información principal', () => {
        mockUseDepartment.mockReturnValue({
            department: department as any,
            loading: false,
            error: null,
        })

        renderPage()

        expect(
            screen.getByText(
                'Departamento Palermo',
            ),
        ).toBeInTheDocument()

        expect(
            screen.getAllByText(
                'Av. Santa Fe 3200',
            ).length,
        ).toBeGreaterThan(0)

        expect(
            screen.getByText(
                /120\.000/,
            ),
        ).toBeInTheDocument()

        expect(
            screen.getByText(
                '80 m²',
            ),
        ).toBeInTheDocument()

        expect(
            screen.getByText(
                'Disponible',
            ),
        ).toBeInTheDocument()
    })

    it('muestra link para editar', () => {
        mockUseDepartment.mockReturnValue({
            department: department as any,
            loading: false,
            error: null,
        })

        renderPage()

        expect(
            screen.getByRole('link', {
                name: /editar departamento/i,
            }),
        ).toHaveAttribute(
            'href',
            '/departamentos/1/editar',
        )
    })

    it('muestra galería cuando hay imágenes', () => {
        mockUseDepartment.mockReturnValue({
            department: department as any,
            loading: false,
            error: null,
        })

        renderPage()

        expect(
            screen.getByRole('img', {
                name: /departamento palermo/i,
            }),
        ).toBeInTheDocument()

        expect(
            screen.getByText(
                '1 fotos',
            ),
        ).toBeInTheDocument()
    })

    it('muestra estado vacío cuando no hay imágenes', () => {
        mockUseDepartment.mockReturnValue({
            department: {
                ...department,
                imagenes: [],
            } as any,
            loading: false,
            error: null,
        })

        renderPage()

        expect(
            screen.getByText(
                /sin imágenes/i,
            ),
        ).toBeInTheDocument()
    })

    it('muestra consultas', () => {
        mockUseDepartment.mockReturnValue({
            department: department as any,
            loading: false,
            error: null,
        })

        renderPage()

        expect(
            screen.getByText(
                'Juan Pérez',
            ),
        ).toBeInTheDocument()

        expect(
            screen.getByText(
                'juan@test.com',
            ),
        ).toBeInTheDocument()

        expect(
            screen.getByText(
                '¿Sigue disponible?',
            ),
        ).toBeInTheDocument()
    })

    it('muestra estado vacío cuando no hay consultas', () => {
        mockUseDepartment.mockReturnValue({
            department: {
                ...department,
                consultas: [],
            } as any,
            loading: false,
            error: null,
        })

        renderPage()

        expect(
            screen.getByText(
                /todavía no tiene consultas/i,
            ),
        ).toBeInTheDocument()
    })

    it('muestra resumen de fotos y consultas', () => {
        mockUseDepartment.mockReturnValue({
            department: department as any,
            loading: false,
            error: null,
        })

        renderPage()

        expect(
            screen.getByText(
                '1 fotos',
            ),
        ).toBeInTheDocument()

        expect(
            screen.getByText(
                '1 consultas',
            ),
        ).toBeInTheDocument()
    })
})