^{:clay {:kindly/options {:kinds-that-hide-code #{:kind/hiccup
                                                  :kind/md
                                                  :kind/html}}
         :quarto {:format {:revealjs {:theme :white
                                      :transition :fade
                                      :transition-speed :fast}}

                  :fontsize "2em"
                  :mainfont "Helvetica"
                  :monofont "Roboto Mono"}
         :hide-info-line true}}
(ns slides
  (:require [clojure.math :as math]
            [clojure.string :as str]
            [dorothy.core :as dot]
            [fastmath.core :as fastmath]
            [fastmath.interpolation :as i]
            [fastmath.random :as random]
            [fastmath.stats :as stats]
            [fastmath.transform :as transf]
            [jon30.core :as core]
            [jon30.data :as data]
            [jon30.graphviz :refer [digraph]]
            [jon30.r :as r-helpers]
            [scicloj.cmdstan-clj.v1.api :as stan]
            [scicloj.hanamicloth.v1.api :as haclo]
            [scicloj.hanamicloth.v1.plotlycloth :as ploclo]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.metamorph.ml :as ml]
            [scicloj.metamorph.ml.design-matrix :as dm]
            [scicloj.metamorph.ml.regression]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.datatype.functional :as fun]
            [tech.v3.tensor :as tensor]))

;; # {background-color="black" background-image="src/resources/slide-0.png" background-size="cover"}r
;; ::: {.notes}
;; Not working very well
;; :::

;; # {background-color="pblack" background-image="src/resources/slide-1.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::

;; # {background-color="black" background-image="src/resources/slide-2.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::

;; # {background-color="black" background-image="src/resources/slide-3.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::

;; # {background-color="black" background-image="src/resources/slide-4.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::


;; # {background-color="white" background-image="src/resources/slide-5.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::


;; # {background-color="white" background-image="src/resources/slide-7.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::

;; # {background-color="white" background-image="src/resources/slide-8.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::

;; # {background-color="white" background-image="src/resources/slide-9.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::

;; # {background-color="white" background-image="src/resources/slide-9.png" background-size="cover"}
;; ::: {.notes}
;; Not working very well
;; :::


