gulp = require 'gulp'
browserify = require 'browserify'
stylus = require 'gulp-stylus'
source = require 'vinyl-source-stream'
sourcemaps = require 'gulp-sourcemaps'
concatCss = require 'gulp-concat-css'
gutil = require 'gulp-util'
bootstrap = require 'bootstrap-styl'

gulp.task 'css', ->
  gulp.src 'resources/stylus/*.styl'
    .pipe stylus use: bootstrap()
    .on 'error', (err) -> console.log err.toString(); @emit 'end'
    .pipe concatCss 'styles.css'
    .pipe gulp.dest 'resources/public/css'

gulp.task 'js', ->
  browserify
    entries: ['./resources/coffee/main.coffee']
    extensions: ['.coffee', '.js']
  .transform 'coffeeify'
  .transform 'debowerify'
  # .transform 'uglifyify'
  .bundle()
  .on 'error', (err) -> console.log err.toString(); @emit 'end'
  .pipe source 'scripts.js'
  .pipe gulp.dest 'resources/public/js'

gulp.task 'watch', ->
  gulp.watch './resources/stylus/*.styl', ['css']
  gulp.watch './resources/coffee/*.coffee', ['js']

gulp.task 'default', ['css', 'js']
