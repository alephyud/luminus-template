#### Internationalization

Your site supports internationalization.

The urls for your site follow the `/(language-code)/(content-url)' pattern,
where `language-code` is the lowercase 2-character ISO code for the language,
such as `en`, `es` or `ru`.

The index url of the site redirects to `/en`. You can change this by
changing the `default-language` variable in `src/clj/<<sanitized>>/i18n.clj`.

The `t` macro uses the `<<project-name>>.i18n/*page-language*` variable
(a keyword like `:en`, `:es`, `:ru`) to choose the language. For your
convenience, you can set it up with `with-language` macro. In a page handler
context, it is already set by the site's middleware based on the path
(e. g. `/en`, `/es`, `/ru`).

Remember that the `*page-language*` and *tscope* are dynamic, so take care when
using lazy data structures or concurrency features!

The internationalzed content of the site is stored in
`resources/content/i18n.clj`, grouped by *tscope*, then by key, then by
language.

The i18n-related source code is located in `src/clj/<<sanitized>>/i18n.clj`.
Amend it to fit your needs / add custom facilities if needed.
