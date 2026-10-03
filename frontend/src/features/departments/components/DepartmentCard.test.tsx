import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { DepartmentCard } from './DepartmentCard'
import type { DepartmentListItem } from '../types/department'

const mockDepartment: DepartmentListItem = {
  id: 1,
  titulo: 'Departamento moderno',
  precio: 150000,
  moneda: 'USD',
  metrosCuadrados: 80,
  disponible: true,
  cantidadImagenes: 3,
  cantidadConsultas: 10,
  imagenPrincipal: 'https://example.com/image.jpg',
}

function renderWithRouter(component: React.ReactElement) {
  return render(<MemoryRouter>{component}</MemoryRouter>)
}

describe('DepartmentCard', () => {
  it('renderiza título', () => {
    renderWithRouter(<DepartmentCard department={mockDepartment} />)
    expect(screen.getByText('Departamento moderno')).toBeInTheDocument()
  })

  it('renderiza precio', () => {
    renderWithRouter(<DepartmentCard department={mockDepartment} />)
    expect(screen.getByText(/150\.000/)).toBeInTheDocument()
  })

  it('renderiza estado', () => {
    renderWithRouter(<DepartmentCard department={mockDepartment} />)
    expect(screen.getByText('Disponible')).toBeInTheDocument()
  })

  it('renderiza superficie', () => {
    renderWithRouter(<DepartmentCard department={mockDepartment} />)
    expect(screen.getByText('80 m²')).toBeInTheDocument()
  })

  it('renderiza cantidad de fotos y consultas', () => {
    renderWithRouter(<DepartmentCard department={mockDepartment} />)
    expect(screen.getByText('3')).toBeInTheDocument()
    expect(screen.getByText('10')).toBeInTheDocument()
  })

  it('muestra fallback "Sin foto" cuando imagenPrincipal es null', () => {
    const department = { ...mockDepartment, imagenPrincipal: null }
    renderWithRouter(<DepartmentCard department={department} />)
    expect(screen.getByText('Sin foto')).toBeInTheDocument()
  })

  it('el link "Ver detalle" apunta a /departamentos/{id}', () => {
    renderWithRouter(<DepartmentCard department={mockDepartment} />)
    const link = screen.getAllByRole('link', { name: /ver detalle/i })
    expect(link[0]).toHaveAttribute('href', '/departamentos/1')
  })
})
