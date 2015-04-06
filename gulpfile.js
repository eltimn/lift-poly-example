'use strict';

var gulp        = require('gulp'),
    addsrc      = require('gulp-add-src'),
    bower       = require('gulp-bower'),
    browserSync = require('browser-sync'),
    concat      = require('gulp-concat'),
    del         = require('del'),
    header      = require('gulp-header'),
    jshint      = require('gulp-jshint'),
    less        = require('gulp-less'),
    minifyCSS   = require('gulp-minify-css'),
    mocha       = require('gulp-mocha'),
    rename      = require('gulp-rename'),
    runSequence = require('run-sequence'),
    uglify      = require('gulp-uglify'),
    pkg         = require('./package.json'),
    vendor      = require('./vendor.json'),
    info        = '// <%= pkg.name %>@v<%= pkg.version %>\n',
    reload      = browserSync.reload;

var config = {
  target: 'target/gulp',
  bower_components: 'bower_components',
  less: {
    paths: [
      'bower_components/bootstrap/less'
    ]
  },
  sources: {
    js: {
      main: ['src/main/assets/js/App.js', 'src/main/assets/js/**/*.js'],
      test: ['src/test/assets/js/**/*.js']
    },
    less: 'src/main/assets/less/styles.less',
  }
}

vendor.js = vendor.js.map(function(it) { return config.bower_components+it; });
vendor.css = vendor.css.map(function(it) { return config.bower_components+it; });

// JavaScript
gulp.task('js:dev', function() {
  return gulp
    .src(config.sources.js.main)
    .pipe(jshint())
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(addsrc.prepend(vendor.js))
    .pipe(concat('scripts.js'))
    .pipe(gulp.dest(config.target))
});

gulp.task('js:pkg', function() {
  return gulp
    .src(config.sources.js.main)
    .pipe(uglify())
    .pipe(addsrc.prepend(vendor.js))
    .pipe(concat('scripts.js'))
    .pipe(header(info, { pkg : pkg }))
    .pipe(gulp.dest(config.target));
});

// create a task that ensures the `js` task is complete before
// reloading browsers
gulp.task('js:watch', ['test', 'js:dev'], browserSync.reload);

// LESS
gulp.task('less:dev', function() {
  return gulp
    .src(config.sources.less)
    .pipe(less(config.less))
    .pipe(addsrc.prepend(vendor.css))
    .pipe(concat('styles.css'))
    .pipe(gulp.dest(config.target))
    .pipe(reload({stream: true}));
});

gulp.task('less:pkg', function() {
  return gulp
    .src(config.sources.less)
    .pipe(less(config.less))
    .pipe(minifyCSS())
    .pipe(addsrc.prepend(vendor.css))
    .pipe(concat('styles.css'))
    .pipe(gulp.dest(config.target));
});

// default - package for production
gulp.task('default', function(callback) {
  runSequence(
    'clean',
    ['test', 'copyfonts', 'js:pkg', 'less:pkg'],
    callback);
});

// watch - for development
gulp.task('watch', ['watch:prepare'], function() {

  browserSync({
    proxy: 'localhost:8080',
    open: false
  });

  gulp.watch(config.sources.js.main, ['js:watch']);
  gulp.watch(config.sources.js.test, ['test']);
  gulp.watch('src/main/assets/less/**/*.less', ['less:dev']);
  gulp.watch('src/main/webapp/**/*.html').on('change', reload);
  gulp.watch('target/browser-sync.txt').on('change', reload);
  gulp.watch(process.env['HOME']+'/.jrebel/javarebel.stats').on('change', reload);
});

gulp.task('watch:prepare', function(callback) {
  runSequence(
    'clean',
    ['test', 'copyfonts', 'js:dev', 'less:dev'],
    callback);
});

 // test
gulp.task('test', function() {
  return gulp
    .src(config.sources.js.test)
    .pipe(jshint())
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(mocha());
});

// misc
gulp.task('clean', function(callback) {
  del([
    config.target,
    'target/gulp-resources'
  ], callback);
});

gulp.task('bower', function() {
  return bower();
});

gulp.task('copyfonts', function() {
  gulp
    .src(config.bower_components+'/font-awesome/fonts/*')
    .pipe(gulp.dest(config.target+'/fonts'));
});
