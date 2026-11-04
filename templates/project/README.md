# projectname

re-frame application built on edd-core-web.

## Development

```shell
clojure -M:shadow-cljs:dev:test:run     # https://projectname.local.localdevhub.com:3000
clojure -M:format fix                   # or: check
clojure -X:clean                        # remove build output and node_modules
```

## Translations

Add keys to `i18n.cljs` and use them in views with `tr`:

```clojure
(ns projectname.home.views
  (:require [edd.i18n :as lang]))

(lang/tr :increments-count)
```
