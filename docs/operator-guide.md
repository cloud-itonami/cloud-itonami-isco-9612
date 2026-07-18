# Operator Guide

## First Deployment

1. Define the operator's facility coverage and crew intake process.
2. Define consent and purpose categories for worker/facility records.
3. Run synthetic operating cases (work-log entry, crew-operation
   scheduling, supply coordination, safety-concern flagging).
4. Enable human-reviewed sign-off for `:high`/`:safety-critical` actions
   (all flagged safety concerns, above-threshold supply orders).
5. Measure operating outcomes and audit coverage.

## Minimum Production Controls

- consent and disclosure log
- safety-critical escalation path (hazardous-material-handling hazard,
  sorting-line-machinery hazard, equipment-condition hazard)
- provenance for all operating records (worker and facility both
  independently registered)
- human review for high-risk cases
- audit export for all gated actions
- a hard, unconditional block on any attempt to route a sorting-
  operation-execution decision, a facility-safety-clearance decision
  (e.g. declaring a facility cleared for safety), or a facility-safety-
  supervisor override decision, through this actor — those decisions
  stay a facility safety supervisor's exclusive authority end to end

## Certification

Certified operators must prove that the governor gates every
safety-critical robot action, that safety-critical risks escalate to
humans, and that no deployment configuration can route a sorting-
operation-execution decision, a facility-safety-clearance decision, or a
facility-safety-supervisor judgment override through this actor.
