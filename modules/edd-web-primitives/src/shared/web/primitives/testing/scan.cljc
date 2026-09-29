(ns web.primitives.testing.scan
  "Read every element carrying an el-id and nest the readings by el-id path.

   Shared by the SDK (runs the scripts in the browser via Playwright) and the
   gallery (runs them directly), so both see exactly the same snapshot.

     \"first-name\"         -> {:first-name {:type :input :value \"John\" :label \"First name\"}}
     \"person--bla--ble\"   -> {:person {:bla {:ble {:type :text :text \"Ble\"}}}}

   Each leaf says what it is (:type). Controls carry their :value (and :label
   when the control has one); everything else carries its visible :text."
  (:require
   [clojure.string :as str]
   [web.primitives.testing.el-id :as el-id]))

(def read-value-js
  "JS function `el => {type, value, label, text}` that detects the primitive
   type of `el` from the rendered DOM and reads its current state."
  "el => {
     const cls  = (n) => n && n.className && n.className.toString ? n.className.toString() : '';
     const has  = (n, c) => cls(n).split(' ').indexOf(c) >= 0;
     const q    = (sel) => el.matches(sel) ? el : el.querySelector(sel);
     const trim = (s) => (s || '').trim();
     const labelOf = (input) => {
       if (!input) return null;
       const own = input.labels && input.labels.length ? trim(input.labels[0].textContent) : '';
       if (own) return own;
       const wrap = input.closest('.MuiFormControlLabel-root');
       const text = wrap ? trim(wrap.textContent) : '';
       return text || null;
     };

     const sw = q('input[role=\"switch\"]');
     if (sw) return {type: 'switch', value: sw.checked, label: labelOf(sw)};

     const cb = q('input[type=\"checkbox\"]');
     if (cb) return {type: 'checkbox', value: cb.checked, label: labelOf(cb)};

     if (el.getAttribute('role') === 'radiogroup') {
       const checked = el.querySelector('input[type=\"radio\"]:checked');
       return {type: 'radio', value: checked ? checked.value : null};
     }

     if (el.getAttribute('role') === 'combobox' || has(el, 'MuiSelect-select')) {
       return {type: 'select', value: trim(el.textContent)};
     }
     const selInner = el.querySelector('[role=\"combobox\"]');
     if (selInner) return {type: 'select', value: trim(selInner.textContent)};

     const slider = el.matches('.MuiSlider-root') ? el : el.querySelector('.MuiSlider-root');
     if (slider) {
       const input = slider.querySelector('input[type=\"range\"], input[type=\"hidden\"]');
       const v = input ? input.value : slider.querySelector('[role=\"slider\"]').getAttribute('aria-valuenow');
       return {type: 'slider', value: v == null ? null : parseFloat(v)};
     }

     const ac = el.matches('.MuiAutocomplete-root') ? el : el.querySelector('.MuiAutocomplete-root');
     if (ac) {
       const input = ac.querySelector('input');
       const chips = Array.from(ac.querySelectorAll('.MuiChip-label')).map(c => trim(c.textContent));
       return {type: 'autocomplete', value: chips.length ? chips : (input ? input.value : ''), label: labelOf(input)};
     }

     const input = el.matches('input, textarea') ? el : el.querySelector('input, textarea');
     if (input) return {type: 'input', value: input.value, label: labelOf(input)};

     return {type: 'text', text: trim(el.innerText)};
   }")

(def collect-js
  "JS function `scope => [[id, {type, value}] ...]` reading every element with
   an id or data-testid under the `scope` CSS selector (whole document when
   nil). Each id is read once, from the first element that carries it."
  (str
   "(scope) => {
      const readValue = " read-value-js ";
      const root = scope ? document.querySelector(scope) : document;
      if (!root) return [];
      const seen = new Map();
      root.querySelectorAll('[data-testid],[id]').forEach(e => {
        const id = e.getAttribute('data-testid') || e.id;
        if (id && !seen.has(id)) seen.set(id, readValue(e));
      });
      return Array.from(seen.entries());
    }"))

(defn ->path
  "Split an el-id string into its nested path of keywords.
     \"person--first-name\" -> [:person :first-name]"
  [id]
  (mapv keyword (str/split id (re-pattern el-id/separator))))

(defn default-ignore?
  "Ids that are gallery chrome, MUI-generated or icons rather than el-ids."
  [id]
  (or (str/blank? id)
      (str/starts-with? id "gallery")
      (str/starts-with? id "variant-")
      (str/ends-with? id "Icon")
      (str/starts-with? id ":r")))

(defn reading
  "Turn a raw reading from `read-value-js` (a map with string keys) into a
   leaf: {:type kw ...} holding :value/:label for controls, :text otherwise.
   Missing or nil fields are left out."
  [raw]
  (let [kind
        (keyword (get raw "type"))

        fields
        (if (= :text kind)
          {:text (get raw "text")}
          {:value (get raw "value")
           :label (get raw "label")})

        present
        (into {} (remove (comp nil? val) fields))]
    (assoc present :type kind)))

(def self-key
  "Key under which an element's own reading is kept when other el-ids nest
   below it, e.g. \"row\" next to \"row--amount\" -> {:row {:el/self .. :amount ..}}."
  :el/self)

(defn- leaf? [node]
  (and (map? node) (contains? node :type)))

(defn- branch? [node]
  (and (map? node) (not (leaf? node))))

(defn- nest-entry [acc [id leaf]]
  (let [path
        (->path id)]
    (if (branch? (get-in acc path))
      (assoc-in acc (conj path self-key) leaf)
      (assoc-in acc path leaf))))

(defn nest
  "Build the nested map from `[id leaf]` pairs, skipping ids for which
   `ignore?` is true. When one id is a prefix of another (\"row\" and
   \"row--amount\"), the shorter one is kept under `self-key` inside the
   deeper branch, so no element is lost."
  ([entries]
   (nest entries default-ignore?))
  ([entries ignore?]
   (let [kept
         (remove (fn [[id _]] (ignore? id)) entries)

         deepest-first
         (sort-by (fn [[id _]] (- (count (->path id)))) kept)]
     (reduce nest-entry {} deepest-first))))
