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

;; # {background-color="black" background-image="src/resources/slide-0.png" background-size="contain"}
;; ::: {.notes}
;; Hi I’m Sami Kallinen, super happy to be here
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
;; I have been working for them for three years, developing a full-stack Clojure application that is used by Swedish municipalities. We have a small but exceptional team of four Clojure developers who are a joy to work with. It's an excellent opportunity for remote work with Clojure, so keep an eye out for any open positions if you're interested.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-4.png" background-size="contain"}
;; ::: {.notes}
;; Intro
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-5.png" background-size="contain"}
;; ::: {.notes}
;; - Why are we here today?
;; - Because we appreciate simplicity,
;; - clarity,
;; - we love our REPL,
;; - we delight in figuring out the data, and making everything into data.
; - So I suppose we are all here because we love...
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
;; I guess we are here not only because of Bayesian statistics, but also to Clojure,
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
;; - Im also here because of this project, that has been done in collaboration with Daniel Slusky, who is a driving forse in the Scicloj community.
;; - A few months ago, I had a conversation with him about submitting a proposal to test the current state of the tools in the Scicloj ecosystem with a fun data problem.
;; - But where to get such a real-world data problem. How about sailing?
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-10.png" background-size="contain"}
;; ::: {.notes}
;;  One could even sail down to Belgium to give the talk.
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
;; Speaking of that, this summer, I got my third boat. It's not large, it may not win modern races, but it's sturdy and dependable.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-21.png" background-size="contain"}
;; ::: {.notes}
;; I thought I would be sailing all summer, like I said, perhaps to Belgium, but it is an old boat. So there was quite some maintenance.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-22.png" background-size="contain"}
;; ::: {.notes}
;; Yup, maintenance and less sailing...
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
;; So I have a boat, I can collect some data. What could go wrong.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/slide-27.png" background-size="contain"}
;; ::: {.notes}
;;
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
;; Here is an example of a commercial weather routing app that utilizes polar data to help you plan your voyage while considering the varying weather conditions along your route. The app requires information on how your boat performs under different weather scenarios in order to determine your position at a given time.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-31.png" background-size="contain"}
;; ::: {.notes}
;; What makes the boat move? Essentially, two things: water and wind.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-32.png" background-size="contain"}
;; ::: {.notes}
;; Or the way the wind affects the sails and the water impacts the hull.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-33.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; That's an oversimplification. In actuality, there are numerous variables. Here is a non-exhaustive list resulting from a short brainstorm of what they could be.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-34.png" background-size="contain"}
;; ::: {.notes}
;; That's an oversimplification, but let's keep it simple. For this discussion, let's focus on the wind speed and wind direction.
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
;; Hey, we are sailing!
;; :::

^:kindly/hide-code
(kind/fragment [])


;; # {background-color="black" background-image="src/resources/slide-36.png" background-size="contain"}
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
;; This synthetic data has been generated by an external library. This data serves as the basis for the model we will attempt to build, and it assumes a boat that is expected to be quite similar to ours.
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

;; ## {background-color="white" background-image="src/resources/slide-45.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; Todo. Change pictureNormally, the sail of a boat functions similar to an airplane wing, generating lift as air passes over it. This explains why boats can sometimes move faster than the wind.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-46.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; I'll give you a moment to digest this, there will be a test at the end.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-47.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; But in the case of a slump, when the wind is coming from behind, around 180 degrees, we are sailing downwind and the sails no longer function like airplane wings; they act more like parachutes. This can restrict the maximum speed and accounts for the slump.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-48.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;;
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## So far so good {background-color="black" background-image="src/resources/slide-23.png" background-size="contain"}
;; ::: {.notes}
;; Sailing boat pic change this? Fuzzy
;; - So far so good
;; :::

^:kindly/hide-code
(kind/fragment [])


