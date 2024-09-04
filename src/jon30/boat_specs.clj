(ns jon30.boat-specs)



(def yacht {:name "jon30"
            ;; Lwl : Length waterline (m)
            ;; Approximated from blueprints
            :lwl 7.5
  ;; Vol : Displ. volume of canoebody (m^3)
  ;; Not sure about this, but since the water displaced = mass of
  ;; vessel we are guesstimating that this is same as the mass of the
  ;; boat. ie 1 m^3 = 1 tonne of displacement
            :vol 4.5
  ;; Bwl : Beam waterline (m)
  ;; Approximated from blueprints
            :bwl 2.7
  ;; Tc : Canoe body draft (m)
  ;; Approximated from blueprints
            :tc 0.7
  ;; WSA : Wetted surface area (m^2)
  ;; Approximated from blueprints
            :wsa 14.27
  ;; Tmax : Draft max, i.e. Keel (m)
  ;; Measured (but varied depending on load)
            :tmax 1.65
  ;; Amax : Max. section area (m^2)
  ;; Approximated from blueprints
            :amax 1.1204
  ;; Mass : Total mass of the yacht, including keel (kg)
  ;; Based on reported figures (can be inaccurate, the reported figures vary)
            :mass 4500
  ;; Ff : Freeboard height fore (m)
  ;; Currently mean that is approximated from blueprints
            :ff 0.94
  ;; Fa : Freeboard height aft (m)
  ;; Currently mean that is approximated from blueprints
            :fa 0.94
  ;; Boa : Beam overall (m)
  ;; Measured and reported, high certainty
            :boa 3.03
  ;; Loa : Length overall (m)
  ;; Measured and reported, high certainty
            :loa 9.06}

;;
  )

;; In yacht design, particularly when referring to a **keel** or **foil-like structures** (such as rudders or daggerboards), the terms **root chord, upper chord, tip chord, lower chord,** and **span** are used to describe various dimensions of these appendages. These terms relate to the shape and size of the keel or foil, which significantly impact the yacht's hydrodynamic performance. Here's what each term means:

;; ### 1. **Root Chord (Upper Chord):**
;;    - The **root chord** (sometimes called the **upper chord**) is the distance between the leading edge (front) and the trailing edge (back) of the keel **where it attaches to the hull of the yacht**. 
;;    - It is typically the widest section of the keel because more surface area is generally needed near the hull to generate stability and lift.
;;    - The root chord influences the lift and drag characteristics at the top of the keel, impacting overall boat stability.

;; ### 2. **Tip Chord (Lower Chord):**
;;    - The **tip chord** (sometimes called the **lower chord**) is the distance between the leading and trailing edges at the **bottom of the keel**, near its tip.
;;    - This part of the keel is often narrower than the root chord and is critical in controlling drag. A smaller tip chord can reduce induced drag, improving efficiency.
;;    - The shape of the tip chord affects how water flows around the keel and influences both lift and drag.

;; ### 3. **Span:**
;;    - The **span** of a keel refers to its **vertical height**, or the distance from where it attaches to the hull (root) down to its tip (bottom). It is essentially the **depth** or **height** of the keel in the water.
;;    - A greater span provides more lateral resistance, which improves upwind performance and stability by reducing sideways movement (leeway). However, a longer span also increases the **draft** of the yacht, which can limit where the yacht can sail (e.g., in shallow waters).
;;    - The span plays a critical role in the keel’s hydrodynamic properties, affecting how much lift the keel generates and how much drag it produces.

;; ### 4. **Chord:**
;;    - In general, the **chord** refers to the straight-line distance between the leading edge and the trailing edge of the keel or foil at any given point along its span.
;;    - The chord length typically changes along the span of the keel, with the root chord (near the hull) being the longest and the tip chord (at the bottom) being shorter. This tapering shape reduces drag and improves efficiency.

