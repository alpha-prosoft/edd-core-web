
all: lint format

lint:
	clojure -M:lint --lint src/main src/test

format:
	clojure -Sdeps '{:deps {cljfmt/cljfmt {:mvn/version "0.9.2"}}}' \
			-M -m cljfmt.main fix src/main/ src/test

antiq:
	clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "RELEASE"}}}' -M -m antq.core

clean:
	bash clean.sh

run:
	shadow-cljs watch devcards -A:dev

push:
	git push origin HEAD:refs/for/master%topic=env/${ENV}