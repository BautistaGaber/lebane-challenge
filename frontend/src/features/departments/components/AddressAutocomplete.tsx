import { useEffect, useState } from 'react'
import {
    searchAddresses,
} from '../services/geoapifyService'
import type {
    AddressSuggestion,
} from '../types/geoapify'

type AddressAutocompleteProps = {
    value: string
    onChange: (value: string) => void
    onSelect: (address: AddressSuggestion) => void
    placeholder?: string
}

export function AddressAutocomplete({value, onChange, onSelect, placeholder = 'Ingresá una dirección'}: AddressAutocompleteProps) {
    const [suggestions, setSuggestions] = useState<AddressSuggestion[]>([])

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [isOpen, setIsOpen] = useState(false)

    const [selectedValue, setSelectedValue] = useState<string | null>(value || null)

    useEffect(() => {
        const query = value.trim()

        if (query.length < 3 || query === selectedValue) {
            setSuggestions([])
            setIsOpen(false)
            return
        }

        const controller = new AbortController()

        const timeoutId = window.setTimeout(async () => {
            setLoading(true)
            setError(null)

            try {
                const results = await searchAddresses(query, controller.signal,)

                setSuggestions(results)
                setIsOpen(true)
            } catch (error) {
                if (error instanceof DOMException && error.name === 'AbortError')
                {
                    return
                }

                setSuggestions([])
                setIsOpen(false)
                setError('No se pudieron buscar direcciones.',)
            } finally {
                setLoading(false)
            }
        }, 350)

        return () => {
            window.clearTimeout(timeoutId)
            controller.abort()
        }
    }, [value, selectedValue])

    function handleChange(event: React.ChangeEvent<HTMLInputElement>)
    {
        const nextValue = event.target.value

        if (nextValue !== selectedValue) {
            setSelectedValue(null)
        }

        onChange(nextValue)
    }

    function handleSelect(suggestion: AddressSuggestion) {
        setSelectedValue(suggestion.formatted)

        onChange(suggestion.formatted)
        onSelect(suggestion)

        setSuggestions([])
        setIsOpen(false)
        setError(null)
    }

    return (
        <div className="relative">
            <input
                type="text"
                value={value}
                onChange={handleChange}
                onFocus={() => {
                    if (suggestions.length > 0) {
                        setIsOpen(true)
                    }
                }}
                placeholder={placeholder}
                autoComplete="off"
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-2 focus:ring-slate-200"
            />

            {loading && (
                <p className="mt-2 text-xs text-slate-500">
                    Buscando direcciones...
                </p>
            )}

            {error && (
                <p className="mt-2 text-xs text-red-600">
                    {error}
                </p>
            )}

            {isOpen && suggestions.length > 0 && (
                <div
                    role="listbox"
                    className="absolute z-20 mt-2 max-h-72 w-full overflow-y-auto rounded-lg border border-slate-200 bg-white shadow-lg"
                >
                    {suggestions.map((suggestion, index) => (
                        <button
                            key={`${suggestion.formatted}-${index}`}
                            type="button"
                            onMouseDown={(event) =>
                                event.preventDefault()
                            }
                            onClick={() =>
                                handleSelect(suggestion)
                            }
                            className="block w-full border-b border-slate-100 px-4 py-3 text-left text-sm text-slate-700 transition last:border-b-0 hover:bg-slate-50"
                        >
                            <span className="font-medium">
                                {suggestion.formatted}
                            </span>

                            {suggestion.city && (
                                <span className="mt-1 block text-xs text-slate-400">
                                    {[
                                        suggestion.city,
                                        suggestion.state,
                                    ]
                                        .filter(Boolean)
                                        .join(', ')}
                                </span>
                            )}
                        </button>
                    ))}
                </div>
            )}
        </div>
    )
}