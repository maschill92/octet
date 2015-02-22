(ns bytebuf.spec
  (:refer-clojure :exclude [type read])
  (:require [bytebuf.proto :as proto :refer [IStaticSize]]
            [bytebuf.buffer :as buffer]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Abstraction definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol ISpecType
  (tag [_] "Get the type tag."))

(defprotocol IReadableSpec
  (read [_ buff start] "Read all data from buffer."))

(defprotocol IWritableSpec
  (write [_ buff start data] "Read all data from buffer."))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Spec Constructors
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti spec
  (fn [& params]
    (cond
      (and (> (count params) 1)
           (even? (count params)))
      :associative

      (and (= (count params) 1)
           (instance? clojure.lang.IPersistentVector (first params)))
      :indexed)))

(defmethod spec :associative
  [& params]
  (let [data (mapv vec (partition 2 params))
        dict (into {} data)
        types (map second data)]
    (reify
      clojure.lang.Counted
      (count [_]
        (count types))

      IStaticSize
      (size [_]
        (reduce #(+ %1 (proto/size %2)) 0 types))

      IReadableSpec
      (read [_ buff pos]
        (loop [index pos result {} pairs data]
          (if-let [[fieldname type] (first pairs)]
            (let [[readeddata readedbytes] (read type buff index)]
              (recur (+ index readedbytes)
                     (assoc result fieldname readeddata)
                     (rest pairs)))
            [(- index pos) result])))

      IWritableSpec
      (write [_ buff pos data']
        (let [written (reduce (fn [index [fieldname type]]
                                (let [value (get data' fieldname nil)
                                      written (write type buff index value)]
                                  (+ index written)))
                              pos data)]
          (- written pos))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Types implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn int32
  "Create a int32 indexed data type."
  ([] (int32 0))
  ([default]
   (reify
     ISpecType
     (tag [_] :static)

     IReadableSpec
     (read [_ buff pos]
       [(buffer/read-int buff pos)
        (Integer/BYTES)])

     IWritableSpec
     (write [_ buff pos value]
       (let [value (or value default)]
         (buffer/write-int buff pos value)
         (Integer/BYTES)))

    IStaticSize
    (size [_]
      (Integer/BYTES)))))

(defn int64
  "Create a int64 indexed data type."
  ([] (int64 0))
  ([default]
   (reify
     ISpecType
     (tag [_] :static)

     IReadableSpec
     (read [_ buff pos]
       [(buffer/read-long buff pos)
        (Long/BYTES)])

     IWritableSpec
     (write [_ buff pos value]
       (let [value (or value default)]
         (buffer/write-long buff pos value)
         (Long/BYTES)))

     IStaticSize
     (size [_]
       (Long/BYTES)))))

;; (defn string
;;   ([^long size]
;;    (reify
;;      IType
;;      (tag [_] :static)

;;      IReadableType
;;      (read [_ buff pos]
;;        (let [tmpbuf (byte-array size)]
;;          (.get buff tmpbuf)
;;          (println 1111 buff)
;;          (.position buff (- (.position buff) size))
;;          (println 2222 buff)
;;          [(String. tmpbuf "UTF-8") size]))

;;      IWritableType
;;      (write [_ buff pos value]
;;        (let [input (.getBytes value "UTF-8")
;;              length (count input)
;;              tmpbuf (byte-array size)]
;;          (System/arraycopy input 0 tmpbuf 0 length)
;;          (when (< length size)
;;            (Arrays/fill tmpbuf length size (byte 0)))

;;          (println 444 (vec input))
;;          (println 555 (vec tmpbuf))
;;          (.put buff tmpbuf)
;;          (println 666 buff)
;;          (.position buff (- (.position buff) size))
;;          (println 777 buff)

;;          size))

;;      ITypeSize
;;      (size [_] size)
;;      (size [_ _] size))))
