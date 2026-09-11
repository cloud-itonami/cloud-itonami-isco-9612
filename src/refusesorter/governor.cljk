(ns refusesorter.governor
  "RefuseSorterGovernor — the independent safety/scope layer gating every
  facility scheduling/logistics proposal an advisor may make for a
  refuse-sorting-facility crew. The governor never dispatches hardware
  itself, never sorts waste materials at the facility floor itself, and
  never finalizes a sorting-operation-execution decision or a facility-
  safety-clearance decision (e.g. declaring a facility cleared for
  safety), and never overrides a facility safety supervisor's judgment —
  those are permanently out of this actor's scope and remain a facility
  safety supervisor's exclusive judgment (README's 'Robotics premise':
  this actor coordinates FACILITY SCHEDULING/LOGISTICS ONLY — it never
  sorts waste materials or makes facility-safety-clearance decisions
  itself). Modeled closely on cloud-itonami-isco-9212's
  livestockfarm.governor for the itonami actor shape, extended with a
  second, independent hazardous-material-handling hazard-scope dimension
  (refuse sorters manually sort waste materials at recycling/sorting
  facilities, so sharp-object/biohazard/chemical-contamination stakes
  stack on top of the sorting-line-machinery entanglement hazard).

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. worker provenance      — the crew member must be independently
                                 verified/registered before any action.
    2. facility provenance    — the facility must be independently
                                 verified/registered before any action.
    3. no-actuation            — proposal :effect must be :propose (the
                                 governor never dispatches hardware and
                                 never sorts waste materials itself; it
                                 only gates what the advisor may
                                 coordinate).
    4. closed op-allowlist     — only :log-work-record,
                                 :schedule-crew-operation,
                                 :flag-safety-concern and
                                 :coordinate-supply-order may ever be
                                 proposed; anything else is refused.
    5. scope-excluded action   — any proposal to directly finalize a
                                 sorting-operation-execution decision, or
                                 to directly finalize a facility-safety-
                                 clearance decision (e.g. declaring a
                                 facility cleared for safety), or to
                                 override a facility safety supervisor's
                                 judgment, is a hard, permanent block
                                 (checked both against the proposed :op
                                 and, defense-in-depth, against the
                                 proposal's :rationale text — matched as
                                 full finalization/execution ACTION
                                 phrases such as \"finalize the sorting
                                 operation execution decision\" /
                                 \"declare the facility safety cleared\" /
                                 \"override the facility safety
                                 supervisor's judgment\", never as bare
                                 nouns like \"waste\", \"conveyor\" or
                                 \"sharp\", so the check can never
                                 self-trip on the advisor's own routine
                                 rationale text, e.g. \"logged work
                                 record for worker …\" or \"scheduled
                                 crew operation for waste sorting task
                                 …\" or \"…routed for facility safety
                                 supervisor review\" — all three
                                 legitimately contain hazard-domain bare
                                 nouns but none is a finalization action,
                                 and all are exercised by
                                 `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`).
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off
  regardless of confidence):
    6. :op :flag-safety-concern (a hazardous-material-handling /
                                 sorting-line-machinery concern always
                                 escalates to a human, never
                                 auto-commits).
    7. :op :coordinate-supply-order above `supply-cost-threshold`.
    8. low confidence (< `confidence-floor`)."
  (:require [kotoba.lang.text :as str]
            [refusesorter.store :as store]))

(def confidence-floor 0.6)
(def supply-cost-threshold 2000)

(def allowed-ops
  #{:log-work-record :schedule-crew-operation
    :flag-safety-concern :coordinate-supply-order})

;; Defense-in-depth: none of these ops are ever in `allowed-ops` above, so
;; they are already refused by the closed-allowlist check below; they are
;; named again here — as explicit finalization/execution ACTIONS, never
;; bare nouns — so a future allowlist edit cannot silently re-open either
;; of these two independent out-of-scope paths (sorting-operation-
;; execution finalization, facility-safety-clearance finalization),
;; nor the facility-safety-supervisor-judgment-override path, without
;; also touching this list.
(def ^:private scope-excluded-ops
  #{:finalize-sorting-operation-execution :approve-sorting-operation-execution
    :finalize-sorting-execution-decision :approve-sorting-decision
    :declare-facility-safety-cleared :finalize-facility-safety-clearance
    :clear-facility-for-safety
    :override-safety-supervisor-judgment :override-facility-safety-supervisor-judgment})

;; Full finalization/execution ACTION phrases only — never bare nouns
;; ("waste", "sorting", "conveyor", "sharp", "facility", "safety",
;; "facility safety supervisor") — so this can never match inside the
;; mock advisor's own default rationale text (which legitimately
;; contains those bare nouns, e.g. "waste sorting task" / "facility
;; safety supervisor review"). See
;; `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`.
(def ^:private scope-excluded-phrases
  ["finalize the sorting operation execution decision"
   "approve the sorting operation execution"
   "finalize the sorting execution decision"
   "approve the sorting decision"
   "declare the facility safety cleared" "declare the facility cleared for safety"
   "finalize the facility safety clearance" "clear the facility for safety"
   "override the facility safety supervisor's judgment"
   "override the safety supervisor's judgment"
   "override facility safety supervisor judgment"
   "override safety supervisor judgment"])

(defn- contains-excluded-phrase? [s]
  (let [s (str/lower (or s ""))]
    (boolean (some #(str/includes? s %) scope-excluded-phrases))))

(defn- hard-violations [proposal worker-record facility-record]
  (let [{:keys [op rationale]} proposal]
    (cond-> []
      (nil? worker-record)
      (conj {:rule :no-worker
             :detail "未登録 worker への提案は不可（worker record は独立して検証・登録済みでなければならない）"})

      (nil? facility-record)
      (conj {:rule :no-facility
             :detail "未登録 facility への提案は不可（facility record は独立して検証・登録済みでなければならない）"})

      (not= :propose (:effect proposal))
      (conj {:rule :no-actuation
             :detail "effect は :propose のみ許可（governor は現場作業を直接実行しない）"})

      (not (contains? allowed-ops op))
      (conj {:rule :unknown-op
             :detail (str op " は closed op-allowlist に無い — 提案不可")})

      (or (contains? scope-excluded-ops op) (contains-excluded-phrase? rationale))
      (conj {:rule :scope-excluded-action
             :detail "sorting-operation-execution 判断の確定、facility-safety-clearance 判断の確定、facility safety supervisor の判断の上書きは、この actor の権限外 — 常に永続ブロック"}))))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a `store`
  implementing `refusesorter.store/Store`. Pure — never mutates the
  store, never dispatches a facility-floor operation, never finalizes a
  facility-safety-clearance decision."
  [request _context proposal store]
  (let [worker-record (store/worker store (:worker-id request))
        facility-record (some->> (:facility-id proposal) (store/facility store))
        hard (hard-violations proposal worker-record facility-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        supply-order-over-threshold?
        (and (= :coordinate-supply-order (:op proposal))
             (number? (:cost proposal))
             (> (:cost proposal) supply-cost-threshold))
        always-risky? (or (= :flag-safety-concern (:op proposal))
                           supply-order-over-threshold?)]
    {:ok? (and (not hard?) (not low?) (not always-risky?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? always-risky?))}))
