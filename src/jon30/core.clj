^{:clay {:kindly/options {:kinds-that-hide-code #{:kind/hiccup
                                                  :kind/md
                                                  :kind/html}}}}
(ns jon30.core
  (:require
   [clojure.string :as str]
   [dorothy.core :as dot]
   [fastmath.interpolation :as i]
   [fastmath.random :as random]
   [fastmath.stats :as stats]
   [fastmath.core :as fastmath]
   [fastmath.transform :as transf]
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
   [tablecloth.column.api :as tcc]
   [scicloj.kindly.v4.kind :as kind]
   [tech.v3.tensor :as tensor]
   [tech.v3.datatype.functional :as fun]
   [clojure.math :as math]))


;; # Workbook, notes and explorations
;; Please note that this document is not intended for the conference presentation, but is a working document.

;; # 0. List of technical items I will use and try to demonstrate
;; ###  - clay
;; ###  - fastmath
;; ###  - dorothy
;; ###  - stan
;; ###  - hanamicloth
;; ###  - plotlycloth
;; ###  - metamorph.ml
;; ###  - metamorph design-matrix
;; ###  - tablecloth
;; ###  - libpython-clj
;; ###  - tech.ml family of libraries underneath

;; # 1.  Question/goal/estimand
;; Predicting boat velocity based on wind.

;; What do you do with these predictions.
;; ### Polars
(def polar-image
  (-> "https://upload.wikimedia.org/wikipedia/commons/2/29/Downwind_polar_diagram_to_determine_velocity_made_good_at_various_wind_speeds.jpg"
      java.net.URL.
      javax.imageio.ImageIO/read))
polar-image
;; - Polars are charts used by racing sailors to optimize the speed of the vessel based on the boat design, sails, wind, and other conditions. They indicate the optimal speed for the boat in different conditions.
;; ### Route and weather planning
;; - I am not in a racer, but you still need polar data for passage planning planning.
(defonce route-image
  (->  "https://www.predictwind.com/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fmap.b8e74bd1.png&w=3840&q=75"
       (java.net.URL.)
       (javax.imageio.ImageIO/read)))
route-image

;; - The route depends on the weather at the point where you will be in X hours or days. To determine your location, the weather and your boat are taken into account. To estimate your position at any given time, polar charts are used to calculate the boat's speed under the existing conditions.

;; ### "Quantified sailing"
;; Mostly, it can be utilized as a refined navigation tool to accelerate the process of receiving feedback and learning. By constructing a model, you can inquire from it, for instance: Considering our theoretical boat model, how probable is it that I should be sailing at my current speed? If I am sailing slower than the theoretical speed, which is highly unlikely, it indicates that I am not maximizing my sailing abilities. I can also measure the distance between my current performance and the theoretical optimum. Similarly, I can compare my current performance to my past performance using my usual sailing model to determine how close I am to the optimum. Concurrently, I am consistently updating the model to observe progress over time. Additionally, it can double as a log for troubleshooting scenarios. For example, if you bring your entire wine collection onboard, how does the added weight impact performance, and so forth.

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
     [:sea-state {:label "Sea State"
              :shape :ellipse}]
     [:hydrodynamic-forces {:label "Hydrodynamic Forces"
                            :shape :box}]

     [:vessel-velocity {:label "Boat's Velocity"
                        :shape :circle}]

     [:wind-angle-strength :> :aerodynamic-forces]
     [:wind-angle-strength :> :hydrodynamic-forces]

     [:sea-state :> :hydrodynamic-forces]
     [:current :> :hydrodynamic-forces]

     [:aerodynamic-forces :> :vessel-velocity]
     [:hydrodynamic-forces :> :vessel-velocity]]
    digraph)


