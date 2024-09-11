(ns jon30.empirical)

(def csvs (->> ["CSV-02_09_2024, 20_40_22.csv"
                "CSV-17_07_2024, 20_50_51.csv"
                "CSV-19_07_2024, 19_38_03.csv"
                "CSV-21_07_2024, 12_47_47.csv"
                "CSV-21_07_2024, 12_48_06.csv"
                "CSV-22_07_2024, 18_03_46.csv"
                "CSV-29_07_2024, 17_56_24.csv"
                "CSV-29_07_2024, 21_48_35.csv"]
               (map #(str "/Users/samikallinen/Downloads/" %))
               (map (fn [csv] (tc/dataset csv {:separator ";"
                                               :key-fn (comp keyword
                                                             #(clojure.string/replace % #"\)" "")
                                                             #(clojure.string/replace % #"\(" "")
                                                             #(clojure.string/replace % #" " "-"))})))
               ((fn [dss]
                  (-> (apply tc/concat dss)
                      (tc/unique-by))))))

(comment nnnnnnnnnnnnnn(-> csvs
             (tc/add-column :TWA #(tcc/abs (:TWA %)))
             (tc/select-columns [:SOG :TWA :TWS])
             (tc/drop-missing)
             (tc/add-column :keep (fn [{:keys [TWS TWA SOG]}]
                                    (map (fn [w a s]
                                           (if (or
                                                (and (> (* s 1.2) w)
                                                     (> s 2))
                                                (< s 0.5)
                                                (and (> (/ s a)  0.05)
                                                     (< w 10)))
                                             :red
                                             :green)) TWS TWA SOG)))
             (tc/select-rows #(-> % :keep #{:green}))
             (tc/drop-columns :keep)
             (tc/write! "jon-empirical.csv")))
