(ns projectname.core
  (:require [edd.core :as core]
            [projectname.i18n :as i18n]
            [projectname.styles :refer [theme styles]]
            [projectname.home.core :as home]
            [projectname.about.core :as about]
            [projectname.page :as page]))

(defn ^:export init
  [config]
  (core/init
   {:pages       {:home  home/main
                  :about about/main}
    :languages    [:en :de]
    :translations i18n/translations
    :routes       ["/" {""             :home
                        ["about/" :id] :about}]
    :menu         [:home :about]
    :theme        (theme)
    :styles       styles
    :name         "projectname"
    :body         page/body}))

