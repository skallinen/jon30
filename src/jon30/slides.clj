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
;; I’ve done all kinds of things, but around three or four years ago, I shifted towards almost full-time development. Thus, in many respects, I am indeed, still an old novice.
;; :::
^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-2.png" background-size="contain"}
;; ::: {.notes}
;; I am with with 8-bit-sheep, a consultancy that I co-founded with some other sheep about 7 years ago. We specialize in building tech stuff and especially building data stuff.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-3.png" background-size="contain"}
;; ::: {.notes}
;; I should also give a shout out to KP System, which was recently acquired by Geomatikk.
;;
;; I work with them developing a full-stack Clojure application that is used by Swedish municipalities. We have a small gret team of four Clojure developers who are an absolute joy to work with. It's an excellent opportunity for remote work with Clojure, so keep an eye out for any open positions if you're interested.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-4.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; Intro
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-5.png" background-size="contain"}
;; ::: {.notes}
;; - Why are we here today?
;; - Because we appreciate simplicity
;; - clarity,
;; - we love our REPL
;; - we delight in figuring out the data, and making everything into data.
; - So I suppose you are also here because you love...
;; :::


^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-6.png" background-size="contain"}
;; ::: {.notes}
;; Because we love Baysian statistics!
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-7.png" background-size="contain"}
;; ::: {.notes}
;; I guess we are here not only because of Bayesian statistics, but also because of Clojure,
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-8.png" background-size="contain"}
;; ::: {.notes}
;; But I am also here because of Scicloj, a community that develops data science tools and libraries for Clojure.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-9.png" background-size="contain"}
;; ::: {.notes}
;; - Furthermore I'm here because of this project, that has been done in collaboration with Daniel Slusky, who is a driving forse in the Scicloj community.
;; - A few months ago, we had a conversation abou submitting a proposal to test the current state of the tools in the Scicloj ecosystem with a fun data problem. Not so much to show the implementations but just to have a real data problem we'll demo.
;; - But where to get such a real-world data problem. How about sailing? Combining both passions of Clojure and Sailing.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-10.png" background-size="contain"}
;; ::: {.notes}
;;  One could even sail down to Belgium to give the talk!
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-11.png" background-size="contain"  visibility="hidden"}
;; ::: {.notes}
;; A few words about my sailing. If someone had told me five years ago that I would become a sailor, I would have laughed in their face. I used to think sailing was only for wealthy and prominent yachtsmen with lots of status and resources.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-12.png" background-size="contain"  visibility="hidden"}
;; ::: {.notes}
;; But then this happened. Without going into all the details, suddenly I couldn't stay with my family due to no vaccines, so I had to find a place nearby. I searched the internet and ended up buying a boat for the price of a fancy laptop.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-13.png" background-size="contain"  visibility="hidden"}
;; ::: {.notes}
;; I've also learned that anyone can take up sailing. Meet Moxy Marlinspike and his gang of anarchists who sailed the Caribbean more or less for free.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-14.png" background-size="contain"  visibility="hidden"}
;; ::: {.notes}
;; If you're interested, they've made a documentary about their adventures.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-15.png" background-size="contain"  visibility="hidden"}
;; ::: {.notes}
;; So I moved in. It was small, the size of a luxurious dog house.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-16.png" background-size="contain"  visibility="hidden"}
;; ::: {.notes}
;; I lived and worked there for 6 months.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-17.png" background-size="contain"  visibility="hidden"}
;; ::: {.notes}
;;
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-20.png" background-size="contain"}
;; ::: {.notes}
;; Speaking of saling, this summer, I got my third boat. It's not large, it may not win modern races, but it's sturdy and dependable.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-21.png" background-size="contain"}
;; ::: {.notes}
;; I thought I would be sailing all summer, like I said, perhaps to Belgium, but it is an old boat. We always plan to sail and end up doing maintenance..
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-22.png" background-size="contain"}
;; ::: {.notes}
;; Maintenance is fun too.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-26.png" background-size="contain"}
;; ::: {.notes}
;; I've also installed some tools that track and log the boat's performance while sailing.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/ydwg_drawing.png" background-size="contain"}
;; ::: {.notes}
;;
;; :::


