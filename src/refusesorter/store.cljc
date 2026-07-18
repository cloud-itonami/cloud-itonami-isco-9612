(ns refusesorter.store
  "SSoT for the ISCO-08 9612 refuse-sorting-facility scheduling/logistics
  coordination actor (itonami actor pattern, ADR-2607121000 / CLAUDE.md
  Actors section; README's 'Robotics premise' — a facility scheduling/
  logistics coordination robot manages crew scheduling, sorting-log/
  throughput/progress-record logging and protective-equipment/consumables
  procurement coordination for a refuse-sorting facility crew under this
  advisor/governor pair, which never dispatches hardware itself, never
  sorts waste materials itself, and never finalizes a sorting-operation-
  execution decision or a facility-safety-clearance decision, and never
  overrides a facility safety supervisor's judgment — those remain the
  facility safety supervisor's exclusive judgment). Modeled closely on
  cloud-itonami-isco-9212's livestockfarm.store for the itonami actor
  shape, extended with a second, independent hazardous-material-handling
  hazard-scope dimension (refuse sorters manually sort waste materials at
  recycling/sorting facilities, so sharp-object/biohazard/chemical-
  contamination stakes stack on top of the sorting-line-machinery
  entanglement hazard).

  Domain:

    worker   — a registered refuse-sorting-facility crew member
               (:worker-id, :name)
    facility — a registered recycling/sorting facility {:facility-id
               :name :max-supply-cost number}. `:max-supply-cost` is an
               informational registered ceiling used only to decide
               whether a `:coordinate-supply-order` proposal escalates to
               human sign-off (the governor never blocks a within-
               threshold order outright; it only decides commit vs.
               escalate).
    record   — a committed operating record (a logged sorting-log/
               throughput/progress entry, a scheduled crew operation, a
               flagged safety concern, or a coordinated supply order) —
               written ONLY via commit-record!.
    ledger   — append-only audit trail, commit or hold.")

(defprotocol Store
  (worker [s worker-id])
  (facility [s facility-id])
  (records-of [s worker-id])
  (ledger [s])
  (register-worker! [s worker])
  (register-facility! [s facility])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (worker [_ worker-id] (get-in @a [:workers worker-id]))
  (facility [_ facility-id] (get-in @a [:facilities facility-id]))
  (records-of [_ worker-id] (filter #(= worker-id (:worker-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-worker! [s w]
    (swap! a assoc-in [:workers (:worker-id w)] w) s)
  (register-facility! [s f]
    (swap! a assoc-in [:facilities (:facility-id f)] f) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:workers {} :facilities {} :records [] :ledger []}
                                    seed)))))
