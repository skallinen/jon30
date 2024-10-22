^{:clay {:kindly/options {:kinds-that-hide-code #{:kind/hiccup
                                                  :kind/md
                                                  :kind/html}}
         :quarto {:format {:revealjs {:theme :white
                                      :transition :none
                                      :transition-speed :fast}}

                  :fontsize "2em"
                  :mainfont "Helvetica"
                  :monofont "Roboto Mono"}
         :hide-info-line true
         :hide-ui-header true}}
(ns slides
  (:require [fastmath.random :as random]
                       [jon30.core :as core]
                       [jon30.data :as data]
                       [scicloj.cmdstan-clj.v1.api :as stan]
                       [scicloj.hanamicloth.v1.plotlycloth :as ploclo]
                       [scicloj.kindly.v4.kind :as kind]
                       [scicloj.metamorph.ml :as ml]
                       [scicloj.metamorph.ml.design-matrix :as dm]
                       [scicloj.metamorph.ml.regression]
                       [tablecloth.api :as tc]
                       [tablecloth.column.api :as tcc]
                       [tech.v3.tensor :as tensor]))

;; # {background-color="black" background-image="src/resources/slide-0.png" background-size="contain"}
;; ::: {.notes}
;; Hi I’m Sami Kallinen, super happy to be here.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="plack" background-image="src/resources/slide-1.png" background-size="contain"}
;; ::: {.notes}
;; - I’ve done all kinds of things, but around three or four years ago, I shifted towards being more or less a full-time clojure programmer.
;; 0:25
;; :::
^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-2.png" background-size="contain"}
;; ::: {.notes}
;; - I am with with 8-bit-sheep. We specialize in building tech and data stuff.
;; 0:30
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-3.png" background-size="contain"}
;; ::: {.notes}
;; - I am also with KP system and should give a shout out to them.
;;
;; - We are developing a full-stack Clojure application that is used by Swedish municipalities. We have a great Clojure team and it is a good opportunity for remote work with Clojure, so keep an eye out for any open positions.
;; - 0:50
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-7.png" background-size="contain"}
;; ::: {.notes}
;; I guess we are here because of this fantastic thing called Clojure,
;; 0:55
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-8.png" background-size="contain"}
;; ::: {.notes}
;; - But I am also here because of Scicloj, an amazing community that develops data science tools and libraries for Clojure.
;; - 1:00
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-9.png" background-size="contain"}
;; ::: {.notes}
;; - Furthermore I'm here because of this demo today, that has been done in collaboration with Daniel Slusky.
;; - We want explore a real world data problem, to demonstrate what you can do with the Clojure data science tools
;; - 1:20
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-10.png" background-size="contain"}
;; ::: {.notes}
;; - For the demo we have collected some data while sailing!
;; - 1:24
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-20.png" background-size="contain"}
;; ::: {.notes}
;; - This is the boat we have been collecting data from.
;; - I had a whole section here about the boat and my sailing story, but you will be relieved to hear that I had to cut it to make everything fit. But you can still ask me about it later..
;; - 1:40
;; :::

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/slide-27.png" background-size="contain"}
;; ::: {.notes}
;; - Our goal today is to predict vessel speed based on the wind conditions
;; - 0:10 | 2:10
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-28.png" background-size="contain"}
;; ::: {.notes}
;; - Why do we want to do that?
;; - To create these, namely polar charts.
;; - Don't worry about how to read them now, it will become clear later.
;; - 0:20 | 2:30
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-29.png" background-size="contain"}
;; ::: {.notes}
;; - What should we do with polar diagrams?
;; - We need them if we race,
;; - for weather routing,
;; - and for what I refer to as quantified sailing.
;; - 0:15 | 2:45
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## Predicting velocity? {background-color="white" background-image="src/resources/vars.png" background-size="contain"}
;; ::: {.notes}
;; - We are using wind strength and wind angle to predict velocity
;; - Many more variables are present but we will simplify the problem to these two predictors for now.
;; - 0:19 | 3:05
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-18.png" background-size="contain"}
;; ::: {.notes}
;; We are sailing! And measuring some data!
;; 7:02
;; 3:20
;; 3:50
;; 4:50
;; 3:10
;; :::

^:kindly/hide-code
(kind/fragment [])