^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-25.png" background-size="contain"}
;; ::: {.notes}
;; So we have a boat, we can collect some data. All aboard?
;; :::

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/slide-27.png" background-size="contain"}
;; ::: {.notes}
;; Predicting vessel speed based on the wind conditions
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-28.png" background-size="contain"}
;; ::: {.notes}
;; Why do we want to do that? To create these, namely polar charts. Don't worry about how to read them now, it will become clear later.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-29.png" background-size="contain"}
;; ::: {.notes}
;; What should we do with polar diagrams? We need them if we race, weather routing, and what I refer to as quantified sailing.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-30.png" background-size="contain"}
;; ::: {.notes}
;; Here is an example of a commercial weather routing app that utilizes polar data to help you plan your voyage while considering the varying weather conditions along your route. The different colors represent different models.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-31.png" background-size="contain"}
;; ::: {.notes}
;; What makes the boat move? Essentially, two things: water and wind.
;; :::

^:kindly/hide-code(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-32.png" background-size="contain"}
;; ::: {.notes}
;; Or the way the wind affects the sails and the water impacts the hull.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-34.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; This is let's keep it simple. For this discussion, let's focus on the wind speed and wind direction.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## What affects the velocity? {background-color="white" background-image="src/resources/vars.png" background-size="contain"}
;; ::: {.notes}
;; As we are using wind strength and wind angle to predict velocity, ie how fast the boat moves throught the water!
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-35.png" background-size="contain"}
;; ::: {.notes}
;; Or actually, let's make it even simpler and only focus on the wind angle.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-18.png" background-size="contain"}
;; ::: {.notes}
;; We are sailing!
;; :::

^:kindly/hide-code
(kind/fragment [])


;; # {background-color="black" background-image="src/resources/slide-36.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;;Imagine for a moment that on earth we only had one constant wind strength and that it never changes.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; # Start simple
;; ::: {.notes}
;;Imagine for a moment that on earth we only had one constant wind strength and that it never changes.
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
;; - We have been collecting some experimental data, but lets leave that to the side.
;; - Let's begin with some synthetic data.
;; - We prefer synthetic data when we start modeling because it is usually not very noisy and we are familiar with its behavior when we debug our models.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-42.png" background-size="contain"}
;; ::: {.notes}
;; - This synthetic data has been generated by an external library called PythonVPP.
;; - We have given it some specs of our boat and it gives some theoretical figures how such a boat should sail.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-43.png" background-size="contain"}
;; ::: {.notes}
;; This is how the data looks. Notice how easy it is to make a simple plot with hanamicloth.
;; On the y axis we see the vessel speed.
;; On the x-axis, we have the wind angles. This indicates the angle at which the wind strikes the boat in relation to our direction of the bow, ie. the direction of travel.

;; At 0 angle we get 0 speed, because you cannot sail straight into the wind

;; You just need to bear off a bit and we're already sailing pretty fast.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-44.png" background-size="contain"}
;; ::: {.notes}
;; - But what is this slump here? Glad you asked.
;; - In these parts, when the wind comes slightly from the front or the side, the sail acts as an airplane wing that creates lift. This is why some boats can sail faster than the wind in certain conditions.
;; - When sailing downwind in these parts, the wind coming from behind makes the sail act like a parachute.
;; :::

^:kindly/hide-code
(kind/fragment [])

^:kindly/hide-code
(kind/fragment [])
;; ## Smooth sailing! {background-color="black" background-video="src/resources/gliding.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; 
;; :::
;;

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="white" background-image="src/resources/slide-49.png" background-size="contain"}
;; ::: {.notes}
;; So lets learn the ropes here and get busy modelling. Regression is a statistical tool used to uncover the relationship between two or more variables.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-50.png" background-size="contain"}
;; ::: {.notes}
;; In our scenario, we aim to determine the vessel's velocity given a specific wind angle.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-51.png" background-size="contain"}
;; ::: {.notes}
;; Another way to communicate this is by using an equation like this one. The alphas represent the parameters or weights, if you prefer. We will attempt to find parameters that produce a line that best fits the data.
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
;; - The function where we model the data is not actually needed as we could pass some flags to hanamicloth that would then result in a regression line.
;; - We have the more explicit version here to have more control and to show how we do more complex versions later.
;; - Let's run through what the modelling function does.  
;; - First off, we take the data with the wind-strength function.
;; - We then shape and process the data to fit the requirements of our equation.
;; - This process can be easily done with a design matrix, which I will explain in the upcoming slides.
;; - Subsequently, we send the matrix to be trained in the model. 
;; - We use the model to forecast new velocity values using a range of angles provided in the angles var. 
;; - After getting the predictions, do some houskeeping before visualization.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-54.png" background-size="contain"}
;; ::: {.notes}
;; Here we have it. No surprices, perhaps, but it is safe to say that this model is pretty terrible.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-62.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; Not only is it a bad model but the wind varies on Earth. Therefore, we need to introduce another variable, which includes the wind strength. That means we are now going to attempt multivariate regression.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/multivariate.png" background-size="contain"}
;; ::: {.notes}
;; Multivariate
;; Not only is it a bad model but the wind varies on Earth. Therefore, we need to introduce another variable, which includes the wind strength. That means we are now going to attempt multivariate regression.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/vars.png" background-size="contain"}
;; ::: {.notes}
;; As a reminder we are using wind strength and wind angle to predict velocity!
;; :::


