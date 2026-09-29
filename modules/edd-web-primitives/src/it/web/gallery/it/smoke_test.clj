(ns web.gallery.it.smoke-test
  (:require
   [clojure.test :refer [deftest is]]
   [testing.pw :refer [step]]
   [web.gallery.it.helpers :as h :refer [with-gallery]]
   [web.primitives.sdk.console :as console]))

(deftest every-component-renders-without-runtime-errors
  (with-gallery
    (let [errors (console/watch-errors!)]

      (step "load the single-page gallery (mounts every component)"
            (fn []
              (h/load-gallery!)

              (is
               (pos? (count (h/page-ids))))))

      (step "scroll through every section"
            (fn []
              (doseq [pid (h/page-ids)]
                (h/open-page! pid))))

      (step "no uncaught page errors"
            (fn []
              (is
               (empty? (console/page-errors errors)))))

      (step "no unexpected console errors"
            (fn []
              (is
               (empty? (console/console-errors errors))))))))

(deftest every-section-has-variants
  (with-gallery
    (step "each section renders at least one variant"
          (fn []
            (doseq [pid (h/page-ids)]
              (h/open-page! pid)

              (is
               (seq (h/variant-ids))
               (str pid ": has variants")))))))