;; # Start simple
;; ::: {.notes}
;; - Lets for now ignore the wind data for a moment and and pretend that we only have one wind strenght
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-37.png" background-size="contain"}
;; ::: {.notes}
;;  And lets agree that the strength is 6 knots. What is 6 knots?
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-38.png" background-size="contain"}
;; ::: {.notes}
;; Its 6.9 mph. What is that?
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-39.png" background-size="contain"}
;; ::: {.notes}
;; It's 11 km/h
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-40.png" background-size="contain"}
;; ::: {.notes}
;; Which is 3 m/s.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-41.png" background-size="contain"}
;; ::: {.notes}
;; - So now we need some data.
;; - We have been collecting some experimental data, but lets leave that to the side for now.
;; - Let's begin with some synthetic data.
;; - We prefer synthetic data when we start modeling
;; - Because usually we have less noise and we are familiar with its behavior for when we debug our models.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-42.png" background-size="contain"}
;; ::: {.notes}
;; - This data has been generated by an external library.
;; - It generates data that tells how a boat like ours should ideally perform in theory,
;; - based on some dimensional inputs we give the library.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-43.png" background-size="contain"}
;; ::: {.notes}
;; - This is how the data looks.
;; - Notice how easy it is to make a simple plot with hanamicloth.
;; - On the y axis we see the vessel speed.
;; - On the x-axis, we have the wind angles. This indicates the angle at which the wind hits the boat in relation to our direction of the bow, ie. the direction of travel.

;; - At 0 angle we get 0 speed, because you cannot sail straight into the wind

;; - But the velocity goes up pretty fast
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-44.png" background-size="contain"}
;; ::: {.notes}
;; - But what is this slump here? Glad you asked.
;; - When you sail downwind, the sail works as a parachute.
;; - And this is not as efficient as when you sail with a side or upwind wind, as the sail then operates like an airplane wing generating lift.
;; :::

^:kindly/hide-code
(kind/fragment [])

^:kindly/hide-code
(kind/fragment [])
;; ## {background-color="black" background-video="src/resources/gliding.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; We have some data! Smooth sailing so far.
;; 11:00
;; 8>50
;; :::
;;

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="white" background-image="src/resources/slide-49.png" background-size="contain"}
;; ::: {.notes}
;; So lets start learning the ropes here and get busy modelling. Regression is a statistical tool used to uncover the relationship between two or more variables.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-51.png" background-size="contain"}
;; ::: {.notes}
;; - This is the model we want to fit.
;; - Like we said we want to find out the velocity given the angle.
;; - The alphas represent the parameters or weights, if you prefer.
;; - We will attempt to find parameters that produce a line that best fits the data.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-52.png" background-size="contain"}
;; ::: {.notes}
;; Here is the code that builds the model and plots it.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-53.png" background-size="contain"}
;; ::: {.notes}
;; - We are doing this for demo but you could just a parameter to hanamicloth to generate a regression line.
;; - The design matrix function takes data and a nice formula that generates the data that the training function needs.
;; - That is sent to the model and then the model to the prediction function.
;; - After getting the predictions we add the line to the visualization
;; - The upcoming models are generated in the same way.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-54.png" background-size="contain"}
;; ::: {.notes}
;; Here we have it. No surprices, perhaps, but it is safe to say that this model is pretty terrible.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/multivariate.png" background-size="contain"}
;; ::: {.notes}
;; Multivariate
;; Now, lets add the wind variable to the model
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/vars.png" background-size="contain"}
;; ::: {.notes}
;; As a reminder we are using wind strength and wind angle to predict velocity!
;; :::


^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/multivariate-equation.png" background-size="contain"}
;; ::: {.notes}
;; Here we add the variable and its parameter to the model. Again we want to find the parameter that fits the data.
;; :::
;;
^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-64.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; The design matrix needs to reflect this change.
;; :::

^:kindly/hide-code
(kind/fragment [])
;; ## {background-color="black" background-image="src/resources/slide-65.png" background-size="contain"}
;; ::: {.notes}
;; Our code needs to change since we now need to examine the model as a three-dimensional surface.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## Multivariate regression
;; ::: {.notes}
;; Now we are exploring in 3D, but the fit is unsurprisingly, rather bad still.
;; :::

^:kindly/hide-code
(def layout
  {:width 1000
   :height 600
   :margin {:l 0 :r 0 :t 0 :b 0}
   :scene {:xaxis {:title "Wind"}
           :yaxis {:title "Angle"
                   :autorange "reversed"}
           :zaxis {:title "Velocity"}
           :camera {:up {:x 0 :y 0 :z 1}
                    :center {:x 0 :y 0 :z 0}
                    :eye {:x -1 :y -1 :z -0.3}}}})

