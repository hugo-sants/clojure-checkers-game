(ns AbstractObjects.Game
  (:require
   [AbstractObjects.Board :refer [count-piece distance-index execute-play
                                  get-piece valid-moviment? valid-pos?]]
   [AbstractObjects.Piece :refer [get-color is-piece? isKing?]]
   [Util.util :refer [get-index-i get-index-j]]))

;; Creates a new game with the initial board.
(defn make-game [board]
  {:board board})

;; Returns the board of a game.
(defn get-board [game]
  (:board game))

;; Checks if the player is moving a piece of their color.
;; Player 1 (true) → blue pieces (true)
;; Player 2 (false) → red pieces (false)
(defn check-player [board player i1 j1]
  (or (and player (get-color (get-piece board i1 j1)))
      (and (not player) (not (get-color (get-piece board i1 j1))))))

;; Checks if a piece can capture any opponent piece.
(defn capture? [board i j]
  (let [piece (get-piece board i j),
        directions [[1 1] [1 -1] [-1 1] [-1 -1]],
        max-distance (if (isKing? piece) 7 2)]
    (some true?
          (for [[di dj] directions
                k (range 2 (inc max-distance))
                :let [i2 (+ i (* k di)),
                      j2 (+ j (* k dj)),
                      ci (+ i (* (dec k) di)),
                      cj (+ j (* (dec k) dj)),
                      middle (get-piece board ci cj)]
                :when (and (<= 0 i2 7)
                           (<= 0 j2 7))]
            (and (valid-moviment? board i j i2 j2)
                 (is-piece? middle)
                 (not= (get-color piece) (get-color middle)))))))

;; Returns the possible capture positions [i j] for a piece.
(defn capture-positions [board i j]
  (let [piece        (get-piece board i j)
        directions   [[1 1] [1 -1] [-1 1] [-1 -1]]
        max-distance (if (isKing? piece) 7 2)]
    (set
     (for [k (range 2 (inc max-distance))
           [di dj] directions
           :let [i2 (+ i (* k di))
                 j2 (+ j (* k dj))]
           :when (and (<= 0 i2 7)
                      (<= 0 j2 7)
                      (valid-moviment? board i j i2 j2))]
       [i2 j2]))))

;; Checks if there is any mandatory capture for the player.
(defn mandatory-capture? [board player]
  (some true?
        (for [i (range 8)
              j (range 8)
              :when (check-player board player i j)]
          (capture? board i j))))

;; Executes a player's move.
;; Returns false if the move is invalid.
(defn make-play [game player pos-initial pos-end]
  (let [board (get-board game),
        i1 (get-index-i pos-initial),
        j1 (get-index-j pos-initial),
        i2 (get-index-i pos-end),
        j2 (get-index-j pos-end),
        must-capture? (mandatory-capture? board player),
        captures-dest (capture-positions board i1 j1)]
    (cond
      ;; Invalid positions or wrong piece
      (or (not (valid-pos? i1 j1))
          (not (valid-pos? i2 j2))
          (not (check-player board player i1 j1))
          (not (valid-moviment? board i1 j1 i2 j2))) false
      ;; Mandatory capture ignored
      (and must-capture?
           (not (contains? captures-dest [i2 j2]))) false
      ;; Valid move
      :else (execute-play board i1 j1 i2 j2))))

;; Checks if a piece can continue capturing after a move.
(defn continue-capture? [board pos-initial pos-end]
  (let [i1 (get-index-i pos-initial),
        j1 (get-index-j pos-initial),
        i2 (get-index-i pos-end),
        j2 (get-index-j pos-end)]
    (if (< (distance-index i1 j1 i2 j2) 2)
      false
      (capture? board i2 j2))))

;; Checks if the player has won (opponent has no pieces left).
(defn player-win? [game player]
  (= (count-piece (get-board game) (not player)) 0))

;; Returns a string representing the current player.
(defn player-toString [player]
  (if player
    "Player: 1 (🔵)"
    "Player: 2 (🔴)"))

;; Returns the player's victory message.
(defn player-win-toString [player]
  (if player
    "🏆 Player-1 won 🏆"
    "🏆 Player-2 won 🏆"))

;; Decides the next game state after a move.
;;   Returns a map with the keys:
;;   - :next-player → boolean (true = player 1, false = player 2)
;;   - :continue-piece → position to be used if there is still a capture
;;   - :finished? → true if the game has ended
(defn next-turn [game player pos1 pos2]
  (let [board (get-board game)]
    (cond
      ;; there is still a capture with the same piece
      (continue-capture? board pos1 pos2)
      {:next-player player :continue-piece pos2 :finished? false}
      ;; player has won
      (player-win? game player)
      {:next-player player :continue-piece nil :finished? true}
      ;; normal turn
      :else
      {:next-player (not player) :continue-piece nil :finished? false})))