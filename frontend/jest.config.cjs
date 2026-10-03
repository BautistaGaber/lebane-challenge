module.exports = {
  testEnvironment: 'jsdom',

  setupFilesAfterEnv: [
    '<rootDir>/src/test/setup.ts',
  ],

  transform: {
    '^.+\\.(ts|tsx)$': 'babel-jest',
  },

  testMatch: [
    '<rootDir>/src/**/*.test.{ts,tsx}',
  ],

  clearMocks: true,

  moduleNameMapper: {
    '^\\.\\./config/environment$':
        '<rootDir>/src/test/mocks/environment.ts',

    '\\.(css|less|sass|scss)$':
        'identity-obj-proxy',
  },
}