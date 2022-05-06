(ns web.primitives.styles)

(def theme
  {:palette        {:primary   {:main  "#fee600"
                                :dark  "#fed500"
                                :light "#fff483"}
                    :secondary {:main  "#037080"
                                :dark  "#004454"
                                :light "#b3d4d8"}
                    :success   {:main  "#35b37e"
                                :dark  "#0b7d3e"
                                :light "#c2e8d8"}
                    :info      {:main  "#00c2ff"
                                :dark  "#0093ff"
                                :light "#b2ecff"}
                    :warning   {:main  "#ff581e"
                                :dark  "#ff1e03"
                                :light "#ffccbb"}
                    :error     {:main  "#e61e33"
                                :dark  "#cf030a"
                                :light "#f7bbc1"}
                    :grey      {:050 "#fbfbfb"
                                :100 "#f8f8f8"
                                :200 "#edeeee"
                                :300 "#e9eaea"
                                :400 "#d5d5d6"
                                :500 "#aaabad"
                                :600 "#808185"
                                :700 "#55575c"
                                :800 "#404247"
                                :900 "#212121"}}
   :accent-palette {:dark    "rgb(3, 68, 84)"
                    :default "rgb(3, 112, 128)"
                    :60      "rgb(104, 169, 179)"
                    :30      "rgb(179, 212, 216)"
                    :20      "rgb(229, 240, 242)"}
   :typography     {:font-family "Verdana,Roboto,Arial,Quicksand"}
   :z-index        {:app-bar 1200
                    :drawer  1100}
   :components     {:MuiCard
                    {:styleOverrides
                     {:root {:borderRadius "12px" :width "100%"}}}

                    :MuiCardHeader
                    {:styleOverrides
                     {:root   {:padding "24px 24px 0 24px"}
                      :action {:marginRight 0}}}

                    :MuiCardContent
                    {:styleOverrides
                     {:root {:padding "24px"}}}

                    :MuiFormControlLabel
                    {:styleOverrides
                     {:root
                      {:margin-left "-3px"}}}

                    :MuiFormControl
                    {:styleOverrides
                     {:root
                      {"& .form-select-input-label"
                       {:marginLeft            "-14px"
                        "&.MuiFormLabel-filled" {:top "9px"}
                        "&.Mui-focused"         {:top "9px"}}}}}
                    :MuiGridListTile
                    {:styleOverrides
                     {:tile
                      {:overflow "inherit"}}}

                    :MuiTypography
                    {:styleOverrides
                     {:h1        {:fontSize      "34px"
                                  :lineHeight    "40px"
                                  :fontWeight    "900!important"
                                  :letterSpacing "0.2px"}
                      :h2        {:fontSize      "34px"
                                  :lineHeight    "40px"
                                  :fontWeight    "400!important"
                                  :letterSpacing "0.2px"}
                      :h3        {:fontSize      "22px"
                                  :lineHeight    "25px"
                                  :fontWeight    "400!important"
                                  :letterSpacing "0.13px"}
                      :h4        {:fontSize      "20px"
                                  :lineHeight    "24px"
                                  :fontWeight    "600!important"
                                  :letterSpacing "0.1px"}
                      :h5        {:fontSize      "20px"
                                  :lineHeight    "24px"
                                  :fontWeight    "400!important"
                                  :letterSpacing "0.15px"}
                      :h6        {:fontSize      "18px"
                                  :lineHeight    "21px"
                                  :fontWeight    "400!important"
                                  :letterSpacing "0.1px"}
                      :subtitle1 {:fontSize      "16px"
                                  :lineHeight    "20px"
                                  :fontWeight    "400!important"
                                  :letterSpacing "0.18px"}
                      :subtitle2 {:fontSize      "14px"
                                  :lineHeight    "22px"
                                  :fontWeight    "500!important"
                                  :letterSpacing "0.1px"}
                      :body1     {:fontSize      "14px"
                                  :lineHeight    "20px"
                                  :fontWeight    "400!important"
                                  :letterSpacing "0.16px"}
                      :body2     {:fontSize      "14px"
                                  :lineHeight    "20px"
                                  :fontWeight    "400!important"
                                  :letterSpacing "0.15px"}
                      :caption   {:fontSize      "12px"
                                  :lineHeight    "14px"
                                  :fontWeight    "500!important"
                                  :letterSpacing "0px"}
                      :overline  {:fontSize      "12px"
                                  :lineHeight    "14px"
                                  :fontWeight    "400!important"
                                  :letterSpacing "0.16px"}
                      :button    {:fontSize      "14px"
                                  :lineHeight    "16px"
                                  :fontWeight    "medium"
                                  :letterSpacing "0.3px"}}}

                    :MuiIconButton
                    {:styleOverrides
                     {:root
                      {:padding                    "12px"
                       :color                      "inherit"
                       "&.Mui-disabled"            {:opacity 0.5}
                       "&.MuiIconButton-sizeSmall" {:padding 10}}}}

                    :MuiButton
                    {:styleOverrides
                     {:contained
                      {:colorInterpolation "none"}
                      :root
                      {:borderRadius  "8px"
                       :color         "inherit"
                       :textTransform "none"
                       :minWidth      "8rem"}
                      :outlined
                      {:border "solid 1px #2b2d33"}
                      :containedSecondary
                      {:color "#fff"}
                      :textSecondary
                      {:color "#037080"}}}

                    :MuiToggleButton
                    {:styleOverrides
                     {:root {:borderRadius 8
                             :padding      1}}}

                    :MuiToolbar
                    {:styleOverrides
                     {:regular
                      {:minHeight "100px"
                       :height    "100px"}
                      :gutters {:padding "0 2rem 0 2rem"
                                "@media (min-width: 600px)"
                                {:padding "0 2rem 0 2rem"}}}}

                    :MuiAppBar
                    {:styleOverrides
                     {:positionFixed
                      {:margin-left "2rem"}}}

                    :MuiAccordion
                    {:styleOverrides
                     {:root
                      {"&:before"        {:opacity 0}
                       "& .Mui-expanded" {:margin 0}
                       "&.Mui-expanded"  {:margin 0}}}}

                    :MuiAccordionSummary
                    {:styleOverrides {:root
                                      {:padding         0
                                       :minHeight       0
                                       "&.Mui-expanded" {:minHeight 0
                                                         :margin    0}}
                                      :content {"&.Mui-expanded" {:margin "12px 0"}}}}

                    :MuiAutocomplete
                    {:styleOverrides
                     {:loading {:display "none"}
                      :listbox {:display "grid"}
                      :option
                      {"&[aria-selected=\"true\"]" {:backgroundColor "#d5d5d6!important"}}}}

                    :MuiGrid
                    {:styleOverrides
                     {:root {"& .sun-editor" {:borderBottom                                "none"
                                              "& .se-toolbar"                              {:backgroundColor "#ffffff"}
                                              "& .se-container .se-line-breaker"           {:display "none!important"}
                                              "& .se-container .se-line-breaker-component" {:display "none!important"}
                                              "& .se-wrapper .se-wrapper-inner"            {:height "auto!important"}}}}}
                    :MuiDivider
                    {:styleOverrides
                     {:root {:height 1.1}}}

                    :MuiTabs
                    {:styleOverrides
                     {:root      {:width "100%"}
                      :indicator {:height "4px"}}}

                    :MuiTab
                    {:styleOverrides
                     {:root {:minWidth        "160px"
                             "&.Mui-selected" {:color "inherit"}}}}

                    :MuiAlert
                    {:styleOverrides
                     {:message         {:width "100%"}
                      :root            {:width "100%"}
                      :standardSuccess {:backgroundColor   "#eaf7f2"
                                        "& .MuiAlert-icon" {:color "#47ba89"}}
                      :standardError   {:backgroundColor   "#fce8ea"
                                        "& .MuiAlert-icon" {:color "#e83244"}}
                      :standardWarning {:backgroundColor   "#fdeee8"
                                        "& .MuiAlert-icon" {:color "#f66732"}}
                      :standardInfo    {:backgroundColor   "#e5f8ff"
                                        "& .MuiAlert-icon" {:color "#1a4d82"}}}}

                    :MuiDialogTitle
                    {:styleOverrides
                     {:root {:padding "24px 24px 8px 24px"}}}

                    :MuiDialogContent
                    {:styleOverrides
                     {:root {:padding "8px 24px 24px 24px"}}}

                    :MuiDialogActions
                    {:styleOverrides
                     {:root {:padding "8px 24px 24px 24px"}}}

                    :MuiFormLabel
                    {:styleOverrides
                     {:root {"&.Mui-focused" {:color "rgba(0, 0, 0, 0.54)"}}}}

                    :MuiTextField
                    {:styleOverrides
                     {:root {"& .MuiFormLabel-root.Mui-error" {:color "#e61e33!important"}}}}

                    :MuiInputBase
                    {:styleOverrides
                     {:input {:textOverflow "ellipsis"}}}

                    :MuiLink
                    {:styleOverrides
                     {:root {:cursor "pointer"}}}

                    :MuiMenuItem
                    {:styleOverrides
                     {:root
                      {"&.Mui-selected" {:backgroundColor "#d5d5d6!important"
                                         "&:hover"        {:backgroundColor "#d5d5d6"}}}}}}})
