const CompressionWebpackPlugin = require('compression-webpack-plugin')

module.exports = {
  // 基本路径
  publicPath: process.env.NODE_ENV === 'production' ? '/' : '/',
  
  // 输出文件目录
  outputDir: 'dist',
  
  // 静态资源目录
  assetsDir: 'static',
  
  // 生产环境是否生成 sourceMap 文件
  productionSourceMap: false,
  
  // 服务器配置
  devServer: {
    port: 8080,
    host: '0.0.0.0',
    https: false,
    hotOnly: false,
    // 代理配置 - 修正代理目标指向正确的后端服务端口和上下文路径
    proxy: {
      '/api': {
        target: 'http://localhost:8100',  // 后端服务运行在8100端口
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'  // 保持/api前缀，因为后端的context-path也是/api
        }
      },
      '/upload': {
        target: 'http://localhost:8099',  // 后端服务运行在8099端口
        changeOrigin: true
      }
    },
    // 自动打开浏览器
    open: false,
    // 覆盖层设置
    overlay: {
      warnings: false,
      errors: true
    }
  },
  
  // webpack配置
  configureWebpack: {
    // 解决警告
    performance: {
      hints: false
    },
    // 配置别名
    resolve: {
      alias: {
        '@': require('path').resolve(__dirname, 'src')
      }
    },
    plugins: [
      new CompressionWebpackPlugin({
        filename: '[path][base].gz',
        algorithm: 'gzip',
        test: /\.(js|css|json|txt|html|ico|svg)(\?.*)?$/i,
        threshold: 10240,
        minRatio: 0.8
      })
    ]
  },
  
  // 链式webpack配置
  chainWebpack: config => {
    // 设置 svg-sprite-loader
    config.module
      .rule('svg')
      .exclude.add(require('path').resolve(__dirname, 'src/assets/icons'))
      .end()
    
    config.module
      .rule('icons')
      .test(/\.svg$/)
      .include.add(require('path').resolve(__dirname, 'src/assets/icons'))
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
      .end()
  },
  
  // 全局样式
  css: {
    // 是否使用css分离插件 ExtractTextPlugin
    extract: process.env.NODE_ENV === 'production',
    // 开启 CSS source maps?
    sourceMap: false,
    // css预设器配置项
    loaderOptions: {
      less: {
        javascriptEnabled: true
      }
    }
  },
  
  // 第三方插件配置
  pluginOptions: {
    // 可以在这里配置其他插件
  }
}