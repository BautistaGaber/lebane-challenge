import {useState} from 'react'

type ImageWithFallbackProps = {
    src: string | null
    alt: string
}

export function ImageWithFallback({src,alt}: ImageWithFallbackProps) {
    const [hasError, setHasError] = useState(false)

    if (!src || hasError) {
        return (
            <div
                className="flex h-12 w-16 items-center justify-center rounded-lg bg-slate-100 text-center text-xs text-slate-400">
                Sin foto
            </div>
        )
    }

    return (
        <img
            src={src}
            alt={alt}
            className="h-12 w-16 rounded-lg object-cover"
            onError={() => setHasError(true)}
        />
    )
}