;; # {background-color="white" background-image="src/resources/slide-49.png" background-size="contain"}
;; ::: {.notes}
;; So lets get busy modelling. Regression is a statistical tool used to un100% auto the relationship between two variables.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-50.png" background-size="contain"}
;; ::: {.notes}
;; In our scenario, it aims to determine the vessel's velocity given a specific wind angle.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-51.png" background-size="contain"}
;; ::: {.notes}
;; Another way to communicate this is by using an equation like this one. Some of you may recognize that this forms a straight line. The betas represent the parameters or weights, if you prefer. We will attempt to find parameters that produce a line that best fits the data.
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
;; - We then shape and manipulate the data to fit the requirements of our equation. 
;; - This process can be easily done with a design matrix, which I will explain in the upcoming slides.
;; - It should be noted that in this scenario, it is not actually necessary. 
;; - Subsequently, we send the matrix to be trained in the model. 
;; - We utilize the model to forecast new velocity values using a variety of angles provided. 
;; - After obtaining the predictions, we include additional columns in the dataset and make some adjustments before visualization
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-54.png" background-size="contain"}
;; ::: {.notes}
;; Here we have it. We are not surprices, perhaps, but it is safe to say that this model is pretty terrible.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-55.png" background-size="contain" visibility="hidden"} 
;; ::: {.notes}
;; So let's play with polynomials! They provide us with greater flexibility when fitting the curve.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-56.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; So this is a bit more complicated formula, we call it a cubic polynomial.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-57.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; And once again, here is the equation. "Fitting" refers to the process of attempting to identify the beta parameter combination that best aligns the curve with the data. But to do that our model needs the angle variable in different forms as we can se in the terms of the equation.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-58.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; We can easily manipulate the data to meet these requirements using the design matrix. Those familiar with R will recognize this as essentially the same as the beloved formula in R. Perhaps not as elegant, but maybe a bit clearer.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-59.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; We generate the plot in the same way as before, but now we have a helper function for the modeling thread.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-60.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; Looks great. Even if we're just eyeballing it, it's fitting quite well!
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-61.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; If we inspect the model, it also metrics that show the model's performance. An R-squared value of 0.97 is almost perfect. 
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-24.png" background-size="contain" visibility="hidden"}
;; ::: {.notes}
;; Fair weather sailing
;; :::


^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-62.png" background-size="contain"}
;; ::: {.notes}
;; Not only is it a bad model but the wind varies on Earth. Therefore, we need to introduce another variable, which includes the wind strength. That means we are now going to attempt multivariate regression.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/multivariate.png" background-size="contain"}
;; ::: {.notes}
;; Multivariate
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-63.png" background-size="contain"}
;; ::: {.notes}
;; We will now incorporate the wind intensity and its parameters, alpha, into the model.
;; :::

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
;; We will also update our code to generate the plot since we now need to examine the model as a three-dimensional surface.
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
                  :showscale false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color :purple}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))

;; ## {background-color="black" background-image="src/resources/quadratic-formula.png" background-size="contain"}
;; ::: {.notes}
;; So lets complicate it a bit more by adding terms.
;; :::
^:kindly/hide-code
(kind/fragment [])


;; ## A forumla for quadratic regression
;; ::: {.notes}
;; If you recall the equation we examined earlier for the cubic polynomial, we will now utilize the same one but also incorporate a cubic polynomial for the wind values.
;; :::
;; This corresponds to something like `(velocity ~ angle + I(angle^2) + wind + I(wind^2))` in R.

(-> data/vpp-polar-01
    tc/dataset
    (#(apply dm/create-design-matrix %
             [[:velocity]
              [[:angle '(identity angle)]
               [:angle2 '(* angle angle)]
               [:wind '(identity wind)]
               [:wind2 '(* wind wind)]]])))


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
                  :showscale false
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color :purple}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))

;; ## {background-color="black" background-image="src/resources/cubic-formula.png" background-size="contain"}
;; ::: {.notes}
;; So let's complicate it even more and do a so called cubic polynomial
;; :::

;; ## {background-color="black" background-image="src/resources/cubic-equation.png" background-size="contain"}
;; ::: {.notes}
;; If you recall the equation we examined earlier for the cubic polynomial, we will now utilize the same one but also incorporate a cubic polynomial for the wind values.
;; :::

;; ## Cubic polynomial
;; ::: {.notes}
;; - Cubic polynomial.
;; - Again the cubic works.
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
                  :showscale false
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color :purple}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))


;; ## Mo data, mo problems
;; ::: {.notes}
;; - We now have additional data that will assist us in creating a more precise model. We are now 100% autoing everything from 4 knots to 30 knots of wind
;; - Keep in mind that this data is artificially generated.
;; - We are encountering some challenges with this data.
;; - The spike issue is likely a numerical problem within the external library we are utilizing to generate the data, which consists of a large amount of stateful object-oriented programming code.
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
                    :showscale false
                    :cauto false
                    :zmin 0
                    :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color :purple}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
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
                  :showscale false
                  :cauto false
                  :zmin 0
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color :purple}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
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
                  :showscale false
                  :zmin 0
                  :colorscale color-custom-scale
                  :z z-trace-for-surface})
             (-> {:type :scatter3d
                  :mode :markers
                  :marker {:size 6
                           :line {:width 0.5
                                  :opacity 0.8}
                           :color :purple}
                  :x (tcc/- (:x training-data-trace)
                            min-angle)
                  :y (tcc/- (:y training-data-trace)
                            min-wind)
                  :z (:z training-data-trace)})]
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))

;; ## {background-video="src/resources/atlantic.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; Since then, besides the Baltic Sea where I usually sail, I have also sailed in the Mediterranean and the Atlantic.
;; :::
;;
^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/bayesian.png" background-size="contain"}
;; ::: {.notes}
;; So now we come to Bayesian Statistics. In addition to the reasons mentioned earlier, there are particular reasons for why we are choosing it.
;; :::


