#!/bin/bash

echo "Updating browser list"
npx browserslist@latest --update-db &> /dev/null || echo "Browser list not updated."

clj -M:shadow-cljs:dev:test watch app
