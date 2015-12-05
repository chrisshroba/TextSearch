(ns textsearch.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [cljsjs.react]
            [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [put! take! chan >! <!]]))

; Simple logging function
(defn log [s] (.log js/console s))


; Set up database stuff
(def sqlite3 (js/require "sqlite3"))
(def db (atom nil))


(defn get-query
  []
  (let [query-field (dom/getElement "query")
        query (.-value query-field)]
    {:query query}))

(defn results-component
  [column-names data]
  [:table
   [:tr (for [name (vals column-names)]
               [:th name])]
   (for [datum data]
     [:tr (for [field-name (keys column-names)]
            [:td (get datum field-name)])])])

(defn foo
  []
  [:table.table
   (for [i (range 10)]
     ^{:key (str "tr-" i)} [:tr (for [j (range 3)]
                                  ^{:key (str "td-" j)} [:td (str "Row " i ", Col " j)])])])

(defn fill-table-rows
  [table column-names data]
;;     (reagent/render [results-component column-names data] table)
    (reagent/render [foo] (dom/getElement "results")
    (log 1)
    ))

(defn run-query
  [q]
  (let [out (chan)]
    (.all @db
          "SELECT * FROM message WHERE text LIKE ? LIMIT 10"
          (:query q)
          (fn [err, res]
            (put! out res)))
    out))

(defn start-query
  []
  (let [results-chan (-> (get-query)
                         run-query)]
    (take! results-chan
           (fn [results] (fill-table-rows
                          (dom/getElement "results")
                          {"handle_id" "Handle ID",
                           "text" "Text"}
                          results))))(log 2))

(defn main-page
  []
  [:div
   [:div
    "Enter query: "
    [:input#query {:type "text"}]]
   [:button#but {:on-click start-query} "Click me!!"]
   [:div#results
    [:table#results-table.table-striped]]])

(defn mount-root
  []
  (reagent/render [main-page] (dom/getElement "app")))

(defn setup-db
  []
  (reset! db (new sqlite3.Database
                  "/Users/chrisshroba/Library/Messages/chat.db")))


(defn init!
  []
  (mount-root))
(setup-db)
