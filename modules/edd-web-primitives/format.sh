#!/bin/bash

clojure -Sdeps '{:deps {cljfmt {:mvn/version "0.6.7"}}}' -m cljfmt.main fix src/main/ src/test
