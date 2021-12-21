(ns edd.db)

(def default-db
  {::user                    nil
   ::active-panel            :home
   ::drawer                  false
   ::ready                   true
   ::selected-language       :en
   ::show-language-switcher? true
   ::menu-items              {}
   ::menu-expanded           {}
   ::languages               {}
   ::translations            {}
   ::config                  {}
   ::routes                  {}})
