(ns tailrecursion.boot-hoplon.impl
  (:require
    [boot.util                              :as util]
    [clojure.pprint                         :as pp]
    [clojure.java.io                        :as io]
    [clojure.string                         :as string]
    [clojure.java.shell                     :as sh]
    [tailrecursion.boot-hoplon.compiler :as hl]
    [tailrecursion.boot-hoplon.tagsoup  :as ts]))

(defn prerender [engine output-dir render-js-path html-files]
  (let [win?      (#{"Windows_NT"} (System/getenv "OS"))
        phantom?  (= 0 (:exit (sh/sh (if win? "where" "which") engine)))
        phantom!  #(let [{:keys [exit out err]} (sh/sh engine %1 %2)
                         warn? (and (zero? exit) (not (empty? err)))]
                    (when warn? (println (string/trimr err)))
                    (if (= 0 exit) out (throw (Exception. err))))
        doing-it  (delay (util/info "Prerendering HTML files...\n"))
        not-found #(util/info "Skipping prerender: %s not found on path.\n" engine)]
    (if (not phantom?)
      (do (not-found) identity)
      (doseq [[out-path in-path] html-files]
        @doing-it
        (let [->frms #(-> % ts/parse-page ts/pedanticize)
              forms1 (-> in-path slurp ->frms)
              forms2 (-> (phantom! render-js-path in-path) ->frms)
              [_ att1 [_ hatt1 & head1] [_ batt1 & body1]] forms1
              [html* att2 [head* hatt2 & head2] [body* batt2 & body2]] forms2
              script? (comp (partial = 'script) first)
              rm-scripts #(remove script? %)
              att (merge att1 att2)
              hatt (merge hatt1 hatt2)
              head (concat head1 (rm-scripts head2))
              batt (merge batt1 batt2)
              body (concat (rm-scripts body2) body1)
              merged `(~'html ~att (~'head ~hatt ~@head) (~'body ~batt ~@body))]
          (util/info "• %s\n" out-path)
          (spit (io/file output-dir out-path) (ts/print-page "html" merged)))))))

(defn hoplon [cljs-dir html-dir hl-files opts]
  (util/info "Compiling Hoplon pages...\n")
  (doseq [[out-path in-path] hl-files]
    (util/info "• %s\n" out-path)
    (hl/compile-file (io/file in-path) (io/file cljs-dir) (io/file html-dir) :opts opts)))

(defn html2cljs [file]
  (->> file slurp hl/as-forms
       (#(with-out-str (pp/write % :dispatch pp/code-dispatch)))
       clojure.string/trim
       (#(subs % 1 (dec (count %))))))
