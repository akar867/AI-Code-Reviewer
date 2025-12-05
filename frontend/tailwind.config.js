/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        risk: {
          low: '#16a34a',
          medium: '#f59e0b',
          high: '#dc2626'
        }
      }
    }
  },
  plugins: []
};
