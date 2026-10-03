import {
  act,
  fireEvent,
  render,
  screen,
} from '@testing-library/react'
import {useState} from 'react'

import {AddressAutocomplete} from './AddressAutocomplete'
import {searchAddresses} from '../services/geoapifyService'
import type {AddressSuggestion} from '../types/geoapify'

jest.mock(
    '../services/geoapifyService',
    () => ({
      searchAddresses: jest.fn(),
    }),
)

const mockSearchAddresses =
    searchAddresses as jest.MockedFunction<
        typeof searchAddresses
    >

const suggestion: AddressSuggestion = {
  formatted:
      'Av. Corrientes 1234, Buenos Aires',
  latitude: -34.6037,
  longitude: -58.3816,
  city: 'Buenos Aires',
  state: 'Buenos Aires',
  postcode: 'C1043',
}

type ControlledAutocompleteProps = {
  onChangeSpy?: jest.Mock
  onSelect?: jest.Mock
}

function ControlledAutocomplete({
                                  onChangeSpy,
                                  onSelect = jest.fn(),
                                }: ControlledAutocompleteProps) {
  const [value, setValue] = useState('')

  function handleChange(nextValue: string) {
    setValue(nextValue)
    onChangeSpy?.(nextValue)
  }

  return (
      <AddressAutocomplete
          value={value}
          onChange={handleChange}
          onSelect={onSelect}
      />
  )
}

describe('AddressAutocomplete', () => {
  beforeEach(() => {
    mockSearchAddresses.mockReset()
  })

  afterEach(() => {
    jest.useRealTimers()
  })

  it('no llama a la API con menos de 3 caracteres', () => {
    render(<ControlledAutocomplete />)

    fireEvent.change(
        screen.getByRole('textbox'),
        {
          target: {
            value: 'av',
          },
        },
    )

    expect(
        mockSearchAddresses,
    ).not.toHaveBeenCalled()
  })

  it('aplica debounce', async () => {
    jest.useFakeTimers()

    mockSearchAddresses.mockResolvedValue([])

    render(<ControlledAutocomplete />)

    fireEvent.change(
        screen.getByRole('textbox'),
        {
          target: {
            value: 'avenida',
          },
        },
    )

    expect(
        mockSearchAddresses,
    ).not.toHaveBeenCalled()

    await act(async () => {
      jest.advanceTimersByTime(349)
    })

    expect(
        mockSearchAddresses,
    ).not.toHaveBeenCalled()

    await act(async () => {
      jest.advanceTimersByTime(1)
    })

    expect(
        mockSearchAddresses,
    ).toHaveBeenCalledTimes(1)
  })

  it('llama searchAddresses cuando corresponde', async () => {
    jest.useFakeTimers()

    mockSearchAddresses.mockResolvedValue([])

    render(<ControlledAutocomplete />)

    fireEvent.change(
        screen.getByRole('textbox'),
        {
          target: {
            value: 'avenida',
          },
        },
    )

    await act(async () => {
      jest.advanceTimersByTime(350)
    })

    expect(
        mockSearchAddresses,
    ).toHaveBeenCalledWith(
        'avenida',
        expect.any(AbortSignal),
    )
  })

  it('muestra loading', async () => {
    jest.useFakeTimers()

    let resolveSearch:
        | ((value: AddressSuggestion[]) => void)
        | undefined

    mockSearchAddresses.mockImplementation(
        () =>
            new Promise((resolve) => {
              resolveSearch = resolve
            }),
    )

    render(<ControlledAutocomplete />)

    fireEvent.change(
        screen.getByRole('textbox'),
        {
          target: {
            value: 'avenida',
          },
        },
    )

    await act(async () => {
      jest.advanceTimersByTime(350)
    })

    expect(
        screen.getByText(
            /buscando direcciones/i,
        ),
    ).toBeInTheDocument()

    await act(async () => {
      resolveSearch?.([])
    })
  })

  it('muestra sugerencias', async () => {
    jest.useFakeTimers()

    mockSearchAddresses.mockResolvedValue([
      suggestion,
    ])

    render(<ControlledAutocomplete />)

    fireEvent.change(
        screen.getByRole('textbox'),
        {
          target: {
            value: 'corrientes',
          },
        },
    )

    await act(async () => {
      jest.advanceTimersByTime(350)
    })

    expect(
        await screen.findByText(
            suggestion.formatted,
        ),
    ).toBeInTheDocument()
  })

  it('al seleccionar una sugerencia llama onChange y onSelect', async () => {
    jest.useFakeTimers()

    const onChange = jest.fn()
    const onSelect = jest.fn()

    mockSearchAddresses.mockResolvedValue([
      suggestion,
    ])

    render(
        <ControlledAutocomplete
            onChangeSpy={onChange}
            onSelect={onSelect}
        />,
    )

    fireEvent.change(
        screen.getByRole('textbox'),
        {
          target: {
            value: 'corrientes',
          },
        },
    )

    await act(async () => {
      jest.advanceTimersByTime(350)
    })

    fireEvent.click(
        await screen.findByText(
            suggestion.formatted,
        ),
    )

    expect(onChange).toHaveBeenLastCalledWith(
        suggestion.formatted,
    )

    expect(onSelect).toHaveBeenCalledWith(
        suggestion,
    )
  })

  it('muestra error si searchAddresses falla', async () => {
    jest.useFakeTimers()

    mockSearchAddresses.mockRejectedValue(
        new Error('Network error'),
    )

    render(<ControlledAutocomplete />)

    fireEvent.change(
        screen.getByRole('textbox'),
        {
          target: {
            value: 'corrientes',
          },
        },
    )

    await act(async () => {
      jest.advanceTimersByTime(350)

      await Promise.resolve()
    })

    expect(
        await screen.findByText(
            /no se pudieron buscar direcciones/i,
        ),
    ).toBeInTheDocument()
  })

  it('maneja AbortError sin mostrar error', async () => {
    jest.useFakeTimers()

    mockSearchAddresses.mockRejectedValue(
        new DOMException(
            'Aborted',
            'AbortError',
        ),
    )

    render(<ControlledAutocomplete />)

    fireEvent.change(
        screen.getByRole('textbox'),
        {
          target: {
            value: 'corrientes',
          },
        },
    )

    await act(async () => {
      jest.advanceTimersByTime(350)

      await Promise.resolve()
    })

    expect(
        screen.queryByText(
            /no se pudieron buscar direcciones/i,
        ),
    ).not.toBeInTheDocument()
  })
})