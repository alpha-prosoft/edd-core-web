(ns web.devcards.icon-button
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawGrid RawIconButton EddIconButton]]
   [web.primitives.icons.core :refer [AddIcon CloseIcon DeleteIcon LinkIcon LinkOffIcon
                                      ArrowDropDownCircleIcon ArrowDropUpCircleIcon]]))

(defcard-rg :iconbutton-add
  "## IconButton Add (default :size \"medium\" option \"small\")"
  (apply-stiles [RawGrid {:container true :align-items "baseline" :spacing 4 :direction "row"}
                 [RawGrid {:item true}
                  [RawIconButton
                   {:id       "button-add"
                    :on-click (fn [] (print "on-click"))}
                   [AddIcon]]]
                 [RawGrid {:item true}
                  [RawIconButton
                   {:id       "button-add"
                    :size     "small"
                    :on-click (fn [] (print "on-click"))}
                   [AddIcon]]]]))

(defcard-rg :iconbutton-close
  "## IconButton Close"
  (apply-stiles [RawIconButton
                 {:id       "button-close"
                  :on-click (fn [] (print "on-click"))}
                 [CloseIcon]]))

(defcard-rg :iconbutton-delete
  "## IconButton Delete"
  (apply-stiles [RawIconButton
                 {:id       "button-delete"
                  :on-click (fn [] (print "on-click"))}
                 [DeleteIcon]]))

(defcard-rg :iconbutton-delete-disabled
  "## IconButton Delete disabled"
  (apply-stiles [RawIconButton
                 {:id       "button-delete"
                  :disabled true}
                 [DeleteIcon]]))

(defcard-rg :edd-icon-button-with-on-hover-icon
  "## EddIconButton with on hover icon"
  (apply-stiles [EddIconButton
                 {:id       "button-on-hover-icon"
                  :on-click (fn [] (print "on-click"))}
                 [LinkIcon]
                 [LinkOffIcon]]))

(defcard-rg :iconbutton-delete
  "## IconButton No icon"
  (apply-stiles [RawGrid {:container true :align-items "baseline" :spacing 4 :direction "row"}
                 [RawGrid {:item true}
                  [RawIconButton
                   {:id       "button-delete"
                    :on-click (fn [] (print "on-click"))}]]
                 [RawGrid {:item true}
                  [RawIconButton
                   {:id       "button-delete"
                    :size     "small"
                    :on-click (fn [] (print "on-click"))}]]]))

(defcard-rg :navigation-icon-buttons
  "## Navigation Icon Buttons"
  (apply-stiles [RawGrid {:container true
                          :direction "column"
                          :sx {:width "fit-content"}}
                 [RawGrid {:item true}
                  [RawIconButton {:size "small"
                                  :edge "start"
                                  :sx {:padding "7px!important"}
                                  :disabled true}
                   [ArrowDropUpCircleIcon {:color "secondary"
                                           :font-size "small"}]]]
                 [RawGrid {:item true}
                  [RawIconButton {:size "small"
                                  :edge "start"
                                  :sx {:padding "7px!important"}}
                   [ArrowDropDownCircleIcon {:color "secondary"
                                             :font-size "small"}]]]]))
