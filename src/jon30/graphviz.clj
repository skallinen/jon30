

(ns jon30.graphviz
  (:require [dorothy.core :as dot]
            [dorothy.jvm]
            [scicloj.kindly.v4.kind :as kind]))

(defn digraph [spec]
  (-> spec
      dot/digraph
      dot/dot
      (dorothy.jvm/render {:format :svg})
      kind/html))

(digraph [[:b0 :> :b1 :> :b2 :> :b3]
          [:b2 :> :b0]])
