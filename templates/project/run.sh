#!/bin/bash

aliases=${1-:""}
echo "Extra aliases $aliases"

echo "Updating browser list"
npm install caniuse-lite
npx browserslist@latest --update-db &> /dev/null || echo "Browser list not updated."

echo "Starging shadow"

clj -M:shadow-cljs:dev:test:cider-cljs:$aliases watch app
