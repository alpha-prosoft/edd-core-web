SRC := src/main src/shared src/test src/gallery src/sdk src/it
VERSION ?= 1.0.0-SNAPSHOT
SUNEDITOR_VENDOR := src/main/web/primitives/text_editor/vendor

all: lint format

# Refresh the stock SunEditor bundle and CSS after bumping suneditor in
# src/main/deps.cljs. Fixes go in text_editor/vendor/overrides.css, never into
# the copied vendor files.
suneditor-vendor:
	mkdir -p $(SUNEDITOR_VENDOR)
	cp node_modules/suneditor/dist/suneditor.min.js $(SUNEDITOR_VENDOR)/
	cp node_modules/suneditor/dist/suneditor.min.css $(SUNEDITOR_VENDOR)/
	cp node_modules/suneditor/dist/suneditor-contents.min.css $(SUNEDITOR_VENDOR)/

suneditor-vendor-check: npm-deps
	bash check-suneditor-vendor.sh

lint:
	clojure -M:lint --lint $(SRC)

format:
	clojure -Sdeps '{:deps {cljfmt/cljfmt {:mvn/version "0.9.2"}}}' \
			-M -m cljfmt.main fix $(SRC)

antiq:
	clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core

clean:
	bash clean.sh

run: gallery

# shadow-cljs 3.5+ no longer installs npm deps implicitly; do it explicitly.
npm-deps:
	clojure -M:gallery -m shadow.cljs.devtools.cli run shadow.cljs.npm-deps

gallery: npm-deps
	clojure -M:gallery -m shadow.cljs.devtools.cli watch gallery

gallery-release: npm-deps
	clojure -M:gallery -m shadow.cljs.devtools.cli release gallery

it:
	rm -rf target/pw-traces
	clojure -M:it; status=$$?; clojure -M:it-report; exit $$status

it-report:
	clojure -M:it-report

it-traces:
	clojure -M:it-traces

it-uberjar:
	clojure -T:build it-uberjar

it-ci: it-uberjar gallery-release
	bash run-it.sh

jar:
	clojure -T:build primitives :version '"$(VERSION)"'

sdk:
	clojure -T:build sdk :version '"$(VERSION)"'

jars:
	clojure -T:build all :version '"$(VERSION)"'

push:
	git push origin HEAD:refs/for/master%topic=env/${ENV}

.PHONY: all lint format suneditor-vendor suneditor-vendor-check npm-deps antiq clean run gallery gallery-release it it-report it-traces it-uberjar it-ci push jar sdk jars
