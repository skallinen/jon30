^{:clay {:kindly/options {:kinds-that-hide-code #{:kind/hiccup :kind/md :kind/html}}}}
(ns jon30.core
  (:require
   [clojure.string :as str]
   [dorothy.core :as dot]
   [fastmath.interpolation :as i]
   [fastmath.random :as random]
   [jon30.data :as data]
   [jon30.graphviz :refer [digraph]]
   [jon30.r :as r-helpers]
   [scicloj.cmdstan-clj.v1.api :as stan]
   [scicloj.hanamicloth.v1.api :as haclo]
   [scicloj.hanamicloth.v1.plotlycloth :as ploclo]
   [scicloj.metamorph.ml :as ml]
   [scicloj.metamorph.ml.design-matrix :as dm]
   [scicloj.metamorph.ml.regression]
   [tablecloth.api :as tc]
   [tablecloth.column.api :as tcc]))

;; # Workbook, notes and explorations
;; Please note that this document is not intended for the conference presentation, but is a working document.
;; # 1.  Question/goal/estimand
;; Predicting boat velocity based on wind

;; # 2. Causal models
;;  Optimizing the velocity of a sailing vessel involves carefully managing a range of interconnected factors. Central to this is the sail plan, which directly influences the total sail area and, in turn, the vessel's speed. The wind angle and strength also play crucial roles, as they determine how effectively the sails can harness wind power. A well-optimized sail plan will maximize the total sail area appropriate for the conditions, ensuring that the sails are configured to capture the wind most efficiently. Additionally, the hull speed, influenced by the boat's length at the waterline and the heeling angle, is a critical determinant of overall velocity. Reducing friction and maintaining a favorable heeling angle can significantly enhance the hull speed, contributing to a faster vessel.  

;; The human factor is equally vital in optimizing sailing speed. The captain's competence directly impacts both their performance and the performance of the crew, both of which are essential for maintaining high vessel velocity. Managing fatigue for both the captain and crew is crucial, as fatigue can diminish performance, thereby reducing the vessel's velocity. A well-coordinated and skilled crew, led by an experienced and alert captain, can make the necessary adjustments to the sail plan, respond to changing wind conditions, and maintain an optimal heeling angle, all of which contribute to maximizing the vessel's velocity on the water.
;; ## Reasoning about the model
;; Here is a rough sketch of how these factors are interrelated based on first principles.The velocity of the boat is a result of the interaction between aerodynamic and hydrodynamic forces.

(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:aerodynamic-forces {:label "Aerodynamic Forces"
                           :shape :box}]

     [:hydrodynamic-forces {:label "Hydrodynamic Forces"
                            :shape :box}]

     [:vessel-velocity {:label "Boat's Velocity"
                        :shape :circle}]


     [:aerodynamic-forces :> :vessel-velocity]
     [:hydrodynamic-forces :> :vessel-velocity]]
    digraph)

;;  The wind direction and speed are the forces that propel the vessel.
(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:wind-angle-strength {:label "Wind Angle and Strength"
                            :shape :ellipse}]
     [:aerodynamic-forces {:label "Aerodynamic Forces"
                           :shape :box}]

     [:hydrodynamic-forces {:label "Hydrodynamic Forces"
                            :shape :box}]

     [:vessel-velocity {:label "Boat's Velocity"
                        :shape :circle}]

     [:wind-angle-strength :> :aerodynamic-forces]
     [:wind-angle-strength :> :hydrodynamic-forces]
     [:aerodynamic-forces :> :vessel-velocity]
     [:hydrodynamic-forces :> :vessel-velocity]]
    digraph)

;;  The current also and waves are also important variables.
(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:wind-angle-strength {:label "Wind Angle and Strength"
                            :shape :ellipse}]
     [:aerodynamic-forces {:label "Aerodynamic Forces"
                           :shape :box}]

     [:current {:label "Current"
                :shape :ellipse}]
     [:waves {:label "Waves"
              :shape :ellipse}]
     [:hydrodynamic-forces {:label "Hydrodynamic Forces"
                            :shape :box}]

     [:vessel-velocity {:label "Boat's Velocity"
                        :shape :circle}]

     [:wind-angle-strength :> :aerodynamic-forces]
     [:wind-angle-strength :> :hydrodynamic-forces]

     [:waves :> :hydrodynamic-forces]
     [:current :> :hydrodynamic-forces]

     [:aerodynamic-forces :> :vessel-velocity]
     [:hydrodynamic-forces :> :vessel-velocity]]
    digraph)

