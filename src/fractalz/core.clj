(ns fractalz.core
  (:require [bukkure.logging :as log]
            [bukkure.commands :as commands]
            [fractalz.pyramid :as pyramid]))

(defonce plugin (atom nil))

(defn make-sierpinsky-pyramid
  [size name]
  (pyramid/make-sierpinsky-pyramid size
                                   (pyramid/player-by-name name)
                                   @plugin))

(defn sierpinsky
  [sender size]
  (pyramid/make-sierpinsky-pyramid size sender @plugin)
  true)

(defn on-enable [plugin-instance]
  (log/info "Starting your new bukkure plugin!")
  (reset! plugin plugin-instance)

  (log/info "Registering Commands")
  (commands/register-command plugin-instance "sierpinsky" #'sierpinsky :int))

(defn on-disable [plugin]
  (log/info "Stopping your new bukkure plugin!"))
