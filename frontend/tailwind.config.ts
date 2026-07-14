import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#7c3aed",
        "primary-light": "#a78bfa",
        "primary-dark": "#5b21b6",
        secondary: "#06b6d4",
        accent: "#f472b6",
        "accent-light": "#fbcfe8",
        pop: "#fbbf24",
        "pop-light": "#fef3c7",
        success: "#10b981",
        warning: "#f59e0b",
        danger: "#ef4444",
        surface: "#f8fafc",
        "surface-dark": "#1e1b4b",
      },
      borderRadius: {
        xl: "1rem",
        "2xl": "1.5rem",
        "3xl": "2rem",
      },
      boxShadow: {
        pop: "0 4px 14px 0 rgba(124, 58, 237, 0.25)",
        "pop-lg": "0 10px 30px 0 rgba(124, 58, 237, 0.3)",
        glow: "0 0 20px rgba(6, 182, 212, 0.4)",
      },
      animation: {
        "bounce-slow": "bounce 2s infinite",
        "pulse-glow": "pulse-glow 2s ease-in-out infinite",
      },
      keyframes: {
        "pulse-glow": {
          "0%, 100%": { boxShadow: "0 0 5px rgba(124, 58, 237, 0.3)" },
          "50%": { boxShadow: "0 0 20px rgba(124, 58, 237, 0.6)" },
        },
      },
    },
  },
  plugins: [],
};
export default config;
