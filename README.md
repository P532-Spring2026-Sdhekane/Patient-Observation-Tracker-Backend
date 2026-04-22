# Patient Observation Tracker

### CSCI-P532 Object Oriented Software Development — Project 3 (Week 1)

**Live URL:** https://patient-observation-tracker-backend.onrender.com

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

## Running Tests

```bash
mvn test
```

Unit tests: 19 tests across `ObservationFactoryTest`, `SimpleConjunctiveStrategyTest`, `CommandTest`, `ObserverListenerTest`. No `@SpringBootTest` used.
