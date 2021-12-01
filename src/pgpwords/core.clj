(ns pgpwords.core
  (:require [pgpwords.pgpwords :refer :all])
  (:import java.security.SecureRandom)
  (:gen-class))

;; Use for OTCRPP One Time Challenge-Response Pass Phrase

;; randoms
(defn rndbytes
  "Returns a random byte array of the specified size."
  [size]
  (let [seed (byte-array size)]
    (.nextBytes (SecureRandom.) seed)
    seed))

(defn rnd-ints [n] 
  (map #(bit-and % 0xff) (rndbytes n)))

;; interleave utils
(defn interleave-all
  ([c1 c2]
   (lazy-seq
    (let [s1 (seq c1), s2 (seq c2)]
      (if (and s1 s2)
        (cons (first s1) (cons (first s2) (interleave-all (rest s1) (rest s2))))
        (or s1 s2))))))

;; pgpwords system alterates words between lists for parity check
(defn map-alternate
  "map function f to even elements of hs using evenwords list
   map function f to odd  elements of hs using oddwords  list"
  [f hs]
  (let [es (take-nth 2 hs)
        os (take-nth 2 (rest hs))]
    (interleave-all
     (map (partial f evenwords) es)
     (map (partial f oddwords) os))))

;; convert number seq to pgpword seq
(defn hex->words [hs] (map-alternate get hs))

;; convert pgpword seq to number seq
(defn words->hex [hs] (map-alternate #(.indexOf %1 %2) hs))

;; alternates: (.contains wh -1)  or (boolean (some #{-1} wh))
(defn error-check [wordseq]
  (let [h (words->hex wordseq)
        i (.indexOf h -1)]
    (if (= i -1)
      "No Errors"
      (str "First error is word: " (nth wordseq i) ", at position: " (inc i)))))

;; convert byte sequence to an int
(defn ints->long [is]
  (let [facs (iterate #(* % 0x100) 1)]
    (reduce + (map * (reverse is) facs))))

;; convert pgpwords to an int. Use this to find Card ID
(defn words->long [ws]
  (ints->long (words->hex ws)))

;; long to pgpwords
(defn long->words [lv]
  (let [hs (Long/toHexString lv)
        hprs (map #(apply str "0x" %) (partition 2 hs))
        nums (map read-string hprs)]
    (hex->words nums)))

;; same but use ordinary random
(defn pgpwords-gen [n]
    (hex->words
     (take n (repeatedly #(rand-int 0xFF)))))

;; generate N pgpwords
(defn pgpwords-sr-gen [n]
    (hex->words (rnd-ints n)))

(defn crgen
  "generate a card row: random 2 word challenge and 3 word response"
  []
  (let [cints (rnd-ints 2)  ;challenge ints
        rints (rnd-ints 3)] ;response ints
    {:challenge {:i (ints->long cints)
                 :w (hex->words cints)}
     :response  {:i (ints->long rints)
                 :w (hex->words rints)}}))

(defn cardidgen
  "generate a card ID: 3 pgp words and integer equivalent"
  []
  (let [cidints (rnd-ints 3)]  ;card id ints
    {:i (ints->long cidints)
      :w (hex->words cidints)}))




;; HOTP STUFF

(defn secret-key []
  (let [buff (make-array Byte/TYPE 10)]
    (-> (java.security.SecureRandom.)
        (.nextBytes buff))
    (-> (org.apache.commons.codec.binary.Base32.)
        (.encode buff)
        (String.))))

(defn hotp-token [secret idx]
  (let [secret (-> (org.apache.commons.codec.binary.Base32.)
                   (.decode secret))
        idx (-> (java.nio.ByteBuffer/allocate 8)
                (.putLong idx)
                (.array))
        key-spec (javax.crypto.spec.SecretKeySpec. secret "HmacSHA1")
        mac (doto (javax.crypto.Mac/getInstance "HmacSHA1")
              (.init key-spec))
        hash (->> (.doFinal mac idx)
                  (into []))]

    (let [offset (bit-and (hash 19) 0xf)
          bin-code (bit-or (bit-shift-left (bit-and (hash offset) 0x7f) 24)
                           (bit-shift-left (bit-and (hash (+ offset 1)) 0xff) 16)
                           (bit-shift-left (bit-and (hash (+ offset 2)) 0xff) 8)
                           (bit-and (hash (+ offset 3)) 0xff))]
      (format "%06x" (mod bin-code 0xffffff)))))

;; use HOTP
;; NOTE: The value for k must be random, unique and secret!
;;       Do not leave it in a source file.
;;       Do not use this example value, generate your own and
;;       keep it secure.
(def hotp-state (atom {:k "RGCDWWSXRYBXIKBC" :c 0}))

(defn hotp->ints [s]
  (->> (partition 2 s)
       (map #(->> % (apply str) (str "0x") read-string))))

(defn token->words [t]
  (hex->words (hotp->ints t)))

(defn use-one-hotp-token []
  (swap! hotp-state update-in [:c] inc)
  (let [ht (hotp-token (:k @hotp-state) (:c @hotp-state))]
  {:tok ht :words (token->words ht)}))

;; etc utils
(defn ihx "convert int to hex string" [i]
  (Integer/toString i 16))

(defn is->hxs
  "map int seq to pretty hex strings"
  [is]
  (->> is
       (map ihx)
       (map #(.toUpperCase %))
       (map #(str "0x" %))))

(defn validate-ws [wordseq numseq]
  (= (words->hex wordseq) numseq))

;; compare expected vs actual result
;; show first lookup or parity errors in the word sequence
(defn validate-and-errors [wordseq numseq]
  {:valid (validate-ws wordseq numseq)
   :errors (error-check wordseq)})

;; use it
(use-one-hotp-token)
;; => {:tok "6f0da3", :words ("gremlin" "asteroid" "reform")}

;; alt - for pw gen, not for verifiable passphrase

;; gen random and word sequence
(defn pwgen [n]
  (let [myri (rnd-ints n)]
    {:rndint (ints->long myri) :phrase (hex->words myri)}))

(pwgen 6)
;; {:rndint 108606487323372, :phrase ("flagpole" "responsive" "trouble" "borderline" "tiger" "unicorn")}
