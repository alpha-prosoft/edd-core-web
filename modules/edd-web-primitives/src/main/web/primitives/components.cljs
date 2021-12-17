(ns web.primitives.components
  (:require
   [web.primitives.layout.core :as layout-core]
   [web.primitives.data-display.core :as data-display-core]
   [web.primitives.feedback.core :as feedback-core]
   [web.primitives.inputs.core :as inputs-core]
   [web.primitives.lab.core :as lab-core]
   [web.primitives.navigation.core :as navigation-core]
   [web.primitives.surfaces.core :as surfaces-core]
   [web.primitives.utils.core :as utils-core]
   [web.primitives.text-editor.core :as text-editor-core]))

(def RawGrid layout-core/RawGrid)
(def RawBox layout-core/RawBox)

(def RawBadge data-display-core/RawBadge)
(def RawTooltip data-display-core/RawTooltip)
(def RawTypography data-display-core/RawTypography)
(def RawList data-display-core/RawList)
(def RawListItem data-display-core/RawListItem)
(def RawListItemText data-display-core/RawListItemText)
(def RawListItemIcon data-display-core/RawListItemIcon)
(def RawListSubheader data-display-core/RawListSubheader)
(def RawDivider data-display-core/RawDivider)
(def RawMenu data-display-core/RawMenu)
(def RawMenuList data-display-core/RawMenuList)
(def RawMenuItem data-display-core/RawMenuItem)
(def RawTableContainer data-display-core/RawTableContainer)
(def RawTable data-display-core/RawTable)
(def RawTableBody data-display-core/RawTableBody)
(def RawTableHead data-display-core/RawTableHead)
(def RawTableRow data-display-core/RawTableRow)
(def RawTableCell data-display-core/RawTableCell)
(def RawAvatar data-display-core/RawAvatar)

(def RawDialog feedback-core/RawDialog)
(def RawSnackbar feedback-core/RawSnackbar)
(def RawCircularProgress feedback-core/RawCircularProgress)
(def EddLinearProgress feedback-core/EddLinearProgress)
(def RawBackdrop feedback-core/RawBackdrop)

(def RawTextField inputs-core/RawTextField)
(def RawSwitch inputs-core/RawSwitch)
(def RawSelect inputs-core/RawSelect)
(def RawFormSelect inputs-core/RawFormSelect)
(def RawChoiceButton inputs-core/RawChoiceButton)
(def RawDatePicker inputs-core/RawDatePicker)
(def EddDatePicker inputs-core/EddDatePicker)
(def RawButton inputs-core/RawButton)
(def RawFab inputs-core/RawFab)
(def RawIconButton inputs-core/RawIconButton)
(def EddIconButton inputs-core/EddIconButton)
(def RawCheckbox inputs-core/RawCheckbox)
(def RawRadioGroup inputs-core/RawRadioGroup)
(def RawNumberField inputs-core/RawNumberField)
(def RawPercentField inputs-core/RawPercentField)

(def RawAutocomplete lab-core/RawAutocomplete)
(def RawAlert lab-core/RawAlert)
(def RawSkeleton lab-core/RawSkeleton)
(def RawToggleButtonGroup lab-core/RawToggleButtonGroup)
(def RawToggleButton lab-core/RawToggleButton)
(def RawTimelineDot lab-core/RawTimelineDot)

(def RawTabs navigation-core/RawTabs)
(def RawLink navigation-core/RawLink)
(def RawDrawer navigation-core/RawDrawer)
(def RawSwipeableDrawer navigation-core/RawSwipeableDrawer)

(def RawAccordion surfaces-core/RawAccordion)
(def EddAccordion surfaces-core/EddAccordion)
(def RawCard surfaces-core/RawCard)
(def RawClickAwayListener surfaces-core/RawClickAwayListener)
(def RawAppBar surfaces-core/RawAppBar)
(def RawToolbar surfaces-core/RawToolbar)

(def RawSlide utils-core/RawSlide)

(def RawTextEditor text-editor-core/RawTextEditor)
(def EddTextEditor text-editor-core/EddTextEditor)

(def RawTreeView lab-core/RawTreeView)
(def RawTreeItem lab-core/RawTreeItem)