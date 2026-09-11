# cloud-itonami-isco-9612

Open Occupation Blueprint for **ISCO-08 9612**: Refuse Sorters.

This repository designs a forkable OSS business for a refuse-sorting-
facility scheduling and logistics coordination practice: a facility
scheduling and supply-coordination robot manages crew/task records under a
governor-gated actor, so a recycling/sorting-facility crew keeps its own
operating records instead of renting a closed workforce-management SaaS.

**Maturity: `:implemented`.** `src/refusesorter/` implements the
`RefuseSorterActor` as a `langgraph.graph/state-graph`
(`refusesorter.actor`) wired to a `Refuse Sorter Advisor`
(`refusesorter.advisor`) and an independent `RefuseSorterGovernor`
(`refusesorter.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt) +->
:hold (:hard?)`. 25 tests / 55 assertions green (`kbb -M:test`). HARD
invariants (always hold, never overridable): worker
provenance, facility provenance, no-actuation (`:effect` must be
`:propose`), a closed op-allowlist (`:log-work-record`,
`:schedule-crew-operation`, `:flag-safety-concern`,
`:coordinate-supply-order` — nothing else may ever be proposed), and a
permanent, unconditional block on any proposal that would directly
finalize a sorting-operation-execution decision *or* a
facility-safety-clearance decision (e.g. declaring a facility cleared for
safety), or that would override a facility safety supervisor's judgment.
Always-escalate paths (human sign-off regardless of confidence, mapping
this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)):
`:flag-safety-concern` (always) and `:coordinate-supply-order` above the
registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot
performs the physical domain work**. Here a facility scheduling/logistics
coordination robot performs crew scheduling, sorting-log/throughput/
progress-record logging and protective-equipment/consumables procurement
coordination for a refuse-sorting-facility crew, under an actor that
proposes actions and an independent **RefuseSorterGovernor** that gates
them. The governor never dispatches hardware itself, never sorts waste
materials on the facility floor itself, and never finalizes a
sorting-operation-execution decision or a facility-safety-clearance
decision, and never overrides a facility safety supervisor's judgment;
`:high`/`:safety-critical` actions (such as a flagged hazardous-material-
handling/sorting-line-machinery concern, or an above-threshold supply
order) require human sign-off. **This actor coordinates FACILITY
SCHEDULING/LOGISTICS ONLY — it never sorts waste materials or performs
facility labour work itself and never makes a facility-safety-clearance
decision itself.**

## Core Contract

```text
worker roster + facility registration + safety-reporting policy
        |
        v
Refuse Sorter Advisor -> RefuseSorterGovernor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses,
finalize a sorting-operation-execution decision, finalize a facility-
safety-clearance decision (e.g. declaring a facility cleared for safety),
override a facility safety supervisor's judgment, suppress an operating
record, or disclose sensitive data without governor approval and audit
evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `9612`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
