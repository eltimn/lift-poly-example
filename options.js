module.exports = {
  appDir: 'webapp',
  baseUrl: 'js/',
  mainConfigFile: 'webapp/js/common.js',
  dir: 'webapp-release',
  modules: [
    {
      name: 'common',
      include: [
        'jquery',
        'bootstrap'
      ]
    },
    {
      name: 'user/login',
      exclude: ['common']
    }
  ]
};