;; The current is a relatively simple variable since it can be added to or subtracted from the boat velocity. Additionally, in the waters where we are currently (no pun intended) collecting data, there is minimal current, so we can disregard it for the time being. However, we will still include it in the graph. The sea state may impact the empirical data, but for our exercises, we will also initially overlook this. Now, let's consider what factors could potentially influence these forces.
(-> [(dot/node-attrs {:shape :none
                      :fontname "Helvetica"})
     [:total-sail-area {:label "Total Sail Area"}]
     [:wind-angle-strength {:label "Wind Angle and Strength"
                            :shape :ellipse}]
     [:sails {:label "Sail Plan and Trim"}]
     [:aerodynamic-forces {:label "Aerodynamic Forces"
                           :shape :box}]

     [:hull-speed {:label "Hull (Max) Speed"}]
     [:boat-length-waterline {:label "Boat's Length at the Waterline"}]
     [:heeling-angle {:label "Heeling Angle"}]
     [:friction {:label "Hull Friction"}]

     [:current {:label "Current"
                :shape :ellipse}]
     [:sea-state {:label "Sea State"
              :shape :ellipse}]
     [:hydrodynamic-forces {:label "Hydrodynamic Forces"
                            :shape :box}]

     [:vessel-velocity {:label "Boat's Velocity"
                        :shape :circle}]

     [:sails :> :total-sail-area]
     [:heeling-angle :> :total-sail-area]
     [:total-sail-area :> :aerodynamic-forces]
     [:wind-angle-strength :> :aerodynamic-forces]
     [:aerodynamic-forces :> :heeling-angle]

     [:hull-speed :> :hydrodynamic-forces]
     [:boat-length-waterline :> :friction]
     [:boat-length-waterline :> :hull-speed]
     [:heeling-angle :> :boat-length-waterline]
     [:heeling-angle :> :friction]
     [:friction :> :hydrodynamic-forces]


     [:sea-state :> :hydrodynamic-forces]
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
     [:sails {:label "Sail Plan and Trim"}]

     [:current {:label "Current"
                :shape :ellipse}]
     [:sea-state {:label "Sea State"
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

     [:sails :> :total-sail-area]
     [:heeling-angle :> :total-sail-area]
     [:total-sail-area :> :aerodynamic-forces]
     [:wind-angle-strength :> :aerodynamic-forces]
     [:aerodynamic-forces :> :heeling-angle]

     [:hull-speed :> :hydrodynamic-forces]
     [:boat-length-waterline :> :friction]
     [:boat-length-waterline :> :hull-speed]
     [:heeling-angle :> :boat-length-waterline]
     [:heeling-angle :> :friction]
     [:friction :> :hydrodynamic-forces]

     [:captain-crew-competence :> :crew-performance]
     [:captain-crew-competence :> :fatigue-management]
     [:fatigue-management :> :crew-performance]
     [:crew-performance :> :sails]

     [:sea-state :> :hydrodynamic-forces]
     [:current :> :hydrodynamic-forces]

     [:aerodynamic-forces :> :vessel-velocity]
     [:hydrodynamic-forces :> :vessel-velocity]]
    digraph)

;; ## The Offshore Racing Congress Model
;; Now that we have attempted to understand the problem from "first principles," let's consider some previous work.
;; The Offshore Racing Congress (ORC) publishes the specifications for their model that anyone can implement. Let's take a look at how their model is built.

;;A velocity prediction program (VPP) is mainly utilized to forecast the performance of racing yachts in different sailing conditions. This is crucial for establishing handicaps in yacht racing, guaranteeing that competitions are just and grounded in a scientific evaluation of a yacht's speed potential.

;; The VPP calculates these handicaps by modeling the physical forces acting on the yacht, including aerodynamic and hydrodynamic forces, and finding equilibrium conditions at different points of sail. This allows the VPP to predict the yacht's speed based on its design characteristics, such as hull shape, sail configuration, and righting moment​.


;; The following workflow outlines how the Velocity Prediction Program (VPP) model functions.

;; The VPP model estimates the boat's performance. This model feeds into the Solution Algorithm, which uses two distinct force models to simulate the boat's movement. The Aerodynamic Force Model calculates the air-induced forces on the boat, while the Hydrodynamic Force Model focuses on the water-induced impacts.

;; These two force models converge to define the vessel's Equilibrium Conditions, where the aerodynamic and hydrodynamic forces are in equilibrium.

;; Ultimately, by considering these conditions and models, the process culminates in the Performance Prediction, forecasting the boat's velocity under specified conditions.

(->
 [ ;; nodes
  (dot/node-attrs {:shape :none
                   :fontname "Helvetica"})
  [:vpp-model {:label "VPP Model"
               :shape :ellipse
               :style :filled
               :color :lightblue}]
  [:aerodynamic-model {:label "Aerodynamic Force Model"
                       :shape :ellipse
                       :style :filled
                       :color :lightgreen}]
  [:hydrodynamic-model {:label "Hydrodynamic Force Model"
                        :shape :ellipse
                        :style :filled
                        :color :lightgreen}]
  [:algorithm {:label "Solution Algorithm"
               :shape :ellipse
               :style :filled
               :color :lightyellow}]
  [:equilibrium {:label "Equilibrium Conditions"
                 :shape :ellipse
                 :style :filled
                 :color :lightcoral}]
  [:performance-prediction {:label "Performance Prediction"
                            :shape :ellipse
                            :style :filled
                            :color :lightgrey}]

  ;; edges
  [:vpp-model :> :algorithm]
  [:algorithm :> :aerodynamic-model]
  [:algorithm :> :hydrodynamic-model]
  [:aerodynamic-model :> :equilibrium]
  [:hydrodynamic-model :> :equilibrium]
  [:equilibrium :> :performance-prediction]

  ]

 digraph)
;; Here is another way to explain how the model functions.
(->
 [;; nodes
  (dot/node-attrs {:shape :none
                   :fontname "Helvetica"})

  [:sail-forces {:label "Sail Forces"
                 :shape :ellipse
                 :style :filled
                 :color :lightgreen}]
  [:hull-forces {:label "Hull Forces"
                 :shape :ellipse
                 :style :filled
                 :color :lightgreen}]
  [:balance {:label "Force balancing"
             :shape :ellipse
             :style :filled
             :color :lightyellow}]

  [:sail-plan {:label "Sailplan Geometry"}]
  [:wind-speed {:label "Wind Speed"}]
  [:wind-angle {:label "True Wind Angle"}]

  [:hull-geom {:label "Hull and Keel Geometry"}]
  [:moment {:label "Righting Moment"}]
  [:sea-state {:label "Sea State"}]

  [:boat-speed {:label "Boat Speed"}]
  [:heel {:label "Heel Angle"}]
  [:leeway {:label "Leeway Angle"}]

  ;; edges
  [:sail-forces :> :balance]
  [:hull-forces :> :balance]

  [:hull-geom :> :hull-forces]
  [:moment :> :hull-forces]
  [:sea-state :> :hull-forces]

  [:sail-plan :> :sail-forces]
  [:wind-speed :> :sail-forces]
  [:wind-angle :> :sail-forces]

  [:boat-speed :> :balance]
  [:heel :> :balance]
  [:leeway :> :balance]]
 digraph)

;; The VPP model consists of two parts: the solution algorithm and the boat model. The solution algorithm is responsible for determining an equilibrium condition for each point of sailing, ensuring that:

;; a) the driving force from the sails equals the hull and aerodynamic drag, and

;; b) the heeling moment from the rig is counterbalanced by the righting moment from the hull.