^:kindly/hide-code
(delay
  (let [formula
        [[:velocity]
         [[:angle '(identity angle)]
          [:wind '(identity wind)]]]

        predict-ds
        (-> (for [a (range 181)
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
                  :showscale false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color "#4f988e"}
                  :x (:x training-data-trace)
                  :y (:y training-data-trace)
                  :z (:z training-data-trace)})]
      :layout layout})))

;; ## {background-color="black" background-image="src/resources/cubic-formula.png" background-size="contain"}
;; ::: {.notes}
;; Let's make things a bit more complex. My hunch is that the curves might be well represented by a cubic polynomial.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/cubic-equation.png" background-size="contain"}
;; ::: {.notes}
;; - Here is how the equation looks like. Again we have more parameters. We will find single values for each alpha.
;; - But before we fit the model, lets add some more data to the mix
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## Mo data, mo problems
;; ::: {.notes}
;; - Here we have more data. 
;; - Keep in mind that this data is artificially generated and its a sort of theoretical estimation of how a boat like ours can perform.
;; - There are some problems in the data.
;; - The spike issue is likely a numerical problem within the external library.
;; - The library is a rather large blob of stateful object oriented code.
;; - But that is ok, good even for our needs here today, so we can how the models do.
;; - Next, we will apply a cubic polynomial to this data.
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
        (-> (for [a (range 181)
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

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
                    :showscale false
                    :cauto false
                    :zmin 0
                    :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color "#4f988e"}
                  :x (:x training-data-trace)
                  :y (:y training-data-trace)
                  :z (:z training-data-trace)})]
      :layout (-> layout
                  (update :scene
                          #(-> %
                               (assoc-in [:xaxis :range] (-> predict-ds
                                                             :wind
                                                             ((juxt tcc/reduce-min
                                                                    tcc/reduce-max))))
                               (assoc-in [:yaxis :range] (-> predict-ds
                                                             :angle
                                                             ((juxt tcc/reduce-min
                                                                    tcc/reduce-max)))))))})))


;; ## Better model
;; ::: {.notes}
;; This actually does a pretty good fit. It can deal with the unnatural spike very well. There are some problems.
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
        (-> (for [a (range 181)
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

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
                  :showscale false
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color "#4f988e"}
                  :x (:x training-data-trace)
                  :y (:y training-data-trace)
                  :z (:z training-data-trace)})]
      :layout layout})))

;; ## Custom gradient
;; ::: {.notes}
;; Lets visualize it clearer. First we crate the boundaries
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
(def custom-color-scale
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
;; - We can now observe some shortcomings of this model. While it is a relatively good fit, as is common with polynomials, issues arise at the boundaries where the model becomes erratic.  
;; - The model performs poorly around the point (0, 0).  
;; - But lets use this model. It has some pros.
;; - In contrast to several other models we explored, such as splines and wavelets, this model is remarkably adept at avoiding overfitting peculiarities and data errors like the fin we examined.  
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
        (-> (for [a (range 181)
                  w (range 30)]
              {:angle a
               :wind w
               :velocity 0})
            tc/dataset)

        predict-matrix (-> predict-ds
                           (#(apply dm/create-design-matrix % formula)))

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
                  :showscale false
                  :zmin 0
                  :colorscale custom-color-scale
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color "#4f988e"}
                  :x (:x training-data-trace)
                  :y (:y training-data-trace)
                  :z (:z training-data-trace)})]
      :layout layout})))
;; ## What next?
;; ::: {.notes}
;; - We have some questions.
;; - How do we combine our sythtetic theoretical data with our empirical measurements?
;; - How do we know how uncertain we are with the results?
;; - What mental model should we have?
;; :::
;;
;; - Combine data
;; - Assumptions
;; - Uncertainty
;; - An intuitive mental model


;; ## {background-video="src/resources/atlantic.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; 11 min We are getting into more complex territory. Let's get into Bayesian statistics.
;; 21 min
;; 19 mins
;; 18 mins
;; :::
;;

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/bayesian.png" background-size="contain"}
;; ::: {.notes}
;; So now lets do some Bayesian Statistics, it is a beautiful approach.
;; :::


