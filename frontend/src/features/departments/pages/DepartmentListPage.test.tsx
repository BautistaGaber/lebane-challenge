import {
    render,
    screen,
} from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {
    MemoryRouter,
} from 'react-router-dom'

import {useDepartments} from '../hooks/useDepartments'
import {DepartmentListPage} from './DepartmentListPage.tsx'

jest.mock('../hooks/useDepartments')

jest.mock(
    '../components/DepartmentFilters',
    () => ({
        DepartmentFilters: ({
                                onApply,
                                onClear,
                            }: {
            onApply: () => void
            onClear: () => void
        }) => (
            <div>
                <button
                    type="button"
                    onClick={onApply}
                >
                    Aplicar filtros
                </button>

                <button
                    type="button"
                    onClick={onClear}
                >
                    Limpiar filtros
                </button>
            </div>
        ),
    }),
)

jest.mock(
    '../components/DepartmentTable',
    () => ({
        DepartmentTable: ({
                              departments,
                          }: {
            departments: Array<{id: number}>
        }) => (
            <div>
                Tabla departamentos: {departments.length}
            </div>
        ),
    }),
)

const mockUseDepartments =
    useDepartments as jest.MockedFunction<
        typeof useDepartments
    >

function renderPage() {
    return render(
        <MemoryRouter>
            <DepartmentListPage />
        </MemoryRouter>,
    )
}

describe('DepartmentsListPage', () => {
    beforeEach(() => {
        jest.clearAllMocks()
    })

    it('muestra loading', () => {
        mockUseDepartments.mockReturnValue({
            departments: [],
            loading: true,
            error: null,
            pageInfo: null,
        })

        renderPage()

        expect(
            screen.getByText(
                /cargando departamentos/i,
            ),
        ).toBeInTheDocument()
    })

    it('muestra error', () => {
        mockUseDepartments.mockReturnValue({
            departments: [],
            loading: false,
            error: 'Error al cargar',
            pageInfo: null,
        })

        renderPage()

        expect(
            screen.getByText(
                'Error al cargar',
            ),
        ).toBeInTheDocument()
    })

    it('renderiza la tabla cuando hay datos', () => {
        mockUseDepartments.mockReturnValue({
            departments: [
                {
                    id: 1,
                } as any,
            ],
            loading: false,
            error: null,
            pageInfo: null,
        })

        renderPage()

        expect(
            screen.getByText(
                /tabla departamentos: 1/i,
            ),
        ).toBeInTheDocument()
    })

    it('muestra link para crear un departamento', () => {
        mockUseDepartments.mockReturnValue({
            departments: [],
            loading: false,
            error: null,
            pageInfo: null,
        })

        renderPage()

        expect(
            screen.getByRole('link', {
                name: /nuevo departamento/i,
            }),
        ).toHaveAttribute(
            'href',
            '/departamentos/nuevo',
        )
    })

    it('aplica filtros y mantiene la página inicial', async () => {
        const user = userEvent.setup()

        mockUseDepartments.mockReturnValue({
            departments: [],
            loading: false,
            error: null,
            pageInfo: {
                pagina: 0,
                totalPaginas: 2,
                totalElementos: 20,
                primera: true,
                ultima: false,
            } as any,
        })

        renderPage()

        await user.click(
            screen.getByRole('button', {
                name: /aplicar filtros/i,
            }),
        )

        expect(
            mockUseDepartments,
        ).toHaveBeenLastCalledWith(
            expect.any(Object),
            0,
            10,
        )
    })

    it('limpia filtros y vuelve a la página 0', async () => {
        const user = userEvent.setup()

        mockUseDepartments.mockReturnValue({
            departments: [],
            loading: false,
            error: null,
            pageInfo: {
                pagina: 0,
                totalPaginas: 2,
                totalElementos: 20,
                primera: true,
                ultima: false,
            } as any,
        })

        renderPage()

        await user.click(
            screen.getByRole('button', {
                name: /limpiar filtros/i,
            }),
        )

        expect(
            mockUseDepartments,
        ).toHaveBeenLastCalledWith(
            {},
            0,
            10,
        )
    })

    it('avanza a la página siguiente', async () => {
        const user = userEvent.setup()

        mockUseDepartments.mockReturnValue({
            departments: [],
            loading: false,
            error: null,
            pageInfo: {
                pagina: 0,
                totalPaginas: 3,
                totalElementos: 30,
                primera: true,
                ultima: false,
            } as any,
        })

        renderPage()

        await user.click(
            screen.getByRole('button', {
                name: /siguiente/i,
            }),
        )

        expect(
            mockUseDepartments,
        ).toHaveBeenLastCalledWith(
            expect.any(Object),
            1,
            10,
        )
    })

    it('retrocede a la página anterior', async () => {
        const user = userEvent.setup()

        mockUseDepartments.mockImplementation(
            (_filters, page) => ({
                departments: [],
                loading: false,
                error: null,
                pageInfo: {
                    pagina: page,
                    totalPaginas: 3,
                    totalElementos: 30,
                    primera: page === 0,
                    ultima: page === 2,
                } as any,
            }),
        )

        renderPage()

        await user.click(
            screen.getByRole('button', {
                name: /siguiente/i,
            }),
        )

        await user.click(
            screen.getByRole('button', {
                name: /anterior/i,
            }),
        )

        expect(
            mockUseDepartments,
        ).toHaveBeenLastCalledWith(
            expect.any(Object),
            0,
            10,
        )
    })
})