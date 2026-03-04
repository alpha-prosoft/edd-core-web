# Documentation

Dont over-document 



# Clojure Coding Conventions

## `let` binding formatting

Place the binding name on its own line, the value on the next line (indented),
and separate each binding pair with a blank line.

```clojure
;; good
(let [resolved-rules
      (read-rules ctx)

      role
      (get-in ctx [:meta :user :role])

      app-status
      (get-in ctx [:application :attrs :status])]
  (do-something resolved-rules role app-status))

;; bad — dense, hard to scan
(let [resolved-rules (read-rules ctx)
      role           (get-in ctx [:meta :user :role])
      app-status     (get-in ctx [:application :attrs :status])]
  (do-something resolved-rules role app-status))

## Testing 

Always put new line between expected and actual value, new line after (is ..), 
new line after each (is ...): 

;; good

(is 
  (true? 
  (:success result)))

(is 
  (true? 
  (:success result)))

;; bad 
(is (true? (:success result)))
(is (true? (:success result)))

```

## Prefer `let` over threading

Use `let` with named bindings instead of `->` / `->>` threading macros.
Named intermediate values make the data flow explicit and easier to debug.

```clojure
;; good
(let [with-cmds
      (update-in ctx [:edd-core :commands] apply-cmd-rules)

      with-queries
      (update-in with-cmds [:edd-core :queries] apply-query-rules)]
  with-queries)

;; bad
(-> ctx
    (update-in [:edd-core :commands] apply-cmd-rules)
    (update-in [:edd-core :queries] apply-query-rules))
```

## Prefer `get-in` over `->` for nested map access

```clojure
;; good
(get-in ctx [:meta :user :role])

;; bad
(-> ctx :meta :user :role)
```