;; ## Bayesian Statistics {background-image="src/resources/coin.jpg" background-size="contain"}
;; ::: {.notes}
;; - What if we want to find out if this coin is fair?
;; - In the Bayesian approach, we write a probabilistic program that not only simulates and tracks coin flips but also creates the coins themselves and gives them different levels of unfairness.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## Bayesian Statistics {background-color="white" background-image="src/resources/thomas.png" background-size="contain"}
;; ::: {.notes}
;; - In Bayesian analysis, both our data and the things that are uknown to us are treated just as variables in a probabilistic program.
;; - The program defines the relationships between between all of the variables: in our polynomials, the estimated velocities, the empirical data, and the synthetic data.
;; - It is just like in logic programming while here we also include uncertainty.
;; - Then you just need to run your inference algorithm, that is, just run the simulation.
;; - Think of it like your datalog query but then with some randomness inside.
;; :::
;; - Everything is a variable with uncertainty
;; - Like a datalog query

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/cubic-equation.png" background-size="contain"}
;; ::: {.notes}
;; Here we will use this same cubic polynomial. When we saw it previously we estimated parameters as single values. In Bayesian analysis, we view the parameters not as a single fitted value, but as variables in our probabilistic programs with uncertainty. This means that all he alphas have distributions, allowing us to reason about the likelihood of various parameters and, ultimately, different predicted values.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## STAN in the house {.v-center-conteiner}
;; ::: {.notes}
;; STAN is a probabilistic programming language. STAN can be used in Clojure with the Scicloj library known as cmdstan-clj.
;; :::

;;::: {layout="[[-1], [1], [-1]]"}

;; ![](src/resources/stan_logo.png){fig-align="center"}

;;:::

^:kindly/hide-code
(kind/fragment [])


;; ##
;; ::: {.notes}
;; I will not go this through in detail today, but this is our probabilistic program.
;; :::
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
;; This complies the probabilistic program we just saw into a binary.
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


;; ## Real measurements + syntethetic data (our prior belief)
;; ::: {.notes}
;; We want to combine the theoretical model with our real measured data.
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
                 (tc/rename-columns {:angle :y
                                     :wind :x
                                     :velocity :z})
                 (tc/group-by [:part] {:result-type :as-map})
                 vals
                 (->> (map (fn [{:keys [x y z part]}]
                             {:type :scatter3d
                              :mode :markers
                              :name part
                              :marker {:size 5
                                       :opacity 0.8
                                       :color (case (first part)
                                                :vpp "#4f988e"
                                                :empirical "#a9431e")}
                              :x x
                              :y y
                              :z z}))))
      :layout (-> layout
                  (update :scene
                          #(-> %
                               (assoc-in [:xaxis :range] [0 30])
                               (assoc-in [:yaxis :range] [0 181])
                               (assoc-in [:zaxis :range] [0 (-> core/vpp-and-empirical-data
                                                                :velocity
                                                                tcc/reduce-max)]))))})))

;; ## Creating plots for diagnostics {.scrollable}
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
                    
;; ## Doing diagnostic {.scrollable}
;; ::: {.notes}
;; - I know these graphs might not make sense to you, but trust me, they look good :-). It's not really complicated, but I'm taking this shortcut for the sake of time. We can discuss it further later.
;; - We have included these to emphasize that this inspection is an important part of our workflow.
;; :::

^:kindly/hide-code
(delay
  (kind/fragment
   (rest (core/plot-one-run @core/results-without-empirical
                            {:colorscale "Greys"}))))

;; ## Updating the synthetic model with the measurements  {.scrollable}
;; ::: {.notes}
;; - Voila. This is the result of our model!
;; - Here we have now two surfaces.
;; - According to our model, our velocity values should fall between these two surfaces 80% of the time.
;; - This is one way of exploring our model, but we might want to be more concrete.
;; :::


^:kindly/hide-code
(delay
  (-> (first (core/plot-one-run @core/results-with-empirical
                                {:colorscale "Greens"}))
      (assoc :layout layout)))

