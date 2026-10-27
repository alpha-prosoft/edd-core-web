{:npm-deps {:create-react-class   "15.7.0"
            "@mui/x-tree-view"    "9.16.0"
            "@mui/x-date-pickers" "9.16.0"
            "@mui/material"       "9.5.0"
            "@mui/icons-material" "9.5.0"
            "@mui/lab"            "9.0.0-beta.10"
            "@emotion/react"      "11.14.0"
            "@emotion/styled"     "11.14.1"
            :highlight.js         "11.12.0"
            :d3-scale             "4.0.2"
            :date-fns             "4.4.0"
            :dayjs                "1.11.23"
            :g                    "2.0.1"
            :marked               "18.1.2"
            :moment               "2.31.0"
            :react                "19.3.1"
            :react-dom            "19.3.1"
            :react-number-format  "5.4.5"
            :react-dropzone       "20.1.4"
            :shadow-cljs          "3.5.8"
            ;; Only used to vendor its prebuilt bundle and CSS
            ;; (`make suneditor-vendor`); not required from ClojureScript.
            :suneditor            "3.4.0"
            :dompurify            "3.4.17"
            ;; Browser shim for `process`. React 19's production build references
            ;; the global and shadow-cljs resolves it from node_modules.
            :process              "0.11.10"}}
