(ns web.widgets.login.visibility-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.widgets.login.visibility :as visibility]))

(defn- opener-in
  "A button inside a container with `container-style` and `container-attributes`, in the page."
  [container-style container-attributes]
  (let [container
        (.createElement js/document "div")

        button
        (.createElement js/document "button")]
    (set! (.-cssText (.-style container)) container-style)
    (doseq [[attribute value] container-attributes]
      (.setAttribute container attribute value))
    (set! (.-textContent button) "Log in")
    (.appendChild container button)
    (.appendChild (.-body js/document) container)
    button))

(defn- visible-in?
  [container-style container-attributes]
  (let [button
        (opener-in container-style container-attributes)

        visible?
        (visibility/visible? button)]
    (.remove (.-parentElement button))
    visible?))

(deftest an-opener-that-can-be-seen-is-visible
  (is
   (true?
    (visible-in? "" {}))))

(deftest an-opener-in-a-hidden-menu-is-not-visible
  (testing "visibility:hidden on an ancestor"
    (is
     (false?
      (visible-in? "visibility: hidden" {}))))

  (testing "display:none on an ancestor"
    (is
     (false?
      (visible-in? "display: none" {}))))

  (testing "opacity 0 on an ancestor"
    (is
     (false?
      (visible-in? "opacity: 0" {}))))

  (testing "no size"
    (let [button
          (opener-in "" {})]
      (set! (.-cssText (.-style button)) "width: 0; height: 0; padding: 0; border: 0")

      (is
       (false?
        (visibility/visible? button)))

      (.remove (.-parentElement button))))

  (testing "inert ancestor"
    (is
     (false?
      (visible-in? "" {"inert" ""}))))

  (testing "aria-hidden ancestor"
    (is
     (false?
      (visible-in? "" {"aria-hidden" "true"})))))

(deftest gone-or-missing-is-not-visible
  (is
   (false?
    (visibility/visible? (.createElement js/document "button"))))

  (is
   (false?
    (boolean (visibility/visible? nil))))

  (is
   (false?
    (visibility/visible? (.-body js/document)))))
