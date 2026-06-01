# CSV to XML Converter

Spring Boot application that polls an input directory for CSV files, converts them to XML, and writes results to an output directory. Uses Spring Integration for the file pipeline and Java 21 virtual threads for async processing.

## Prerequisites

- Java 21
- Maven 3.9+

### Cursor / VS Code Java support

This repo includes [`.vscode/settings.json`](.vscode/settings.json) pointing the Java language server at Homebrew JDK 21. After cloning, run **Java: Clean Java Language Server Workspace** from the Command Palette, then reload the window, so imports resolve correctly.

## Run

```bash
mvn spring-boot:run
```

On startup, `data/input`, `data/output`, and `data/error` are created under the project directory (if missing). Drop `.csv` files into `data/input`; converted `.xml` files appear in `data/output`.

## Test

```bash
mvn test
```

## Configuration

See `src/main/resources/application.yml`. Key properties under `csv-converter`:

| Property | Default | Description |
|----------|---------|-------------|
| `input-directory` | `${user.dir}/data/input` | Polled for new CSV files |
| `output-directory` | `${user.dir}/data/output` | XML output location |
| `error-directory` | `${user.dir}/data/error` | Failed files moved here |
| `poll-interval-ms` | `1000` | Poll interval |
| `max-messages-per-poll` | `1` | Files per poll cycle |

Virtual threads are enabled via `spring.threads.virtual.enabled=true`.

## Data format

Input files use pipe-separated rows (despite the `.csv` extension). The first field is the entity type:

| Type | Row format | Attached to |
|------|------------|-------------|
| Person | `P\|firstname\|lastname` | Starts a new person block |
| Phone | `T\|mobile\|landline` | Current person, or current family after `F`; landline may be empty |
| Address | `A\|street\|city` or `A\|street\|city\|zip` | Current person, or current family after `F`; zip is optional |
| Family | `F\|name\|born` | Current person (year of birth as integer) |

### Row order rules

Rows are processed top to bottom. Each `P` row starts a new person block. Child rows apply to the current person until an `F` row appears; after `F`, `T` and `A` rows apply to that current family until the next `F` or `P`.

- A file must contain at least one `P` row.
- `T`, `A`, and `F` must follow a `P` row (not appear before the first person).
- After `P`, person-level `T`, `A`, and/or `F` rows may appear before the next `P`.
- After `F`, family-level `T` and `A` rows may appear before the next `F` or `P`.
- Each person may have one `T` row.
- Each person may have multiple `A` and `F` rows, and each family may have multiple `A` rows.

Example:

```
P|Ada|Lovelace
T|0701111111|08-111111
A|Person St|London|11111
F|Lovelace|1815
T|0702222222|08-222222
A|Family St|Oxford|22222
F|Byron|1788
P|Grace|Hopper
```

Invalid files are moved to `data/error`.
