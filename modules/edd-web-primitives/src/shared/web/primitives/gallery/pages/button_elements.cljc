(ns web.primitives.gallery.pages.button-elements
  "Element ids for the Button gallery page, shared by the view and the UI
   tests. Values are plain :el-id data (keywords / vectors); components and
   the SDK resolve them, so neither side builds id strings.

   View:  [RawButton {:el-id disabled} \"Disabled\"]
          [RawButton {:el-id (variant :contained)} \"Contained\"]
   Test:  (button/click! (variant :contained))
          (button/enabled? disabled)")

(def disabled :button-disabled)

(defn variant
  "Button for a named demo variant, e.g. (variant :contained)."
  [v]
  [:button v])

(defn variant-readout
  "State readout shown under a variant, e.g. (variant-readout :contained)."
  [v]
  [:button-readout v])
