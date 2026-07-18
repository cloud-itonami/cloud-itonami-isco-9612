# Security Policy

This project handles refuse-sorting-facility operating workflows. Treat
vulnerabilities as potentially high impact even when the demo data is
synthetic — this domain's failure modes include physical worker-safety
risk (hazardous-material-handling hazard: sharp-object injury, biohazard
exposure, chemical contamination) and sorting-line-machinery risk
(conveyor entanglement, equipment condition).

## Do Not Disclose Publicly

Report privately before opening public issues for:

- credential exposure
- real worker, facility or operator data exposure
- authorization bypass
- RefuseSorterGovernor bypass
- audit-ledger tampering
- over-disclosure in reports or exports
- unsafe robot action dispatch
- any path that lets a proposal reach a sorting-operation-execution
  decision, a facility-safety-clearance decision (e.g. declaring a
  facility cleared for safety), or a facility-safety-supervisor override
  decision

## Reporting

Use GitHub private vulnerability reporting when available for the repository.
If that is unavailable, contact the repository maintainers through the
cloud-itonami organization before publishing details.

Include:

- affected commit or version
- reproduction steps
- expected and actual behavior
- impact on worker/facility data, policy enforcement or audit logging
- suggested fix, if known

## Production Guidance

- Store secrets outside Git.
- Keep real worker/facility/operator data outside this repository.
- Run policy tests before deployment.
- Export and review audit logs regularly.
- Use least privilege for operators and service accounts.
