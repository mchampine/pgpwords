(ns pgpwords.core-test
  (:require [clojure.test :refer :all]
            [pgpwords.core :refer :all]))

(def wse1 ["aardvark" "adviser" "accrue" "aggregate" "adrift"])
(def hse1 [0x00 0x01 0x02 0x03 0x04])

(def wse2 ["adult" "detector" "music" "resistor"])
(def hse2 [0x05 0x45 0x85 0xC5])

(def wse3 ["allow" "direction" "alone" "disable" "ammo" "disbelief"])
(def hse3 [0x0A 0x4A 0x0B 0x4B 0x0C 0x4C])

(def parity-good ["breadline" "corrosion" "Algol" "molecule" "scallion"])
(def parity-good-hex [0x28 0x3A 0x9 0x94 0xB3])
(def parity-bad  ["cellulose" "corrosion" "Algol" "molecule" "scallion"])

(deftest a1
  (testing "4 words"
    (is (= (hex->words hse2) wse2))))

(deftest a2
  (testing "5 words"
    (is (= (hex->words hse1) wse1))))

(deftest a3
  (testing "6 words"
    (is (= (hex->words hse3) wse3))))

(deftest a5
  (testing "parity good"
    (is (= (words->hex parity-good) parity-good-hex))))

(deftest a6
  (testing "parity bad"
    (is (= (words->hex parity-bad) [-1 0x3A 0x9 0x94 0xB3]))))

(deftest a7
  (testing "error check good"
    (is (= (error-check ["adult" "visitor" "tapeworm" "Yucatan"])
           "No Errors"))))

(deftest a8
  (testing "error check bad"
    (is (= (error-check ["adult" "visito" "tapeworm" "Yucatan"])
           "First error is word: visito, at position: 2"))))

(deftest a9
  (testing "round trip"
    (is (= (let [pg7 (pgpwords-gen 7)] (validate-and-errors pg7 (words->hex pg7)))
           {:valid true, :errors "No Errors"}))))