^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/quadratic-equation.png" background-size="contain"}
;; ::: {.notes}
;; We will now incorporate the wind intensity and its parameters, alpha, into the model.
;; :::
;;
^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-64.png" background-size="contain"}
;; ::: {.notes}
;; We will also update the design matrix to reflect this change.
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
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))

;; ## {background-color="black" background-image="src/resources/quadratic-formula.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; So lets complicate it a bit more by adding terms.
;; :::
^:kindly/hide-code
(kind/fragment [])
1

;; ## A forumla for quadratic regression {visibility="hidden"}
;; ::: {.notes}
;; - This is how the design matrix functions. It is akin to the popular formula in R.
;; - It is a nifty function that transforms your data into the data tha modelling function expect. Here it adds the squared columns.

;; :::
;; This corresponds to something like `(velocity ~ angle + I(angle^2) + wind + I(wind^2))` in R.

(require '[scicloj.metamorph.ml :as ml]
         '[scicloj.metamorph.ml.design-matrix :as dm])

(-> data/vpp-polar-01
    tc/dataset
    (#(apply dm/create-design-matrix %
             [[:velocity]
              [[:angle '(identity angle)]
               [:angle2 '(* angle angle)]
               [:wind '(identity wind)]
               [:wind2 '(* wind wind)]]])))


;; ## Quadratic polynomial {visibility="hidden"}
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
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))



;; ## {background-color="black" background-image="src/resources/cubic-formula.png" background-size="contain"}
;; ::: {.notes}
;; So let's complicate it even more and do a so called cubic polynomial TODO typo in slide
;; :::

;; ## {background-color="black" background-image="src/resources/cubic-equation.png" background-size="contain"}
;; ::: {.notes}
;; If you recall the equation we examined earlier for the cubic polynomial, we will now utilize the same one but also incorporate a cubic polynomial for the wind values.
;; :::

;; ## Cubic polynomial
;; ::: {.notes}
;; - Cubic polynomial.
;; - Works pretty well, but we can do better.
;; - But lets add more data.
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
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))


;; ## Mo data, mo problems
;; ::: {.notes}
;; - We now have additional data that will assist us in creating a more precise model. We are now covering everything from 4 knots to 30 knots of wind in the training data.
;; - Keep in mind that this data is artificially generated.
;; - We are encountering some challenges with this data.
;; - The spike issue is likely a numerical problem within the external library.
;; - It is a rather large blob of stateful object oriented code.
;; - But that is ok, good even for our needs here today.
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
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"
                               :range (-> predict-ds
                                          :wind
                                          ((juxt tcc/reduce-min
                                                 tcc/reduce-max)))}
                       :yaxis {:title "Angle"
                               :range (-> predict-ds
                                          :angle
                                          ((juxt tcc/reduce-min
                                                 tcc/reduce-max)))}
                       :zaxis {:title "Velocity"}}}})))


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
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))

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
;; - We can now observe some shortcomings of this model. While it is a relatively good fit, as is common with polynomials, issues arise at the boundaries where the model becomes erratic.  
;; - The model performs poorly around the point (0, 0).  
;; - Nevertheless, we will proceed with this model for now.
;; - Maintaining model simplicity is generally advantageous from various perspectives.  
;; - It facilitates clear reasoning.  
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
                  :colorscale color-custom-scale
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
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))
;; ## What next?
;; ::: {.notes}
;; - We have used basic tools to gain an understanding of what type of model could be effective for our issue.
;; - But we are left with some questions.
;; - How do we combine our sythtetic theoretical data with our experimental empirical measurements
;; - What is our mental model for this process?
;; - What implicit assumptions are made when we do so?
;; - What if we also want to understan how certain/uncertan are we in the results we obtain?
;; - Our current approach does not give easy answers to these questions.
;; :::
;;
;; - Combine the empirical and the synthetic?
;; - An intuitive mental model?
;; - Assumptions?
;; - Uncertainty?

