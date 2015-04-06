define(['jquery'], function($) {
  'use strict';

  var timeoutRtn = 0;

  function startMonitor() {
    timeoutRtn = setTimeout(monitorPassword, 1000);
  };

   // Monitor the password input field and select the yes_password
   // radio if something has been entered.

  function monitorPassword() {
    if (!$("#yes_password").attr("checked")) {
      var pwd = $("#id_password").val();
      if (pwd.length > 0) {
        $("#yes_password").attr("checked", "checked");
      }
      else {
        startMonitor();
      }
    }
  };

  startMonitor();
});

// require(["/js/common.js", "jquery"], function(common, $) {
//   $.each([ 1, 2, 3 ], function(index, number) {
//     console.log( number );
//   });
// });

// require(["jquery"], function($) {
//   $.each([ 1, 2, 3 ], function(index, number) {
//     console.log( number );
//   });
// });


/*(function(exports) {
  exports.App.namespace("views.user");
  exports.App.views.user.Login = (function($) {
    "use strict";

    var inst = {};

    inst.timeoutRtn = 0;

    inst.init = function() {
      inst.startMonitor();
      throw "Error2";
    };

    inst.startMonitor = function() {
      inst.timeoutRtn = setTimeout(App.views.user.Login.monitorPassword, 1000);
    };

     // Monitor the password input field and select the yes_password
     // radio if something has been entered.

    inst.monitorPassword = function() {
      if (!$("#yes_password").attr("checked")) {
        var pwd = $("#id_password").val();
        if (pwd.length > 0) {
          $("#yes_password").attr("checked", "checked");
        }
        else {
          inst.startMonitor();
        }
      }
    };

    return inst;
  }(jQuery));

})(this);*/
