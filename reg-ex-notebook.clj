;; # Reg-Ex-101 Notebook

;; This is a [Clerk](https://book.clerk.vision/) notebook which allows you to enter a [Java regular expression](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html) 
;; and a text. It will then show you if and how the regular expression can be matches against the text.

;; > It's your own local https://regex101.com/

;; ---------------------------------------------------------------------------------------------

^{:nextjournal.clerk/visibility {:result :hide}}
(ns user
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]))

;; `reg-ex-of` returns a `java.util.regex.Pattern` for `x` if `x` is a _valid_ reg-ex. Else it returns a `String`
;; with an exception text.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn reg-ex-of [x]
  (try
    (java.util.regex.Pattern/compile x)
    (catch Throwable t (str "(reg-ex-of " (pr-str x) ") : " t))))

;; ---------------------------------------------------------------------------------------------
;; ## Data Model

;; First we define our notebook's **state** which will consist of **vars**/`atom`s. The state will 
;; change _re-actively_ to user input and changes will _cascade_ through sub-parts of the state.

;; `reg-ex-input` carries the 
;; regular expression as it is entered by the user. 
;; Below we will use `(input-widget #'reg-ex-input)` to create an input field for it.

;; > This `atom` will be **sync'ed** between the JVM runtime and the browser runtime ([sci](https://github.com/babashka/sci)).
;; So changes on either side will be propagated to the other. We _sync_ all of the 
;; _data model vars/atoms_ of this notebook.

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide}}
(def reg-ex-input (atom nil))

;; `reg-ex-str` carries the regular expression (`String`! Not `java.util.regex.Pattern`!) which has been entered by the user
;; __if__ it was a __valid__ regular expression. Else it will carry an exception message `String`
;; (see `reg-ex-of`).

;; > The error handling in this notebook is pretty bad/lame. Try enter invalid reg-ex and see how the app responds. It does work
;; but things can be improved.

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide}}
(def reg-ex-str (atom nil))

;; `text` carries the 
;; text (`String`) which is entered by the user and which will be matched against the regular expression in `reg-ex-str`.

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide}}
(def text (atom nil))

;; `match` carries the result of applying `re-matches` to `reg-ex-str` and `text` (see `update-match!` below).

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide}}
(def match (atom nil))

;; `matches` carries the result of applying `re-seq` to `reg-ex-str` and `text` (see `update-match!` below).

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide}}
(def matches (atom nil))

;; ---------------------------------------------------------------------------------------------
;; ## Model Updates

;; `update-match!` is used to propagate changes of `reg-ex-str` and `text`
;; to `match` and `matches`.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn update-match! []
  (reset! match
          (try (re-matches (reg-ex-of @reg-ex-str) @text)
               (catch Throwable t (str "update-match! " t))))

  (reset! matches
          (try (into [] (re-seq (reg-ex-of @reg-ex-str) @text))
               (catch Throwable t (str "update-match! " t)))))

;; Add a watch that will propagate any changes of `reg-ex-input` 
;; to `reg-ex-str`. 

^{:nextjournal.clerk/visibility {:result :hide}}
(add-watch
 reg-ex-input
 :process-reg-ex-input
 (fn [_ _ _ x] (reset! reg-ex-str (str (reg-ex-of x)))))

;; Add a watch that will _trigger_ a re-computation of `match` and `matches`
;; when `reg-ex-str` changes. 

;; > i.e. when `reg-ex-str` is _updated_. It need not be a different value. 

^{:nextjournal.clerk/visibility {:result :hide}}
(add-watch
 reg-ex-str
 :process-reg-ex-str
 (fn [& _] (update-match!)))

;; Add a watch that will _trigger_ a re-computation of `match` and `matches`
;; when `text` changes. 

^{:nextjournal.clerk/visibility {:result :hide}}
(add-watch
 text
 :process-text
 (fn [& _] (update-match!)))

;; ---------------------------------------------------------------------------------------------
;; ## User Interface

;; `input-widget` takes a **var** (pointing to an `atom`) and creates a `:render-fn` that renders (i.e. de-references) that **var**/`atom`
;; and propagates `on-change` events to the `atom`.
;; The `:render-fn` is executed in the browser (not the JVM). Since it de-references the **var**/`atom` it will be called automatically 
;; (_re-actively_) whenever the `atom` changes.
;; Use this function to create text input fields for entering text (`String`).

;; > Note that the empty map is just a dummy value we need for using `clerk/with-viewer` so that we can _install_ the `:render-fn` that does all the work.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn input-widget [a-var]
  (clerk/with-viewer
    {:render-fn
     `(fn [& _]
        [:input.bg-stone-400.text-gray-950
         {:size 100
          :value @@~a-var
          :on-change (fn [e] (reset! (deref ~a-var) (-> e .-target .-value)))}])}
    {}))

^{:nextjournal.clerk/visibility {:result :hide}}
(def reg-ex-input-widget
  (input-widget #'reg-ex-input))

;; `text-input-widget` is an HTML `textarea` input field which displays the content of `text` 
;; and propagates user input to `text`.

^{:nextjournal.clerk/visibility {:result :hide}}
(def text-input-widget
  (clerk/with-viewer
    {:render-fn
     '(fn [& _]
        [:textarea.bg-stone-400.text-gray-950
         {:value @text
          :rows 10
          :cols 40
          :on-change (fn [e] (reset! text (-> e .-target .-value)))}])}
    {}))

;; `render-match` returns the `String` value that will be displayed for the current value of `match`.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn render-match []
  (if-not @match
    "<NONE>"
    (if (string? @match)
      (pr-str @match)
      (str @match))))

;; The user interface:
;; 
;; * Enter the regular expression in the first row
;; * The second row displays the regular expression `<reg-ex>` in green as `[<reg-ex>]` and as a Java literal `String`. You can use this for copy&paste into Java sources.
;; * Enter the text (multi-line) in the third row. It will also be displayed as a Java literal `String` (so you can see line-breaks as `\n`).
;; * The fourth and fifth row display the matches for `re-matches` and `re-seq`.

;; > Click _show code_ to see the code.

^{:nextjournal.clerk/visibility {:code :fold}}
(clerk/html
 {:nextjournal.clerk/width :full}
 [:table.table-fixed.border-r.border-l.border-t.border-b

  [:tr.border-b
   [:td.border-r {:width "15%"} "Enter a regular expression"]
   [:td reg-ex-input-widget]]

  [:tr.border-b
   [:td.border-r "Regular expression"]
   [:td [:code.!bg-green-900 (str "[" @reg-ex-str "]")]
    [:br]
    "Java-String: " [:code.!bg-blue-900 (pr-str @reg-ex-str)]]]

  [:tr.border-b
   [:td.border-r "Text"]
   [:td
    [:table {:cellpadding "5"}
     [:tr
      [:td {:width "25%"} text-input-widget]
      [:td.whitespace-nowrap.keep-all "Java-String:"]
      [:td.break-all.font-mono.!bg-blue-900 {:width "70%"} (pr-str @text)]]]]]

  [:tr.border-b
   [:td.border-r "Match via re-matches"]
   [:td (str (render-match))]]

  [:tr
   [:td.border-r "Matches via re-seq"]
   [:td (str (or @matches "<NONE>"))]]])
