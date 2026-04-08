(ns Util.util)

;; Clears the terminal and repositions the cursor at the top-left corner.
(defn clear-screen []
  (print (str "\u001b[2J\u001b[H"))
  (flush))

;; Converts a position in the format "a3" to matrix indices.
;; Returns a map with:
;;   :i → row (0–7)
;;   :j → column (0–7)
(defn convert-play [play]
  {:i (- 8 (Character/digit (second play) 10))
   :j (- (int (first play)) (int \a))})

;; Returns the row index from the position.
(defn get-index-i [pos]
  (:i (convert-play pos)))

;; Returns the column index from the position.
(defn get-index-j [pos]
  (:j (convert-play pos)))