#!/bin/bash

aliases=${1-:""}
echo "Extra aliases $aliases"

echo "Installing npm deps from edd-web-primitives"
clj -M:shadow-cljs run shadow.cljs.npm-deps

echo "Starting widgets gallery on http://localhost:3009"
clj -M:shadow-cljs:dev:gallery:cider-cljs$aliases watch gallery
