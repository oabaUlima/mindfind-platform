/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        mindfind: {
          teal: '#0F766E',
          'teal-light': '#14B8A6',
          'teal-soft': '#CCFBF1',
          lavender: '#EDE9FE',
          'lavender-soft': '#F5F3FF',
          'lavender-border': '#DDD6FE',
          gray: '#F8FAFC',
          'gray-muted': '#F1F5F9',
        },
      },
    },
  },
  plugins: [],
};
