/* eslint-env node */
/* global module, require */
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: '#1877f2',
        accent: '#42b72a',
        warning: '#f7b928',
        danger: '#f02849',
        background: '#f0f2f5',
        surface: '#ffffff',
        outline: '#d7dce3',
        muted: '#606770',
      },
      fontFamily: {
        sans: ['Helvetica Neue', 'Arial', 'Segoe UI', 'system-ui', '-apple-system', 'sans-serif'],
      },
      boxShadow: {
        card: '0 4px 12px rgba(0, 0, 0, 0.08)',
        subtle: '0 1px 2px rgba(0, 0, 0, 0.06)',
      },
      borderRadius: {
        card: '16px',
      },
      spacing: {
        gutter: '1.5rem',
      },
      transitionTimingFunction: {
        soft: 'cubic-bezier(0.22, 1, 0.36, 1)',
      },
    },
  },
  plugins: [require('@tailwindcss/forms'), require('@tailwindcss/typography')],
}
