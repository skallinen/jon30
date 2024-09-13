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

;; # {background-color="black" background-image="src/resources/slide-0.png" background-size="cover"}
;; ::: {.notes}
;; Hi I’m Sami Kallinen
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="plack" background-image="src/resources/slide-1.png" background-size="cover"}
;; ::: {.notes}
;; I’ve done all kinds of things, but around three or four years ago, I shifted towards almost full-time development. Thus, in many respects, I am indeed, still an old novice.
;; :::
^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-2.png" background-size="cover"}
;; ::: {.notes}
;; I am with with 8-bit-sheep, a consultancy that I co-founded with some other sheep about 7 years ago. We specialize in building tech stuff and especially building data stuff.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-3.png" background-size="cover"}
;; ::: {.notes}
;; I should also give a shout out to KP System, which was recently acquired by Geomatikk.
;;
;; I have been working for them for three years, developing a full-stack Clojure application that is used by Swedish municipalities. We have a small but exceptional team of four Clojure developers who are a joy to work with. It's an excellent opportunity for remote work with Clojure, so keep an eye out for any open positions if you're interested.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-4.png" background-size="cover"}
;; ::: {.notes}
;; Intro
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-5.png" background-size="cover"}
;; ::: {.notes}
;; Why are we here today? (pause) I presume most of you know what we are talking about?
;; :::


^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-6.png" background-size="cover"}
;; ::: {.notes}
;; 
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-7.png" background-size="cover"}
;; ::: {.notes}
;; Thanks to the hard work of Arne and the community, we are also here thanks to Clojure, not just Bayesian statistics.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-8.png" background-size="cover"}
;; ::: {.notes}
;; But I am also here because of Scicloj, a community that develops data science tools and libraries for Clojure.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-9.png" background-size="cover"}
;; ::: {.notes}
;; This project has been done in collaboration with Daniel Slutsky a few months ago.
;; A few monthjs back I had a conversation with Daniel, a driving force in the community, about submittin a proposal to the heart of Clojure showcasing the Scicloj tools.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-10.png" background-size="cover"}
;; ::: {.notes}
;; To showcase the tools effectively, you'll need a real-world data problem. How about combining your two passions: Clojure and Sailing? You could even sail down to Belgium to give the talk.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="white" background-image="src/resources/slide-11.png" background-size="cover"}
;; ::: {.notes}
;; A few words about my sailing. If someone had told me five years ago that I would become a sailor, I would have laughed in their face. I used to think sailing was only for wealthy and prominent yachtsmen with lots of status and resources.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-12.png" background-size="cover"}
;; ::: {.notes}
;; But then this happened. Without going into all the details, suddenly I couldn't stay with my family due to no vaccines, so I had to find a place nearby. I searched the internet and ended up buying a boat for the price of a fancy laptop.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-13.png" background-size="cover"}
;; ::: {.notes}
;; I've also learned that anyone can take up sailing. Meet Moxy Marlinspike and his gang of anarchists who sailed the Caribbean more or less for free.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-14.png" background-size="cover"}
;; ::: {.notes}
;; If you're interested, they've made a documentary about their adventures.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-15.png" background-size="cover"}
;; ::: {.notes}
;; So I moved in. It was small, the size of a luxurious dog house.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-16.png" background-size="cover"}
;; ::: {.notes}
;; I lived and worked there for 6 months.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-17.png" background-size="cover"}
;; ::: {.notes}
;; 
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-18.png" background-size="cover"}
;; ::: {.notes}
;; Since I had a boat, I decided to learn how to sail it. However, this was during the pandemic, so there were no courses available. I had to figure it out on my own. My teachers were my friends Valantina and Makko via Facebook Messenger, as they were stuck in the Caribbean with their boat. I'm not sure I would recommend this approach. The first time I went out as the captain of the boat, I hit a rock, which was not fun.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-video="src/resources/atlantic.mp4" background-video-loop="true" background-video-muted="true"}
;; ::: {.notes}
;; Since then, besides the Baltic Sea where I usually sail, I have also sailed in the Mediterranean and the Atlantic.
;; :::
;;
^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-20.png" background-size="cover"}
;; ::: {.notes}
;; This summer, I acquired my third boat. It's still quite small, but it is almost like a miniature version of a Sparkman & Stephens Swan, which sailors here might appreciate. Despite its size, it's robust and reliable, even if it may not be the fastest in modern races.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-21.png" background-size="cover"}
;; ::: {.notes}
;; I thought I would be sailing all summer, perhaps to Belgium, but it is an old boat.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-22.png" background-size="cover"}
;; ::: {.notes}
;; Not just about Instagram and swimsuits, but actually quite a bit of upkeep.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-26.png" background-size="cover"}
;; ::: {.notes}
;; I've also installed some tools that track and log the boat's performance while sailing.
;; :::


