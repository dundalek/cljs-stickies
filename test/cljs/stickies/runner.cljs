(ns stickies.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [stickies.core-test]))

(doo-tests 'stickies.core-test)