;; Let's provide more specifics about what influences the forces.
(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:total-sail-area {:label "Total Sail Area"}]
     [:wind-angle-strength {:label "Wind Angle and Strength"
                            :shape :ellipse}]
     [:sail-plan {:label "Sail Plan"}]
     [:sail-trim {:label "Sail Trim"}]
     [:aerodynamic-forces {:label "Aerodynamic Forces"
                           :shape :box}]

     [:hull-speed {:label "Hull (Max) Speed"}]
     [:boat-length-waterline {:label "Boat's Length at the Waterline"}]
     [:heeling-angle {:label "Heeling Angle"}]
     [:friction {:label "Hull Friction"}]

     [:current {:label "Current"
                :shape :ellipse}]
     [:waves {:label "Waves"
              :shape :ellipse}]
     [:hydrodynamic-forces {:label "Hydrodynamic Forces"
                            :shape :box}]

     [:vessel-velocity {:label "Boat's Velocity"
                        :shape :circle}]

     [:sail-plan :> :total-sail-area]
     [:sail-trim :> :total-sail-area]
     [:heeling-angle :> :total-sail-area]
     [:total-sail-area :> :aerodynamic-forces]
     [:wind-angle-strength :> :aerodynamic-forces]
     [:aerodynamic-forces :> :heeling-angle]

     [:hull-speed :> :hydrodynamic-forces]
     [:boat-length-waterline :> :friction]
     [:boat-length-waterline :> :hull-speed]
     [:heeling-angle :> :boat-length-waterline]
     [:heeling-angle :> :friction]
     [:heeling-angle :> :hull-speed]
     [:heeling-angle :> :hydrodynamic-forces]
     [:friction :> :hydrodynamic-forces]


     [:waves :> :hydrodynamic-forces]
     [:current :> :hydrodynamic-forces]

     [:aerodynamic-forces :> :vessel-velocity]
     [:hydrodynamic-forces :> :vessel-velocity]]
    digraph)
;; Finally, we can incorporate an idea about how the crew impacts the outcome.
(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:total-sail-area {:label "Total Sail Area"}]
     [:wind-angle-strength {:label "Wind Angle and Strength"
                            :shape :ellipse}]
     [:sail-plan {:label "Sail Plan"}]
     [:sail-trim {:label "Sail Trim"}]

     [:current {:label "Current"
                :shape :ellipse}]
     [:waves {:label "Waves"
              :shape :ellipse}]
     [:aerodynamic-forces {:label "Aerodynamic Forces"
                           :shape :box}]

     [:hull-speed {:label "Hull (Max) Speed"}]
     [:boat-length-waterline {:label "Boat's Length at the Waterline"}]
     [:heeling-angle {:label "Heeling Angle"}]
     [:friction {:label "Hull Friction"}]
     [:hydrodynamic-forces {:label "Hydrodynamic Forces"
                            :shape :box}]

     [:captain-crew-competence {:label "Captain's and Crew's Competence"}]
     [:crew-performance {:label "Crew's Performance"}]
     [:fatigue-management {:label "Fatigue Management"}]

     [:vessel-velocity {:label "Boat's Velocity"
                        :shape :circle}]

     [:sail-plan :> :total-sail-area]
     [:sail-trim :> :total-sail-area]
     [:heeling-angle :> :total-sail-area]
     [:total-sail-area :> :aerodynamic-forces]
     [:wind-angle-strength :> :aerodynamic-forces]
     [:aerodynamic-forces :> :heeling-angle]

     [:hull-speed :> :hydrodynamic-forces]
     [:boat-length-waterline :> :friction]
     [:boat-length-waterline :> :hull-speed]
     [:heeling-angle :> :boat-length-waterline]
     [:heeling-angle :> :friction]
     [:heeling-angle :> :hull-speed]
     [:heeling-angle :> :hydrodynamic-forces]
     [:friction :> :hydrodynamic-forces]

     [:captain-crew-competence :> :crew-performance]
     [:captain-crew-competence :> :fatigue-management]
     [:fatigue-management :> :crew-performance]
     [:crew-performance :> :sail-plan]
     [:crew-performance :> :sail-trim]

     [:waves :> :hydrodynamic-forces]
     [:current :> :hydrodynamic-forces]

     [:aerodynamic-forces :> :vessel-velocity]
     [:hydrodynamic-forces :> :vessel-velocity]]
    digraph)


