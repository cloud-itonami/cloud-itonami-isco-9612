# Contributing

`cloud-itonami-isco-9612` accepts contributions to the OSS actor, policy tests,
documentation, examples and open occupation blueprint.

## Development

```bash
clojure -M:test
```

Keep changes small and include tests for policy, audit, store or disclosure
behavior.

## Rules

- Do not commit real worker, facility or operator data, credentials or operating
  documents.
- Keep production writes and disclosures behind RefuseSorterGovernor.
- Treat this occupation's workflows as high-risk: add tests for permission,
  scope-exclusion, safety-escalation and audit logging.
- Never widen the closed op-allowlist to include a sorting-operation-
  execution op, a facility-safety-clearance op (e.g. one that would declare a
  facility cleared for safety), or a facility-safety-supervisor-override op,
  without a dedicated ADR and explicit human review.
- Document any new business-model or operator assumption in `docs/`.

## Pull Requests

PRs should describe:

- what behavior changed
- which policy invariant is affected
- how it was tested
- whether operator or certification docs need updates
