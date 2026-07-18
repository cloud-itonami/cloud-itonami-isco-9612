(ns refusesorter.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [refusesorter.actor :as actor]
            [refusesorter.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-worker! st {:worker-id "worker-1" :name "Aiko Tanaka"})
    (store/register-facility! st {:facility-id "FACILITY-1" :name "Tanaka Materials Recovery Facility" :max-supply-cost 2000})
    st))

(deftest commits-a-registered-work-log
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:worker-id "worker-1" :op :log-work-record :stake :low
                  :facility-id "FACILITY-1" :task "throughput progress log"}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "worker-1"))))))

(deftest holds-an-unregistered-facility-proposal
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:worker-id "worker-1" :op :log-work-record :stake :low
                  :facility-id "FACILITY-ghost" :task "throughput progress log"}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :hold (:disposition (:state result))))
    (is (empty? (store/records-of st "worker-1")))))

(deftest interrupts-then-approves-safety-concern-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:worker-id "worker-1" :op :flag-safety-concern :stake :low
                  :facility-id "FACILITY-1" :hazard-type :hazardous-material-hazard}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "worker-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (= 1 (count (store/records-of st "worker-1")))))))

(deftest holds-a-scope-excluded-sorting-execution-op-even-at-high-confidence
  (testing "an actor run can never commit a proposal that would finalize a sorting-operation-execution decision, regardless of disposition path"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:worker-id "worker-1" :op :approve-sorting-operation-execution :stake :low
                    :facility-id "FACILITY-1" :task "sorting execution decision"}
          result (actor/run-request! graph request {} "thread-4")]
      (is (= :done (:status result)))
      (is (= :hold (:disposition (:state result))))
      (is (empty? (store/records-of st "worker-1"))))))

(deftest holds-a-scope-excluded-facility-safety-clearance-op-even-at-high-confidence
  (testing "an actor run can never commit a proposal that would finalize a facility-safety-clearance decision (e.g. declaring a facility cleared for safety), regardless of disposition path"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:worker-id "worker-1" :op :declare-facility-safety-cleared :stake :low
                    :facility-id "FACILITY-1" :task "facility safety clearance"}
          result (actor/run-request! graph request {} "thread-5")]
      (is (= :done (:status result)))
      (is (= :hold (:disposition (:state result))))
      (is (empty? (store/records-of st "worker-1"))))))

(deftest holds-a-scope-excluded-supervisor-override-op-even-at-high-confidence
  (testing "an actor run can never commit a proposal that would override a facility safety supervisor's judgment, regardless of disposition path"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:worker-id "worker-1" :op :override-facility-safety-supervisor-judgment :stake :low
                    :facility-id "FACILITY-1" :task "supervisor override"}
          result (actor/run-request! graph request {} "thread-6")]
      (is (= :done (:status result)))
      (is (= :hold (:disposition (:state result))))
      (is (empty? (store/records-of st "worker-1"))))))
