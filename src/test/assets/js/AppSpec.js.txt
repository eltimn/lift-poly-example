var assert = require("assert"),
    app = require("./App").App;

describe("App object", function() {
  it("initialize with settings", function() {
    app.init({x: "hola"});
    assert.equal(app.settings.x, "hola");
  });
  it("create a namespace", function() {
    app.namespace("views.test");
    assert(app.views.hasOwnProperty("test"));
  });
});
