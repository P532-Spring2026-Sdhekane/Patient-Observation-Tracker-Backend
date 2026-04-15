# Patient Observation Tracker
### CSCI-P532 Object Oriented Software Development — Project 3 (Week 1)

![CI](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/ci.yml/badge.svg)

**Live URL:** https://YOUR_APP.onrender.com

---

## Running with Docker

```bash
# Build the image
docker build -t tracker .

# Run with persistent SQLite volume
docker run -p 8080:8080 -v $(pwd)/data:/app/data tracker
```

Then open http://localhost:8080 in your browser.

---

## Design Patterns

### 1. Strategy — `DiagnosisEngine` / `DiagnosisStrategy`
The rule-evaluation algorithm is extracted behind a `DiagnosisStrategy` interface with a single method `evaluate(AssociativeFunction, List<Observation>)`. `DiagnosisEngine` (Engine layer) holds the injected strategy. Week 1 ships `SimpleConjunctiveStrategy`, which fires a rule only when every argument `PhenomenonType` ID has at least one ACTIVE observation for the patient. Swapping to `WeightedScoringStrategy` in Week 2 requires zero changes to the engine or any other class.

### 2. Observer — `ObservationManager` / `ObservationEvent` / Listeners
`ObservationManager` uses Spring's `ApplicationEventPublisher` to publish an `ObservationEvent` (CREATED or REJECTED) whenever an observation changes state. Two `@EventListener` components react independently: **`AuditLogListener`** persists a durable `AuditLogEntry` row to the database, and **`RuleEvaluationListener`** re-runs all active diagnostic rules for the affected patient and logs any new inferences. Adding a third listener in Week 2 (`PropagationListener`) requires no changes to existing code.

### 3. Factory — `ObservationFactory`
All `Measurement` and `CategoryObservation` objects are constructed exclusively through `ObservationFactory`. The factory validates that: (a) the `PhenomenonType` kind matches the observation kind (QUANTITATIVE/QUALITATIVE), (b) the supplied unit is in the `PhenomenonType`'s allowed-units set, and (c) the `Phenomenon` belongs to the correct `PhenomenonType`. Controllers never call `new Measurement(...)` directly. The `ObservationManager` trusts that anything the factory produced is valid and focuses only on orchestration.

### 4. Command — `TrackerCommand` / `CommandLog`
Every state-changing user action (`CreatePatientCommand`, `RecordObservationCommand`, `RejectObservationCommand`) implements the `TrackerCommand` interface with `execute()`, `getCommandType()`, and `getPayloadJson()`. The `CommandLog` component executes each command and immediately persists a `CommandLogEntry` with the JSON payload, timestamp, and acting user (`"staff"` in Week 1). Storing the payload as JSON means the undo path in Week 2 can reconstruct the original request without a schema change.

---

## Architecture (Four-Layer)

| Layer | Stereotype | Classes |
|---|---|---|
| Client | `@RestController` | `PatientController`, `ObservationController`, `CatalogueController`, `LogController` |
| Manager | `@Service` (Manager) | `PatientManager`, `ObservationManager`, `CatalogueManager` |
| Engine | `@Service` (Engine) | `DiagnosisEngine` |
| ResourceAccess | `@Repository` | All `*Repository` interfaces |

---

## Knowledge vs. Operational Level

Following Fowler's *Analysis Patterns* Chapter 2–3 principles:

- **Knowledge level** (rarely changes, created by staff): `PhenomenonType`, `Phenomenon`, `Protocol`, `AssociativeFunction`
- **Operational level** (created per patient interaction): `Patient`, `Measurement`, `CategoryObservation`, `CommandLogEntry`, `AuditLogEntry`

Knowledge-level entities are never created as a side-effect of recording an observation.

---

## Running Tests

```bash
mvn test
```

Unit tests: 19 tests across `ObservationFactoryTest`, `SimpleConjunctiveStrategyTest`, `CommandTest`, `ObserverListenerTest`. No `@SpringBootTest` used.
