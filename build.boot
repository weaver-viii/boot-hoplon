(set-env!
  ;; using the sonatype repo is sometimes useful when testing Clojurescript
  ;; versions that not yet propagated to Clojars
  ;; :repositories #(conj % '["sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"}])
  :dependencies '[[org.clojure/clojure   "1.6.0"     :scope "provided"]
                  [boot/core             "2.0.0-rc9" :scope "provided"]
                  [adzerk/bootlaces      "0.1.10"    :scope "test"]])

(require
  '[clojure.java.io :as io]
  '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.0")

(bootlaces! +version+ :dev-dependencies "tailrecursion/boot_hoplon/pod_deps.edn")

(task-options!
  pom  {:project     'tailrecursion/boot-hoplon
        :version     +version+
        :description "Boot task for the Hoplon web development environment."
        :url         "https://github.com/tailrecursion/boot-hoplon"
        :scm         {:url "https://github.com/tailrecursion/boot-hoplon"}
        :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})
