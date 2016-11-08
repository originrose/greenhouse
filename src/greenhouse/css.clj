(ns greenhouse.css
  (:import [com.helger.css.reader CSSReader]
           [com.helger.commons.charset CCharset]
           [com.helger.css ECSSVersion]
           [com.helger.css.decl CSSExpressionMemberFunction CSSExpressionMemberMath
                                CSSExpressionMemberTermSimple CSSExpressionMemberTermURI
                                ECSSExpressionOperator CSSSelectorAttribute
                                CSSSelectorSimpleMember]
           [com.helger.css.decl.visit CSSVisitor DefaultCSSVisitor]
           [com.helger.css.writer CSSWriterSettings])
  (:require [clojure.java.io :as io]
            [garden.selectors :refer [attr]]
            [camel-snake-kebab.core :refer [->kebab-case]]))

(def libs* (atom {}))

(definterface PGarden
  (toGarden []))

(defn as-css
  [v]
  (.getAsCSSString v (CSSWriterSettings.) 0))

(defmulti parse-selector-member type)

(defmethod parse-selector-member CSSSelectorAttribute
  [m]
  (if-let [op (.getOperator m)]
    (attr (.getAttrName m) (as-css op) (.getAttrValue m))
    (attr (.getAttrName m))))

;(defmethod parse-selector-member CSSSelectorMemberFunctionLike
;  [m])
;(defmethod parse-selector-member CSSSelectorMemberNot
;  [m])

(defmethod parse-selector-member CSSSelectorSimpleMember
  [m]
  (keyword (as-css m)))

;(defmethod parse-selector-member ECSSSelectorCombinator
;  [m])

(defmethod parse-selector-member :default
  [m]
  (as-css m))

(defn parse-selector
  [s]
  (let [members (map parse-selector-member (.getAllMembers s))]
    members))

(defn parse-selectors
  [selectors]
  (vec (apply concat (map parse-selector selectors))))

(defmulti parse-expression-member type)
(defmethod parse-expression-member :default
  [m]
  (as-css m))

;(defmethod parse-expression-member CSSExpressionMemberFunction
;  [m]
;  (as-css m))
;
;(defmethod parse-expression-member CSSExpressionMemberMath
;  [m]
;  (as-css m))
;
;(defmethod parse-expression-member CSSExpressionMemberTermSimple
;  [m]
;  (as-css m))
;
;(defmethod parse-expression-member CSSExpressionMemberTermURI
;  [m]
;  (as-css m))
;
;(defmethod parse-expression-member ECSSExpressionOperator
;  [m]
;  (as-css m))

(defn parse-expression
  "Parses the value side of a declaration.
  e.g.
    color: rgb(200, 100, 213);

  The expression is the rgb(...) portion.)
  "
  [expr]
  (let [members (map parse-expression-member (.getAllMembers expr))]
    (apply str (interpose " " members))))

(defn css->garden-visitor
  []
  (let [state* (atom {:styles [] ; accumulates all of the parsed styles
                      :selectors nil ; holds the current rule's selectors
                      :classes {} ; accumlutes all the single class styles
                      })]
    (proxy [DefaultCSSVisitor PGarden] []
      (onBeginStyleRule [rule]
        (let [selectors (parse-selectors (.getAllSelectors rule))]
          (swap! state* assoc :selectors selectors :declarations {})))

      (onEndStyleRule [rule]
        (swap! state*
               (fn [{:keys [styles selectors declarations classes] :as state}]
                 (let [styles (conj styles (conj selectors declarations))
                       fs (first selectors)
                       classes (if (and (= 1 (count selectors))
                                        (keyword? fs))
                                 (assoc classes fs declarations
                                        (keyword (->kebab-case (subs (name fs) 1)))
                                        declarations)
                                 classes)]
                   (assoc state :styles styles :classes classes)))))

      (onDeclaration [decl]
        (swap! state* assoc-in [:declarations (keyword (.getProperty decl))]
               (parse-expression (.getExpression decl))))

      (toGarden []
        @state*))))

(defn parse-css-dom
  "Parse a CSS string"
  [css-dom]
  (let [visitor (css->garden-visitor)]
    (CSSVisitor/visitCSS css-dom visitor)
    (.toGarden visitor)))

(defn parse-css-str
  "Parse a CSS string"
  [css-str]
  (let [css-dom (CSSReader/readFromString css-str
                                          CCharset/CHARSET_UTF_8_OBJ
                                          ECSSVersion/CSS30)]
    (parse-css-dom css-dom)))

(defn parse-css-file
  [path]
  (let [css-dom (CSSReader/readFromFile (io/file path)
                                        CCharset/CHARSET_UTF_8_OBJ
                                        ECSSVersion/CSS30)]
    (parse-css-dom css-dom)))

(defn import-file
  "Import the classes from a CSS file.
  e.g.
    (import-file \"resources/\""
  [css-ns path]
  (let [{:keys [classes styles]} (parse-css-file path)]
    (swap! libs* assoc (name css-ns) classes)
    :done))

(defn clear-imports
  []
  (reset! libs* {}))

(defn mixin
  [ns-class]
  (if-let [styles (get-in @libs* [(namespace ns-class) (keyword (name ns-class))])]
    (list styles)
    (throw (Exception. (format "\n\tInvalid css mixin: %s" ns-class)))))


