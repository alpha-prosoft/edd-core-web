# Core library for doing re-frame applications



# Project template (originally re-gen)

Generator for initalizing re-frame web application using react, material-ui and router.

```
npm install -g shadow-cljs # Node.js and JVM are required to be installed
```
Options
```
npx @rpofuk/re-gen --help
Usage: re-gen [options] [command]

Contact management system

Options:
  -V, --version           output the version number
  -h, --help              output usage information

Commands:
  create|c <projectName>  Bootstrap project
  add|a <pageName>        Add a page
```

```
# Create projecet
npx @rpofuk/re-gen@1.2.1 create myproject
cd myproject 

# Create new view
npx @rpofuk/re-gen@1.2.1 add demo

# Running
shadow-cljs -A:dev watch :app
```


Generated project has this structure:
```

.
├── deps.edn
├── Dockerfile
├── package.json
├── package-lock.json
├── project.iml
├── resources
│   └── public
│       ├── devcards.html
│       └── index.html
├── shadow-cljs.edn
└── src
    └── main
        ├── edd
        │   ├── core.cljs
        │   ├── db.cljs
        │   ├── events.cljs
        │   ├── i18n.cljs
        │   ├── routing.cljs
        │   ├── subs.cljs
        │   ├── util.cljs
        │   └── views.cljs
        └── myproject
            ├── about
            │   ├── db.cljs
            │   ├── events.cljs
            │   ├── subs.cljs
            │   └── views.cljs
            ├── core.cljs
            ├── home
            │   ├── db.cljs
            │   ├── events.cljs
            │   ├── subs.cljs
            │   └── views.cljs
            ├── i18n.cljs
            └── styles.cljs

```

Adding new view
```
npx @rpofuk/re-gen add news

; src/main/myproject/core.cljs (dont forget required :))
; Add translations to i18n file for UX

(defn ^:export init
  [config]
  (core/init
    {...
     :panels       {...
                    :news news/main-panel}
     ...}))



```
Development

```
node bin/index.js
sudo npm install -g ./
```

During development you can use local installation:
```
npx re-gen --help
```

## Internationalization (i18n)

### Translation Function (tr)

The `tr` function in `src/edd/i18n.cljs` provides flexible translation capabilities with parameter substitution.

#### Usage Examples:

**Basic translation:**
```clojure
(tr :first-name)  ; Translates the :first-name key
```

**Nested key translation:**
```clojure
(tr [:user :first-name])  ; Translates {:user {:first-name "..."}}
```

**Map-based syntax:**
```clojure
(tr {:message :first-name})  ; Translates the :first-name key
(tr {:message [:user :first-name]})  ; Translates nested key
```

**With positional parameters:**
```clojure
(tr {:message :greeting
     :params ["Bob", "Alice"]})
; If translation is "Hello {0}, welcome {1}!"
; Returns: "Hello Bob, welcome Alice!"
```

**With named parameters:**
```clojure
(tr {:message :greeting
     :params {:name "Bob" :place "Berlin"}})
; If translation is "Hello {name}, welcome to {place}!"
; Returns: "Hello Bob, welcome to Berlin!"
```

#### Translation format:

In your translation files, use placeholders for parameters:
- Positional: `{0}`, `{1}`, `{2}`, etc.
- Named: `{name}`, `{place}`, `{count}`, etc.

Example translation structure:
```clojure
{:en {:greeting "Hello {name}!"
      :welcome "Welcome {0} to {1}"
      :user {:first-name "First Name"
             :last-name "Last Name"}}}
```