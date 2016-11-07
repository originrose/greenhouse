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
            [garden.selectors :refer [attr]]))

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
  (let [state* (atom {:styles []})]
    (proxy [DefaultCSSVisitor PGarden] []
      (onBeginStyleRule [rule]
        (let [selectors (parse-selectors (.getAllSelectors rule))]
          (swap! state* assoc :rule selectors :declarations {})))

      (onEndStyleRule [rule]
        (swap! state* (fn [{:keys [styles rule declarations] :as state}]
                        (assoc state :styles (conj styles (conj rule declarations))))))

      (onDeclaration [decl]
        (swap! state* assoc-in [:declarations (keyword (.getProperty decl))]
               (parse-expression (.getExpression decl))))

      (toGarden []
        (:styles @state*)))))

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