;;But we will not start by modeling all of this right now. To understand the problem and showcase some of the tools today, we need to simplify the problem a lot. So today, the goal is to be able to model the vessel's speed based on wind angle and wind strength.
(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:wind-angle {:label "Wind Angle"}]
     [:wind-strength {:label "Wind Strength"}]
     [:vessel-velosity {:label "Boat's velocity"
                        :shape :circle}]
     [:wind-angle :> :vessel-velosity]
     [:wind-strength :> :vessel-velosity]]
    digraph)

;; And even earlier than that. Let's further simplify the issue by only considering the wind angle and assuming that the wind always blows at 6 knots.
(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:wind-angle {:label "Wind Angle"}]
     [:vessel-velosity {:label "Boat's velocity"
                        :shape :circle}]
     [:wind-angle :> :vessel-velosity]]
    digraph)


;; But let's introduce an unobserved variable and an error term that will incorporate all the other variables.
(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:wind-angle {:label "Wind Angle"}]
     [:error {:label "Error"}]
     [:vessel-velosity {:label "Boat's velocity"
                        :shape :circle}]
     [:error :> :vessel-velosity]
     [:wind-angle :> :vessel-velosity]]
    digraph)

#_(def sos
    (tc/dataset "https://cdn.sanity.io/files/jo7n4k8s/production/262f04c41d99fea692e0125c342e446782233fe4.zip/stack-overflow-developer-survey-2024.zip"))

#_(-> sos
      (get "MiscTechHaveWorkedWith")
      rand-nth)
;; # 3. Statistical model(s)
;; # 3.1 Simulate data
;; # 3.2 Build a model
;; # 4. Validate model
;; # 5. Analyze data
;; # 5.1 Import and clean recorded data
;; # 5.2 Run model and analyze results

;; # 0. Playground

#_(require '[scicloj.clay.v2.api :as clay])
#_(clay/make! {:source-path "/Users/samikallinen/projects/jon30/src/jon30/core.clj"})
;; Generating synthetic data to develop models. Utilizing a VPP (Velocity Prediction Program), a tool that uses a physical model to compute the polars. Data is created using a free online tool by inputting boat data to generate polars for wind strengths of 6, 10, 15, and 20 knots. The tool produces a polar diagram as an image. Without access to an API or exact values, I used SketchUp to scale the image correctly and manually measured the specific points below.

