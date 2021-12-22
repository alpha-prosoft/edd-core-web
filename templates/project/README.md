# projectname 

## Development 

```shell
shadow-cljs -A:dev watch :app
```

This is template project for using ClojureScript, MaterialUI with re-frame. 
Powered by shadow-cljs. 

Project is using edd-core-web in order to provide common functionality 
like navigation and bootstraping. It is not required to use this library
but it is usefull for starting the project. 



## Translations

To put new translation you can update i18n.cljs file with new keys. 
You can use translations on view by using i18n/tr function. 

```clojure

(ns projectname.home.views
  (:require [edd.i18n :as lang]))

...

(lang/tr :increments-count)

```

## config.xml
