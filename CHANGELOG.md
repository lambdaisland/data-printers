# Unreleased

## Added

## Fixed

## Changed

# 0.9.58 (2025-02-06 / 476317b)

## Added

- Also handle cider-puget in `lambdaisland.data-printer.auto`

# 0.8.55 (2025-02-06 / 6559dc3)

## Added

- Add `lambdaisland.data-printer.auto` namespace, which will auto-detect which libraries it is able to extend

# 0.7.47 (2021-11-15 / 7f65723)

## Fixed

- cljs compat in cider-puget namespace

# 0.7.40 (2021-11-15 / a73fc95)

## Changed

- Drop the dependency of `cider-puget` on `lambdaisland.classpath`, rely on the
  lower-level and lighter-weight `clojure.java.classpath` instead.

# 0.6.36 (2021-11-12 / 6c49e0a)

## Added

- `lambdaisland.data-printers.cider-puget`, for dealing with CIDER's inlined
  Puget. Bit of a hack, use with caution.

# 0.5.33 (2021-11-12 / 69d27c6)

## Changed

- Set `*print-readably*` for pprint

# 0.0.22 (2021-02-28 / f763641)

## Fixed

- Remove unnecessary type-name call

# 0.0.19 (2021-02-25 / 68f6e4e)

## Fixed

- Improve ClojureScript handling

# 0.0.11 (2021-02-24 / c94fdcc)

## Changed

- Include optional dependencies in the pom, marked as optional

# 0.0.8 (2021-02-24 / 0aa1553)

## Added

- `lambdaisland.data-printers/register-print`
- `lambdaisland.data-printers/register-pprint`
- `lambdaisland.data-printers.puget/register-puget`
- `lambdaisland.data-printers.deep-diff/register-deep-diff`
- `lambdaisland.data-printers.deep-diff2/register-deep-diff2`
- `lambdaisland.data-printers.transit/register-write-handler`
