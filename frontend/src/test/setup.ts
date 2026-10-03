import '@testing-library/jest-dom'

import {
    TextDecoder,
    TextEncoder,
} from 'node:util'

import {
    ReadableStream,
} from 'node:stream/web'

Object.defineProperties(globalThis, {
    TextEncoder: {
        value: TextEncoder,
        configurable: true,
    },

    TextDecoder: {
        value: TextDecoder,
        configurable: true,
    },

    ReadableStream: {
        value: ReadableStream,
        configurable: true,
    },
})