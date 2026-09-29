(ns web.primitives.testing.el-id
  "Resolves an :el-id value into the id string rendered into the DOM.

   Callers never build strings themselves. Components and the SDK both call
   `el-id`, so a view and a test using the same value always agree:

     :first-name                     -> \"first-name\"
     :app.person/first-name          -> \"app.person/first-name\"
     [:person \"Bla\" \"ble\"]           -> \"person--bla--ble\"
     [some-element-fn \"Bla\"]         -> whatever (some-element-fn \"Bla\") resolves to
     \"already-built\"                 -> \"already-built\"

   Suffixes are joined with \"--\" so scanned ids can be split back into a
   nested path."
  (:require
   [clojure.string :as str]))

(def separator "--")

(defn- clean-suffix [suffix]
  (let [text
        (if (keyword? suffix) (name suffix) (str suffix))

        lowered
        (str/lower-case (str/trim text))

        plus-expanded
        (str/replace lowered "+" "plus")

        spaces-collapsed
        (str/replace plus-expanded #"\s+" "-")]
    (str/replace spaces-collapsed #"[^a-z0-9-]" "")))

(defn- keyword->string [kw]
  (subs (str kw) 1))

(defn- base->string [base]
  (if (keyword? base)
    (keyword->string base)
    (str base)))

(defn- join-suffixes [base suffixes]
  (str/join separator (cons (base->string base) (map clean-suffix suffixes))))

(defn- fn-vector? [value]
  (and (vector? value) (fn? (first value))))

(defn el-id
  "Resolve an el-id value (keyword, vector, fn vector or string) to its id.
   With extra arguments, `base` is combined with each suffix: (el-id :row 1 :amount)."
  ([value]
   (cond
     (nil? value)
     nil

     (keyword? value)
     (keyword->string value)

     (fn-vector? value)
     (el-id (apply (first value) (rest value)))

     (vector? value)
     (join-suffixes (first value) (rest value))

     :else
     (str value)))
  ([base & suffixes]
   (join-suffixes base suffixes)))

(defn el-id-sel [f & args]
  (str "[id='" (apply f args) "']"))

(defn testid-sel [f & args]
  (str "[data-testid='" (apply f args) "']"))

(defn testid-prefix-sel [f & args]
  (str "[data-testid^='" (apply f args "") "']"))

(defn sel
  "Selector that matches an element by el-id regardless of whether the
   primitive exposed it as :id (inputs) or :data-testid (containers/buttons).
   Prefer this in tests so callers do not need to know the per-primitive
   attribute mapping."
  [f & args]
  (let [value
        (apply f args)]
    (str "[id='" value "'],[data-testid='" value "']")))