;; # Multivariate regression
;; ::: {.notes}
;; Not working very well
;; :::
^:kindly/hide-code
(delay
  (let [formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:wind '(identity wind)]]]

        predict-ds
        (-> (for [a core/angles
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

        training-data (-> data/vpp-polar-01
                          tc/dataset)
        min-wind (-> training-data
                     :wind
                     tcc/reduce-min)

        min-angle (-> training-data
                      :angle
                      tcc/reduce-min)

        training-design-matrix (-> training-data
                                   (#(apply dm/create-design-matrix % formula)))

        model
        (-> training-design-matrix
            (ml/train {:model-type :fastmath/ols}))

        predictions
        (-> (ml/predict (-> predict-matrix
                            (tc/drop-columns [:velocity]))
                        model)
            (tc/add-column :angle (:angle predict-ds))
            (tc/add-column :wind (:wind predict-ds)))

        z-trace-for-surface
        (-> predictions
            (tc/drop-columns [0])
            (tc/pivot->wider :wind :velocity)
            (tc/drop-columns [:angle])
            (tc/rows))

        training-data-trace
        (-> training-data
            (tc/select-rows (comp not neg? :velocity))
            (tc/rename-columns {:angle :y
                                :wind :x
                                :velocity :z}))]
    (kind/plotly
     {:data [(-> {:type :surface
                  :colorscale "Greys"
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1200
               :height 600}})))

;; ## velocity ~ angle + angle<sup>2</sup>
;; ::: {.notes}
;; Quadratic polynomial
;; :::
^:kindly/hide-code
[[:velocity]
 [[:angle '(identity angle)]
  [:angle2 '(* angle angle)]
  [:wind '(identity wind)]
  [:wind2 '(* wind wind)]]]

;; ## Quadratic polynomial
;; ::: {.notes}
;; Quadratic polynomial. It may be a better fit, but it doesn't resemble the shape we are trying to fit.
;; :::
^:kindly/hide-code
(delay
  (let [formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:angle2 '(* angle angle)]
          [:wind '(identity wind)]
          [:wind2 '(* wind wind)]]]
        predict-ds
        (-> (for [a core/angles
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

        training-data (-> data/vpp-polar-01
                          tc/dataset)
        min-wind (-> training-data
                     :wind
                     tcc/reduce-min)

        min-angle (-> training-data
                      :angle
                      tcc/reduce-min)

        training-design-matrix (-> training-data
                                   (#(apply dm/create-design-matrix % formula)))

        model
        (-> training-design-matrix
            (ml/train {:model-type :fastmath/ols}))

        predictions
        (-> (ml/predict (-> predict-matrix
                            (tc/drop-columns [:velocity]))
                        model)
            (tc/add-column :angle (:angle predict-ds))
            (tc/add-column :wind (:wind predict-ds)))

        z-trace-for-surface
        (-> predictions
            (tc/drop-columns [0])
            (tc/pivot->wider :wind :velocity)
            (tc/drop-columns [:angle])
            (tc/rows))

        training-data-trace
        (-> training-data
            (tc/select-rows (comp not neg? :velocity))
            (tc/rename-columns {:angle :y
                                :wind :x
                                :velocity :z}))]
    (kind/plotly
     {:data [(-> {:type :surface
                  :colorscale "Greys"
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1200
               :height 600}})))

;; ## velocity ~ angle + angle<sup>2</sup>  + angle<sup>3</sup>  TODO! Parabola
;; ::: {.notes}
;; Cubic polynomial
;; :::
^:kindly/hide-code
[[:velocity]
 [[:angle '(identity angle)]
  [:angle2 '(* angle angle)]
  [:angle3 '(* angle angle)]
  [:wind '(identity wind)]
  [:wind2 '(* wind wind)]
  [:wind3 '(* wind wind wind)]]]

;; ## Parameters
;; ::: {.notes}
;; If you recall the equation we examined earlier for the cubic polynomial, we will now utilize the same one but also incorporate a cubic polynomial for the wind values.
;; :::


;; Velocity =

;; α₀ +

;; α₁ * Wind +

;; α₂ * Wind² +

;; α₃ * Wind³

;; β₀ +

;; β₁ * Angle +

;; β₂ * Angle² +

;; β₃ * Angle³



;; ## Cubic polynomial
;; ::: {.notes}
;; Cubic polynomial. Now we are it is starting to look
;; much better. But we are only looking at four wind speeds. We need
;; more. So lets look at a range from 4 to 30 knots
;; :::
^:kindly/hide-code
(delay
  (let [formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:angle2 '(* angle angle)]
          [:angle3 '(* angle angle angle)]
          [:wind '(identity wind)]
          [:wind2 '(* wind wind)]
          [:wind3 '(* wind wind wind)]]]
        predict-ds
        (-> (for [a core/angles
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

        training-data (-> data/vpp-polar-01
                          tc/dataset)
        min-wind (-> training-data
                     :wind
                     tcc/reduce-min)

        min-angle (-> training-data
                      :angle
                      tcc/reduce-min)

        training-design-matrix (-> training-data
                                   (#(apply dm/create-design-matrix % formula)))

        model
        (-> training-design-matrix
            (ml/train {:model-type :fastmath/ols}))

        predictions
        (-> (ml/predict (-> predict-matrix
                            (tc/drop-columns [:velocity]))
                        model)
            (tc/add-column :angle (:angle predict-ds))
            (tc/add-column :wind (:wind predict-ds)))

        z-trace-for-surface
        (-> predictions
            (tc/drop-columns [0])
            (tc/pivot->wider :wind :velocity)
            (tc/drop-columns [:angle])
            (tc/rows))

        training-data-trace
        (-> training-data
            (tc/select-rows (comp not neg? :velocity))
            (tc/rename-columns {:angle :y
                                :wind :x
                                :velocity :z}))]
    (kind/plotly
     {:data [(-> {:type :surface
                  :colorscale "Greys"
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1200
               :height 600}})))


;; ## Mo data, mo problems
;; ::: {.notes}
;;So now we have a lot more data to assist us in creating a more accurate model. However, we have encountered some issues. The program responsible for generating this data does not perform well at low wind speeds. Furthermore, there seems to be this anomaly where there is a sudden spike in data when the winds are strong. These discrepancies are puzzling. In the case of a sailing boat, there is typically a maximum speed a boat can reach. This phenomenon is related to the idea that the greater the force propelling the boat forward, the larger the wake it produces, which eventually causes the boat to climb onto its own wave at a steeper and steeper angle. But now lets do the cubic polynomial on this data.
;; :::
^:kindly/hide-code
(delay
  (let [formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:angle2 '(* angle angle)]
          [:angle3 '(* angle angle angle)]
          [:wind '(identity wind)]
          [:wind2 '(* wind wind)]
          [:wind3 '(* wind wind wind)]]]
        predict-ds
        (-> (for [a core/angles
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

        training-data (-> data/vpp-polar-01
                          tc/dataset)
        min-wind (-> training-data
                     :wind
                     tcc/reduce-min)

        min-angle (-> training-data
                      :angle
                      tcc/reduce-min)

        training-data        (-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
                                 (tc/rename-columns {:twa :angle
                                                     :tws :wind
                                                     :vessel-speed :velocity}))
        training-design-matrix (-> training-data
                                   (#(apply dm/create-design-matrix % formula)))

        model
        (-> training-design-matrix
            (ml/train {:model-type :fastmath/ols}))

        predictions
        (-> (ml/predict (-> predict-matrix
                            (tc/drop-columns [:velocity]))
                        model)
            (tc/add-column :angle (:angle predict-ds))
            (tc/add-column :wind (:wind predict-ds)))

        z-trace-for-surface
        (-> predictions
            (tc/drop-columns [0])
            (tc/pivot->wider :wind :velocity)
            (tc/drop-columns [:angle])
            (tc/rows))

        training-data-trace
        (-> training-data
            (tc/select-rows (comp not neg? :velocity))
            (tc/rename-columns {:angle :y
                                :wind :x
                                :velocity :z}))]
    (kind/plotly
     {:data [#_(-> {:type :surface
                  :colorscale "Greys"
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1200
               :height 600}})))


;; ## Better model
;; ::: {.notes}
;; This actually does a pretty good fit. It can deal with the unnatural spike very well
;; :::
^:kindly/hide-code
(delay
  (let [formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:angle2 '(* angle angle)]
          [:angle3 '(* angle angle angle)]
          [:wind '(identity wind)]
          [:wind2 '(* wind wind)]
          [:wind3 '(* wind wind wind)]]]
        predict-ds
        (-> (for [a core/angles
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

        training-data (-> data/vpp-polar-01
                          tc/dataset)
        min-wind (-> training-data
                     :wind
                     tcc/reduce-min)

        min-angle (-> training-data
                      :angle
                      tcc/reduce-min)

        training-data        (-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
                                 (tc/rename-columns {:twa :angle
                                                     :tws :wind
                                                     :vessel-speed :velocity}))
        training-design-matrix (-> training-data
                                   (#(apply dm/create-design-matrix % formula)))

        model
        (-> training-design-matrix
            (ml/train {:model-type :fastmath/ols}))

        predictions
        (-> (ml/predict (-> predict-matrix
                            (tc/drop-columns [:velocity]))
                        model)
            (tc/add-column :angle (:angle predict-ds))
            (tc/add-column :wind (:wind predict-ds)))

        z-trace-for-surface
        (-> predictions
            (tc/drop-columns [0])
            (tc/pivot->wider :wind :velocity)
            (tc/drop-columns [:angle])
            (tc/rows))

        training-data-trace
        (-> training-data
            (tc/select-rows (comp not neg? :velocity))
            (tc/rename-columns {:angle :y
                                :wind :x
                                :velocity :z}))]
    (kind/plotly
     {:data [(-> {:type :surface
                  :colorscale "Greys"
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1200
               :height 600}})))

;; ## Custom gradient
;; ::: {.notes}
;; First we crate the boundaries
;; :::
^:kind/hidden
(def color-boundries
  [0.011111111111
   0.222222222222
   0.333333333333
   0.444444444444
   0.555555555556
   0.666666666667
   0.777777777778
   0.888888888889
   1.0])

;; ## Custom colourscale
;; ::: {.notes}
;; - Here we create a custom scale.
;; - Velocities below zero are nonsensical to us, so let's color those areas of the surface.
;; :::

^:kind/hidden
(def color-custom-scale
  (into [[0.0 "rgb(220, 20, 60)"]]
        (mapv (fn [c n]  [c (str "rgb(" n
                                 ", " n
                                 ", " n
                                 ")")])
              color-boundries
              (->> color-boundries
                   count
                   (/ 255) ;; sorry Stuart
                   int
                   (range 1 255)))))

;; ## Analysis
;; ::: {.notes}
;; We can now observe some shortcomings of this model. While it is a relatively good fit, as is common with polynomials, issues arise at the boundaries where the model becomes erratic.
;; - The model performs poorly around the point (0, 0).
;; - Specifically, the orientation of the axis at 0 degrees regardless of wind strength is incorrect. It should ideally be a straight line representing 0 velocity at 0 angle.
;; - This is logical as a boat directly facing into the wind would not be able to sail, and the model accounts for this.
;; - Another related issue is that towards 180 degrees, the velocity begins to increase again. This outcome is unrealistic in practice.
;; - Nevertheless, we will proceed with this model for now. Why?
;; - The rationale is simple. Maintaining model simplicity is generally advantageous from various perspectives.
;; - It facilitates clear reasoning.
;; - In contrast to several other models we explored, such as splines and wavelets, this model is remarkably adept at avoiding overfitting peculiarities and data errors like the fin we examined.
;; - While not particularly relevant to our current scenario, polynomial models, especially those of lower degrees, tend to exhibit solid performance.
;; :::

^:kindly/hide-code
(delay
  (let [formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:angle2 '(* angle angle)]
          [:angle3 '(* angle angle angle)]
          [:wind '(identity wind)]
          [:wind2 '(* wind wind)]
          [:wind3 '(* wind wind wind)]]]
        predict-ds
        (-> (for [a core/angles
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

        training-data (-> data/vpp-polar-01
                          tc/dataset)
        min-wind (-> training-data
                     :wind
                     tcc/reduce-min)

        min-angle (-> training-data
                      :angle
                      tcc/reduce-min)

        training-data        (-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
                                 (tc/rename-columns {:twa :angle
                                                     :tws :wind
                                                     :vessel-speed :velocity}))
        training-design-matrix (-> training-data
                                   (#(apply dm/create-design-matrix % formula)))

        model
        (-> training-design-matrix
            (ml/train {:model-type :fastmath/ols}))

        predictions
        (-> (ml/predict (-> predict-matrix
                            (tc/drop-columns [:velocity]))
                        model)
            (tc/add-column :angle (:angle predict-ds))
            (tc/add-column :wind (:wind predict-ds)))

        z-trace-for-surface
        (-> predictions
            (tc/drop-columns [0])
            (tc/pivot->wider :wind :velocity)
            (tc/drop-columns [:angle])
            (tc/rows))

        training-data-trace
        (-> training-data
            (tc/select-rows (comp not neg? :velocity))
            (tc/rename-columns {:angle :y
                                :wind :x
                                :velocity :z}))]
    (kind/plotly
     {:data [(-> {:type :surface
                  :cauto false
                  :zmin 0
                  :colorscale color-custom-scale
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1200
               :height 600}})))

;; # Bayesian Statistics
;; ::: {.notes}
;; Now we aim to get into Bayesian Statistics. There are various reasons I mentioned earlier in the presentation, but now we have some more practical reasons for wanting to pursue it.
;; :::


;; ## Bayesian Statistics
;; ::: {.notes}
;; - Synthetic data
;; - Boat performance
;; - Troubleshoot
;; - Different Objective
;; - Hypothetical optimal performance
;; - Real-world measurements
;; - Update findings
;; - Bayesian approach
;; We've utilized synthetic data on our boat's performance to troubleshoot and delve into how we'd like to tackle this issue. However, we now aim to pursue a different objective. We may consider the data used thus far as a hypothetical optimal performance of our boat. In such a scenario, we aim to assess how well we truly adhere to this theoretical best possible performance. Thus, we'll need to incorporate real-world measurements on how we operate our boat to update our findings. This is a scenario where a Bayesian approach proves useful.;;
;; :::

;;

;; Using synthetic data as a benchmark for our boat's optimal performance, and through a Bayesian approach, we plan to compare and update this with real-world measurements to assess and enhance our actual performance.


;; ## Parameters
;; ::: {.notes}
;; In Bayesian analysis, we view the parameters not as a single fitted value, but as random variables with uncertainty. This means that all the alphas and betas have distributions, allowing us to reason about the likelihood of various parameters and, ultimately, different predicted values.
;; :::

;;

;; Velocity =

;; α₀ +

;; α₁ * Wind +

;; α₂ * Wind² +

;; α₃ * Wind³

;; β₀ +

;; β₁ * Angle +

;; β₂ * Angle² +

;; β₃ * Angle³

;; ## STAN in the house
;; ::: {.notes}
;; We can use STAN from Clojure with the library called cmdstan-clj
;; :::
;;STAN is a probabilistic programming language that enables users to create intricate statistical models and conduct Bayesian inference on them. It boasts some of the most efficient algorithms for modeling. STAN can be utilized in Clojure with the Scicloj library known as cmdstan-clj.


;; ## Modelling with STAN
;; ::: {.notes}
;; Follows simple C syntax. in the data block you define the observed data. in the parameters block we define what the unknown quantities ie. parameters are, in the transformed parameters you create a new parameter based on the parameters you set previously. the model is specified in the model block, at the end we define sk priors for our paramters. that is
;; :::

^:kindly/hide-code
(kind/md
 "```stan
data {
    int n;
    vector[n] angle;
    vector[n] angle2;
    vector[n] angle3;
    vector[n] wind;
    vector[n] wind2;
    vector[n] wind3;
    vector[n] velocity;
    array[n] int<lower=0,upper=1> should_observe;
}
parameters {
    real a0;
    real a1_angle;
    real a2_angle;
    real a3_angle;
    real a1_wind;
    real a2_wind;
    real a3_wind;
    real<lower=0> sigma;
}
transformed parameters {
    vector[n] mu;
    mu = a0 + a1_angle * angle + a2_angle * angle2 + a3_angle * angle3 + a1_wind * wind + a2_wind * wind2 + a3_wind * wind3;
}
model {
    for(i in 1:n) {
      if(should_observe[i]==1) {
        velocity[i] ~ normal(mu[i],sigma);
      }
    }
    sigma ~ exponential(10);
    a0 ~ normal(0,10);
    a1_angle ~ normal(0,10);
    a2_angle ~ normal(0,10);
    a3_angle ~ normal(0,10);
    a1_wind ~ normal(0,10);
    a2_wind ~ normal(0,10);
    a3_wind ~ normal(0,10);
}
```")

;; ## Compiling the model
;; ::: {.notes}
;; This complies the model, the core/jon-polynomial-modal-code refers to the same code we saw an previous slide.
;; :::
(def jon-polynomial-model
  (delay
    (stan/model core/jon-polynomial-model-code)))


;; ## Training the model
;; ::: {.notes}
;; Otherwise, we will use the same code as earlier to get a plot, but now we have to organize the data to send to STAN before we proceed with modeling.
;; :::

^:kindly/hide-code
(kind/md
 "```clojure
(-> full-training-data
    (tc/drop-columns [:part])
    (->> (into {}))
    (update-vals vec)
    (assoc :n (tc/row-count full-training-data))
    (#(stan/sample @jon-polynomial-model
                   %
                   {:num-samples 200}))
    :samples)
```")


;; ## Creating plots for diagnostics
^:kindly/hide-code
(kind/md
 "```clojure
     (for [k [:a0
              :a1_angle :a2_angle :a3_angle ;; :a4_angle
              :a1_wind :a2_wind :a3_wind    ;; :a4_wind
              :sigma]]
       (-> samples
           (ploclo/layer-point {:=x :i
                                :=y k
                                :=color :chain
                                :=color-type :nominal})
           ploclo/plot))
```")
;; ## Doing the modelling {.scrollable}
;; ::: {.notes}
;; To keep track of our modeling process, we also include some plots to ensure that everything is progressing as we expect. These look good!
;; :::

^:kindly/hide-code
(delay
  (core/plot-one-run @core/results-without-empirical
                {:colorscale "Greys"}))

;; ## Real measurements + syntethetic data
;; ::: {.notes}
;; We want to update the theoretical model with our real measured data
;; :::
^:kindly/hide-code
(delay
  (kind/plotly
   (let [min-angle (-> core/vpp-and-empirical-data
                       :angle
                       tcc/reduce-min)
         min-wind (-> core/vpp-and-empirical-data
                      :wind
                      tcc/reduce-min)]
     {:data  (-> core/vpp-and-empirical-data
                 (tc/select-rows (comp not neg? :velocity))
                 (tc/rename-columns {:angle :x
                                     :wind :y
                                     :velocity :z})
                 (tc/group-by [:part] {:result-type :as-map})
                 vals
                 (->> (map (fn [{:keys [x y z part]}]
                             {:type :scatter3d
                              :mode :markers
                              :name part
                              :marker {:size 5
                                       :opacity 0.8}
                              :x (tcc/- x min-angle)
                              :y (tcc/- y min-wind)
                              :z z}))))
      :layout {:width 1200
               :height 600}})))

;; ## Updating the synthetic model with the measurements
;; ::: {.notes}
;; We want to update the theoretical model with our real measured data
;; :::


^:kindly/hide-code
(delay
  (core/plot-one-run @core/results-with-empirical
                     {:colorscale "Greens"}))

;; ## Comparing synthetic with empirical
;; ::: {.notes}
;; We want to update the theoretical model with our real measured data
;; :::
;; Here are some numbers comparing the optimal performance and the actual performance of the two different distributions.


;; ## Comparing synthetic with empirical <- Daniel
;; ::: {.notes}
;; Fin
;; :::
;; We want to show the two surfaces only synthetic and after adding the empirical

^:kindly/hide-code
(def empirical-example-idx
  (let [rng (random/rng :isaac 5336)]
    (random/irandom rng (tc/row-count core/empirical-data))))

^:kindly/hide-code
(defn empirical-example [i]
  (-> core/empirical-data
      (tc/select-rows [i])
      (tc/rows :as-maps)
      first))

^:kindly/hide-code
(def n-vpp-examples
  (-> core/vpp-data
      tc/row-count))


^:kindly/hide-code
(defn show-empirical-example [i]
  (let [{:as example
         :keys [velocity]} (empirical-example i)]
    (->> [{:results @core/results-without-empirical
           :color "grey"
           :title "posterior without empirical"}
          {:results @core/results-with-empirical
           :color "green"
           :title "posterior with empirical"}]
         (map (fn [{:keys [results color title]}]
                (-> results
                    :samples

                    (tc/rename-columns {(keyword (str "mu." (+ n-vpp-examples
                                                               i
                                                               1)))
                                        :posterior-velocity})

                    (tc/select-columns :posterior-velocity)
                    (ploclo/base {:=title title})
                    (ploclo/layer-histogram {:=x :posterior-velocity
                                             :=histogram-nbins 30
                                             :=mark-color color})
                    (ploclo/update-data (constantly
                                         (tc/dataset
                                          {:x [10 10]
                                           :y [0 15]})))
                    (ploclo/layer-line {:=mark-size 4
                                        :=mark-color "red"})
                    ploclo/plot)))
         (cons (kind/hiccup
                [:h3 (str "empirical example #" i)]))
         kind/fragment)))

^:kindly/hide-code
(delay
  (show-empirical-example 9))

;; ## Comparing synthetic with empirical <- Daniel
;; ::: {.notes}
;; Fin
;; :::
;; Some point estimates. Take one empirical value show how it realates to the syntehtic distribution and how it relates to the posterior distributin

;; ## drumroll.. Polars
;; ::: {.notes}
;; Fin
;; :::

^:kindly/hide-code
(delay
  (let [{:keys [z-traces-for-surface
                min-angle
                min-wind]} @core/results-with-empirical]
    (-> (->> z-traces-for-surface
             tensor/->tensor
             (map-indexed
              (fn [i-stat stat-surfaces]
                (->> [5 15]
                     (map (fn [wind]
                            (let [velocities-by-angle (-> wind
                                                          (- 4)
                                                          stat-surfaces)]
                              (-> {:stat i-stat
                                   :wind wind
                                   :angle (-> velocities-by-angle
                                              count
                                              range
                                              (tcc/+ min-angle))
                                   :velocity velocities-by-angle}
                                  tc/dataset))))
                     (apply tc/concat))))
             (apply tc/concat))
        (tc/select-rows #(-> % :velocity pos?))
        (ploclo/layer-line
         {:=r :velocity
          :=theta :angle
          :=coordinates :polar
          :=color :wind
          :=color-type :nominal})
        ploclo/plot
        (assoc-in [:layout :polar]

                  {:angularaxis {:tickfont {:size 16}
                                 :direction "clockwise"}
                   :sector [-90 90]}))))


;; Polars, two polars with 6 knots and 12 knots. With credible intervals. Maybe next to each oter one for sythetic and one for posteriors


;; # Conclusion
;; We've made good progress. We can and will keep working on the actual model.

;; The tools Scicloj tools are there. We can work on pretty advanced problems with Clojure data science tools.

;; ## Selected Dependencies
;; ::: {.notes}
;; To tackle this issue, we made extensive use of several libraries within the Scicloj ecosystem. We heavily relied on tablecloth by Tomasz Sulej, which is akin to dplyr or Pandas in Clojure. Tablecloth builds upon the work of Chris Nuernberger in facilitating quick data analysis in Clojure. Visualization tools made use of Jon Anthonys' Hanami, a valuable resource for creating vega-lite templates. Carsten Behring's contributions were instrumental in modeling, predictions, and designing matrices. For mathematical models, fastmath by Tomasz Sulej was frequently utilized. Easy-to-use visualization tools like hanamicloth and plotlycloth were provided by Daniel Slustky and Kira McLean. All this work was carried out in Clay by Daniel Slutsky and others. However, you need not remember these individually since they have been consolidated into the meta-library known as Noj.
;; :::
;;
;; - "Tablecloth" by Tomasz Sulej

;; - Chris Nuernberger's work on fast data science on Clojure

;; - "Hanami" by Jon Anthonys

;; - "Metamorph"etc, Carsten Behring's contributions in modeling, predictions, and designing matrices

;; - "Fastmath" by Tomasz Sulej

;; - "Hanamicloth and Plotlycloth" by Daniel Slustky and Kira McLean

;; - "Clay" by Daniel Slutsky and others

;; - "Noj" which consolidated all the above libraries

;; - The Scicloj Community!


;; ## Noj
;;"Noj" brings together essential Clojure libraries for data science purposes and provides documentation on how to effectively utilize them in tandem.

;; Forget about version compatibility issues - "Noj" has your back.

;; Unsure about which tool is optimal for a specific task? "Noj" has thought about that already!

;; https://github.com/scicloj/noj


;; ## Opportunities to take advantage of
;; There are numerous opportunities here. If you are eager to learn, there are plenty of chances for you. You can explore Clojure, Data Science, or Open Source projects. Scicloj provides an excellent program for mentoring open source developers. Check out the website to learn more; it's a fantastic resource.
