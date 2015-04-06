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

// require.config({
//   paths: {
//     "common": "/js/common"
//     "jquery": "/lib/jquery",
//   },
//   shim: {}
// });
/*
// Start the main app logic.
requirejs(['jquery', 'canvas', 'app/sub'],
function   ($,        canvas,   sub) {
  //jQuery, canvas and the app/sub module are all
  //loaded and can be used here now.
});*/

// require(["alerter"], function(alerter) {
//   // alerter("hola")
// });
