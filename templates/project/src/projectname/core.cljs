(ns projectname.core
  (:require [edd.core :as core]
            [projectname.i18n :as i18n]
            [projectname.styles :refer [theme styles]]
            [projectname.home.views :as home]
            [projectname.page :as page]
            [projectname.about.views :as about]))

(defn ^:export init
  [config]
  (core/init
   {:panels       {:home  home/main-panel
                   :about about/main-panel}
    :languages    [:en :de]
    :translations i18n/translations
    :routes       ["/" {""             :home
                        ["about/" :id] :about}]
    :menu         [:home :about]
    :theme        (theme)
    :styles       styles
    :name         "projectname"
    :body         page/body}))

