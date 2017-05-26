(ns stickies.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [stickies.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
