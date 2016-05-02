(ns zhcljsom.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [zhcljsom.core-test]))

(enable-console-print!)

(doo-tests 'zhcljsom.core-test)
