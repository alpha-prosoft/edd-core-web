(ns projectname.i18n
  (:require [edd.i18n :as i18n]))

(def translations
  {:en {:language-name    "English"
        :menu             {:home  "Home"
                           :about  "About"}
        :language         "Language"
        :about            "About"
        :home             "Home"
        :increments-count "Current count: "}
   :de {:language-name    "English"
        :menu             {:home  "Home"
                           :about "Úber uns"}
        :language         "Sprache"
        :about            "Űber uns"
        :home             "Home"
        :increments-count "Bis jetzt: "}})