^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-23.png" background-size="cover"}
;; ::: {.notes}
;; But fortunately, we were able to enjoy some of the beautiful Baltic archipelago along the southern coastline of Finland.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-24.png" background-size="cover"}
;; ::: {.notes}
;; 
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-25.png" background-size="cover"}
;; ::: {.notes}
;;Enough of this, let's dive into the project. 
;; :::

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/slide-27.png" background-size="cover"}
;; ::: {.notes}
;;
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-28.png" background-size="cover"}
;; ::: {.notes}
;; Why do we want to do that? To create these, namely polar charts. Don't worry about how to read them now, it will become clear later.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-29.png" background-size="cover"}
;; ::: {.notes}
;; What should we do with polar diagrams? We need them if we race, weather routing, and what I refer to as quantified sailing.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-30.png" background-size="cover"}
;; ::: {.notes}
;; Here is an example of a commercial weather routing app that utilizes polar data to help you plan your voyage while considering the varying weather conditions along your route. The app requires information on how your boat performs under different weather scenarios in order to determine your position at a given time.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-31.png" background-size="cover"}
;; ::: {.notes}
;; What makes the boat move? Essentially, two things: water and wind.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-32.png" background-size="cover"}
;; ::: {.notes}
;; Or the way the wind affects the sails and the water impacts the hull.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-33.png" background-size="cover"}
;; ::: {.notes}
;; That's an oversimplification. In actuality, there are numerous variables. Here is a non-exhaustive list resulting from a short brainstorm of what they could be.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-34.png" background-size="cover"}
;; ::: {.notes}
;; But let's keep it simple. For this discussion, let's focus on the wind speed and wind direction.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-35.png" background-size="cover"}
;; ::: {.notes}
;; Or actually, let's make it even simpler and only focus on the wind angle.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; # {background-color="black" background-image="src/resources/slide-36.png" background-size="cover"}
;; ::: {.notes}
;;Imagine for a moment that on earth we only had one constant wind strength and that it never changes.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-37.png" background-size="cover"}
;; ::: {.notes}
;;  And lets agree that the strength is 6 knots. What is 6 knots?
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-38.png" background-size="cover"}
;; ::: {.notes}
;; Its 6.9 mph. What is that?
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-39.png" background-size="cover"}
;; ::: {.notes}
;; It's 11 km/h
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-40.png" background-size="cover"}
;; ::: {.notes}
;; Which is 3 m/s.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-41.png" background-size="cover"}
;; ::: {.notes}
;; So now we need some data. Let's begin with some synthetic data. We prefer synthetic data when we start modeling because it is usually not very noisy and we are familiar with its behavior when we debug our models.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-42.png" background-size="cover"}
;; ::: {.notes}
;; We begin with data generated by a so called VPP. This data serves as the basis for the model we will attempt to build, and it pertains to a boat that is expected to be quite similar to ours.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-43.png" background-size="cover"}
;; ::: {.notes}
;; This is how the looks. Notice how easy it is to make a simple plot with hanamicloth.

;; On the y axis we see the vessel speed.

;; On the x-axis, we have the wind angles. This indicates the angle at which the wind strikes the boat in relation to our direction of the bow, ie. the direction of travel.

;; At 0 angle we get 0 speed, because you cannot sail straight into the wind

;; You just need to bear off a bit and we're already sailing pretty fast.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-44.png" background-size="cover"}
;; ::: {.notes}
;; But what is this slump here? Glad you asked.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-45.png" background-size="cover"}
;; ::: {.notes}
;; Normally, the sail of a boat functions similar to an airplane wing, generating lift as air passes over it. This explains why boats can sometimes move faster than the wind.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-46.png" background-size="cover"}
;; ::: {.notes}
;; I'll give you a moment to digest this, there will be a test at the end.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-47.png" background-size="cover"}
;; ::: {.notes}
;; But in the case of a slump, when the wind is coming from behind, around 180 degrees, we are sailing downwind and the sails no longer function like airplane wings; they act more like parachutes. This can restrict the maximum speed and accounts for the slump.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-48.png" background-size="cover"}
;; ::: {.notes}
;; 
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-49.png" background-size="cover"}
;; ::: {.notes}
;; So lets get busy modelling. Regression is a statistical tool used to uncover the relationship between two variables.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-50.png" background-size="cover"}
;; ::: {.notes}
;; In our scenario, it aims to determine the vessel's velocity given a specific wind angle.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-51.png" background-size="cover"}
;; ::: {.notes}
;; Another way to communicate this is by using an equation like this one. Some of you may recognize that this forms a straight line. The betas represent the parameters or weights, if you prefer. We will attempt to find parameters that produce a line that best fits the data.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-52.png" background-size="cover"}
;; ::: {.notes}
;; Here is the code that builds the model and plots it.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-53.png" background-size="cover"}
;; ::: {.notes}
;;  The function where we model the data is not actually needed as we could pass some flags to hanamicloth that would hten result in a regression line, but we have the more explicit version here to have control and to show how we do more complex versions later. Lets run through what the modelling function does.  
;; First off we take the data with the wind-strength function.
;; We then shape and manipulate the data to fit the requirements of our equation. This process can be smoothly executed with a design matrix, which I will explain in the upcoming slides. It should be noted that in this scenario, it is not actually necessary. Subsequently, we send the matrix to be trained in the model. We utilize the model to forecast new velocity values using a variety of angles provided. After obtaining the predictions, we include additional columns in the dataset and make some adjustments before visualization.

