^{:clay {:hide-info-line true
         :hide-ui-header true}}
(ns clay-examples
  (:require [scicloj.clay.v2.api :as clay]
            [clojure.string :as str]))

;; # 5555

(+ 1111 2222)

(comment
  (clay/make! {:source-path "src/jon30/clay_examples.clj"
               :format [:quarto :revealjs]
               :post-process (fn [html]
                               (-> html
                                   (str/replace
                                    #"<h1>5555</h1>"
                                    "")))}))
