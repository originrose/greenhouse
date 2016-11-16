(ns greenhouse.css
  (:import [com.helger.css.reader CSSReader]
           [com.helger.commons.charset CCharset]
           [com.helger.css ECSSVersion]
           [com.helger.css.decl CSSExpressionMemberFunction CSSExpressionMemberMath
                                CSSExpressionMemberTermSimple CSSExpressionMemberTermURI
                                ECSSExpressionOperator CSSSelectorAttribute
                                CSSSelectorSimpleMember CSSMediaQuery]
           [com.helger.css.decl.visit CSSVisitor DefaultCSSVisitor]
           [com.helger.css.writer CSSWriterSettings])
  (:require [clojure.java.io :as io]
            [garden.selectors :refer [attr]]
            [garden.core :as garden]
            [garden.stylesheet :refer [at-media at-keyframes]]
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
    (list 'attr (.getAttrName m) (as-css op) (.getAttrValue m))
    (list 'attr (.getAttrName m))))

(defmethod parse-selector-member CSSSelectorSimpleMember
  [m]
  (keyword (as-css m)))

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

(defn parse-expression
  "Parses the value side of a declaration.
  e.g.
    color: rgb(200, 100, 213);

  The expression is the rgb(...) portion.)
  "
  [expr]
  (let [members (map parse-expression-member (.getAllMembers expr))]
    (apply str (interpose " " members))))

(defn parse-media-query
  [q]
  (let [medium (.getMedium q)
        med-kw (when medium
                     (keyword (.toLowerCase medium)))
        modifier (.getModifier q)
        mod-kw (if (not= modifier com.helger.css.decl.CSSMediaQuery$EModifier/NONE)
                 (keyword (.toLowerCase (.getCSSText modifier)))
                 false)
        media (if medium
                {med-kw (or mod-kw true)}
                {})
        expressions (into media (map (fn [expr]
                                       [(keyword (.toLowerCase (.getFeature expr))) (parse-expression (.getValue expr))])
                                     (.getAllMediaExpressions q)))]
    expressions))

(defn parse-media-rule
  [query-rule]
  (let [queries (.getAllMediaQueries query-rule)]
    (at-media (apply merge (map parse-media-query queries)))))

(defn css->garden-visitor
  []
  (let [state* (atom {:styles [] ; accumulates all of the parsed styles
                      :selectors nil ; holds the current rule's selectors
                      :classes {} ; accumlutes all the single class styles
                      :media-rule nil ; keep the current media query
                      :media-styles nil ; stash styles here when accumulating styles within media rule
                      :keyframe-rule nil ; keep the current keyframe rule
                      :keyframe-blocks []
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

      (onBeginMediaRule [rule]
        (swap! state*
               (fn [{:keys [styles] :as state}]
                 (assoc state
                        :media-rule (parse-media-rule rule)
                        :media-styles styles
                        :styles []))))

      (onEndMediaRule [rule]
        (swap! state*
               (fn [{:keys [media-rule media-styles styles] :as state}]
                 (assoc state
                        :styles (conj media-styles (apply list 'at-media (get-in media-rule [:value :media-queries]) styles))
                        :media-rule nil
                        :media-styles nil))))

      (onBeginKeyframesRule [rule])
      (onEndKeyframesRule [rule]
        (swap! state*
               (fn [{:keys [keyframe-rule keyframe-blocks styles] :as state}]
                 (assoc state
                        :styles (conj styles (apply list 'at-keyframes (keyword (.toLowerCase (.getAnimationName rule)))
                                                    keyframe-blocks))
                        :keyframe-blocks []))))

      (onBeginKeyframesBlock [block])
      (onEndKeyframesBlock [block]
        (swap! state*
               (fn [{:keys [keyframe-blocks declarations] :as state}]
                   (assoc state :keyframe-blocks (conj keyframe-blocks [(keyword (.toLowerCase (first (.getAllKeyframesSelectors block))))
                                                                        declarations])))))

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

(defn css->garden
  [css]
  (:styles (parse-css-str css)))

(defn css-file->garden
  [path]
  (css->garden (slurp path)))

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

