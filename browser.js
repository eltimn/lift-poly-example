
var browserSync = require("browser-sync");

var config = {
  files: [
    'target/web/dist/*.css',
    // 'target/web/dist/*.js',
    'src/main/webapp/*.html',
    // 'target/browser-sync.txt',
    process.env['HOME'] + '/.jrebel/javarebel.stats'
  ],
  proxy: {
    target: 'localhost:8080'
  },
  open: false
};

browserSync(config);
