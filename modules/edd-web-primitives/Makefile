SRC := src/main src/shared src/test src/gallery src/sdk src/it
VERSION ?= 1.0.0-SNAPSHOT
SUNEDITOR_CSS := src/main/web/primitives/text_editor/css/vendor

all: lint format

# Refresh the stock SunEditor CSS after bumping suneditor in src/main/deps.cljs.
# Fixes go in text_editor/css/overrides.css, never into the vendor files.
suneditor-css:
	mkdir -p $(SUNEDITOR_CSS)
	cp node_modules/suneditor/dist/suneditor.min.css $(SUNEDITOR_CSS)/
	cp node_modules/suneditor/dist/suneditor-contents.min.css $(SUNEDITOR_CSS)/

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

gallery:
	clojure -M:gallery -m shadow.cljs.devtools.cli watch gallery

gallery-release:
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

.PHONY: all lint format suneditor-css antiq clean run gallery gallery-release it it-report it-traces it-uberjar it-ci push jar sdk jars
