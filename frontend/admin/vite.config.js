import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 管理后台Vite配置
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 后端接口代理
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 上传文件访问代理 静态资源在后端context-path /api 下
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => '/api' + path
      }
    }
  }
})
