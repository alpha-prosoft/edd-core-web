(ns web.primitives.styles)

(def theme
  {:palette        {:primary   {:main "#fee600"
                                :dark "#fed500"
                                :light "#fff483"}
                    :secondary {:main "#037080"
                                :dark "#004454"
                                :light "#b3d4d8"}
                    :success {:main "#35b37e"
                              :dark "#0b7d3e"
                              :light "#c2e8d8"}
                    :info {:main "#00c2ff"
                           :dark "#0093ff"
                           :light "#b2ecff"}
                    :warning {:main "#ff581e"
                              :dark "#ff1e03"
                              :light "#ffccbb"}
                    :error {:main "#e61e33"
                            :dark "#cf030a"
                            :light "#f7bbc1"}
                    :grey {:050 "#fbfbfb"
                           :100 "#f8f8f8"
                           :200 "#edeeee"
                           :300 "#e9eaea"
                           :400 "#d5d5d6"
                           :500 "#aaabad"
                           :600 "#808185"
                           :700 "#55575c"
                           :800 "#404247"
                           :900 "#212121"}}
   :accent-palette {:dark "rgb(3, 68, 84)"
                    :default "rgb(3, 112, 128)"
                    :60 "rgb(104, 169, 179)"
                    :30 "rgb(179, 212, 216)"
                    :20 "rgb(229, 240, 242)"}
   :typography {:font-family "Verdana,Roboto,Arial,Quicksand"}
   :z-index    {:app-bar 1200
                :drawer  1100}
   :overrides  {:MuiCard
                {:root {:border-radius "12px" :width "100%"}}
                :MuiCardHeader
                {:root {:padding "24px 24px 0 24px"}
                 :action {:margin-right 0}}
                :MuiCardContent
                {:root {:padding "24px"}}
                :MuiFormControlLabel
                {:root
                 {:margin-left "-3px"}}
                :MuiGridListTile
                {:tile
                 {:overflow "inherit"}}
                :MuiTypography
                {:h1 {:font-size "34px"
                      :line-height "40px"
                      :font-weight "900!important"
                      :letter-spacing "0.2px"}
                 :h2 {:font-size "34px"
                      :line-height "40px"
                      :font-weight "400!important"
                      :letter-spacing "0.2px"}
                 :h3 {:font-size "22px"
                      :line-height "25px"
                      :font-weight "400!important"
                      :letter-spacing "0.13px"}
                 :h4 {:font-size "20px"
                      :line-height "24px"
                      :font-weight "600!important"
                      :letter-spacing "0.1px"}
                 :h5 {:font-size "20px"
                      :line-height "24px"
                      :font-weight "400!important"
                      :letter-spacing "0.15px"}
                 :h6 {:font-size "18px"
                      :line-height "21px"
                      :font-weight "400!important"
                      :letter-spacing "0.1px"}
                 :subtitle1 {:font-size "16px"
                             :line-height "20px"
                             :font-weight "400!important"
                             :letter-spacing "0.18px"}
                 :subtitle2 {:font-size "14px"
                             :line-height "22px"
                             :font-weight "500!important"
                             :letter-spacing "0.1px"}
                 :body1 {:font-size "14px"
                         :line-height "20px"
                         :font-weight "400!important"
                         :letter-spacing "0.16px"}
                 :body2 {:font-size "14px"
                         :line-height "20px"
                         :font-weight "400!important"
                         :letter-spacing "0.15px"}
                 :caption {:font-size "12px"
                           :line-height "14px"
                           :font-weight "500!important"
                           :letter-spacing "0px"}
                 :overline {:font-size "12px"
                            :line-height "14px"
                            :font-weight "400!important"
                            :letter-spacing "0.16px"}
                 :button {:font-size "14px"
                          :line-height "16px"
                          :font-weight "medium"
                          :letter-spacing "0.3px"}}
                :MuiIconButton
                {:root
                 {"&.Mui-disabled" {:opacity 0.5}
                  "&.MuiIconButton-sizeSmall" {:padding 10}}}
                :MuiButton
                {:contained
                 {:color               "#2b2d33"
                  :background-color    "#e9eaea"
                  :color-interpolation "none"}
                 :root
                 {:border-radius 8
                  :text-transform "none"
                  :min-width "8rem"}
                 :outlined
                 {:border "solid 1px #2b2d33"}}
                :MuiToggleButton
                {:root {:border-radius 8
                        :padding 1}}
                :MuiToolbar
                {:regular
                 {:min-height "100px"
                  :height     "100px"}
                 :gutters {:padding "0 2rem 0 2rem"
                           "@media (min-width: 600px)"
                           {:padding "0 2rem 0 2rem"}}}
                :MuiAppBar
                {:positionFixed
                 {:margin-left "2rem"}}
                :MuiSwitch
                {:root
                 {"& .MuiSwitch-colorSecondary" {:color "#037080"}
                  "& .MuiSwitch-colorSecondary + .MuiSwitch-track"
                  {:background-color  "#b3d4d8"
                   :opacity 1}
                  "& .MuiSwitch-colorSecondary.Mui-checked " {:color "#037080"}
                  "& .MuiSwitch-colorSecondary.Mui-checked + .MuiSwitch-track"
                  {:background-color "#b3d4d8"
                   :opacity 1}}}
                :MuiAccordion
                {:root
                 {"&:before" {:opacity 0}
                  "& .Mui-expanded" {:margin 0}
                  "&.Mui-expanded" {:margin 0}}}
                :MuiAccordionSummary
                {:root
                 {:padding 0
                  "&.Mui-expanded" {:min-height 0
                                    :margin 0}}
                 :content {"&.Mui-expanded" {:margin "12px 0"}}}
                :MuiAutocomplete
                {:loading {:display "none"}}
                :MuiGrid
                {:root {"& .sun-editor" {:border-bottom "none"
                                         "& .se-toolbar" {:background-color "#ffffff"}
                                         "& .se-container .se-line-breaker" {:display "none!important"}
                                         "& .se-container .se-line-breaker-component" {:display "none!important"}
                                         "& .se-wrapper .se-wrapper-inner" {:height "auto!important"}}}}
                :MuiDivider
                {:root
                 {:height 1.1}}
                :PrivateTabIndicator
                {:root {:height 4}}
                :MuiTabs
                {:root {:width "100%"}}
                :MuiAlert
                {:message {:width "100%"}
                 :root {:width "100%"}}
                :MuiDialogTitle
                {:root {:padding "24px 24px 8px 24px"}}
                :MuiDialogContent
                {:root {:padding "8px 24px 24px 24px"}}
                :MuiDialogActions
                {:root {:padding "8px 24px 24px 24px"}}
                :MuiFormLabel
                {:root {"&.Mui-focused" {:color "rgba(0, 0, 0, 0.54)"}}}
                :MuiTextField
                {:root {"& .MuiFormLabel-root.Mui-error" {:color "#e61e33!important"}}}
                :MuiInputBase
                {:input {:text-overflow "ellipsis"}}}})
