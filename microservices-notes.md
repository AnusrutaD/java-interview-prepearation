# Microservices — Revision Notes (Q&A)

Week 2 prep for the Spring + Microservices mock. One entry per question: **Keywords**, **Model answer**, **Traps**.
Question IDs match `plans/Week2_Microservices_SpringBoot_Plan.pdf` (MS/DS = question bank, TR/REST/DEP = ProgressTracker).

Tags: 🟢 Strong · 🟡 Partial · 🔴 Weak

### TR1 — Have you worked on microservices? — 🟡 Partial (confident, but too generic)

**Keywords:** who you work for (Grid Dynamics → Visa CyberSource) · team (AS Platform, shared services) · ~7 services owned · 2 named services + what they do · sync (HTTP) + async (Kafka) · one result with numbers · hook

**Model answer (~60 sec):**
> "Yes. I'm with Grid Dynamics, working on Visa's **CyberSource payment-gateway platform**, which runs a few hundred microservices. I'm in the **AS Platform team**, the shared-services team, and I own about **seven services end to end**. Two examples: **mks-proxy**, a Vert.x reverse proxy that terminates TLS and routes requests using the service registry; and **config-loader-utility**, which loads compliance configuration from **Consul**, certificates from **Vault**, and validates incoming requests. Services talk **synchronously over HTTP** on the request path and **asynchronously over Kafka** for data sync. The piece I'm proudest of is a **Kafka-based sync pipeline with a two-tier Hazelcast cache across 50 instances**, which brought the transaction failure rate from **60% to under 5% at 15,000+ RPS**. Happy to go deeper into any of these."

**Structure:** context (company / platform / team) → what you own (named services) → how they communicate → one measurable win → stop.

**Traps:** say "with Grid Dynamics, on the Visa platform", not "in the Visa engineering team"; the interviewer may check. "Handling inter-service communication and architecture" is vague; name the services and the tech. Always end on a number, then stop talking.
