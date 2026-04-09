module.exports = {
  presets: [
    '@vue/cli-plugin-babel/preset'
  ],
  plugins: [
    // 添加对可选链操作符的支持
    '@babel/plugin-transform-optional-chaining',
    // 添加对空值合并操作符的支持
    '@babel/plugin-transform-nullish-coalescing-operator'
  ]
}