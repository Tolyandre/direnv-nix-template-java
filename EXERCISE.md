# Coding Exercise: Rate Limiter

A practice exercise modelled on a Revolut-style live-coding round: build a small
service from scratch, staged, with **code quality**, **testing**, and **concurrency**
as the primary things being judged. Keep each stage simple so you get through as
many stages as possible.

> **Do the interview thing first.** Spend the first few minutes *clarifying
> requirements* (see the questions under each stage) before you type. In the real
> interview, saying these out loud is part of the score.

---

## The problem

Build an in-memory **`RateLimiter`** that decides whether a request should be
allowed or rejected:

```java
boolean tryAcquire(String clientId);
```

`true` = admitted (budget consumed), `false` = rejected (over the limit).

Given to you (don't rewrite unless you want to):
- `RateLimiter` — the interface / contract.
- `TimeSource` + `SystemTimeSource` — injectable clock (nanoseconds, monotonic).
- `ManualTimeSource` (test) — a clock you advance by hand, so tests are instant
  and deterministic instead of using `Thread.sleep`.
- `FixedWindowRateLimiter` — an empty stub that throws; your starting point.
- `FixedWindowRateLimiterTest` — an empty test class with TODOs.

---

## Stages

Work strictly test-first. Only start the next stage once the current one is green
and you've done a quick refactor pass.

### Stage 1 — Fixed window, single client
Allow at most `maxRequests` within a rolling `window` (e.g. 100 requests / 1 second).
- The `(maxRequests + 1)`-th request inside the window is rejected.
- Once the window elapses, the budget resets and requests are allowed again.
- **Clarify:** What time unit / window granularity? What happens exactly at the
  boundary? Should config be validated (reject `maxRequests <= 0`, null window)?

### Stage 2 — Per-client isolation
Each `clientId` has its own independent budget. One noisy client must not consume
another client's allowance.
- **Clarify:** Roughly how many distinct clients? (Affects data-structure choice.)
  Same limit for everyone, or per-client config?

### Stage 3 — Thread safety (correctness under concurrency)
`tryAcquire` is called from many threads at once. The limiter must **never
over-admit**: across all threads, no more than `maxRequests` may be admitted in a
window for a given client. No lost updates, no race between "read count" and
"increment".
- Write a concurrency test: N threads hammer one client; assert admitted count is
  exactly the limit. Use a `CountDownLatch` to release all threads at once.
- **Clarify:** Is a small amount of over/under-admission acceptable, or must it be
  exact? (Drives lock vs. CAS design.)

### Stage 4 — High performance under contention
A single global lock would serialise every client. Reduce contention:
- Per-key state in a `ConcurrentHashMap`, updated with atomics / CAS, or lock
  striping — so different clients don't block each other.
- Be able to explain the throughput trade-off you chose.
- **Clarify:** Read-heavy or write-heavy? Latency target? Single node only?

### Stage 5 — Sliding window accuracy + memory hygiene *(stretch / discussion)*
Fixed windows allow a burst of up to `2 × maxRequests` straddling a boundary.
Improve accuracy with a **sliding window** (log or weighted counter), and make sure
idle clients' state is eventually **evicted** so memory doesn't grow unbounded.
- **Clarify:** Is boundary bursting acceptable? What's the memory budget / client
  cardinality? TTL for idle clients?

---

## Non-functional bar (what "production-ready" means here)
- **Tests**: fast, deterministic (no real sleeping — use `ManualTimeSource`),
  one behaviour per test, clear names. Include a concurrency test from Stage 3 on.
- **Concurrency**: correct first, then fast. Be explicit about your happens-before
  reasoning (locks / atomics / `volatile`).
- **Quality**: small methods, immutable where possible, validated inputs, no dead
  code, meaningful names.

## Discussion prompts (for the wrap-up conversation)
- Fixed vs. sliding window vs. token bucket vs. leaky bucket — trade-offs.
- How would this work **distributed** (multiple app instances)? (Redis, shared
  counters, sync cost, clock skew.)
- What do you return to the caller in real life beyond a boolean? (`Retry-After`,
  remaining quota, HTTP 429.)
- How do you evict stale clients without a background thread stalling requests?
- How would you test it under real load / find the throughput ceiling?

---

## TDD loop & how to run
1. Write ONE failing test (**RED**).
2. Simplest code to pass (**GREEN**).
3. Refactor (tests stay green).
4. Repeat.

```sh
mvn test                     # run everything
mvn -Dtest=FixedWindowRateLimiterTest test   # run just one test class
```

**Suggested time budget (~90 min mock):** S1 ~20m · S2 ~10m · S3 ~25m · S4 ~20m ·
S5/discussion ~15m. If a stage runs long, lock in what works and move on — completing
stages simply beats a half-finished clever solution.

When you've made progress, ask me to review: I'll check correctness, thread-safety,
test quality, and how an interviewer would likely push back.