;; ## Comparing synthetic with empirical  {.scrollable}
;; ::: {.notes}
;; - This is an example how we could use this model, even in real time.
;; - We recorded a set of values on our boat.
;; - In this case we sailed at 175.5 degrees.
;; - That was downwind.
;; - The wind speed was 10.6 knots.
;; - At that moment, our sailing speed was 4.1 knots.
;; - Comparing our speed to the distribution resulting from just the syntetic data, it is evident that we was quite far from the optimal speeds. We should have been sailing at over 5 knots. We are at the lower end of the distribution, so we should take a way that getting a low value like this should be pretty uncommon.
;; - Looking at the velocity distribution resulting from the model with the measured empirical data, the situation seems slightly better. Compared to our usual performance, we were not too far from the optimal speed. It was not our best day, but we were fairly close to the optimum.
;; - We can interpret the upper one to represent the ideal or potential speed distribution for a boat like us
;; - And the lower  distribution the real distribution that takes into account our real boat, our skills and everything else that was not able to be taken into account in our ideal synthetic data.
;; - So based on this we might want to start to debug the boat.
;; - Are we carrying too much weight.
;; - Do we need better sails.
;; - Should we learn to trim the sails better and so forth.
;; :::


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
         :keys [velocity angle wind]} (empirical-example i)]
    (->> [{:results @core/results-without-empirical
           :color "grey"
           :title "posterior without empirical"}
          {:results @core/results-with-empirical
           :color "green"
           :title "posterior with empirical"}]
         (map (fn [{:keys [results color title]}]
                (-> results
                    :samples

                    (tc/rename-columns {(keyword (str "velocity_rep." (+ n-vpp-examples
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
                                          {:x [velocity velocity]
                                           :y [0 150]})))
                    (ploclo/layer-line {:=mark-size 4
                                        :=mark-color "#a9431e"})
                    ploclo/plot
                    (assoc-in [:layout :width]  900)
                    (assoc-in [:layout :height]  400)
                    (assoc-in [:layout :margin] {:l 0 :r 0 :b 100 :t 100
                                                 :pad 4})
                    (assoc-in [:layout :xaxis :range]  [0 10]))))
         (cons (kind/hiccup
                [:h4 (str "empirical example #" i
                          " angle:" angle
                          " wind: " wind
                          " velocity:" velocity )]))
         kind/fragment)))

^:kindly/hide-code
(delay
  (show-empirical-example 151))

