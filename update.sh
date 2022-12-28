#!/bin/bash 

clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "2.2.970"}}}' -M -m antq.core 

