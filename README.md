# Flamingo QA Automation Assignment

The project covers:

- Restful Booker REST API CRUD scenarios
- Hygraph GraphQL positive and negative scenarios
- DemoQA Web Tables UI scenarios with Playwright
- Allure reporting, structured logging, screenshots on UI failures, and GitHub Actions CI

## Tech Stack

- Java 11
- JUnit 5
- REST Assured
- Playwright for Java
- AssertJ
- Jackson
- Allure
- Lombok
- Log4j2 / SLF4J

## Prerequisites

- Java 11+
- Maven 3.8+
- Chromium for Playwright UI tests

Install the Playwright browser locally:

```bash
mvn -DskipTests exec:java@install-playwright-chromium
```

On Linux CI, the workflow installs Chromium with system dependencies:

```bash
mvn -B -DskipTests exec:java@install-playwright-chromium-with-deps
```

## How To Run

Run the full suite:

```bash
mvn clean test
```

Run the same command used in GitHub Actions:

```bash
mvn -B test -Dui.headless=true
```

Run a specific test group:

```bash
mvn test -Dgroups=api
mvn test -Dgroups=graphql
mvn test -Dgroups=ui
```

Run UI tests headed while debugging:

```bash
mvn test -Dgroups=ui -Dui.headless=false
```

Generate and open the Allure report:

```bash
mvn allure:serve
```

Allure results are written to `target/allure-results`.

## Test Coverage

### REST API

Target: `https://restful-booker.herokuapp.com`

Covered scenarios:

- authenticate admin user
- create booking from JSON test data
- create and retrieve booking by id
- create and update booking
- create and delete booking
- reject delete with invalid token

Write tests create their own data and clean it up in `@AfterEach`. Auth token creation is handled inside the API client for methods that require authentication.

### GraphQL

Target: public Hygraph Movie schema.

Covered scenarios:

- query movie list with limit
- query connection with pagination metadata
- query single movie by id
- query using GraphQL variables
- query using fragment and nested publisher fields
- non-existing movie id
- malformed query
- non-existing field validation error

GraphQL requests use typed request/response envelopes and Jackson models instead of raw string assertions.

### UI

Target: `https://demoqa.com/webtables`

Covered scenarios:

- add a new record and find it by search
- edit an existing record
- delete a record
- search/filter table rows
- validate multiple added records

The UI layer uses Page Object Model with a dedicated table component. Playwright locator assertions and auto-waiting are used instead of custom polling waits.

## Project Structure

```text
src/main/java/com/flamingo/qa
  api        Restful Booker client, models, request specs, test data factory
  graphql    GraphQL client, query strings, typed response models
  ui         Playwright session, page objects, table component, UI models
  config     system property / environment / default property config
  core       Jackson, reporting, and logging helpers

src/test/java/com/flamingo/qa
  api        REST API tests
  graphql    GraphQL tests
  ui         Playwright UI tests
  support    JUnit extensions, retry, screenshots, test log buffering
```

## Reporting And Logs

The suite produces:

- Allure results in `target/allure-results`
- Surefire reports in `target/surefire-reports`
- per-test logs in `target/test-logs`
- HTTP request/response attachments for REST and GraphQL calls
- screenshots, current URL, browser console output, and framework logs on UI failure

Logs include timestamp, thread id, level, class/method, and test context so parallel execution remains readable.

## Configuration

Defaults are in `src/test/resources/config/default.properties`.

Configuration can be overridden by system property or environment variable.

Examples:

```bash
mvn test -Dui.headless=false
mvn test -Dgraphql.endpoint=https://example.com/graphql
GRAPHQL_TOKEN=secret mvn test -Dgroups=graphql
```

## CI

GitHub Actions workflow: `.github/workflows/tests.yml`

It runs on push, pull request, or manual dispatch:

1. checks out the repository
2. sets up Java 11
3. installs Playwright Chromium with Linux dependencies
4. runs `mvn -B test -Dui.headless=true`
5. uploads Allure results and Surefire reports as artifacts

## Notes And Tradeoffs

- Tests run in parallel by default. Restful Booker write tests are isolated to one thread because they depend on create/update/delete state in a public API.
- Restful Booker resets public test data periodically, so tests create data at runtime instead of depending on existing records.
- DemoQA ads and sticky overlays can block interactions; the base page removes known blockers after navigation.
- DemoQA Web Tables did not expose a reliable working sorting behavior during implementation, so the UI coverage focuses on add, edit, delete, search, and multi-row validation.
- API and GraphQL HTTP exchanges are logged and attached to Allure, with sensitive values masked.