;; ## drumroll.. Polars
;; ::: {.notes}
;; - Tada! Finally the polars!
;; - This functions similarly to the 2D plots we previously examined, but now with angles represented in a polar coordinate system.
;; - It may be a bit trickier to interpret, but for sailors, it's quite intuitive as it simulates the boat's direction. When the sailor stands on the deck these are the direction the wind hits her.
;; - Once again, we observe the band where 80% of our data should fall.
;; - To improve the model, we might delve deeper into exploring splines or Gaussian processes. While we experimented with them, in our tests they tended to overfit and incorporate errors from the source data into the model.
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
                (->> [6 18]
                     (map (fn [wind]
                            (let [velocities-by-angle (-> wind
                                                          (- min-wind)
                                                          stat-surfaces)]
                              (-> {:stat i-stat
                                   :wind (str wind " knots")
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
          :=color :wind})
        ploclo/plot
        (assoc-in [:layout :polar]

                  {:angularaxis {:tickfont {:size 16}
                                 :direction "clockwise"}
                   :sector [-90 90]})
        (assoc-in [:layout :width]  1200)
        (assoc-in [:layout :height]  600))))

;; ## Here be dolphins! {background-color="black" background-video="src/resources/dolphins.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; 13 min
;; 34 min
;; :::
;;
^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/slide-67.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;;
;; :::

^:kindly/hide-code
(kind/fragment [])


;; ## Conclusion
;; ::: {.notes}

;; - The main conclusion is that are the tools are here! We can work on pretty advanced problems with Clojure data science tools. This was not the case a few years ago.
;; :::
;; - Clojure data science tools are here!

^:kindly/hide-code
(kind/fragment [])

;; ## Selected Dependencies for this project
;;
;; - Tablecloth
;; - Tech.ml
;; - Metamorph
;; - Fastmath
;; - PythonVPP
;; - Hanami
;; - Hanamicloth and Plotlycloth
;; - Cmdstan-clj
;; - Clay
;; - Noj, released today!


^:kindly/hide-code
(kind/fragment [])

;; ## Noj
;; ::: {.notes}
;; - "Noj" is realeased today.
;; - It brings together all these and the documentation into one place so you dont have to worry about versions etc.
;; :::
;; - "Noj" all Clojure data science libraries and documentation in one place!
;; - All Released today! Noj v2 alpha FTW!


^:kindly/hide-code
(kind/fragment [])

;; ## Opportunities!
;; ::: {.notes}
;; If you want to learn Clojure, Data Science or Open source work Scicloj offers a mentoring program for you!
;; - The website is a valuable resource for further information.
;; :::
;; - Open source mentoring!
;; - The website: https://scicloj.github.io/

^:kindly/hide-code
(kind/fragment [])

;; ## Some heroes
;; ::: {.notes}
;; A small thank you to some of the people who made this project possible
;; :::
;;
;; - Generateme
;; - Chris Nuernberger
;; - Jon Anthony
;; - Carsten Behring
;; - Kira McLean
;; - Timothy Pratley
;; - Daniel Slutsky (also the co-author of of this presentation)
;; - The Scicloj Community!



;; ##  Thank you! {background-video="src/resources/sailing-downwind.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; 34 minutes.
;; 37 minutes.
;; :::
;; Project code: https://github.com/skallinen/jon30

^:kindly/hide-code
(kind/fragment [])

;; ## Todo {visibility="hidden"}
;; [x] Stan in the house slide
;; [x] X and y labels. :layout :scene :xaxis :titlea
;; [x] fix the equations.
;; [x] fix colors of plot
;; [x] remove gradient legend
;; [ ] Fix the structure of the slides
;; [x] add names to axis
;; [x] not all symbols are defined
;; [x] wortex shedding?
;; [ ]
;; [ ] introduce the experimental data more cleverly, differentiate between make more clear.
;; [ ] spike problem just probably a numercial problen, external library is a huge blob of stateful OOP code.
;; [ ] Do the equation slides
;; [ ] Restructure the beginning
;; [ ] Redo the intro slide with simplicity
;; [ ] Change the wing-image



;; ** nice slides
;; is it a custom reveal theme?
;; ** nice joke (Clojure -> Bayesian statistics)
;; ** good pace
;; ** pitched well (assumed background knowledge)
;; ** clear delivery
;; ** not all of the symbols were defined
;; might be better to replace the symbols with words
;; ** vortex shedding?
;; is it a necessary phrase?
;; ** how did you do the regression?
;; did it come from fastmath?
;; ** what (conceptually) do the betas mean?
;; maybe better to say offset and gradient (of the line) for the 2D case, before the intuition is lost at higher dimensions

;; ** what is dm?
;; ** haclo/layer-line?
;; ** I like the resting slide :)
;; ** didn't understand how you added terms to the model
;; looked efficient though!
;; ** plotly?
;; did you reach out to python or is 3D plotly now part of Hanamicloth?
;; ** x y z axes cold have more descriptive names
;; I'd forgotten what each represented!
;; ** Bayesian plots don't have a visible x-axis
;; (until you scroll down)

;; ** there is more emphasis on the topic (goodness of the model etc.) than on the implementation
;; if you're looking for areas to save time then you could do less 'real' analysis of the plots and model.

;; ** how were the polar plots plotted?
;; plotly or Hanami etc.?

;; ** tools Scicloj tools is a typo? (Conclusion slide)

;; ** really nice theme
;; ** is the code available?:
;; expected velosity given angle and wind
;;
;; 
;; our parameters are very few. they are the unknowns. other things are derived from them. in our cse everythin gis derived plus noise.
;; - second run through.
;; vincent.
;; - what is sailing. 3d map.
;; small graphic from wind direction
;; overwhelming how to interpretet the 3d graphs. might explain it a bit more.
;; teodor.
;; had code examples dm/ create ml/ something. what require lines.
;; fast
;; why are we moving to bays
;;
;; was it clear why we want baysian modelling.
;;
;; converging.
;; units on the legend 6 "knots"

;; would be nice to see where you sailed.
;;
;; 
;; how to merge empirical & synthetic data?
;; what is our mental model for that process?
;; what are our implicit assumptions?
;; how (un)certain are we?
;;

;; Repetition

;; The goal: Predicting velocity by wind conditions

;; - Plus noise
;; The idea is that unknown to us are assumed to be random and this allows us use the rules of probability. Learning from synthetic data is just conditional probability. This is the way we get answers in a principled way that conforms to intutition.

;; The basian approach says everything is a propabilistic program. Anything weather unobserved parameters or unobserved data.
;; Both unobserved parameters and observed data are just variables in our program.


;; In the Bayesian approach we write a probabilistic program which not only tosses and counts the coin flips but also creates the coins themselves with with varying unfairness.




