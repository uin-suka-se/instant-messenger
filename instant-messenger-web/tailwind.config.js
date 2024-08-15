/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts,scss}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Poppins', 'sans-serif'],
      },
    },
    content: {
      'you': '"You: "',
      'else': '"Someone Else: "',
    },
    backgroundImage: {
      'minimalist': "url('/assets/img/image.png')",
    },
  },
  plugins: [],
}
