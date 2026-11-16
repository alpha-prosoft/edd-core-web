(ns web.widgets.login.views-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.widgets.login.views :as views]))

(defn- region-attributes
  [announce notice]
  (second (views/notices announce notice)))

(defn- alert-props
  [announce notice]
  (second (nth (views/notices announce notice) 2)))

(deftest notices-are-said-by-the-dialogs-one-live-region
  (testing "the region is there before a notice comes, so the notice is announced"
    (is
     (= {:aria-live "polite" :aria-atomic "true" :id views/live-region-id}
        (select-keys (region-attributes nil nil) [:aria-live :aria-atomic :id]))))

  (testing "the notice in it is not a live region of its own"
    (is
     (nil?
      (:role (alert-props nil "A code is on its way."))))

    (is
     (contains? (alert-props nil "A code is on its way.") :role))))

(deftest an-app-announcer-replaces-the-live-region
  (is
   (nil?
    (:aria-live (region-attributes (fn [_]) "A code is on its way."))))

  (testing "a new notice goes to the announcer while the dialog is open"
    (is
     (= :code-resent
        (views/notice-to-announce {:announce identity :open? true :notice :code-resent :previous :reset-code-sent}))))

  (testing "the same notice is said once; nothing without an announcer or a dialog"
    (is
     (nil?
      (views/notice-to-announce {:announce identity :open? true :notice :code-resent :previous :code-resent})))

    (is
     (nil?
      (views/notice-to-announce {:announce nil :open? true :notice :code-resent :previous nil})))

    (is
     (nil?
      (views/notice-to-announce {:announce identity :open? false :notice :code-resent :previous nil})))))
