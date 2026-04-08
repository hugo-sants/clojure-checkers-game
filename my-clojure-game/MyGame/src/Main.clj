(ns Main
   (:require
    [AbstractObjects.Board :refer [board-toString make-board]]
    [AbstractObjects.Game :refer [get-board make-game make-play
                                  player-toString player-win-toString
                                  next-turn]]
    [Util.util :refer [clear-screen]]))

 ;; Main function that executes the game loop.
 (defn run-game []
   (let [initial-board (make-board)
         initial-game (make-game initial-board)]
     (loop [current-player true,
            game-state initial-game,
            continue-piece nil]
       (clear-screen)
       (println (board-toString (get-board game-state)))
       (println)
       (println (player-toString current-player))
       (print "Piece: ") (flush)
       (let [pos1 (read-line)]
         ;; It prevents the player from swapping pieces if there is still a mandatory capture.
         (if (and continue-piece (not= continue-piece pos1))
           (do
             (println)
             (println "❌ Ivalid Play! ❌
                       \n You should continue with the same piece. 🔄")
             (Thread/sleep 1900)
             (recur current-player game-state continue-piece))
           (do
             (print "Destination: ") (flush)
             (let [pos2 (read-line),
                   new-board (make-play game-state current-player pos1 pos2)]
               (if (false? new-board)
                 (do
                   (println)
                   (println "❌ Ivalid Play! ❌
                             \n Try again 🔄")
                   (Thread/sleep 1900)
                   (recur current-player game-state continue-piece))
                 (let [new-game-state (make-game new-board)
                       turn-info (next-turn new-game-state current-player pos1 pos2)]
                   (if (:finished? turn-info)
                     (do
                       (clear-screen)
                       (println (board-toString (get-board new-game-state)))
                       (println (player-win-toString current-player)))
                     (recur (:next-player turn-info)
                            new-game-state
                            (:continue-piece turn-info))))))))))))

(defn -main []
  (run-game))
