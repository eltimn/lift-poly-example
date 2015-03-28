var expect = require('chai').expect,
    app = require('../../../main/assets/js/App').App;

describe('App object', function() {
  'use strict';

  it('initialize with settings', function() {
    app.init({x: 'hola'});
    expect(app.settings.x).to.equal('hola');
  });
  it('create a namespace', function() {
    app.namespace('views.test');
    expect(app.views).to.have.property('test');
  });
});