;; ## Bayesian Statistics
;; ::: {.notes}
;; - Synthetic data
;; - So far we've been using the synthetic data for model debugging, now it's time to leverage the fact that it also signifies the theoretical optimal performance of the boat.
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

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/cubic-equation.png" background-size="contain"}
;; ::: {.notes}
;; In Bayesian analysis, we view the parameters not as a single fitted value, but as random variables with uncertainty. This means that all the alphas and betas have distributions, allowing us to reason about the likelihood of various parameters and, ultimately, different predicted values.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## STAN in the house {.v-center-conteiner}
;; ::: {.notes}
;; STAN is a probabilistic programming language that enables users to create intricate statistical models and conduct Bayesian inference on them. It boasts some of the most efficient algorithms for modeling. STAN can be utilized in Clojure with the Scicloj library known as cmdstan-clj.
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
;; ## Doing the modelling {.scrollable}
;; ::: {.notes}
;; - Before reviewing the results, we must verify that our modeling is functioning correctly.
;; - Bayesian methods construct models through simulation.
;; - In this display, we can observe the parameter values for each iteration of the simulation.
;; - The values should remain consistent around similar numbers, as shown here.
;; - We conducted the simulation four times, each represented by a different color.
;; - These results appear favorable, as the values are consistent across iterations, and the different attempts show similarities.;; :::
;; :::

^:kindly/hide-code
(delay
  (kind/fragment
   (rest (core/plot-one-run @core/results-without-empirical
                            {:colorscale "Greys"}))))

;; ## Doing the modelling {.scrollable}
;; ::: {.notes}
;; - We are modeling the same data as before.
;; - Now we have introduced two surfaces, and the gap between them is where our anticipated values should lie with a 95% probability. This is the true interpretation. You might have come across confidence intervals in conventional statistics, and most people think they refer to this exact concept, however they are wrong. In Bayesian statistics, it is quite straightforward to pose such questions to our model.
;; :::
^:kindly/hide-code
(delay
  (first (core/plot-one-run @core/results-without-empirical
                      {:colorscale "Greys"}))
  )

;; ## {background-color="black" background-image="src/resources/device.png" background-size="contain"}
;; ::: {.notes}
;; Earlier, I mentioned that I have installed some measurement devices on the boat. We can now proceed to examine that data.
;; :::

^:kindly/hide-code
(kind/fragment [])
                    
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
                                       :opacity 0.8
                                       :color (case (first part)
                                                :vpp :purple
                                                :empirical :red)}
                              :x (tcc/- x min-angle)
                              :y (tcc/- y min-wind)
                              :z z}))))
      :layout {:width 1000
               :height 600
               :scene {:xaxis {:title "Wind"}
                       :yaxis {:title "Angle"}
                       :zaxis {:title "Velocity"}}}})))

;; ## Updating the synthetic model with the measurements  {.scrollable}
;; ::: {.notes}
;; We want to update the theoretical model with our real measured data
;; :::


^:kindly/hide-code
(delay
  (first (core/plot-one-run @core/results-with-empirical
                      {:colorscale "Greens"})))

;; ## Updating the synthetic model with the measurements  {.scrollable}
;; ::: {.notes}
;; We want to update the theoretical model with our real measured data
;; :::

^:kindly/hide-code
(delay
  (kind/fragment (rest (core/plot-one-run @core/results-with-empirical
                             {:colorscale "Greens"}))))


;; ## Comparing synthetic with empirical  {.scrollable}
;; ::: {.notes}
;; Fin
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
                                             ;;  :=histogram-nbins 30
                                             :=mark-color color})
                    (ploclo/update-data (constantly
                                         (tc/dataset
                                          {:x [velocity velocity]
                                           :y [0 500]})))
                    (ploclo/layer-line {:=mark-size 4
                                        :=mark-color "red"})
                    ploclo/plot
                    (assoc-in [:layout :width]  900)
                    (assoc-in [:layout :height]  450)
                    (assoc-in [:layout :margin] {:l 0 :r 0 :b 150 :t 0
                                                 :pad 4}))))
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

;; ## {background-color="black" background-image="src/resources/ship-baltic2024.jpg" background-size="contain"}
;; ::: {.notes}
;; But fortunately, we were able to enjoy some of the beautiful Baltic archipelago along the southern coastline of Finland.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/slide-67.png" background-size="contain"}
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
;; - "Noj" brings together essential Clojure libraries for data science purposes and provides documentation on how to effectively utilize them in tandem.
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
;; - Daniel Slutsky
;; - The Scicloj Community!



;; ##  Thank you! {background-video="src/resources/sailing-downwind.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;;
;; :::
;;


^:kindly/hide-code
(kind/fragment [])


;; ## Todo {visibility="hidden"}
;; [x] Stan in the house slide
;; [x] X and y labels. :layout :scene :xaxis :titlea
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