(->
 data/vpp-polar-01
 (->>
  (map #(update % :wind str)))
 tc/dataset
 (ploclo/layer-line
  {:=r :velocity
   :=theta :angle
   :=coordinates :polar
   :=rotation 90
   :=color :wind})
 ploclo/plot
 (assoc-in [:layout :polar]

           {:angularaxis {:tickfont {:size 16}
                          :direction "clockwise"}
            :sector [-90 90]}))

;; ## Explore in cartesian coordinates
(def angles (range 1 (inc 180)))

(->
 data/vpp-polar-01
 (->>
  (map #(update % :wind str)))
 tc/dataset
 (haclo/plot haclo/point-chart
             {:=x :angle
              :=y :velocity
              :=color :wind}))

;; ## Pick one curve
;; First, let's create a helper function to filter out a single curve (wind strength) from the data.
(defn wind-strength
  [strength]
  (->>  data/vpp-polar-01
        (filter (comp #{strength} :wind))
        (map #(update % :wind str))
        tc/dataset))

;; Let's start with the 6 knot winds.
(-> (wind-strength 6)
    (haclo/plot haclo/point-chart
                {:=x :angle
                 :=y :velocity
                 :=color :wind}))

;; # Regressions
;; Let's analyze the curves using regression. First, we'll demonstrate a basic linear regression, which obviously will not provide a good fit.

(-> (wind-strength 6)
    (haclo/base
     {:=x :angle})
    (haclo/layer-point
     {:=y :velocity
      :=color :wind})
    (haclo/update-data  (fn [_]
                          (->
                           (wind-strength 6)
                           (dm/create-design-matrix
                         ;; (velocity ~ angle)
                            [:velocity]
                            [[:angle '(identity angle)]])
                           (ml/train {:model-type :fastmath/ols})
                           (->> (ml/predict (tc/dataset {:angle angles})))
                           (tc/add-column :angle angles)
                           (tc/add-column :wind "linear regression")
                           (tc/rename-columns {:velocity :velocity-lm}))))
    (haclo/layer-line {:=y :velocity-lm
                       :=color :wind}))

;; ## Polynomials
;; Polynomials provide us with greater flexibility when fitting the curve.


;; ### Creating a helper function that predicts the vessel's velocity based on the wind strength and direction.

(defn wind-regression-predict
  [{:keys [angles strength formula reg-name]}]
  (->
   (wind-strength strength)
   (#(apply dm/create-design-matrix % formula))
   (ml/train {:model-type :fastmath/ols})
   (->> (ml/predict (-> {:angle angles
                         :velocity 0}
                        tc/dataset
                        (#(apply dm/create-design-matrix
                                 %
                                 formula))
                        (tc/drop-columns [:velocity]))))
   (tc/add-column :angle angles)
   (tc/add-column :wind (str reg-name "-regression-" strength))
   (tc/rename-columns {:velocity (keyword (str "velocity-" strength "-"reg-name))})))

;; ### A forumla for cubic regression
;; This corresponds to something like `(velocity ~ angle + angle^2 + angle^3)` in R.
(def cubic-formula [[:velocity]
                    [[:angle '(identity angle)]
                     [:angle2 '(* angle angle)]
                     [:angle3 '(* angle angle angle)]]])

(->
 (wind-strength 6)
 (haclo/base
  {:=x :angle})
 (haclo/layer-point
  {:=y :velocity
   :=color :wind})
 (haclo/update-data  (fn [_]
                       (wind-regression-predict
                        {:angles   angles
                         :strength 6
                         :formula  cubic-formula
                         :reg-name "cubic"})))
 (haclo/layer-line {:=y :velocity-6-cubic
                    :=color :wind})
 (haclo/update-data (fn [_]
                      (-> (wind-strength 20)
                          (tc/rename-columns {:velocity :velocity-20}))))
 (haclo/layer-point {:=y :velocity-20
                     :=color :wind})
 (haclo/update-data (fn [_]
                      (wind-regression-predict
                       {:angles   angles
                        :strength 20
                        :formula  cubic-formula
                        :reg-name "cubic"})))
 (haclo/layer-line {:=y :velocity-20-cubic
                    :=color :wind}))
;; Not bad at all, one key benefit of these polynomials is that they are highly efficient to calculate, which is not so much our concern currently, but they do have some drawbacks as well.
(->
 (wind-strength 6)
 (haclo/base
  {:=x :angle})
 (haclo/layer-point
  {:=y :velocity
   :=color :wind})
 (haclo/update-data (fn [_]
                      (wind-regression-predict
                       {:angles   (range 1 (inc 360))
                        :strength 6
                        :formula  cubic-formula
                        :reg-name "cubic"})))
 (haclo/layer-line {:=y :velocity-6-cubic
                    :=color :wind})
 (haclo/update-data (fn [_]
                      (-> (wind-strength 20)
                          (tc/rename-columns {:velocity :velocity-20}))))
 (haclo/layer-point {:=y :velocity-20
                     :=color :wind})
 (haclo/update-data  (fn [_]
                       (wind-regression-predict
                        {:angles   (range 1 (inc 360))
                         :strength 20
                         :formula  cubic-formula
                         :reg-name "cubic"})))
 (haclo/layer-line {:=y :velocity-20-cubic
                    :=color :wind})
 ;;
 )

;; ## Generate splines to explore the curves
(def splines
  (->> data/vpp-polar-01
       (group-by :wind)
       vals
       (map (fn [v]
              {(:wind (first v)) (i/interpolation :b-spline
                                                  (map :angle v)
                                                  (map :velocity v))}))
       (mapcat identity)
       (into {})))

#_(def knots
    (->> data/vpp-polar-01
         (group-by :wind)
         vals
         (map (fn [v]
                {(:wind (first v))
                 (let [degree nil
                       xs (m/seq->double-array (map :angle v))
                       ys (m/seq->double-array (map :velocity v))
                       ^MathFunction obj (BSpline. xs ys (unchecked-int (or degree (m/dec (count xs)))))]
                   {:f      (fn ^double [^double x] (.evaluate obj x))
                    :knots (.getKnots obj)
                    :y (.getY obj)
                    :x (.getX obj)})}
                #_{(:wind (first v)) (i/interpolation :b-spline
                                                      (map :angle v)
                                                      (map :velocity v))}))
         (mapcat identity)
         (into {})))

(def spline-ds
  (->> splines
       (map (fn [[w f]]
              (let [angles (range 181)]
                (map (fn [angle]
                       {:angle angle
                        :velocity (f angle)
                        :wind (str "spline-" w)}) angles))))
       (mapcat identity)
       #_(into data/vpp-polar-01)
       (map #(update % :wind str))
       tc/dataset))
;; ## Spline for 10 knots of wind
(-> spline-ds
    (tc/select-rows (comp #{"spline-6"} :wind))
    (tc/rename-columns {:velocity :velocity-spline})
    (haclo/base
     {:=x :angle})
    (haclo/layer-line {:=y :velocity-spline
                       :=color :wind})
    (haclo/update-data  (fn [_]
                          (wind-strength 6))

                        ;;
                        )
     (haclo/layer-point
  {:=y :velocity
   :=color :wind}))


;; # Genareting data for constant 6 knot wind
;; Generating syntethetic measurement points pretending the wind is always at streanth 6

(->> (repeatedly 300 #(rand-int 180))
     (map (fn [angle]
            (let [mu ((get splines 6) angle)
                  velocity (random/sample
                         (random/distribution :normal
                                              {:sd 0.3 :mu mu}))]
              {:angle angle
               :velocity velocity
               :wind "spline-6"})))
     tc/dataset
     (#(haclo/plot %  haclo/point-chart
                   {:=x :angle
                    :=y :velocity})))

;; # Testing stan
(comment

  (def model-code
    "
data {
      int<lower=0> N;
      array[N] int<lower=0,upper=1> y;
      }
parameters {
            real<lower=0,upper=1> theta;
            }
model {
       theta ~ beta(1,2);  // uniform prior on interval 0,1
       y ~ bernoulli(theta);
}
")

  (def model
    (stan/model model-code))

  (def data
    {:N 10
     :y [0 1 0 0 0 0 0 0 0 1]})
  (def sampling
    (stan/sample model data {:num-chains 4}))
  ;;
  )

(def stan-spline-model-code
  "
data {
    int n;
    int k;
    array[n] int velocity;
    matrix[n, k] B;
}
parameters {
    real a;
    vector[k] w;
    real<lower=0> sigma;
}
transformed parameters {
    vector[n] mu;
    mu = a + B * w;
}
model {
    for (i in 1:n) {
        velocity[i] ~ normal(mu[i], sigma);
    }
    a ~ normal(100, 10);
    w ~ normal(0, 10);
    sigma ~ exponential(1);
}")

(def blossom-model (stan/model stan-spline-model-code))

#_(-> (tc/dataset (str "https://raw.githubusercontent.com/rmcelreath/rethinking/"
                     "slim/data/cherry_blossoms.csv")
                {:separator ";"
                 :key-fn keyword})
    (tc/select-columns [:year :doy])
    (tc/select-rows (comp number? :doy))
    ((fn [dat]
       (-> dat
           (r-helpers/base-function "year" 13)
           ((fn [B]
              {:B (tc/rows B)
               :k (count B)
               :n (-> dat :year count)
               :velocity (-> dat :doy vec)
               :w (vec (repeat (count B) 0))}))
           (->> (stan/sample blossom-model))
           :samples
           (tc/select-columns (comp (partial re-find #"mu") name))
           (tc/aggregate-columns (juxt tcc/mean
                                       tcc/standard-deviation
                                       (comp first #(tcc/percentiles % [2.5]))
                                       (comp first #(tcc/percentiles % [97.5]))))
           tc/pivot->longer
           (tc/add-column :col #(->> % :$column (map (comp
                                                      {"-0" :mean
                                                       "-1" :sd
                                                       "-2" :ptile-2.5
                                                       "-3" :ptile-97.5}
                                                      (partial re-find #"-.*")
                                                      name))))
           (tc/add-column :row-id (fn [ds] (->> ds :$column (map (comp read-string
                                                                       #(clojure.string/replace % #"-.*" "")
                                                                       #(clojure.string/replace % #"mu." "") name)))))
           (tc/drop-columns :$column)
           (tc/pivot->wider :col [:$value] {:drop-missing? false})
           (tc/order-by :row-id)
           (tc/left-join
            (tc/add-column dat :row-id (fn [dt] (range 1 (-> dt
                                                             tc/shape
                                                             first
                                                             inc))))
            :row-id)
           (tc/select-columns [:year :row-id :doy :mean :sd :ptile-2.5 :ptile-97.5])
           (tc/add-column :mean-sd (fn [row] (map #(- %1 %2) (:mean row) (:sd row))))
           (tc/add-column :mean+sd (fn [row] (map #(+ %1 %2) (:mean row) (:sd row))))
           #_(tc/pivot->longer (complement #{:year}))
           (haclo/base     {:=x :row-id
                            :=y :doy})
           haclo/layer-point
           (haclo/layer-line {:=y :mean
                              :=mark-color "red"})
           (haclo/layer-line {:=y :mean-sd
                              :=mark-color "gray"})
           (haclo/layer-line {:=y :mean+sd
                              :=mark-color "gray"})))

     ;;
     ))
;; # Testing spline model on first synthetic boat data 

(def jon-spline-model-code
  "
data {
    int n;
    int k;
    array[n] real velocity;
    matrix[n, k] B;
}
parameters {
    real a;
    vector[k] w;
    real<lower=0> sigma;
}
transformed parameters {
    vector[n] mu;
    mu = a + B * w;
}
model {
    for (i in 1:n) {
        velocity[i] ~ normal(mu[i], sigma);
    }
    a ~ normal(100, 10);
    w ~ normal(0, 10);
    sigma ~ exponential(1);
}")

(def jon-model (stan/model jon-spline-model-code))

;; ## Samples of μ's and the mean of μ's
(->> (repeatedly 300 #(rand-int 180))
     (map (fn [angle]
            (let [mu ((get splines 10) angle)
                  velocity (random/sample
                         (random/distribution :normal
                                              {:sd 0.3 :mu mu}))]
              {:angle angle
               :velocity velocity})))
     tc/dataset
     ((fn [dat]
        (-> dat
            (tc/drop-columns :wind)
            (r-helpers/base-function "angle" 5)
            ((fn [B]
               {:B (tc/rows B)
                :k (count B)
                :n (-> dat :angle count)
                :velocity (-> dat :velocity vec)
                :w (vec (repeat (count B) 0))}))
            (->> (stan/sample jon-model))
            :samples
            (tc/select-columns (comp (partial re-find #"mu") name))
            (tc/aggregate-columns (juxt tcc/mean
                                        tcc/standard-deviation
                                        (comp first #(tcc/percentiles % [2.5]))
                                        (comp first #(tcc/percentiles % [97.5]))))
            tc/pivot->longer
            (tc/add-column :col #(->> % :$column (map (comp
                                                       {"-0" :mean
                                                        "-1" :sd
                                                        "-2" :ptile-2.5
                                                        "-3" :ptile-97.5}
                                                       (partial re-find #"-.*")
                                                       name))))
            (tc/add-column :row-id (fn [ds] (->> ds :$column (map (comp read-string
                                                                        #(clojure.string/replace % #"-.*" "")
                                                                        #(clojure.string/replace % #"mu." "") name)))))
            (tc/drop-columns :$column)
            (tc/pivot->wider :col [:$value] {:drop-missing? false})
            (tc/order-by :row-id)
            (tc/left-join
             (tc/add-column dat :row-id (fn [dt] (range 1 (-> dt
                                                              tc/shape
                                                              first
                                                              inc))))
             :row-id)
            (tc/select-columns [:angle :row-id :velocity :mean :sd :ptile-2.5 :ptile-97.5])
            (tc/add-column :mean-sd (fn [row] (map #(- %1 %2) (:mean row) (:sd row))))
            (tc/add-column :mean+sd (fn [row] (map #(+ %1 %2) (:mean row) (:sd row))))
            #_(tc/pivot->longer (complement #{:year}))
            (haclo/base     {:=title "Distribution of means"
                             :=x :angle
                             :=y :velocity})
            haclo/layer-point
            (haclo/layer-line {:=y :mean
                               :=mark-color "red"})
            (haclo/layer-line {:=y :mean-sd
                               :=mark-color "gray"})
            (haclo/layer-line {:=y :mean+sd
                               :=mark-color "gray"}))))

     ;;
     )

;; Splines for all wind strengths
(-> spline-ds
    (haclo/plot haclo/line-chart
                {:=x :angle
                 :=y :velocity
                 :=color :wind}))

;; ## Normalized
(-> spline-ds
    (tc/group-by :wind)
    (tc/add-column :normalised #(tcc/normalize (:velocity %)))
    tc/ungroup
    (haclo/plot haclo/line-chart
                {:=x :angle
                 :=y :normalised
                 :=color :wind}))


;; ## Scaled
;; Examining how the curves behave when scaled can provide insight into how to construct a model.
(-> spline-ds
    (tc/group-by :wind)
    (tc/add-column :max #(apply max (:velocity %)))
    tc/ungroup
    (tc// :scaled [:velocity :max])
    (haclo/plot haclo/line-chart
                {:=x :angle
                 :=y :scaled
                 :=color :wind}))

(-> spline-ds
    (tc/group-by :wind)
    (tc/add-column :max #(apply max (:velocity %)))
    tc/ungroup
    (tc// :scaled [:velocity :max])
    (tc/drop-columns [:velocity :max])
    (tc/pivot->wider :wind :scaled)
    (tc/- :velocity-diff-20-6 ["spline-20" "spline-6"])
    (tc/- :velocity-diff-20-10 ["spline-20" "spline-10"])
    (tc/- :velocity-diff-20-15 ["spline-20" "spline-15"])
    (tc/drop-columns ["spline-20" "spline-15" "spline-10" "spline-6"])
    (tc/pivot->longer (complement #{:angle}) {:target-columns :wind
                                              :value-column-name :diff})
    (haclo/plot haclo/line-chart
                {:=title "Wind strength component?"
                 :=x :angle
                 :=y :diff
                 :=color :wind}))

#_(->>
   (range 1 181)
   #_(map #(let [alpha 3] (+ % alpha)))
   (map (fn [x]
          (let [alpha 3
                beta 3
                beta2 2
                z (range 1 1000 10/180)]
            {:x x
             :y (double (+ alpha (* beta x)  (* (* -1 (/ beta 2)) x)))})))
   (#(-> %
         tc/dataset
         (haclo/base {:=x :x
                      :=y :y})
         (haclo/layer-line))))

;; # Random questions
;; - What would be a good path for the multivariate model ie using wind strength and wind angle as predictors.
;; - Polar chart: adding a few parameters? rotate and clockwise/counter-clockwise? Enable sectors?
;; - What scicloj libraries should i use/showcase that ia m not showcasing.
;; - 
;; - For later: code review... make sure everything is idiomatic.
;; - For later: presentation review.

;; # Comments
;; - Cartesian easier to understand
;; - "Intuition that it is cubic."
;; Homework:
;; - Think about the model
;; - Cubic polynomial regrssion
;; - Plotly addition

;; # Stuff
 
#_(clay/make! {:source-path "/Users/samikallinen/projects/jon30/src/core.clj"})

#_(require '[nextjournal.clerk :as clerk])

#_(clerk/serve! {:browse true})
;; https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/gotland/2023-10-02/2024-08-07?elements=datetime%2CdatetimeEpoch%2Ctemp%2Ctempmax%2Ctempmin%2Cprecip%2Cwindspeed%2Cwindgust%2Cfeelslike%2Cfeelslikemax%2Cfeelslikemin%2Cpressure%2Cstations%2Cdegreedays%2Caccdegreedays&include=fcst%2Cobs%2Chistfcst%2Cstats%2Chours&key=TX27NQVYGHA9FQQ2AA2MN7Z8A&contentType=csv
#_(def weather-url "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/gotland/2023-10-02/2023-11-30?elements=datetime%2CdatetimeEpoch%2Ctemp%2Ctempmax%2Ctempmin%2Cprecip%2Cwindspeed%2Cwindgust%2Cfeelslike%2Cfeelslikemax%2Cfeelslikemin%2Cpressure%2Cstations%2Cdegreedays%2Caccdegreedays&include=fcst%2Cobs%2Chistfcst%2Cstats%2Cdays&key=TX27NQVYGHA9FQQ2AA2MN7Z8A&contentType=csv")

#_(def weather (->
                (tc/dataset weather-url)))


;; # Todo
;; [ ] Rename speed to velocity and strength
;; [ ] Show how to use metamporph pipes for perdiction
;; [ ] rewrite spline code using metamorph?
;; [ ] try modelling wind strengths with a polynomial?
