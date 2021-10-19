(ns web.devcards.table
  (:require
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros (defcard-rg)]

   [web.devcards.utils :refer [apply-stiles]]
   [web.primitives.components :refer [RawTableContainer RawTable RawTableHead RawTableRow RawTableCell RawTableBody]]))

(defcard-rg :table
  "## Table"
  (apply-stiles [RawTableContainer
                 [RawTable
                  [RawTableHead
                   [RawTableRow
                    [RawTableCell "RawTableHead - RawTableRow - RawTableCell 1"]
                    [RawTableCell "RawTableHead - RawTableRow - RawTableCell 2"]
                    [RawTableCell "RawTableHead - RawTableRow - RawTableCell 3"]]]
                  [RawTableBody
                   [RawTableRow
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 1/1"]
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 1/2"]
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 1/3"]]
                   [RawTableRow
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 2/1"]
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 2/2"]
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 2/3"]]]]]))

(defcard-rg :table-no-lines
  "## Table with no Lines"
  (apply-stiles [RawTableContainer
                 [RawTable
                  [RawTableHead
                   [RawTableRow
                    [RawTableCell {:style {:border-bottom "none"}} "RawTableHead - RawTableRow - RawTableCell 1"]
                    [RawTableCell {:style {:border-bottom "none"}} "RawTableHead - RawTableRow - RawTableCell 2"]]]
                  [RawTableBody
                   [RawTableRow
                    [RawTableCell {:style {:border-bottom "none"}} "RawTableBody - RawTableRow - RawTableCell 1/1"]
                    [RawTableCell {:style {:border-bottom "none"}} "RawTableBody - RawTableRow - RawTableCell 1/2"]]
                   [RawTableRow
                    [RawTableCell {:style {:border-bottom "none"}} "RawTableBody - RawTableRow - RawTableCell 2/1"]
                    [RawTableCell {:style {:border-bottom "none"}} "RawTableBody - RawTableRow - RawTableCell 2/2"]]]]]))

(defcard-rg :table-two-headers
  "## Table with two headers"
  (apply-stiles [RawTableContainer
                 [RawTable
                  [RawTableHead
                   [RawTableRow
                    [RawTableCell "RawTableHead - RawTableRow - RawTableCell 1"]
                    [RawTableCell "RawTableHead - RawTableRow - RawTableCell 2"]
                    [RawTableCell "RawTableHead - RawTableRow - RawTableCell 3"]]]
                  [RawTableBody
                   [RawTableRow
                    [RawTableCell {:variant "head"} "RawTableBody - RawTableRow - RawTableCell 1/1"]
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 1/2"]
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 1/3"]]
                   [RawTableRow
                    [RawTableCell {:variant "head"} "RawTableBody - RawTableRow - RawTableCell 2/1"]
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 2/2"]
                    [RawTableCell "RawTableBody - RawTableRow - RawTableCell 2/3"]]]]]))