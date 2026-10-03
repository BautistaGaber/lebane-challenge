import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { DepartmentFilters } from './DepartmentFilters'
import type { DepartmentFilters as Filters } from '../types/department'

describe('DepartmentFilters', () => {
  const mockFilters: Filters = {}

  it('cambiar estado', async () => {
    const user = userEvent.setup()
    const onChange = jest.fn()
    const onApply = jest.fn()
    const onClear = jest.fn()
    render(<DepartmentFilters filters={mockFilters} onChange={onChange} onApply={onApply} onClear={onClear} />)
    const select = screen.getByLabelText(/estado/i)
    await user.selectOptions(select, 'true')
    expect(onChange).toHaveBeenCalled()
  })

  it('cambiar precio mínimo', async () => {
    const user = userEvent.setup()
    const onChange = jest.fn()
    render(<DepartmentFilters filters={mockFilters} onChange={onChange} onApply={jest.fn()} onClear={jest.fn()} />)
    const input = screen.getByLabelText(/precio mínimo/i)
    await user.type(input, '1000')
    expect(onChange).toHaveBeenCalled()
  })

  it('cambiar precio máximo', async () => {
    const user = userEvent.setup()
    const onChange = jest.fn()
    render(<DepartmentFilters filters={mockFilters} onChange={onChange} onApply={jest.fn()} onClear={jest.fn()} />)
    const input = screen.getByLabelText(/precio máximo/i)
    await user.type(input, '2000')
    expect(onChange).toHaveBeenCalled()
  })

  it('cambiar m² mínimo', async () => {
    const user = userEvent.setup()
    const onChange = jest.fn()
    render(<DepartmentFilters filters={mockFilters} onChange={onChange} onApply={jest.fn()} onClear={jest.fn()} />)
    const input = screen.getByLabelText(/m² mínimos/i)
    await user.type(input, '50')
    expect(onChange).toHaveBeenCalled()
  })

  it('cambiar m² máximo', async () => {
    const user = userEvent.setup()
    const onChange = jest.fn()
    render(<DepartmentFilters filters={mockFilters} onChange={onChange} onApply={jest.fn()} onClear={jest.fn()} />)
    const input = screen.getByLabelText(/m² máximos/i)
    await user.type(input, '100')
    expect(onChange).toHaveBeenCalled()
  })

  it('click en "Aplicar filtros" ejecuta onApply', async () => {
    const user = userEvent.setup()
    const onApply = jest.fn()
    render(<DepartmentFilters filters={mockFilters} onChange={jest.fn()} onApply={onApply} onClear={jest.fn()} />)
    await user.click(screen.getByRole('button', { name: /aplicar filtros/i }))
    expect(onApply).toHaveBeenCalled()
  })

  it('click en "Limpiar" ejecuta onClear', async () => {
    const user = userEvent.setup()
    const onClear = jest.fn()
    render(<DepartmentFilters filters={mockFilters} onChange={jest.fn()} onApply={jest.fn()} onClear={onClear} />)
    await user.click(screen.getByRole('button', { name: /limpiar/i }))
    expect(onClear).toHaveBeenCalled()
  })
})
