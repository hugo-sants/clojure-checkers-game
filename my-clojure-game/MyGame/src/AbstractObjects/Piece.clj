(ns AbstractObjects.Piece)

;; Creates a new piece.
;; color: true → blue | false → red
;; isKing: true → king | false → normal piece
(defn make-piece [color isKing]
  {:color color, :isKing isKing})

;; Returns the color of the piece.
(defn get-color [piece]
  (:color piece))

;; Returns true if the piece is a king.
(defn isKing? [piece]
  (:isKing piece))

;; Compares two pieces by color.
(defn piece-equals [p1 p2]
  (= (:color p1) (:color p2)))

;; Returns the visual representation of the piece.
;; 🟣 → blue king | 🔵 → normal blue
;; 🟡 → red king | 🔴 → normal red
(defn piece-toString [piece]
  (case [(get-color piece) (isKing? piece)]
    [true  true]  "🟣"
    [true  false] "🔵"
    [false true]  "🟡"
    [false false] "🔴"))

;; Returns true if the object is a valid piece.
(defn is-piece? [object]
  (not= (get-color object) nil))