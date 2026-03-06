(ns web.primitives.data-display.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [web.primitives.data-display.core :refer [RawDivider
                                                      RawBadge
                                                      RawTooltip
                                                      RawTypography
                                                      RawList
                                                      RawListItem
                                                      RawListItemText
                                                      RawListItemIcon
                                                      RawListSubheader
                                                      RawMenu
                                                      RawMenuList
                                                      RawMenuItem
                                                      RawTableContainer
                                                      RawTable
                                                      RawTableBody
                                                      RawTableHead
                                                      RawTableRow
                                                      RawTableCell
                                                      RawAvatar
                                                      RawChip]]
            [reagent.core :as r]))

(deftest raw-divider-renders
  (testing "RawDivider renders without crashing"
    (is (some? (r/as-element [RawDivider {}])))))

(deftest raw-badge-renders
  (testing "RawBadge renders without crashing"
    (is (some? (r/as-element [RawBadge {:badge-content 1 :content "A"}])))
    (is (some? (r/as-element [RawBadge {:badge-content 0 :content "B"}])))))

(deftest raw-tooltip-renders
  (testing "RawTooltip renders without crashing"
    (is (some? (r/as-element [RawTooltip {} "tip"])))
    (is (some? (r/as-element [RawTooltip {:no-grid true} "tip"])))))

(deftest raw-typography-renders
  (testing "RawTypography renders without crashing"
    (is (some? (r/as-element [RawTypography {} "text"])))))

(deftest raw-list-renders
  (testing "RawList renders without crashing"
    (is (some? (r/as-element [RawList {} [:span "item"]])))))

(deftest raw-list-item-renders
  (testing "RawListItem renders without crashing"
    (is (some? (r/as-element [RawListItem {} [:span "item"]])))))

(deftest raw-list-item-text-renders
  (testing "RawListItemText renders without crashing"
    (is (some? (r/as-element [RawListItemText {} "text"])))))

(deftest raw-list-item-icon-renders
  (testing "RawListItemIcon renders without crashing"
    (is (some? (r/as-element [RawListItemIcon {} [:span "icon"]])))))

(deftest raw-list-subheader-renders
  (testing "RawListSubheader renders without crashing"
    (is (some? (r/as-element [RawListSubheader {} "subheader"])))))

(deftest raw-menu-renders
  (testing "RawMenu renders without crashing"
    (is (some? (r/as-element [RawMenu {:open true} [:span "item"]])))))

(deftest raw-menu-list-renders
  (testing "RawMenuList renders without crashing"
    (is (some? (r/as-element [RawMenuList {} [:span "item"]])))))

(deftest raw-menu-item-renders
  (testing "RawMenuItem renders without crashing"
    (is (some? (r/as-element [RawMenuItem {} "item"])))))

(deftest raw-table-container-renders
  (testing "RawTableContainer renders without crashing"
    (is (some? (r/as-element [RawTableContainer {} [:span "row"]])))))

(deftest raw-table-renders
  (testing "RawTable renders without crashing"
    (is (some? (r/as-element [RawTable {} [:span "row"]])))))

(deftest raw-table-body-renders
  (testing "RawTableBody renders without crashing"
    (is (some? (r/as-element [RawTableBody {} [:span "cell"]])))))

(deftest raw-table-head-renders
  (testing "RawTableHead renders without crashing"
    (is (some? (r/as-element [RawTableHead {} [:span "cell"]])))))

(deftest raw-table-row-renders
  (testing "RawTableRow renders without crashing"
    (is (some? (r/as-element [RawTableRow {} [:span "cell"]])))))

(deftest raw-table-cell-renders
  (testing "RawTableCell renders without crashing"
    (is (some? (r/as-element [RawTableCell {} [:span "cell"]])))))

(deftest raw-avatar-renders
  (testing "RawAvatar renders without crashing"
    (is (some? (r/as-element [RawAvatar {} "A"])))
    (is (some? (r/as-element [RawAvatar {}])))))

(deftest raw-chip-renders
  (testing "RawChip renders without crashing"
    (is (some? (r/as-element [RawChip {:label "chip"}])))))