;; ## {background-video="src/resources/atlantic.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; 11 min We are getting into more complex territory. Let's get into Bayesian statistics.
;; :::
;;

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/bayesian.png" background-size="contain"}
;; ::: {.notes}
;; So now lets do some Bayesian Statistics.
;; :::


;; ## Bayesian Statistics {background-image="src/resources/coin.jpg" background-size="contain"}
;; ::: {.notes}
;; - Imagine you're trying to determine if a coin is fair.
;; - Traditional Approach: Base decision on long-term data.
;; - Bayesian Approach: Start with an initial belief and update it with each coin toss.
;; - There is a focus on explainability and simplicity.
;; - The output of a model is a simulation ie just a new dataset we can pose questions to.
;; - This is also why it is much easier to reason about the uncertainty than with traditional approaches.
;; - In our case, essentially, we can view the synthetic data as the theoretical best performance for a boat like ours. Think of the sythetic dta as the assumption that the coin is fair in the coin example
;; - We want to somehow combine combine our sythetic data, which is an ideal, with our measured experimental empirical data. 
;; - So lets get started.
;; :::
;; - Is this coin fair?
;; - Traditional approach: Throw it many times and then decide.
;; - Bayesian approach: Start with a belief, best guess, and update it with each coin toss.
;; - In our case this we are using Baysian to combine the synthetic and empirical data to analyze uncertainty.

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/cubic-equation.png" background-size="contain"}
;; ::: {.notes}
;; In the polynomials we saw previous we estimated parameters as single values. In Bayesian analysis, we view the parameters not as a single fitted value, but as random variables with uncertainty. This means that all the alphas have distributions, allowing us to reason about the likelihood of various parameters and, ultimately, different predicted values.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## STAN in the house {.v-center-conteiner}
;; ::: {.notes}
;; STAN is a probabilistic programming language that lets users to create statistical models and do Bayesian inference on them. Ithas some of the most efficient algorithms for modeling. STAN can be utilized in Clojure with the Scicloj library known as cmdstan-clj.
;; :::

