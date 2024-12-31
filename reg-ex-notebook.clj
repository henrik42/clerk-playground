^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(ns user
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk-slideshow :as slideshow]))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(clerk/add-viewers! [slideshow/viewer])

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn reg-ex-of [x]
  (try
    (java.util.regex.Pattern/compile x)
    (catch Throwable t (str "(reg-ex-of " (pr-str x) ") : " t))))

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide :code :hide}}
(def reg-ex-input (atom nil))

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide :code :hide}}
(def reg-ex-str (atom nil))

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide :code :hide}}
(def text (atom nil))

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide :code :hide}}
(def match (atom nil))

^{::clerk/sync true :nextjournal.clerk/visibility {:result :hide :code :hide}}
(def matches (atom nil))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn update-match! []
  (reset! match
          (try (re-matches (reg-ex-of @reg-ex-str) @text)
               (catch Throwable t (str "update-match! " t))))

  (reset! matches
          (try (into [] (re-seq (reg-ex-of @reg-ex-str) @text))
               (catch Throwable t (str "update-match! " t)))))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(add-watch
 reg-ex-input
 :process-reg-ex-input
 (fn [_ _ _ x] (reset! reg-ex-str (str (reg-ex-of x)))))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(add-watch
 reg-ex-str
 :process-reg-ex-str
 (fn [& _] (update-match!)))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(add-watch
 text
 :process-text
 (fn [& _] (update-match!)))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn input-widget [a-var]
  (clerk/with-viewer
    {:render-fn
     `(fn [& _]
        [:input.bg-stone-400.text-gray-950
         {:size 100
          :value @@~a-var
          :on-change (fn [e] (reset! (deref ~a-var) (-> e .-target .-value)))}])}
    {}))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(def reg-ex-input-widget
  (input-widget #'reg-ex-input))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
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

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn render-match []
  (if-not @match
    "<NONE>"
    (if (string? @match)
      (pr-str @match)
      (str @match))))

^{:nextjournal.clerk/visibility {:code :hide}}
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