;; ### Summary of Chord Terms in Context:
;; - **Root Chord (Upper Chord):** The distance between the leading and trailing edges at the top (where the keel meets the hull).
;; - **Tip Chord (Lower Chord):** The distance between the leading and trailing edges at the bottom (near the keel's tip).
;; - **Span:** The vertical distance from the top of the keel (attached to the hull) to its bottom (the keel tip).

;; ### Keel Shape and Performance:
;; The combination of these elements — root chord, tip chord, and span — shapes the keel's profile, significantly affecting the yacht's **stability, lift, drag**, and overall **sailing performance**. An optimized keel design is crucial for balancing speed, maneuverability, and the ability to sail effectively in various wind and water conditions.

(def keel
  { ;; Cu : Root Chord / Upper Chord (m)
   ;; Approximated from blueprints
   :cu 2.12
   ;; Cl : Tip Chord / Lower Chord (m)
   ;; Approximated from blueprints
   :cl 1.15
   ;; Span : Span (m)
   ;; Currently guess, need to approximate from blueprint
   :span 1})


(def rudder
  { ;; Cu : Root Chord / Upper Chord (m)
   ;; Approximated from blueprints
   :cu 0.66
   ;; Cl : Tip Chord / Lower Chord (m)
   ;; Approximated from blueprints
   :cl 0.6
   ;; Span : Span (m)
   ;; Approximated from blueprints
   :span 1.2})

;; In yacht sail design, the variables you've provided describe key dimensions and attributes of the **main sail**, **jib (headsail)**, and **kite (spinnaker or asymmetric spinnaker)**. Here’s what each of these terms and variables mean:

;; ### 1. **Main Sail Variables**:
;; ```python
;; main = {"Name": "MN1", "P": 16.60, "E": 5.60, "Roach": 0.1, "BAD": 1.0}
;; ```

;; - **Name**: "MN1" is the identifier for the main sail.
  
;; - **P**: This is the **luff length** of the mainsail, measured in meters. It is the distance from the top of the boom to the top of the mast (vertical measurement along the mast). In this case, **P = 16.60 m**.

;; - **E**: This is the **foot length** of the mainsail, measured in meters. It is the horizontal distance from the mast to the clew (the aft lower corner of the sail, near the end of the boom). Here, **E = 5.60 m**.

;; - **Roach**: The **roach** is an indicator of the extra area of the sail that bulges beyond the straight line between the head (top) and clew (bottom). It’s typically expressed as a percentage. The roach here is defined as **0.1** (or 10%). The provided formula for roach is:
  
;;   \[
;;   \text{Roach} = 1 - \frac{A}{0.5PE}
;;   \]
  
;;   Where:
;;   - **A** is the area of the mainsail.
;;   - **PE** is the product of **P** and **E**, representing a general area approximation of a triangular mainsail.

;; - **BAD**: This stands for **Boom Above Deck** and represents the height (in meters) from the deck to the boom. A **BAD** value of **1.0 m** indicates that the boom is 1 meter above the deck.

;; ### 2. **Jib (Headsail) Variables**:
;; ```python
;; jib = {"Name": "J1", "I": 16.20, "J": 5.10, "LPG": 5.40, "HBI": 1.8}
;; ```

;; - **Name**: "J1" is the identifier for the jib (headsail).

;; - **I**: This is the height of the foretriangle, measured vertically from the deck to where the forestay attaches to the mast (top of the foretriangle). It defines the height of the jib. In this case, **I = 16.20 m**.

;; - **J**: This is the base length of the foretriangle, measured horizontally from the mast to the forestay attachment point on the deck. It defines the foot of the jib. Here, **J = 5.10 m**.

;; - **LPG**: This stands for **Luff Perpendicular** and is the distance from the clew of the jib to the luff (the forward edge) measured perpendicularly. It's an important measurement in calculating jib sail area. **LPG = 5.40 m** in this case.

;; - **HBI**: **Height of the Bottom of the Inner stay** (sometimes called inner headstay height). It refers to how high the inner stay (on a cutter rig, for example) is from the deck. **HBI = 1.8 m** represents this height.

;; ### 3. **Kite (Spinnaker/Asymmetric Spinnaker) Variables**:
;; ```python
;; kite = {"Name": "A2", "area": 150.0, "vce": 9.55}
;; ```

;; - **Name**: "A2" refers to the specific type of spinnaker. In racing terms, A2 typically refers to a large running spinnaker designed for downwind sailing in light to moderate wind conditions.

;; - **Area**: This is the total sail area of the kite, measured in square meters. **Area = 150.0 m²** is a relatively large sail, used to capture maximum wind when sailing downwind.

;; - **vce**: **Vertical Center of Effort** is the height at which the aerodynamic forces acting on the sail are concentrated. It is the distance from the deck to the center of the sail’s area, expressed in meters. For this kite, **vce = 9.55 m**. The higher the vce, the more leverage the sail exerts on the boat, which can affect stability.

;; ### Summary of Key Measurements:
;; - **Main Sail**:
;;   - **P** (luff length) = 16.60 m
;;   - **E** (foot length) = 5.60 m
;;   - **Roach** is defined by a custom formula that modifies how much sail area extends beyond a straight triangle.
;;   - **BAD** (Boom Above Deck) = 1.0 m

;; - **Jib (Headsail)**:
;;   - **I** (height of the foretriangle) = 16.20 m
;;   - **J** (base of the foretriangle) = 5.10 m
;;   - **LPG** (Luff Perpendicular) = 5.40 m
;;   - **HBI** (Height of Bottom of Inner stay) = 1.8 m

;; - **Kite (Spinnaker)**:
;;   - **Area** = 150.0 m²
;;   - **vce** (Vertical Center of Effort) = 9.55 m

;; These variables, together with standard measurements like **I** and **J** for jibs or **P** and **E** for mainsails, help define the shape, size, and performance characteristics of a sail plan on a yacht.


(def main
  {:name "Main"
   ;;  P (luff length)
   ;; approximated
   :P 10.50
   ;; E (foot length)
   ;; approximated
   :E 2.7
   ;; Roach is defined by a custom formula that modifies how much sail
   ;; area extends beyond a straight triangle.
   ;; guess
   :roach 0.1
   ;; BAD (Boom Above Deck)
   ;; Approximated from blueprints
   :BAD 1.4})

(def jib
  {:name "Genua"
   ;; I (height of the foretriangle)
   ;; Approximated
   :I 12.1
   ;; J (base of the foretriangle)
   :J 3.8
   ;; LPG (Luff Perpendicular)
   :LPG 5.7
   ;; HBI (Height of Bottom of Inner stay)
   :HBI 5.87

   })

;; main = {"Name": "MN1", "P": 16.60, "E": 5.60, "Roach": 0.1, "BAD": 1.0}
;; jib = {"Name": "J1", "I": 16.20, "J": 5.10, "LPG": 5.40, "HBI": 1.8}
;; kite = {"Name": "A2", "area": 150.0, "vce": 9.55}

;; App : List of appendages
;; Sails : List of Sails
;; Sails: Standard measurements, except Roach is defined as 1-A/(0.5PE) Kite only takes area and vce estimate (this is very rough)
;; VPP.set_analysis()
;; TWA range : range of TWA to use
;; TWS range : range of TWS, must be between [2, 35]


;; Cu : Root Chord / Upper Chord (m)
;; Cl : Tip Chord / Lower Chord (m)
;; Span : Span (m)