;;::: {layout="[[-1], [1], [-1]]"}

;; ![](src/resources/stan_logo.png){fig-align="center"}

;;:::

^:kindly/hide-code
(kind/fragment [])


;; ##
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


;; ## Real measurements + syntethetic data (our prior belief)
;; ::: {.notes}
;; We want to combine the theoretical model with our real measured data to learn the difference between the theoretical data and our specific actuald perforance data. Here the synthetic data is our best guess, or our piror belief, before we have seen our measured
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
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"
                               :range [0 30]}
                       :yaxis {:title "Angle"
                               :range [0 181]}
                       :zaxis {:title "Velocity"
                               :range [0 (-> core/vpp-and-empirical-data
                                             :velocity
                                             tcc/reduce-max)]}}}})))

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
  (first (core/plot-one-run @core/results-with-empirical
                      {:colorscale "Greens"})))

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
;; - It may be a bit trickier to interpret, but for sailors, it's quite intuitive as it simulates the boat's direction. Imagine the boat moving forward and the wind angle indicating how it impacts you on the boat.
;; - Once again, we observe the 95% credible interval.
;; - We could narrow them down, perhaps plotting one standard deviation instead. Let's save that for another time.
;; - Overall, this looks good, except for this curve here. Considering the boat's behavior within its domain, it shouldn't exhibit this pattern.
;; - The horizontal line at the bottom should remain flat, even though it seems to be sloping downwards now. This behavior is characteristic of the cubic model.
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

;; ## Here be dolphins! {background-video="src/resources/dolphins.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; 13 min
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
;; - We've made good progress. We can and will keep working on the actual model.

;; - The tools Scicloj tools are there. We can work on pretty advanced problems with Clojure data science tools.

^:kindly/hide-code
(kind/fragment [])

;; ## Selected Dependencies for this project
;; ::: {.notes}
;;
;; :::
;;
;; - Tablecloth
;; - Tech.ml
;; - Metamorph
;; - Fastmath
;; - Hanami
;; - Hanamicloth and Plotlycloth
;; - Cmdstan-clj
;; - Clay
;; - Noj, released today!


^:kindly/hide-code
(kind/fragment [])

;; ## Noj
;; - "Noj" brings together essential Clojure libraries for data science purposes and provides documentation on how to effectively utilize them.
;; - Forget about version compatibility issues - "Noj" has your back.
;; - Unsure about which tool is optimal for a specific task? "Noj" has thought about that already!
;; - https://github.com/scicloj/noj
;; - Released today! Noj v2 alpha FTW!


^:kindly/hide-code
(kind/fragment [])

;; ## Opportunities to take advantage of
;; There are numerous opportunities here. If you are eager to learn, there are plenty of chances for you. You can explore Clojure, Data Science, or Open Source projects. Scicloj provides an excellent program for mentoring open source developers. Check out the website to learn more; it's a fantastic resource.

^:kindly/hide-code
(kind/fragment [])

;; ## Some heroes
;; ::: {.notes}
;;
;; :::
;;
;; - Generateme
;; - Chris Nuernberger
;; - Jon Anthony
;; - Carsten Behring
;; - Generateme
;; - Kira McLean
;; - Timothy Pratley
;; - Daniel Slutsky
;; - The Scicloj Community!



;; ##  Thank you! {background-video="src/resources/sailing-downwind.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;;2 min
;; :::
;;


^:kindly/hide-code
(kind/fragment [])


;; ## Todo {visibility="hidden"}
;; [x] Stan in the house slide
;; [x] X and y labels. :layout :scene :xaxis :titlea
;; [ ] fix the equations.
;; [ ] fix colors of plot
;; [ ] remove gradient legend
;; [ ] Fix the structure of the slides
;; [ ] add names to axis
;; [ ] not all symbols are defined
;; [ ] whortex shedding?
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

