^{:clay {:kindly/options {:kinds-that-hide-code #{:kind/hiccup :kind/md}}}}
(ns jon30.core
  (:require [aerial.hanami.templates :as ht]
            [charred.api :as charred]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [jon30.data :as data]
            [jon30.r :as r-helpers]
            [fastmath.interpolation.ssj :as ssj]
            [fastmath.interpolation :as i]
            [fastmath.random :as random]
            [scicloj.cmdstan-clj.v1.api :as stan]
            [scicloj.hanamicloth.v1.api :as haclo]
            [scicloj.hanamicloth.v1.plotlycloth :as ploclo]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.noj.v1.stats :as stats]
            [scicloj.noj.v1.vis.hanami :as hanami]
            [scicloj.noj.v1.vis.stats :as vis.stats]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.dataset.print :as print]
            [fastmath.core :as m]
            [clojisr.v1.r :as r])
  (:import [umontreal.ssj.functionfit BSpline PolInterp SmoothingCubicSpline]
           [umontreal.ssj.functions MathFunction]))
;; # Workbook, notes and explorations
;; Please note that this document is not intended for the conference presentation, but is a working document.
;; # 1.  Question/goal/estimand
;; Predicting boat speed based on wind

;; # 2. Causal models
;;  Optimizing the speed of a sailing vessel involves carefully managing a range of interconnected factors. Central to this is the sail plan, which directly influences the total sail area and, in turn, the vessel's speed. The wind angle and strength also play crucial roles, as they determine how effectively the sails can harness wind power. A well-optimized sail plan will maximize the total sail area appropriate for the conditions, ensuring that the sails are configured to capture the wind most efficiently. Additionally, the hull speed, influenced by the boat's length at the waterline and the healing angle, is a critical determinant of overall speed. Reducing friction and maintaining a favorable healing angle can significantly enhance the hull speed, contributing to a faster vessel.

;; The human factor is equally vital in optimizing sailing speed. The captain's competence directly impacts both their performance and the performance of the crew, both of which are essential for maintaining high vessel speed. Managing fatigue for both the captain and crew is crucial, as fatigue can diminish performance, thereby reducing the vessel's speed. A well-coordinated and skilled crew, led by an experienced and alert captain, can make the necessary adjustments to the sail plan, respond to changing wind conditions, and maintain an optimal healing angle, all of which contribute to maximizing the vessel's speed on the water.

(-> [:svg {:width "1295pt", :height "260pt", :viewbox "0.00 0.00 1295.00 260.00", :xmlns "http://www.w3.org/2000/svg", :xmlns:xlink "http://www.w3.org/1999/xlink"}
     [:g {:id "graph1", :class "graph", :transform "scale(1 1) rotate(0) translate(4 256)"}
      [:title "G"]
      [:polygon {:fill "white", :stroke "white", :points "-4,5 -4,-256 1292,-256 1292,5 -4,5"}] "<!-- Wind Angle -->"
      [:g {:id "node1", :class "node"}
       [:title "Wind Angle"]
       [:ellipse {:fill "none", :stroke "black", :cx "58", :cy "-90", :rx "58.2578", :ry "18"}]
       [:text {:text-anchor "middle", :x "58", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Wind Angle"]] "<!-- Vessel Speed -->"
      [:g {:id "node3", :class "node"}
       [:title "Vessel Speed"]
       [:ellipse {:fill "none", :stroke "black", :cx "467", :cy "-18", :rx "62.1856", :ry "18"}]
       [:text {:text-anchor "middle", :x "467", :y "-13.8", :font-family "Times,serif", :font-size "14.00"} "Vessel Speed"]] "<!-- Wind Angle&#45;&gt;Vessel Speed -->"
      [:g {:id "edge2", :class "edge"}
       [:title "Wind Angle-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M100.393,-77.5986C108.529,-75.5937 117.004,-73.6302 125,-72 220.197,-52.5919 331.58,-36.5564 400.722,-27.3761"}]
       [:polygon {:fill "black", :stroke "black", :points "401.571,-30.7944 411.028,-26.017 400.656,-23.8545 401.571,-30.7944"}]] "<!-- Wind Strength -->"
      [:g {:id "node4", :class "node"}
       [:title "Wind Strength"]
       [:ellipse {:fill "none", :stroke "black", :cx "202", :cy "-90", :rx "67.4433", :ry "18"}]
       [:text {:text-anchor "middle", :x "202", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Wind Strength"]] "<!-- Wind Strength&#45;&gt;Vessel Speed -->"
      [:g {:id "edge4", :class "edge"}
       [:title "Wind Strength-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M248.159,-76.8069C294.08,-64.6771 364.506,-46.0739 412.997,-33.265"}]
       [:polygon {:fill "black", :stroke "black", :points "413.917,-36.642 422.692,-30.7041 412.129,-29.8742 413.917,-36.642"}]] "<!-- Sail Plan -->"
      [:g {:id "node6", :class "node"}
       [:title "Sail Plan"]
       [:ellipse {:fill "none", :stroke "black", :cx "269", :cy "-162", :rx "45.4598", :ry "18"}]
       [:text {:text-anchor "middle", :x "269", :y "-157.8", :font-family "Times,serif", :font-size "14.00"} "Sail Plan"]] "<!-- Sail Plan&#45;&gt;Vessel Speed -->"
      [:g {:id "edge6", :class "edge"}
       [:title "Sail Plan-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M274.054,-143.834C280.761,-123.899 294.46,-91.2919 317,-72 341.696,-50.8625 375.389,-37.9583 404.587,-30.1847"}]
       [:polygon {:fill "black", :stroke "black", :points "405.454,-33.5758 414.297,-27.7407 403.745,-26.7876 405.454,-33.5758"}]] "<!-- Total Sail Area -->"
      [:g {:id "node8", :class "node"}
       [:title "Total Sail Area"]
       [:ellipse {:fill "none", :stroke "black", :cx "396", :cy "-90", :rx "70.0433", :ry "18"}]
       [:text {:text-anchor "middle", :x "396", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Total Sail Area"]] "<!-- Sail Plan&#45;&gt;Total Sail Area -->"
      [:g {:id "edge10", :class "edge"}
       [:title "Sail Plan-&gt;Total Sail Area"]
       [:path {:fill "none", :stroke "black", :d "M294.395,-147.003C312.807,-136.854 338.002,-122.967 358.696,-111.561"}]
       [:polygon {:fill "black", :stroke "black", :points "360.482,-114.573 367.551,-106.681 357.103,-108.443 360.482,-114.573"}]] "<!-- Total Sail Area&#45;&gt;Vessel Speed -->"
      [:g {:id "edge8", :class "edge"}
       [:title "Total Sail Area-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M412.826,-72.411C421.845,-63.519 433.112,-52.4107 443.032,-42.6309"}]
       [:polygon {:fill "black", :stroke "black", :points "445.631,-44.9831 450.295,-35.4699 440.716,-39.9983 445.631,-44.9831"}]] "<!-- Hull Speed -->"
      [:g {:id "node11", :class "node"}
       [:title "Hull Speed"]
       [:ellipse {:fill "none", :stroke "black", :cx "538", :cy "-90", :rx "54.3253", :ry "18"}]
       [:text {:text-anchor "middle", :x "538", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Hull Speed"]] "<!-- Hull Speed&#45;&gt;Vessel Speed -->"
      [:g {:id "edge12", :class "edge"}
       [:title "Hull Speed-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M521.533,-72.7646C512.523,-63.8818 501.191,-52.7095 491.195,-42.8538"}]
       [:polygon {:fill "black", :stroke "black", :points "493.449,-40.1618 483.871,-35.6334 488.535,-45.1465 493.449,-40.1618"}]] "<!-- Length at the Waterline -->"
      [:g {:id "node13", :class "node"}
       [:title "Length at the Waterline"]
       [:ellipse {:fill "none", :stroke "black", :cx "435", :cy "-162", :rx "102.21", :ry "18"}]
       [:text {:text-anchor "middle", :x "435", :y "-157.8", :font-family "Times,serif", :font-size "14.00"} "Length at the Waterline"]] "<!-- Length at the Waterline&#45;&gt;Hull Speed -->"
      [:g {:id "edge14", :class "edge"}
       [:title "Length at the Waterline-&gt;Hull Speed"]
       [:path {:fill "none", :stroke "black", :d "M459.41,-144.411C473.533,-134.813 491.456,-122.632 506.618,-112.328"}]
       [:polygon {:fill "black", :stroke "black", :points "508.873,-115.027 515.177,-106.511 504.939,-109.237 508.873,-115.027"}]] "<!-- Healing angle -->"
      [:g {:id "node15", :class "node"}
       [:title "Healing angle"]
       [:ellipse {:fill "none", :stroke "black", :cx "620", :cy "-162", :rx "64.4013", :ry "18"}]
       [:text {:text-anchor "middle", :x "620", :y "-157.8", :font-family "Times,serif", :font-size "14.00"} "Healing angle"]] "<!-- Healing angle&#45;&gt;Vessel Speed -->"
      [:g {:id "edge18", :class "edge"}
       [:title "Healing angle-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M620.886,-143.863C620.97,-124.26 618.145,-92.263 601,-72 583.017,-50.7468 555.347,-37.9561 529.811,-30.2866"}]
       [:polygon {:fill "black", :stroke "black", :points "530.725,-26.9082 520.153,-27.591 528.843,-33.6505 530.725,-26.9082"}]] "<!-- Healing angle&#45;&gt;Hull Speed -->"
      [:g {:id "edge16", :class "edge"}
       [:title "Healing angle-&gt;Hull Speed"]
       [:path {:fill "none", :stroke "black", :d "M600.981,-144.765C590.141,-135.511 576.39,-123.772 564.505,-113.626"}]
       [:polygon {:fill "black", :stroke "black", :points "566.511,-110.737 556.633,-106.906 561.966,-116.061 566.511,-110.737"}]] "<!-- Captain Competence -->"
      [:g {:id "node18", :class "node"}
       [:title "Captain Competence"]
       [:ellipse {:fill "none", :stroke "black", :cx "1033", :cy "-234", :rx "91.1371", :ry "18"}]
       [:text {:text-anchor "middle", :x "1033", :y "-229.8", :font-family "Times,serif", :font-size "14.00"} "Captain Competence"]] "<!-- Captain Performance -->"
      [:g {:id "node20", :class "node"}
       [:title "Captain Performance"]
       [:ellipse {:fill "none", :stroke "black", :cx "1124", :cy "-162", :rx "92.2271", :ry "18"}]
       [:text {:text-anchor "middle", :x "1124", :y "-157.8", :font-family "Times,serif", :font-size "14.00"} "Captain Performance"]] "<!-- Captain Competence&#45;&gt;Captain Performance -->"
      [:g {:id "edge20", :class "edge"}
       [:title "Captain Competence-&gt;Captain Performance"]
       [:path {:fill "none", :stroke "black", :d "M1054.57,-216.411C1066.38,-207.324 1081.2,-195.922 1094.11,-185.989"}]
       [:polygon {:fill "black", :stroke "black", :points "1096.48,-188.584 1102.27,-179.713 1092.21,-183.035 1096.48,-188.584"}]] "<!-- Captain Performance&#45;&gt;Vessel Speed -->"
      [:g {:id "edge24", :class "edge"}
       [:title "Captain Performance-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M1100.6,-144.439C1069.91,-123.66 1013.66,-88.7251 960,-72 815.736,-27.0338 637.517,-18.8144 539.738,-18.0897"}]
       [:polygon {:fill "black", :stroke "black", :points "539.682,-14.5895 529.664,-18.0396 539.647,-21.5894 539.682,-14.5895"}]] "<!-- Crew Performance -->"
      [:g {:id "node25", :class "node"}
       [:title "Crew Performance"]
       [:ellipse {:fill "none", :stroke "black", :cx "868", :cy "-90", :rx "83.5383", :ry "18"}]
       [:text {:text-anchor "middle", :x "868", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Crew Performance"]] "<!-- Captain Performance&#45;&gt;Crew Performance -->"
      [:g {:id "edge26", :class "edge"}
       [:title "Captain Performance-&gt;Crew Performance"]
       [:path {:fill "none", :stroke "black", :d "M1072.81,-147.003C1030.89,-135.541 971.537,-119.311 927.507,-107.272"}]
       [:polygon {:fill "black", :stroke "black", :points "928.364,-103.877 917.795,-104.616 926.517,-110.629 928.364,-103.877"}]] "<!-- Captain Fatigue -->"
      [:g {:id "node21", :class "node"}
       [:title "Captain Fatigue"]
       [:ellipse {:fill "none", :stroke "black", :cx "1215", :cy "-234", :rx "72.1822", :ry "18"}]
       [:text {:text-anchor "middle", :x "1215", :y "-229.8", :font-family "Times,serif", :font-size "14.00"} "Captain Fatigue"]] "<!-- Captain Fatigue&#45;&gt;Captain Performance -->"
      [:g {:id "edge22", :class "edge"}
       [:title "Captain Fatigue-&gt;Captain Performance"]
       [:path {:fill "none", :stroke "black", :d "M1193.89,-216.765C1182.01,-207.621 1166.97,-196.05 1153.88,-185.988"}]
       [:polygon {:fill "black", :stroke "black", :points "1155.68,-182.956 1145.62,-179.633 1151.42,-188.505 1155.68,-182.956"}]] "<!-- Crew Performance&#45;&gt;Vessel Speed -->"
      [:g {:id "edge32", :class "edge"}
       [:title "Crew Performance-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M805.344,-78.0625C730.027,-64.915 604.414,-42.9875 529.27,-29.8701"}]
       [:polygon {:fill "black", :stroke "black", :points "529.466,-26.3514 519.013,-28.0795 528.262,-33.2471 529.466,-26.3514"}]] "<!-- Crew Competence -->"
      [:g {:id "node26", :class "node"}
       [:title "Crew Competence"]
       [:ellipse {:fill "none", :stroke "black", :cx "786", :cy "-162", :rx "82.4479", :ry "18"}]
       [:text {:text-anchor "middle", :x "786", :y "-157.8", :font-family "Times,serif", :font-size "14.00"} "Crew Competence"]] "<!-- Crew Competence&#45;&gt;Crew Performance -->"
      [:g {:id "edge28", :class "edge"}
       [:title "Crew Competence-&gt;Crew Performance"]
       [:path {:fill "none", :stroke "black", :d "M805.433,-144.411C815.976,-135.41 829.18,-124.139 840.737,-114.273"}]
       [:polygon {:fill "black", :stroke "black", :points "843.089,-116.867 848.422,-107.713 838.544,-111.543 843.089,-116.867"}]] "<!-- Crew Fatigue -->"
      [:g {:id "node28", :class "node"}
       [:title "Crew Fatigue"]
       [:ellipse {:fill "none", :stroke "black", :cx "950", :cy "-162", :rx "62.9956", :ry "18"}]
       [:text {:text-anchor "middle", :x "950", :y "-157.8", :font-family "Times,serif", :font-size "14.00"} "Crew Fatigue"]] "<!-- Crew Fatigue&#45;&gt;Crew Performance -->"
      [:g {:id "edge30", :class "edge"}
       [:title "Crew Fatigue-&gt;Crew Performance"]
       [:path {:fill "none", :stroke "black", :d "M930.981,-144.765C920.372,-135.708 906.974,-124.27 895.266,-114.276"}]
       [:polygon {:fill "black", :stroke "black", :points "897.363,-111.464 887.485,-107.633 892.818,-116.788 897.363,-111.464"}]]]]
    kind/hiccup)
;;But we will not start by modeling all of this right now. To understand the problem and showcase some of the tools today, we need to simplify the problem a lot. So today, the goal is to be able to model the vessel's speed based on wind angle and wind strength.
(->  [:svg {:width "277pt", :height "116pt", :viewbox "0.00 0.00 277.00 116.00", :xmlns "http://www.w3.org/2000/svg", :xmlns:xlink "http://www.w3.org/1999/xlink"}
      [:g {:id "graph1", :class "graph", :transform "scale(1 1) rotate(0) translate(4 112)"}
       [:title "G"]
       [:polygon {:fill "white", :stroke "white", :points "-4,5 -4,-112 274,-112 274,5 -4,5"}] "<!-- Wind Angle -->"
       [:g {:id "node1", :class "node"}
        [:title "Wind Angle"]
        [:ellipse {:fill "none", :stroke "black", :cx "58", :cy "-90", :rx "58.2578", :ry "18"}]
        [:text {:text-anchor "middle", :x "58", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Wind Angle"]] "<!-- Vessel Speed -->"
       [:g {:id "node3", :class "node"}
        [:title "Vessel Speed"]
        [:ellipse {:fill "none", :stroke "black", :cx "130", :cy "-18", :rx "62.1856", :ry "18"}]
        [:text {:text-anchor "middle", :x "130", :y "-13.8", :font-family "Times,serif", :font-size "14.00"} "Vessel Speed"]] "<!-- Wind Angle&#45;&gt;Vessel Speed -->"
       [:g {:id "edge2", :class "edge"}
        [:title "Wind Angle-&gt;Vessel Speed"]
        [:path {:fill "none", :stroke "black", :d "M75.063,-72.411C84.209,-63.519 95.6348,-52.4107 105.694,-42.6309"}]
        [:polygon {:fill "black", :stroke "black", :points "108.329,-44.9503 113.059,-35.4699 103.45,-39.9313 108.329,-44.9503"}]] "<!-- Wind Strength -->"
       [:g {:id "node4", :class "node"}
        [:title "Wind Strength"]
        [:ellipse {:fill "none", :stroke "black", :cx "202", :cy "-90", :rx "67.4433", :ry "18"}]
        [:text {:text-anchor "middle", :x "202", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Wind Strength"]] "<!-- Wind Strength&#45;&gt;Vessel Speed -->"
       [:g {:id "edge4", :class "edge"}
        [:title "Wind Strength-&gt;Vessel Speed"]
        [:path {:fill "none", :stroke "black", :d "M184.937,-72.411C175.791,-63.519 164.365,-52.4107 154.306,-42.6309"}]
        [:polygon {:fill "black", :stroke "black", :points "156.55,-39.9313 146.941,-35.4699 151.671,-44.9503 156.55,-39.9313"}]]]]

     kind/hiccup)

;; And even earlier than that. Let's further simplify the issue by only considering the wind angle and assuming that the wind always blows at 10 knots.
(-> [:svg {:width "132pt", :height "116pt", :viewbox "0.00 0.00 132.00 116.00", :xmlns "http://www.w3.org/2000/svg", :xmlns:xlink "http://www.w3.org/1999/xlink"}
     [:g {:id "graph1", :class "graph", :transform "scale(1 1) rotate(0) translate(4 112)"}
      [:title "G"]
      [:polygon {:fill "white", :stroke "white", :points "-4,5 -4,-112 129,-112 129,5 -4,5"}] "<!-- Wind Angle -->"
      [:g {:id "node1", :class "node"}
       [:title "Wind Angle"]
       [:ellipse {:fill "none", :stroke "black", :cx "62", :cy "-90", :rx "58.2578", :ry "18"}]
       [:text {:text-anchor "middle", :x "62", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Wind Angle"]] "<!-- Vessel Speed -->"
      [:g {:id "node3", :class "node"}
       [:title "Vessel Speed"]
       [:ellipse {:fill "none", :stroke "black", :cx "62", :cy "-18", :rx "62.1856", :ry "18"}]
       [:text {:text-anchor "middle", :x "62", :y "-13.8", :font-family "Times,serif", :font-size "14.00"} "Vessel Speed"]] "<!-- Wind Angle&#45;&gt;Vessel Speed -->"
      [:g {:id "edge2", :class "edge"} 2
       [:title "Wind Angle-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M62,-71.6966C62,-63.9827 62,-54.7125 62,-46.1jx1124"}]
       [:polygon {:fill "black", :stroke "black", :points "65.5001,-46.1043 62,-36.1043 58.5001,-46.1044 65.5001,-46.1043"}]]]]
    kind/hiccup)

;; Or, actually, We will collaps all the other to the unobserved variable and the error term.
(-> [:svg {:width "257pt", :height "116pt", :viewbox "0.00 0.00 257.00 116.00", :xmlns "http://www.w3.org/2000/svg", :xmlns:xlink "http://www.w3.org/1999/xlink"}
     [:g {:id "graph1", :class "graph", :transform "scale(1 1) rotate(0) translate(4 112)"}
      [:title "G"]
      [:polygon {:fill "white", :stroke "white", :points "-4,5 -4,-112 254,-112 254,5 -4,5"}] "<!-- Wind Angle -->"
      [:g {:id "node1", :class "node"}
       [:title "Wind Angle"]
       [:ellipse {:fill "none", :stroke "black", :cx "58", :cy "-90", :rx "58.2578", :ry "18"}]
       [:text {:text-anchor "middle", :x "58", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Wind Angle"]] "<!-- Vessel Speed -->"
      [:g {:id "node3", :class "node"}
       [:title "Vessel Speed"]
       [:ellipse {:fill "none", :stroke "black", :cx "125", :cy "-18", :rx "62.1856", :ry "18"}]
       [:text {:text-anchor "middle", :x "125", :y "-13.8", :font-family "Times,serif", :font-size "14.00"} "Vessel Speed"]] "<!-- Wind Angle&#45;&gt;Vessel Speed -->"
      [:g {:id "edge2", :class "edge"}
       [:title "Wind Angle-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M73.8781,-72.411C82.3055,-63.6062 92.813,-52.6282 102.106,-42.9189"}]
       [:polygon {:fill "black", :stroke "black", :points "104.85,-45.1143 109.236,-35.4699 99.7928,-40.274 104.85,-45.1143"}]] "<!-- Unobserved -->"
      [:g {:id "node4", :class "node"}
       [:title "Unobserved"]
       [:ellipse {:fill "none", :stroke "black", :cx "192", :cy "-90", :rx "57.5405", :ry "18"}]
       [:text {:text-anchor "middle", :x "192", :y "-85.8", :font-family "Times,serif", :font-size "14.00"} "Unobserved"]] "<!-- Unobserved&#45;&gt;Vessel Speed -->"
      [:g {:id "edge4", :class "edge"}
       [:title "Unobserved-&gt;Vessel Speed"]
       [:path {:fill "none", :stroke "black", :d "M176.122,-72.411C167.694,-63.6062 157.187,-52.6282 147.894,-42.9189"}]
       [:polygon {:fill "black", :stroke "black", :points "150.207,-40.274 140.764,-35.4699 145.15,-45.1143 150.207,-40.274"}]]]]
    kind/hiccup)

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
;; Generating synthetic data to develop models. Utilizing a VPP (Velocity Prediction Program), a tool that uses a physical model to compute the polars. Data is created using a free online tool by inputting boat data to generate polars for wind speeds of 6, 10, 15, and 20 knots. The tool produces a polar diagram as an image. Without access to an API or exact values, I used SketchUp to scale the image correctly and manually measured the specific points below.

(->
 data/vpp-polar-01
 (->>
  (map #(update % :wind str)))
 tc/dataset
 (ploclo/layer-line
  {:=r :speed
   :=theta :angle
   :=coordinates :polar
   :=rotation 90
   :=color :wind}))

;; ## TODO
"
    polar: {
      domain: {
        x: [0,0.4],
        y: [0,1]
      },
      radialaxis: {
        tickfont: {
          size: 8
        }
      },
      angularaxis: {
        tickfont: {
          size: 8
        },
        rotation: 90,
        direction: \"counterclockwise\"
      }
    },
"

;; Access at least `direction` and `rotation`

;; ## Explore in cartesian coordinates
(->
 data/vpp-polar-01
 (->>
  (map #(update % :wind str)))
 tc/dataset
 (haclo/plot haclo/point-chart
             {:=x :angle
              :=y :speed
              :=color :wind}))

;; ## Generate splines to explore the curves

(def splines
  (->> data/vpp-polar-01
       (group-by :wind)
       vals
       (map (fn [v]
              #_{(:wind (first v))
                 (ssj-math-function (BSpline. (m/seq->double-array
                                               (map :angle v))
                                              (m/seq->double-array
                                               (map :speed v))
                                              (int (dec (count
                                                         (map :angle v))))))}
              {(:wind (first v)) (i/interpolation :b-spline
                                                  (map :angle v)
                                                  (map :speed v))}))
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
                       ys (m/seq->double-array (map :speed v))
                       ^MathFunction obj (BSpline. xs ys (unchecked-int (or degree (m/dec (count xs)))))]
                   {:f      (fn ^double [^double x] (.evaluate obj x))
                    :knots (.getKnots obj)
                    :y (.getY obj)
                    :x (.getX obj)})}
                #_{(:wind (first v)) (i/interpolation :b-spline
                                                      (map :angle v)
                                                      (map :speed v))}))
         (mapcat identity)
         (into {})))

(def spline-ds
  (->> splines
       (map (fn [[w f]]
              (let [angles (range 181)]
                (map (fn [angle]
                       {:angle angle
                        :speed (f angle)
                        :wind (str "spline-" w)}) angles))))
       (mapcat identity)
       #_(into data/vpp-polar-01)
       (map #(update % :wind str))
       tc/dataset))
;; ## Spline for 10 knots of wind
(-> spline-ds
    (tc/select-rows (comp #{"spline-10"} :wind))
    (haclo/plot haclo/line-chart
                {:=x :angle
                 :=y :speed
                 :=color :wind}))

;; # Genareting data for constant 10 knot wind
;; Generating syntethetic measurement points pretending the wind is always at streanth 10

#_(->> (repeatedly 300 #(rand-int 180))
     (map (fn [angle]
            (let [mu ((get splines 10) angle)
                  speed (random/sample
                         (random/distribution :normal
                                              {:sd 0.3 :mu mu}))]
              {:angle angle
               :speed speed
               :wind "spline-10"})))
     tc/dataset
     (#(haclo/plot %  haclo/point-chart
                   {:=x :angle
                    :=y :speed})))

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
    array[n] int speed;
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
        speed[i] ~ normal(mu[i], sigma);
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
               :speed (-> dat :doy vec)
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
    array[n] real speed;
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
        speed[i] ~ normal(mu[i], sigma);
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
                  speed (random/sample
                         (random/distribution :normal
                                              {:sd 0.3 :mu mu}))]
              {:angle angle
               :speed speed})))
     tc/dataset
     ((fn [dat]
        (-> dat
            (tc/drop-columns :wind)
            (r-helpers/base-function "angle" 5)
            ((fn [B]
               {:B (tc/rows B)
                :k (count B)
                :n (-> dat :angle count)
                :speed (-> dat :speed vec)
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
            (tc/select-columns [:angle :row-id :speed :mean :sd :ptile-2.5 :ptile-97.5])
            (tc/add-column :mean-sd (fn [row] (map #(- %1 %2) (:mean row) (:sd row))))
            (tc/add-column :mean+sd (fn [row] (map #(+ %1 %2) (:mean row) (:sd row))))
            #_(tc/pivot->longer (complement #{:year}))
            (haclo/base     {:=title "Distribution of means"
                             :=x :angle
                             :=y :speed})
            haclo/layer-point
            (haclo/layer-line {:=y :mean
                               :=mark-color "red"})
            (haclo/layer-line {:=y :mean-sd
                               :=mark-color "gray"})
            (haclo/layer-line {:=y :mean+sd
                               :=mark-color "gray"}))))

     ;;
     )

;; Splines for all wind speeds
(-> spline-ds
    (haclo/plot haclo/line-chart
                {:=x :angle
                 :=y :speed
                 :=color :wind}))

;; ## Normalized
(-> spline-ds
    (tc/group-by :wind)
    (tc/add-column :normalised #(tcc/normalize (:speed %)))
    tc/ungroup
    (haclo/plot haclo/line-chart
                {:=x :angle
                 :=y :normalised
                 :=color :wind}))


;; ## Scaled
;; Examining how the curves behave when scaled can provide insight into how to construct a model.
(-> spline-ds
    (tc/group-by :wind)
    (tc/add-column :max #(apply max (:speed %)))
    tc/ungroup
    (tc// :scaled [:speed :max])
    (haclo/plot haclo/line-chart
                {:=x :angle
                 :=y :scaled
                 :=color :wind}))

(-> spline-ds
    (tc/group-by :wind)
    (tc/add-column :max #(apply max (:speed %)))
    tc/ungroup
    (tc// :scaled [:speed :max])
    (tc/drop-columns [:speed :max])
    (tc/pivot->wider :wind :scaled)
    (tc/- :speed-diff-20-6 ["spline-20" "spline-6"])
    (tc/- :speed-diff-20-10 ["spline-20" "spline-10"])
    (tc/- :speed-diff-20-15 ["spline-20" "spline-15"])
    (tc/drop-columns ["spline-20" "spline-15" "spline-10" "spline-6"])
    (tc/pivot->longer (complement #{:angle}) {:target-columns :wind
                                              :value-column-name :diff})
    (haclo/plot haclo/line-chart
                {:=title "Wind strength component?"
                 :=x :angle
                 :=y :diff
                 :=color :wind}))

;; ## Cubic polynomial?

;; Fitted with https://curve.fit/zmP2BfIJ/single/20240823093635
(defn cubic-polynomial
  [x]
  (+ (* 5.008E-06 x x x)
     (* -1.744E-03 x x)
     (* 1.783E-01 x)
     1.005E-02))



(->> (range 1 181)
       (map (fn [x]
              {:x x
               :y (cubic-polynomial x)}))

       tc/dataset
       ((fn [ds]
          (haclo/plot ds haclo/line-chart
                      {:=x :x
                       :=y :y}))))

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


