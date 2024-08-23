(ns jon30.r
  (:require [clojisr.v1.r :as r :refer [r eval-r->java r->java java->r java->clj java->native-clj clj->java r->clj clj->r ->code r+ colon require-r]]
            [clojisr.v1.robject :as robject]
            [clojisr.v1.session :as session]
            [tech.v3.dataset :as dataset]
            [tablecloth.api :as tc]))






(defn r>base-function
  [col]
  (r (str "function(dat, num_knots = 13) {
  library(splines)
  library(tidybayes)
  knot_list <- quantile(dat$" col ", probs = seq(0, 1, length.out = num_knots))
  B <- bs(dat$" col ",
          knot = knot_list[-c(1, num_knots)],
          degree = 3,
          intercept = TRUE)
  class(B) <- 'matrix'
  B}")))

(defn base-function
  [data col & opts]
  (-> ((r>base-function col) data (or (first opts) 13))
      r->clj))

(comment
(r/set-default-session-type! :rserve)
(r/discard-all-sessions)
  (require '[tablecloth.api :as tc])
  (-> (tc/dataset (str "https://raw.githubusercontent.com/rmcelreath/rethinking/"
                         "slim/data/cherry_blossoms.csv")
                    {:separator ";"
                     :key-fn keyword})
         (tc/select-columns [:year :doy])
         (tc/select-rows (comp number? :doy))
         (base-function 5))
  ;;
  )
  

