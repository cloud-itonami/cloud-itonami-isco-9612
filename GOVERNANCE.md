# Governance

`cloud-itonami-isco-9612` is an OSS open-occupation blueprint. Governance covers
both code and the operator model.

## Maintainers

Maintainers may merge changes that preserve these invariants:

- the Advisor cannot directly dispatch robot actions or disclose records.
- RefuseSorterGovernor remains independent of the advisor.
- hard policy violations cannot be overridden by human approval.
- a sorting-operation-execution decision, a facility-safety-clearance
  decision (e.g. declaring a facility cleared for safety), and any override
  of a facility safety supervisor's judgment, stay permanently outside this
  actor's op-allowlist.
- every commit, hold and approval path is auditable.
- real worker/facility/operator data stays outside Git.

## Decision Records

Architecture decisions live in `docs/adr/`. Changes to the trust model,
storage contract, public business model, operator certification, license, or
the closed op-allowlist should add or update an ADR.

## Operator Governance

Anyone may fork and operate independently. itonami.cloud certification is a
separate trust mark and should require security, audit, support and data-flow
review.

Certified operators can lose certification for:

- bypassing policy checks
- mishandling worker/facility/operator data
- misrepresenting certification status
- failing to respond to security incidents
- hiding material changes to customer-facing operation
- attempting to route a sorting-operation-execution decision, a facility-
  safety-clearance decision, or a facility-safety-supervisor override
  decision, through this actor
