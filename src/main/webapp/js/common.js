requirejs.config({
  baseUrl: './js',
  paths: {
    "jquery": "/lib/jquery/jquery",
    "bootstrap": "/lib/bootstrap/js"
  },
  shim: {
    "bootstrap": ["jquery"]
  }
});
