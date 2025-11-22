/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./src/**/*.{html,ts,js,jsx,tsx}",
    "./src/app/**/*.{html,ts}",
    "./src/app/**/!(*.spec|*.test).{ts,html}"
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}