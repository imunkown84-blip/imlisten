/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    './app/**/*.{js,ts,jsx,tsx}',
    './components/**/*.{js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        retro: {
          bg: '#f7f4ee',
          card: '#fffdf9',
          ink: '#1c1917',
          muted: '#78716c',
          border: '#292524',
          accent: '#c85a32',
          accentHover: '#b04923',
          yellow: '#f59e0b',
          sage: '#57534e',
          paper: '#eae5d9'
        },
        brand: {
          50: '#fff7ed',
          100: '#ffedd5',
          500: '#c85a32',
          600: '#b04923',
          700: '#8c3516'
        }
      },
      fontFamily: {
        mono: ['Space Mono', 'Courier New', 'monospace'],
        serif: ['Instrument Serif', 'Georgia', 'serif'],
        sans: ['Space Grotesk', 'system-ui', 'sans-serif']
      },
      boxShadow: {
        'retro': '3px 3px 0px 0px #1c1917',
        'retro-sm': '2px 2px 0px 0px #1c1917',
        'retro-lg': '5px 5px 0px 0px #1c1917'
      }
    }
  },
  plugins: []
};

