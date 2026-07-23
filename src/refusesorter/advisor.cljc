(ns refusesorter.advisor
  "Refuse Sorter Advisor — proposing a refuse-sorting-facility scheduling/
  logistics coordination operation (log a sorting-log/throughput/progress
  record, schedule a crew operation, flag a safety concern, coordinate a
  protective-equipment/consumables procurement order) from a crew roster,
  facility registration and safety-reporting policy. Swappable mock/llm;
  the advisor ONLY proposes — `refusesorter.governor` independently gates
  every proposal and always escalates safety concerns and above-threshold
  supply orders. The advisor never proposes to directly finalize a
  sorting-operation-execution decision, or a facility-safety-clearance
  decision, and never proposes to override a facility safety supervisor's
  judgment — those stay permanently out of this actor's scope. Modeled
  closely on cloud-itonami-isco-9212's livestockfarm.advisor for the
  itonami actor shape, extended with a second, independent hazardous-
  material-handling hazard-scope dimension.

  A proposal: {:op :log-work-record|:schedule-crew-operation|
               :flag-safety-concern|:coordinate-supply-order
               :effect :propose :worker-id str :facility-id str
               :cost number :hazard-type kw :task str :stake kw
               :confidence n :rationale str}"
  (:require #?(:clj [clojure.edn :as edn] :cljs [cljs.reader :as edn])))

(defprotocol Advisor
  (-advise [advisor store request] "request -> proposal map"))

(defn- rationale-for [op worker-id facility-id hazard-type]
  (case op
    :log-work-record
    (str "logged work record for worker " worker-id " at facility " facility-id)

    :schedule-crew-operation
    (str "scheduled crew operation for waste sorting task at facility " facility-id)

    :flag-safety-concern
    (str "flagged " (name (or hazard-type :hazard)) " concern for worker "
         worker-id " at facility " facility-id " — routed for facility safety supervisor review")

    :coordinate-supply-order
    (str "coordinated supply order for worker " worker-id " at facility " facility-id)

    (str "proposed " (name op) " for worker " worker-id " at facility " facility-id)))

(defn- infer [_store {:keys [op stake worker-id facility-id cost hazard-type task]
                       :as request}]
  {:op op
   :effect :propose
   :worker-id worker-id
   :facility-id facility-id
   :cost cost
   :hazard-type hazard-type
   :task task
   :stake (or stake :low)
   :confidence (case (or stake :low) :high 0.7 :medium 0.85 :low 0.95)
   :rationale (rationale-for op worker-id facility-id hazard-type)})

(defn mock-advisor []
  (reify Advisor
    (-advise [_ store request] (infer store request))))

(def ^:private system-prompt
  "You are a refuse-sorting-facility scheduling/logistics coordination
   advisor. Given a request, propose an :op (one of :log-work-record,
   :schedule-crew-operation, :flag-safety-concern,
   :coordinate-supply-order), the :worker-id, :facility-id, and any
   :cost/:hazard-type/:task fields, an honest :confidence and a
   :stake. Never propose an op outside this closed list, and never
   propose to directly finalize a sorting-operation-execution decision,
   or a facility-safety-clearance decision (e.g. declaring a facility
   cleared for safety), or to override a facility safety supervisor's
   judgment — those are always out of this actor's scope; it coordinates
   facility scheduling/logistics only and never sorts waste materials
   itself or makes facility-safety-clearance decisions itself. Safety
   concerns always require human sign-off regardless of confidence.")

(defn- parse-proposal [content]
  (try
    (let [p (edn/read-string content)]
      (if (map? p)
        (assoc p :effect :propose)
        {:op :unknown :effect :propose :confidence 0.0 :stake :high
         :rationale "unparseable LLM response"}))
    (catch #?(:clj Exception :cljs js/Error) _
      {:op :unknown :effect :propose :confidence 0.0 :stake :high
       :rationale "LLM response parse failure"})))

(defn llm-advisor
  [chat-model model-generate-fn gen-opts]
  (reify Advisor
    (-advise [_ _store request]
      (let [msgs [{:role :system :content system-prompt}
                  {:role :user :content (str "operation request: " (pr-str request))}]
            resp (model-generate-fn chat-model msgs gen-opts)]
        (parse-proposal (:content resp))))))
