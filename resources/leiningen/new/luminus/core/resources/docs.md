<h2 class="alert alert-success">Congratulations, your <a class="alert-link" href="http://luminusweb.net">Luminus</a> site is ready!</h2>

This page will help guide you through the first steps of building your site.

#### Why are you seeing this page?

The `home-routes` handler in the `<<project-ns>>.routes.home` namespace
defines the route that invokes the `home-page` function whenever an HTTP
request is made to the `/` URI using the `GET` method.

<% if cljs %>
The route table relies on functions `home-page` function in the
`<<project-ns>>.views.static-pages` namespace to render the HTML for this page.

The `home-page` function will in turn call the `<<project-ns>>.views.layout/base-layout` function
to render the layout.

The page contains a link to the compiled ClojureScript found in the `target/cljsbuild/public` folder
(**TODO**):

The rest of this page is rendered by ClojureScript found in the `src/cljs/<<sanitized>>/core.cljs` file.

<% else %>
The route table relies on functions `home-page` and `about-page` in the
`<<project-ns>>.views.static-pages` namespace to render the two pages the
site currently has.

The namespace `<<project-ns>>.views.layout` contains the code required to
render the general page layout.

The `home-page` function will render the `<<project-ns>>.views.base-layout` function
folder using a parameter map containing the `:content` key. This key points to the
contents of the `resources/docs/docs.md` file containing these instructions,
rendered as HTML and wrapped into a `div.container` tag.

Add other functions / namespaces to `<<project-ns>>.views` as your site
evolves and new views appear.

The HTML templates are written using [Hiccup](https://github.com/weavejester/hiccup) templating engine.

<a class="btn btn-primary" href="http://www.luminusweb.net/docs/html_templating.md">learn more about HTML templating »</a>

<% endif %>

#### Organizing the routes

The routes are aggregated and wrapped with middleware in the `<<project-ns>>.handler` namespace:

```
(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))
```

The `app-routes` definition groups all the routes in the application into a single handler.
A default route group is added to handle the `404` case.

<a class="btn btn-primary" href="http://www.luminusweb.net/docs/routes.md">learn more about routing »</a>

The `home-routes` are wrapped with two middleware functions. The first enables CSRF protection.
The second takes care of serializing and deserializing various encoding formats, such as JSON.

#### Managing your middleware

Request middleware functions are located under the `<<name>>.middleware` namespace.

This namespace is reserved for any custom middleware for the application. Some default middleware is
already defined here. The middleware is assembled in the `wrap-base` function.

Middleware used for development is placed in the `<<project-ns>>.dev-middleware` namespace found in
the `env/dev/clj/` source path.

<a class="btn btn-primary" href="http://www.luminusweb.net/docs/middleware.md">learn more about middleware »</a>

<<db-docs>>
<<sassc-docs>>

#### Need some help?

Visit the [official documentation](http://www.luminusweb.net/docs) for examples
on how to accomplish common tasks with Luminus. The `#luminus` channel on the [Clojurians Slack](http://clojurians.net/) and [Google Group](https://groups.google.com/forum/#!forum/luminusweb) are both great places to seek help and discuss projects with other users.