;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-54.png" background-size="cover"}
;; ::: {.notes}
;; Here we have it. We are not surprices, perhaps, but it is safe to say that this model sucks.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-55.png" background-size="cover"}
;; ::: {.notes}
;; So let's play with polynomials! They provide us with greater flexibility when fitting the curve.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-56.png" background-size="cover"}
;; ::: {.notes}
;; So this is a bit more complicated formula, we call it a cubic polynomial.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-57.png" background-size="cover"}
;; ::: {.notes}
;; And once again, here is the equation. "Fitting" refers to the process of attempting to identify the beta parameter combination that best aligns the curve with the data. But to do that our model needs the angle in different forms as we can se in the terms of the equation.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-58.png" background-size="cover"}
;; ::: {.notes}
;; We can easily manipulate the data to meet these requirements using the design matrix. Those familiar with R will recognize this as essentially the same as the beloved formula in R. Perhaps not as elegant, but maybe a bit clearer.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-59.png" background-size="cover"}
;; ::: {.notes}
;; We generate the plot in the same way as before, but now we have a helper function for the modeling thread.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-60.png" background-size="cover"}
;; ::: {.notes}
;; Looks great. Even if we're just eyeballing it, it's fitting quite well!
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-61.png" background-size="cover"}
;; ::: {.notes}
;; If we examine the model, it contains metrics that show the model's performance. An R-squared value of 0.97 is almost perfect. A value of 0 suggests the model doesn't explain any variation in the data, while 1 would indicate the model can explain all the variation in the data.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-62.png" background-size="cover"}
;; ::: {.notes}
;; But unfortunately, or perhaps fortunately, the wind varies on Earth. Therefore, we need to introduce another variable, which includes the wind strength. That means we are testing a multivariate regression.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="white" background-image="src/resources/slide-63.png" background-size="cover"}
;; ::: {.notes}
;; So we include the wind strength and its parameters, alpha, in the model.
;; :::

^:kindly/hide-code
(kind/fragment [])

;; ## {background-color="black" background-image="src/resources/slide-64.png" background-size="cover"}
;; ::: {.notes}
;; We will also update the design matrix to reflect this change.
;; :::

^:kindly/hide-code
(kind/fragment [])
;; ## {background-color="black" background-image="src/resources/slide-65.png" background-size="cover"}
;; ::: {.notes}
;; We will also update our code to generate the plot since we now need to examine the model as a three-dimensional surface.
;; :::

^:kindly/hide-code
(kind/fragment [])



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

;; ## STAN in the house {.v-center-conteiner}
;; ::: {.notes}
;; STAN is a probabilistic programming language that enables users to create intricate statistical models and conduct Bayesian inference on them. It boasts some of the most efficient algorithms for modeling. STAN can be utilized in Clojure with the Scicloj library known as cmdstan-clj.
;; :::

;;::: {layout="[[-1], [1], [-1]]"}

;; ![](src/resources/stan_logo.png){fig-align="center"}

;;:::



;; ## Modelling with STANS
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
;; To keep track of our modeling process, we also include some plots to ensure that everything is progressing as we expect. These look good!
;; :::

^:kindly/hide-code
(delay
  (first (core/plot-one-run @core/results-without-empirical
                      {:colorscale "Greys"}))
  )

;; ## Doing the modelling {.scrollable}
;; ::: {.notes}
;; To keep track of our modeling process, we also include some plots to ensure that everything is progressing as we expect. These look good!
;; :::

^:kindly/hide-code
(delay
  (kind/fragment
   (rest (core/plot-one-run @core/results-without-empirical
                            {:colorscale "Greys"}))))

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

;; ## Comparing synthetic with empirical
;; ::: {.notes}
;; We want to update the theoretical model with our real measured data
;; :::
;; Here are some numbers comparing the optimal performance and the actual performance of the two different distributions.


;; ## Comparing synthetic with empirical  {.scrollable}
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
                    ploclo/plot
                    (assoc-in [:layout :width]  900)
                    (assoc-in [:layout :height]  450))))
         (cons (kind/hiccup
                [:h3 (str "empirical example #" i)]))
         kind/fragment)))

^:kindly/hide-code
(delay
  (show-empirical-example 9))

;; ## Comparing synthetic with empirical
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
                (->> [6 12 18 24]
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
                   :sector [-90 90]})
        (assoc-in [:layout :width]  1200)
        (assoc-in [:layout :height]  600))))


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

;; ## Comparing synthetic with empirical

;; ## Todo
;; [ ] Stan in the house slide