;; ## Keep it simple
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


(def all-splines
  (-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
      (tc/rename-columns {:twa :angle
                          :tws :wind
                          :vessel-speed :velocity})
      (tc/rows :as-maps)
      (->> (group-by :wind)
           vals
           (map (fn [v]
                  {(:wind (first v)) (i/interpolation :b-spline
                                                      (map :angle v)
                                                      (map :velocity v))}))
           (mapcat identity)
           (into {}))))


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

(def blossom-model (delay (stan/model stan-spline-model-code)))

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
             (->> (stan/sample @blossom-model))
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

(def jon-model
  (delay (stan/model jon-spline-model-code)))

;; ## Samples of μ's and the mean of μ's
(delay
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
              (->> (stan/sample @jon-model))
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
       ))

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

;; # Multivariate regression

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

(def color-custom-scale
  (into [[0.0 "rgb(255,0,0)"]]
        (mapv (fn [c n]  [c (str "rgb(" n ", " n ", " n ")")])
              color-boundries
              (range 1 255 (int (/ 255 (count color-boundries)))))))

(delay
  (let [multi-cubic-formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:angle2 '(* angle angle)]
          [:angle3 '(* angle angle angle)]
          [:wind '(identity wind)]
          [:wind2 '(* wind wind)]
          [:wind3 '(* wind wind wind)]
          [:wind4 '(* wind wind wind wind)]]]

        predict-ds
        (-> (for [a angles
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)
        _ (def predict-ds predict-ds)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % multi-cubic-formula)))

        training-data
        (-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
            (tc/rename-columns {:twa :angle
                                :tws :wind
                                :vessel-speed :velocity}))
        _ (def training-data training-data)

        training-design-matrix (-> training-data
                                   (#(apply dm/create-design-matrix % multi-cubic-formula)))

        _ (def training-design-matrix training-design-matrix)

        multi-cubic-model
        (-> training-design-matrix
            (ml/train {:model-type :fastmath/ols}))
        _ (def model multi-cubic-model)

        multi-cubic-predictions
        (-> (ml/predict (-> predict-matrix
                            (tc/drop-columns [:velocity]))
                        multi-cubic-model)
            (tc/add-column :angle (:angle predict-ds))
          (tc/add-column :wind (:wind predict-ds)))
      _ (def multi-cubic-predictions multi-cubic-predictions)

      z-trace-for-surface
      (-> multi-cubic-predictions
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

  (-> multi-cubic-predictions
      (ploclo/layer-line)
      ploclo/plot
      ((fn [m]
         (let [trace1 (-> m
                          :data
                          first
                          (assoc :type :surface)
                          (assoc :colorscale "Greys")
                          ;;#_#_#_
                          (assoc :cauto false)
                          (assoc :zmin 0)
                          (assoc :colorscale color-custom-scale)
                          (assoc :z z-trace-for-surface))
               trace2 (-> m
                          :data
                          first
                          (assoc :type :scatter3d)
                          (assoc :mode :markers)
                          (assoc :marker {:size 6
                                          :line {:width 0.5
                                                 :opacity 0.8}})
                          (assoc :x (:x training-data-trace))
                          (assoc :y (:y training-data-trace))
                          (assoc :z (:z training-data-trace)))]
           (-> m
               (assoc :data [trace1 trace2])
               (assoc-in [:layout :width] 600)
               (assoc-in [:layout :height] 700))))))))

(delay
  (let [multi-cubic-formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:angle2 '(* angle angle)]
          [:angle3 '(* angle angle angle)]
          [:wind '(identity wind)]
          [:wind2 '(* wind wind)]
          [:wind3 '(* wind wind wind)]
          [:wind4 '(* wind wind wind wind)]]]

        predict-ds
        (-> (for [a (range 0 (inc 180))
                  w (range 0 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)
        _ (def predict-ds predict-ds)

        training-data
        (-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
            (tc/rename-columns {:twa :angle
                                :tws :wind
                                :vessel-speed :velocity}))

        _      (def training-data training-data)

        pred-func
        (-> training-data
            ((fn [td]
               (def td td)
               (i/interpolation :cubic-2d
                                (sort (set (:wind td)))
                                (take 180 (:angle td))
                                (partition 180 (:velocity td))))))
        _ (def pred-func pred-func)

        predictions
        (-> predict-ds
            (tc/add-column :prediction #(map (fn [a w] (pred-func w a)) (:angle %) (:wind %)))
            (tc/add-column :prediction #(-> %
                                            :prediction
                                            (->> (map (fn [p] (if (or (neg? p)
                                                                      (> p 10)) nil p))))
                                            (tcc/column)))
            (tc/replace-missing :midpoint))

        _ (def predictions predictions)
        z-trace-for-surface
        (-> predictions
            (tc/drop-columns [0])
            (tc/pivot->wider :wind :prediction)
            (tc/drop-columns [:angle])
            (tc/rows))

        training-data-trace
        (-> training-data
            (tc/select-rows (comp not neg? :velocity))
            (tc/rename-columns {:angle :y
                                :wind :x
                                :velocity :z}))]

    (-> multi-cubic-predictions
        (ploclo/layer-line)
        ploclo/plot
        ((fn [m]           (let [trace1 (-> m
                                            :data
                                            first
                                            (assoc :type :surface)
                                            (assoc :colorscale "Greys")
                                            ;;#_#_#_
                                            (assoc :cauto false)
                                            (assoc :zmin 0)
                                            (assoc :colorscale color-custom-scale)
                                            (assoc :z z-trace-for-surface))
                                 trace2 (-> m
                                            :data
                                            first
                                            (assoc :type :scatter3d)
                                            (assoc :mode :markers)
                                            (assoc :marker {:size 6
                                                            :line {:width 0.5
                                                                   :opacity 0.8}})
                                            (assoc :x (:x training-data-trace))
                                            (assoc :y (:y training-data-trace))
                                            (assoc :z (:z training-data-trace)))]
                             (-> m
                                 (assoc :data [trace1 trace2])
                                 (assoc-in [:layout :width] 600)
                                 (assoc-in [:layout :height] 700)))))
        #_:layout)))

(comment       _ (def pred-func pred-func)
                      (-> predict-ds
                          (tc/add-column :prediction #(->> (map (fn [a w] (pred-func a w)) (:angle %) (:wind %)))))
                      predictions
                      (-> predict-ds
                          (tc/add-column :angle (:angle predict-ds))
                          (tc/add-column :wind (:wind predict-ds)))
                      _ (def predictions predictions)

                      z-trace-for-surface
                      (-> multi-cubic-predictions
                          (tc/drop-columns [0])
                          (tc/pivot->wider :wind :velocity)
                          (tc/drop-columns [:angle])
                          (tc/rows))

                      training-data-trace
                      (-> training-data
                          (tc/select-rows (comp not neg? :velocity))
                          (tc/rename-columns {:angle :y
                                              :wind :x
                                              :velocity :z})))
#_(-> predict-ds
    (tc/add-column :prediction #(map (fn [a w] (pred-func a w)) (:angle %) (:wind %)))
    #_          (tc/add-column :prediction #(-> % :prediction (partial map (fn [p] (when (and (pos? p)
                                                                                             (< p 10)) p))) tc/dataset)))
;; grayscale for the surface?
;; points with bigger residual other color
;; try bi-variat cubic-2d
;; try kriging? maybe not 2d?
;; shape is regular.


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

;; https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/gotland/2023-10-02/2024-08-07?elements=datetime%2CdatetimeEpoch%2Ctemp%2Ctempmax%2Ctempmin%2Cprecip%2Cwindspeed%2Cwindgust%2Cfeelslike%2Cfeelslikemax%2Cfeelslikemin%2Cpressure%2Cstations%2Cdegreedays%2Caccdegreedays&include=fcst%2Cobs%2Chistfcst%2Cstats%2Chours&key=TX27NQVYGHA9FQQ2AA2MN7Z8A&contentType=csv
#_(def weather-url "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/gotland/2023-10-02/2023-11-30?elements=datetime%2CdatetimeEpoch%2Ctemp%2Ctempmax%2Ctempmin%2Cprecip%2Cwindspeed%2Cwindgust%2Cfeelslike%2Cfeelslikemax%2Cfeelslikemin%2Cpressure%2Cstations%2Cdegreedays%2Caccdegreedays&include=fcst%2Cobs%2Chistfcst%2Cstats%2Cdays&key=TX27NQVYGHA9FQQ2AA2MN7Z8A&contentType=csv")

#_(def weather (->
                (tc/dataset weather-url)))


;; # Todo
;; [ ] Rename speed to velocity and strength
;; [ ] Show how to use metamporph pipes for perdiction
;; [ ] rewrite spline code using metamorph?
;; [ ] try modelling wind strengths with a polynomial?



(def jon-spline-slices-model-code
  "
data {
    int n_angles;
    int n_winds;
    int k;
    array[n_angles, n_winds] real velocity;
    matrix[n_angles, k] B;
}
parameters {
    array[n_winds] real a;
    matrix[k, n_winds] w;
    real<lower=0> sigma;
}
transformed parameters {
    matrix[n_angles, n_winds] mu;
    mu = B * w;
}
model {
    for (i_a in 1:n_angles) {
        for (i_w in 1:n_winds) {
            velocity[i_a, i_w] ~ normal(mu[i_a, i_w] + a[i_w], sigma);
        }
    }
    a ~ normal(100, 10);
    for (i_k in 1:k) {
        for (i_w in 1:n_winds) {
            w[i_k, i_w] ~ normal(0, 10);
        }
    }
    sigma ~ exponential(1);
}")

(def jon-spline-slices-model
  (delay
    (stan/model jon-spline-slices-model-code)))



#_(let [angles [1 2 3 4 5 6 7 8]
        min-angle (apply min angles)
        winds [1 2 3]
        min-wind (apply min winds)
        B (-> {:angle angles}
              tc/dataset
              (r-helpers/base-function "angle" 8))
        training-data (tc/dataset
                       (for [angle angles
                             wind winds]
                         (let [mu angle
                               velocity mu]
                           {:angle angle
                            :wind wind
                            :velocity velocity})))
        sampling (->> {:n_angles (count angles)
                       :n_winds (count winds)
                       :B (tc/rows B)
                       :k (count B)
                       :velocity (->> training-data
                                      :velocity
                                      (partition (count winds)))}
                      (stan/sample @jon-spline-slices-model))]
    (-> sampling
        :samples
        (tc/select-columns (comp
                            (partial re-find #"mu")
                            name))
        (->> (map (fn [[k column]]
                    (tcc/mean column)))
             (partition (count angles)))))

(delay
  (let [angles (range 1 181)
        min-angle (apply min angles)
        winds (sort (keys all-splines))
        min-wind (apply min winds)
        B (-> {:angle angles}
              tc/dataset
              (r-helpers/base-function "angle" 7))
        training-data (tc/dataset
                       (for [angle angles
                             wind winds]
                         (let [mu ((get all-splines wind) angle)
                               velocity mu #_(random/sample
                                              (random/distribution :normal
                                                                   {:sd 0.3 :mu mu}))]
                           {:angle angle
                            :wind wind
                            :velocity velocity})))
        sampling (->> {:n_angles (count angles)
                       :n_winds (count winds)
                       :B (tc/rows B)
                       :k (count B)
                       :velocity (->> training-data
                                      :velocity
                                      (partition (count winds)))}
                      (stan/sample @jon-spline-slices-model))
        z-trace-for-surface (-> sampling
                                :samples
                                (tc/select-columns (comp
                                                    (partial re-find #"mu")
                                                    name))
                                (->> (map (fn [[k column]]
                                            (tcc/mean column)))
                                     (partition (count angles))))
        training-data-trace (-> training-data
                                (tc/select-rows (comp not neg? :velocity))
                                (tc/rename-columns {:angle :x
                                                    :wind :y
                                                    :velocity :z}))]
    (kind/plotly
     {:data [(-> {:type :surface
                  :mode :lines
                  :colorscale "Greys"
                  :cauto false
                  :zmin 0
                  ;; :colorscale color-custom-scale
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
      :layout {:width 600
               :height 700}})))



(delay
  (let [predict-ds
        (-> (for [a (range 0 (inc 180))
                  w (range 0 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)
        _ (def predict-ds predict-ds)

        training-data
        (-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
            (tc/rename-columns {:twa :angle
                                :tws :wind
                                :vessel-speed :velocity}))

        _      (def training-data training-data)

        min-wind (-> training-data
                     :wind
                     tcc/reduce-min)

        max-wind (-> training-data
                     :wind
                     tcc/reduce-max)

        min-angle (-> training-data
                      :angle
                      tcc/reduce-min)

        max-angle (-> training-data
                      :angle
                      tcc/reduce-max)

        pred-func
        (-> training-data
            ((fn [td]
               (def td td)
               (i/interpolation :bicubic
                                (sort (set (:wind td)))
                                (take 180 (:angle td))
                                (partition 180 (:velocity td))))))
        _ (def pred-func pred-func)

        predictions
        (-> predict-ds
            (tc/add-column :prediction #(map (fn [a w]
                                               (if (and (< min-wind w max-wind)
                                                        (< min-angle w max-angle))
                                                 (pred-func w a)
                                                 0))
                                             (:angle %) (:wind %)))
            (tc/add-column :prediction #(-> %
                                            :prediction
                                            (->> (map (fn [p] (if (or (neg? p)
                                                                      (> p 10)) nil p))))
                                            (tcc/column)))
            (tc/replace-missing :midpoint))

        _ (def predictions predictions)
        z-trace-for-surface
        (-> predictions
            (tc/drop-columns [0])
            (tc/pivot->wider :wind :prediction)
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
                  :mode :lines
                  :colorscale "Greys"
                  :cauto false
                  :zmin 0
                  ;; :colorscale color-custom-scale
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}}
                  :x (:x training-data-trace)
                  :y (:y training-data-trace)
                  :z (:z training-data-trace)})]
      :layout {:width 600
               :height 700}})))



(delay
  (let [t (transf/transformer :real :fft)
        data (vec (for [x (range 8)]
                    (vec (for [y (range 8)]
                           (+ x y)))))]
    (->> data
         (transf/forward-2d t)
         fastmath/double-double-array->seq
         (mapv vec)
         ((fn [data]
            (-> data
                (assoc-in [0 7] 0)
                (assoc-in [0 6] 0))))
         (transf/reverse-2d t)
         fastmath/double-double-array->seq)))



(delay
  (fun/* (tensor/compute-tensor [10 10]
                                (fn [i j] 1000)
                                :float32)
         (tensor/compute-tensor [10 10]
                                (fn [i j] (/ 1.0
                                             (math/log (+ 1 i j))))
                                :float32)))



(tensor/compute-tensor [10 10]
                       (fn [i j] (/ 1.0
                                    (math/log (+ 3 i j))))
                       :float32)






(-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
    :twa
    distinct
    sort
    vec)


(-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
    (tc/group-by [:twa :tws] {:result-type :as-map})
    (update-vals (fn [ds]
                   (-> ds
                       :vessel-speed
                       first)))
    (get {:twa 1
          :tws (first (vec (sort (keys all-splines))))}))


(delay
  (let [vpp-data (tc/dataset "jon30vpp.csv" {:key-fn keyword})
        twa-tws->vessel-speed (-> vpp-data
                                  (tc/group-by [:twa :tws] {:result-type :as-map})
                                  (update-vals (fn [ds]
                                                 (-> ds
                                                     :vessel-speed
                                                     first))))
        n 256
        smaller-n 200
        angles (-> vpp-data
                   :twa
                   distinct
                   sort
                   vec)
        min-angle (apply min angles)
        winds (-> vpp-data
                  :tws
                  distinct
                  sort
                  vec)
        min-wind (apply min winds)
        B (-> {:angle angles}
              tc/dataset
              (r-helpers/base-function "angle" 7))
        training-data (tensor/compute-tensor
                       [n n]
                       (fn [i-angle i-wind]
                         (max 0
                              (or (when-let [w (get winds i-wind)]
                                    (when-let [a (get angles i-angle)]
                                      (twa-tws->vessel-speed
                                       {:twa a
                                        :tws w})))
                                  0)))
                       :float32)
        t (transf/transformer :real :fft)
        z-trace-for-surface (->> training-data
                                 (transf/forward-2d t)
                                 fastmath/double-double-array->seq
                                 tensor/->tensor
                                 (fun/* (tensor/compute-tensor
                                         [n n]
                                         (fn [i j]
                                           (if (> (+ i j)
                                                  300)
                                             0 1))
                                         :float32))
                                 (transf/reverse-2d t)
                                 fastmath/double-double-array->seq
                                 (take 180)
                                 (map (partial take (count winds))))
        training-data-trace (-> vpp-data
                                (tc/rename-columns {:twa :y
                                                    :tws :x
                                                    :vessel-speed :z})
                                (tc/select-rows #(-> % :z (>= 0))))]
    (kind/plotly
     {:data [(-> {:type :surface
                  :mode :lines
                  :colorscale "Greys"
                  :cauto false
                  :zmin 0
                  ;; :colorscale color-custom-scale
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}}
                  :x (tcc/- (:x training-data-trace)
                            min-wind)
                  :y (tcc/- (:y training-data-trace)
                            min-angle)
                  :z (:z training-data-trace)})]
      :layout {:width 600
               :height 700}})))






(def jon-polynomial-model-code
  "

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
}")

(def jon-polynomial-model
  (delay
    (stan/model jon-polynomial-model-code)))

(def vpp-data
  (-> (tc/dataset "jon30vpp.csv" {:key-fn keyword})
      (tc/select-columns [:twa :tws :vessel-speed])
      (tc/rename-columns {:twa :angle
                          :tws :wind
                          :vessel-speed :velocity})
      (tc/add-column :part :vpp)
      (tc/add-column :should_observe
                     (fn [ds]
                       (let [rng (random/rng :isaac 5336)]
                         (repeatedly
                          (tc/row-count ds)
                          #(if (> 0.05 (random/frandom rng))
                             1 0)))))))


(def vpp-data-with-additions
  (tc/concat vpp-data
             (-> vpp-data
                 (tc/add-column :part :addition)
                 (tc/add-column :velocity #(tcc/* (:velocity %)
                                                  (-> (tcc/+ (tcc/sq (tcc/- (:angle %) 90))
                                                             (tcc/sq (tcc/- (:wind %) 15)))
                                                      (tcc/* -0.02)
                                                      tcc/exp
                                                      (tcc/* 0.4)
                                                      (tcc/+ 0.8)))))))

(defn prepare-polynomials [data]
  (-> data
      (tc/map-columns :angle2 [:angle] (fn [a]
                                         (-> a
                                             (- 90)
                                             (#(* % %))
                                             (/ 90))))
      (tc/map-columns :angle3 [:angle] (fn [a]
                                         (-> a
                                             (- 90)
                                             (#(* % % %))
                                             (/ (* 90 90)))))
      (tc/map-columns :angle4 [:angle] (fn [a]
                                         (-> a
                                             (- 90)
                                             (#(* % % % %))
                                             (/ (* 90 90 90)))))
      (tc/map-columns :wind2 [:wind] (fn [w]
                                       (-> w
                                           (- 15)
                                           (#(* % %))
                                           (/ 15))))
      (tc/map-columns :wind3 [:wind] (fn [w]
                                       (-> w
                                           (- 15)
                                           (#(* % % %))
                                           (/ (* 15 15)))))
      (tc/map-columns :wind4 [:wind] (fn [w]
                                       (-> w
                                           (- 15)
                                           (#(* % % %))
                                           (/ (* 15 15 15)))))))


(delay
  (let [n-angles 180
        n-winds 26
        vpp-data vpp-data
        main-training-data (-> vpp-data
                               prepare-polynomials)
        full-training-data (-> vpp-data-with-additions
                               prepare-polynomials)
        min-angle (-> main-training-data
                      :angle
                      tcc/reduce-min)
        min-wind (-> main-training-data
                     :wind
                     tcc/reduce-min)
        samples (-> full-training-data
                    (tc/drop-columns [:part])
                    (->> (into {}))
                    (update-vals vec)
                    (assoc :n (tc/row-count full-training-data))
                    (#(stan/sample @jon-polynomial-model
                                   %
                                   {:num-samples 200}))
                    :samples)
        z-trace-for-surface (-> samples
                                (tc/select-columns (comp
                                                    (partial re-find #"mu")
                                                    name))
                                (->> (map (fn [[k column]]
                                            (tcc/mean column)))
                                     (take (tc/row-count main-training-data))
                                     (partition n-angles)))
        training-data-traces (-> full-training-data
                                 (tc/select-rows (comp not neg? :velocity))
                                 (tc/rename-columns {:angle :x
                                                     :wind :y
                                                     :velocity :z})
                                 (tc/group-by [:part] {:result-type :as-map})
                                 vals)]
    [(kind/plotly
      {:data (concat [{:type :surface
                       :mode :lines
                       :colorscale "Greys"
                       :cauto false
                       :marker {:line {:opacity 0.5}}
                       :zmin 0
                       :z z-trace-for-surface}]
                     (->> training-data-traces
                          (map (fn [{:keys [x y z part]}]
                                 {:type :scatter3d
                                  :mode :markers
                                  :name part
                                  :marker {:size 3
                                           :opacity 0.8}
                                  :x (tcc/- x min-angle)
                                  :y (tcc/- y min-wind)
                                  :z z}))))
       :layout {:width 600
                :height 700}})
     (for [k [:a0
              :a1_angle :a2_angle :a3_angle ;; :a4_angle
              :a1_wind :a2_wind :a3_wind ;; :a4_wind
              :sigma]]
       (-> samples
           (ploclo/layer-point {:=x :i
                                :=y k
                                :=color :chain
                                :=color-type :nominal})
           ploclo/plot))
     full-training-data]))


(def empirical-data
  (-> (tc/dataset "jon-empirical.csv" {:key-fn keyword})
      (tc/rename-columns {:TWA :angle
                          :TWS :wind
                          :SOG :velocity})
      (tc/add-column :part :empirical)
      (tc/add-column :should_observe 1)))


(def vpp-and-empirical-data
  (tc/concat vpp-data
             empirical-data))



(delay
  (kind/plotly
   (let [min-angle (-> vpp-and-empirical-data
                       :angle
                       tcc/reduce-min)
         min-wind (-> vpp-and-empirical-data
                      :wind
                      tcc/reduce-min)]
     {:data  (-> vpp-and-empirical-data
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
      :layout {:width 600
               :height 700}})))



(defn create-surface [{:keys [n-angles
                              n-winds
                              use-empirical
                              stats]
                       :or {n-angles 180
                            n-winds 26
                            stats [#(stats/quantile % 0.1)
                                   #_tcc/median
                                   #(stats/quantile % 0.9)]}}]
  (let [main-training-data (-> vpp-data
                               prepare-polynomials)
        full-training-data (-> vpp-and-empirical-data
                               (cond-> (not use-empirical)
                                 (tc/map-columns :should_observe
                                                 [:should_observe :part]
                                                 (fn [so p]
                                                   (if (= p :empirical)
                                                     0
                                                     so))))
                               prepare-polynomials)
        min-angle (-> main-training-data
                      :angle
                      tcc/reduce-min)
        min-wind (-> main-training-data
                     :wind
                     tcc/reduce-min)
        samples (-> full-training-data
                    (tc/drop-columns [:part])
                    (->> (into {}))
                    (update-vals vec)
                    (assoc :n (tc/row-count full-training-data))
                    (#(stan/sample @jon-polynomial-model
                                   %
                                   {:num-warmup 400
                                    :num-samples 50
                                    :num-chains 4
                                    :num-threads 4}))
                    :samples)
        z-traces-for-surface (->> stats
                                  (map (fn [f]
                                         (-> samples
                                             (tc/select-columns (comp
                                                                 (partial re-find #"mu")
                                                                 name))
                                             (->> (map (fn [[k column]]
                                                         (f column)))
                                                  (take (tc/row-count main-training-data))
                                                  (partition n-angles))))))
        training-data-traces (-> full-training-data
                                 (tc/select-rows (comp not neg? :velocity))
                                 (tc/rename-columns {:angle :x
                                                     :wind :y
                                                     :velocity :z})
                                 (cond-> (not use-empirical)
                                   (tc/select-rows #(-> % :part (not= :empirical))))
                                 (tc/group-by [:part] {:result-type :as-map})
                                 vals)]
    {:z-traces-for-surface z-traces-for-surface
     :training-data-traces training-data-traces
     :samples samples
     :min-angle min-angle
     :min-wind min-wind}))


(defn plot-one-run [{:keys [z-traces-for-surface
                            training-data-traces
                            min-angle
                            min-wind
                            samples]}
                    {:keys [colorscale]
                     :or {colorscale "Greys"}}]
  (kind/fragment
   [(kind/plotly
     {:data (concat (->> z-traces-for-surface
                         (map (fn [z-trace]
                                {:type :surface
                                 :mode :lines
                                 :colorscale colorscale
                                 :cauto false
                                 :marker {:line {:opacity 0.5}}
                                 :zmin 0
                                 :z z-trace})))
                    (->> training-data-traces
                         (map (fn [{:keys [x y z part]}]
                                {:type :scatter3d
                                 :mode :markers
                                 :name part
                                 :marker {:size 3
                                          :opacity 0.8}
                                 :x (tcc/- x min-angle)
                                 :y (tcc/- y min-wind)
                                 :z z}))))
      :layout {:width 900
               :height 600}})
    (kind/fragment
     (for [k [:a0
              :a1_angle :a2_angle :a3_angle ;; :a4_angle
              :a1_wind :a2_wind :a3_wind ;; :a4_wind
              :sigma]]
       (-> samples
           (ploclo/layer-line (merge {:=x :i
                                       :=y k}
                                      (when (:chain samples)
                                        {:=color :chain
                                         :=color-type :nominal})))

           ploclo/plot
           (assoc-in [:layout :width]  900)
           (assoc-in [:layout :height]  450))))]))




(defn plot-runs [runs {:keys [colorscales]
                       :or {colorscales ["Greys" "Greens"]}}]
  (kind/plotly
   {:data (->> runs
               (map-indexed
                (fn [i {:keys [z-traces-for-surface]}]
                  (->> z-traces-for-surface
                       (map (fn [z-trace]
                              {:type :surface
                               :mode :lines
                               :colorscale (colorscales i)
                               :cauto false
                               :marker {:line {:opacity 0.5}}
                               :zmin 0
                               :z z-trace})))))
               (apply concat))
    :layout {:width 600
             :height 700}}))


(def results-without-empirical
  (delay
    (create-surface {:use-empirical false})))

(def results-with-empirical
  (delay
    (create-surface {:use-empirical true})))

(delay
  (plot-one-run @results-without-empirical
                {:colorscale "Greys"}))

(delay
  (plot-one-run @results-with-empirical
                {:colorscale "Greens"}))

(map realized?
     [results-without-empirical
      results-with-empirical])

(delay
  (plot-runs [@results-without-empirical
              @results-with-empirical]
             {}))
