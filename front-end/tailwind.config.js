// tailwind.config.js
/** @type {import('tailwindcss').Config} */
export default {
  // Esta seção 'content' corrige o 'WARN' do seu log
  content: [
    "./components/**/*.{js,vue,ts}",
    "./layouts/**/*.vue",
    "./pages/**/*.vue",
    "./plugins/**/*.{js,ts}",
    "./app.vue", // Assumindo que você tem um app.vue
    "./nuxt.config.{js,ts}",
  ],
  
  theme: {
    extend: {
      // Mapeando suas variáveis CSS do :root original
      colors: {
        primary: {
          DEFAULT: '#2563eb', // var(--primary)
          dark: '#1e40af',    // var(--primary-dark)
        },
        success: '#10b981', // var(--success)
        warning: '#f59e0b', // var(--warning)
        danger: '#ef4444',  // var(--danger)
        gray: {
          50: '#f9fafb',
          100: '#f3f4f6',
          200: '#e5e7eb',
          300: '#d1d5db',
          600: '#4b5563',
          700: '#374151',
          800: '#1f2937',
          900: '#111827',
        }
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'Helvetica Neue', 'Arial', 'sans-serif'],
      },
      borderRadius: {
        'lg': '12px', // Você usou 12px
        'xl': '16px', // Você usou 16px
      }
    },
  },
  plugins: [],
}