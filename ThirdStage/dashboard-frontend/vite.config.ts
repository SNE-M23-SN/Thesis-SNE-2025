import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  css: {
    postcss: './postcss.config.cjs', // Explicitly link PostCSS config
  },
  server: {
    host: '0.0.0.0', // Allow external connections
    port: 3000,
    hmr: {
      port: 24678, // HMR port for Docker
    },
    watch: {
      usePolling: true, // Enable polling for file changes in Docker
      interval: 1000,
    },
  },
  preview: {
    host: '0.0.0.0', // Allow external connections for preview
    port: 3000,
  },
})