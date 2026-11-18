import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import fs from 'fs';
import path from 'path';
import {defineConfig} from 'vite';
import {VitePWA} from 'vite-plugin-pwa';

export default defineConfig(() => {
  return {
    plugins: [
      react(),
      tailwindcss(),
      VitePWA({
        registerType: 'autoUpdate',
        includeAssets: ['icon.svg', 'apple-touch-icon.png', 'pwa-192x192.png', 'pwa-512x512.png'],
        manifest: {
          id: '/',
          name: 'Loupe Atelier',
          short_name: 'Loupe',
          description:
            'AI luxury macro pre-screening studio calculating authenticity probability scores from sequential macro photography.',
          theme_color: '#181715',
          background_color: '#F7F5F0',
          display: 'standalone',
          orientation: 'portrait-primary',
          start_url: '/',
          scope: '/',
          icons: [
            {
              src: '/pwa-192x192.png',
              sizes: '192x192',
              type: 'image/png',
              purpose: 'any',
            },
            {
              src: '/pwa-512x512.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'any',
            },
            {
              src: '/pwa-maskable-512x512.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'maskable',
            },
          ],
        },
        workbox: {
          globPatterns: ['**/*.{js,css,html,ico,png,svg,woff,woff2}'],
          navigateFallbackDenylist: [/^\/api\//],
        },
        devOptions: {
          enabled: true,
          type: 'module',
        },
      }),
      // When HMR is disabled in AI Studio, Vite 8 still injects /@vite/client and
      // opens a WebSocket to fallback port 24678. Swap the WebSocket transport for
      // a connected no-op transport so neither WebSocket errors nor
      // SendBeforeConnectError occur when plugins call import.meta.hot.send().
      {
        name: 'disable-hmr-client-ws',
        apply: 'serve',
        transform(code: string, id: string) {
          if (
            process.env.DISABLE_HMR === 'true' &&
            id.replace(/\\/g, '/').endsWith('/vite/dist/client/client.mjs')
          ) {
            const start = code.indexOf('const transport = normalizeModuleRunnerTransport(');
            const end = code.indexOf('let willUnload = false;', start);
            if (start !== -1 && end !== -1) {
              return (
                code.slice(0, start) +
                'const transport = normalizeModuleRunnerTransport({ async connect() {}, async disconnect() {}, send() {} });\n' +
                code.slice(end)
              );
            }
          }
        },
      },
      // Copies src/assets into dist/ so root-relative URLs like
      // "/src/assets/images/..." (e.g. generate_image output) still resolve
      // in production builds. Do not remove.
      {
        name: 'copy-unbundled-src-assets',
        apply: 'build',
        closeBundle(error?: Error): void {
          if (error) {
            return;
          }
          const distDir = path.resolve(__dirname, 'dist');
          const srcAssets = path.resolve(__dirname, 'src/assets');
          const distAssets = path.resolve(distDir, 'src/assets');
          if (fs.existsSync(distDir) && fs.existsSync(srcAssets)) {
            fs.cpSync(srcAssets, distAssets, {recursive: true, force: true});
          }
        },
      },
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, '.'),
      },
    },
    server: {
      // HMR is disabled in AI Studio via DISABLE_HMR env var.
      // Do not modify—file watching is disabled to prevent flickering during agent edits.
      hmr: process.env.DISABLE_HMR !== 'true',
      // Disable file watching when DISABLE_HMR is true to save CPU during agent edits.
      watch: process.env.DISABLE_HMR === 'true' ? null : {},
    },
  };
});
