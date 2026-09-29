(ns web.primitives.sdk.accordion
  "RawAccordion / EddAccordion. el-id lands as data-testid on the accordion
   root. The expand toggle is the icon button inside the summary."
  (:require
   [web.primitives.sdk.core :as sdk]))

(defn- toggle [el-id]
  (.first (sdk/within el-id ".MuiAccordionSummary-root button")))

(defn- details [el-id]
  (sdk/within el-id ".MuiAccordion-region"))

(defn expanded?
  [el-id]
  (.isVisible (details el-id)))

(defn toggle!
  [el-id]
  (.click (toggle el-id)))

(defn expand!
  [el-id]
  (when-not (expanded? el-id)
    (toggle! el-id)
    (sdk/wait-visible (details el-id))))

(defn collapse!
  [el-id]
  (when (expanded? el-id)
    (toggle! el-id)
    (sdk/wait-hidden (details el-id))))

(defn header-text
  [el-id]
  (.trim (.innerText (sdk/within el-id ".MuiAccordionSummary-root"))))

(defn details-text
  "Text of the details area (expand first)."
  [el-id]
  (.trim (.innerText (details el-id))))
