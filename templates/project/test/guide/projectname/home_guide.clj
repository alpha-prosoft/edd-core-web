(ns projectname.home-guide
  (:require [projectname.guide-doc :refer [chapter! figure! p! steps!]]
            [projectname.helpers :as h]
            [projectname.home.elements :as el]
            [web.primitives.sdk.button :as button]
            [web.primitives.sdk.display :as display]))

(def guide
  {:title "Getting started"
   :file  "getting-started.md"
   :lead  "What you can do on the **Home** page."})

(defn guide!
  []
  (chapter! "Count clicks"
            "The home page counts how often you select **Increment**.")
  (steps! "Open **Home**."
          "Select **Increment**."
          "The count under the button goes up by one.")
  (h/open! "/" el/title)
  (button/click! el/increment)
  (display/text=? el/clicks "Current count: 1")
  (figure! "home-01-increment"
           "**Home** after one click on **Increment**."
           {:marks [{:target el/increment :label "Counts a click" :color :blue}
                    {:target el/clicks :label "Clicks so far" :color :green}]})
  (p! "Select a name below the counter to open its **About** page."))
