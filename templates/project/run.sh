#!/bin/bash

echo "Updating browser list"
npm install caniuse-lite
npx browserslist@latest --update-db &> /dev/null || echo "Browser list not updated."

clj -M:shadow-cljs:dev:test:cider-cljs watch app
