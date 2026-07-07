/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    './app/**/*.{ts,tsx}',
    './components/**/*.{ts,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#fff4ef',
          100: '#ffe6d9',
          200: '#ffc8ac',
          300: '#ffa176',
          400: '#ff7a44',
          500: '#f4611f',
          600: '#d94a12',
          700: '#b3390e',
          800: '#8f2f10',
          900: '#742a10',
        },
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-in-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(4px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
};
