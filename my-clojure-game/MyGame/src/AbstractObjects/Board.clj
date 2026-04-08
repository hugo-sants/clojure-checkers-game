(ns AbstractObjects.Board
  (:require [AbstractObjects.Piece :refer [get-color is-piece? isKing? make-piece piece-equals piece-toString]]
            [clojure.string :as str]))

;; Creates the initial game board.
(defn make-board []
  (vec (for [i (range 8)]
         (vec (for [j (range 8)]
                (cond
                  ;; Red pieces (top side)
                  (and (< i 3) (not= (mod (+ i j) 2) 0)) (make-piece false false)
                  ;; Blue pieces (bottom side)
                  (and (> i 4) (not= (mod (+ i j) 2) 0)) (make-piece true false)
                  ;; Empty black squares (playable positions)
                  (not= (mod (+ i j) 2) 0) "⬛"
                  ;; White squares (non-playable)
                  :else "⬜"))))))

;; Returns the textual representation of the board.
(defn board-toString [board]
  (let [header "   a b c d e f g h",
        top "  ________________",
        bottom "  ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾",
        rows (for [i (range 8)]
               (let [linha (for [j (range 8)]
                             (let [obj (get-in board [i j])]
                               (if (is-piece? obj)
                                 (piece-toString obj)
                                 (str obj))))]
                 (str (- 8 i) "|" (apply str linha) "|")))]
    (str/join "\n" (concat [header top] rows [bottom]))))

;; Checks if a position is valid.
;; A position is valid if:
;;  - it is within the bounds [0,7];
;;  - it is a black square (playable position).
(defn valid-pos? [i j]
  (and (<= 0 i 7)
       (<= 0 j 7)
       (not= (mod (+ i j) 2) 0)))

;; Returns the piece at position (i, j).
(defn get-piece [board i j]
  (get-in board [i j]))

;; Returns true if the position is empty.
(defn free-position? [board i j]
  (not (is-piece? (get-piece board i j))))

;; Calculates the distance between two positions.
(defn distance-index [i1 j1 i2 j2]
  (max (Math/abs (- i1 i2))
       (Math/abs (- j1 j2))))

;; Checks if the diagonal between (i1, j1) and (i2, j2) is clear.
;; Returns false if:
;;  - there is a piece of the same color in the path;
;;  - there are two consecutive occupied squares.
(defn diagonal-clear? [board piece i1 j1 i2 j2]
  (let [step-i (compare i2 i1)
        step-j (compare j2 j1)
        distance (dec (distance-index i1 j1 i2 j2))]
    (loop [k 1
           last-occupied false]
      (if (> k distance)
        true
        (let [ci (+ i1 (* k step-i))
              cj (+ j1 (* k step-j))
              obj (get-piece board ci cj)
              occupied? (is-piece? obj)]
          (cond
            (piece-equals piece obj) false
            (and occupied? last-occupied) false
            :else (recur (inc k) occupied?)))))))

;; Checks if the piece movement is valid.
;; Returns true only if:
;;  - the movement follows the piece rules (direction, distance);
;;  - the destination position is free;
;;  - and, in case of capture, the conditions are met.
(defn valid-moviment? [board i1 j1 i2 j2]
  (let [piece (get-piece board i1 j1)
        distance (distance-index i1 j1 i2 j2)]
    (if (or (< distance 1)
            (free-position? board i1 j1)
            (not (free-position? board i2 j2)))
      false
      (if (not (isKing? piece))
        (cond
          ;; Movement too long
          (> distance 2) false
          ;; Simple movement
          (= distance 1)
          (cond
            (and (get-color piece) (> i1 i2)) true  ;; Blue moves up
            (and (not (get-color piece)) (< i1 i2)) true ;; Red moves down
            :else false)
          ;; Capture movement
          (= distance 2)
          (let [ci (/ (+ i1 i2) 2)
                cj (/ (+ j1 j2) 2)
                middle (get-piece board ci cj)]
            (and (is-piece? middle)
                 (not= (get-color piece) (get-color middle))
                 (diagonal-clear? board piece i1 j1 i2 j2)))
          :else false)
        ;; If it is a king
        (diagonal-clear? board piece i1 j1 i2 j2)))))

;; Promotes a piece to king if it reaches the last row of the board.
(defn promote-piece [piece i2]
  (let [color (get-color piece)]
    (if (or (and color (= i2 0))
            (and (not color) (= i2 7)))
      (make-piece color true)
      piece)))

;; Executes the move on the board and removes captured pieces along the path.
(defn execute-play [board i1 j1 i2 j2]
  (let [step-i (compare i2 i1)
        step-j (compare j2 j1)
        distance (dec (distance-index i1 j1 i2 j2))
        piece (get-piece board i1 j1)]
    (loop [k 0
           copy-board board]
      (if (> k distance)
        (assoc-in copy-board [i2 j2] (promote-piece piece i2))
        (let [ci (+ i1 (* k step-i))
              cj (+ j1 (* k step-j))]
          (recur (inc k) (assoc-in copy-board [ci cj] "⬛")))))))

;; Counts the number of pieces of a specific color on the board.
(defn count-piece [board color]
  (count
   (for [i (range (count board))
         j (range (count (board 0)))
         :let [piece (get-piece board i j)]
         :when (and (is-piece? piece)
                    (= (get-color piece) color))]
     piece)))