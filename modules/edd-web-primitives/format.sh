#!/bin/bash

clojure -Sdeps '{:deps {cljfmt/cljfmt {:mvn/version "0.8.0"}}}' -M -m cljfmt.main fix src
