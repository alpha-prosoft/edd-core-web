(ns web.widgets.login.visibility)

(defn- hidden-from-people?
  [^js element]
  (some? (.closest element "[inert], [aria-hidden='true']")))

(defn- self-and-ancestors
  [^js element]
  (take-while some? (iterate #(.-parentElement ^js %) element)))

(defn- hidden-by-style?
  [^js element]
  (let [own-style
        (.getComputedStyle js/window element)

        hiding?
        (fn [^js node]
          (let [style
                (.getComputedStyle js/window node)]
            (or (= "none" (.-display style))
                (= "0" (.-opacity style)))))]
    (or (contains? #{"hidden" "collapse"} (.-visibility own-style))
        (boolean (some hiding? (self-and-ancestors element))))))

(defn- rendered?
  [^js element]
  (if (fn? (.-checkVisibility element))
    (.checkVisibility element #js {:checkOpacity          true
                                   :opacityProperty       true
                                   :visibilityProperty    true
                                   :checkVisibilityCSS    true
                                   :contentVisibilityAuto true})
    (not (hidden-by-style? element))))

(defn- has-size?
  [^js element]
  (let [rect
        (.getBoundingClientRect element)]
    (and (pos? (.-length (.getClientRects element)))
         (pos? (.-width rect))
         (pos? (.-height rect)))))

(defn visible?
  "True when a person can see and reach `element`: it is in the page, not the body, not hidden by
   display, visibility or opacity on itself or an ancestor, has a size, and is not under an inert or
   aria-hidden ancestor."
  [^js element]
  (and (some? element)
       (.-isConnected element)
       (not= element (.-body js/document))
       (not (hidden-from-people? element))
       (rendered? element)
       (has-size? element)))
