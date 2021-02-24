# lambdaisland/data-printers

<!-- badges -->
[![CircleCI](https://circleci.com/gh/lambdaisland/data-printers.svg?style=svg)](https://circleci.com/gh/lambdaisland/data-printers) [![cljdoc badge](https://cljdoc.org/badge/lambdaisland/data-printers)](https://cljdoc.org/d/lambdaisland/data-printers) [![Clojars Project](https://img.shields.io/clojars/v/lambdaisland/data-printers.svg)](https://clojars.org/lambdaisland/data-printers)
<!-- /badges -->

Quickly define print handlers for tagged literals across print/pprint implementations.

<!-- opencollective -->

&nbsp;

<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

## Support Lambda Island Open Source

data-printers is part of a growing collection of quality Clojure libraries and
tools released on the Lambda Island label. If you are using this project
commercially then you are expected to pay it forward by
[becoming a backer on Open Collective](http://opencollective.com/lambda-island#section-contribute),
so that we may continue to enjoy a thriving Clojure ecosystem.

&nbsp;

&nbsp;

<!-- /opencollective -->

## Features

One stop shop registration of print handlers

- print-method
- print-dup
- clojure.pprint

<!-- installation -->
## Installation

deps.edn

```
lambdaisland/data-printers {:mvn/version "0.0.0"}
```

project.clj

```
[lambdaisland/data-printers "0.0.0"]
```
<!-- /installation -->

## Rationale

With Clojure/ClojureScript we get a serialization format for free, out of the
box. [EDN](https://github.com/edn-format/edn) or Extensible Data Notation is a
subset of the Clojure syntax format, which can represent all of Clojure's
built-in primitives and collections.

In LISP parlance serializing data is called "printing", and deserializing is
referred to as "reading".

``` clojure
(pr-str {:x 1 :y 2})
;;=> "{:x 1 :y 2}"

(read-string "{:x 1 :y 2}")
;;=> {:x 1 :y 2}
```

Types that are not native to Clojure however will throw a spanner in the works.
They will print in a way that is noisy and opaque, and what is worse: this
printed version is no longer valid EDN. It will file to be read back.

``` clojure
(deftype CustomType [x])

(pr-str (->CustomType 1))
;; => "#object[my.ns.CustomType 0x49763a36 \"my.ns.CustomType@49763a36\"]"

(read-string "#object[my.ns.CustomType 0x49763a36 \"my.ns.CustomType@49763a36\"]")
;; java.lang.RuntimeException
;; No reader function for tag object
```

To solve this EDN provides tagged literals, for instance the value
`(->CustomType 1)` could be represented as `#my.ns/CustomType {:x 1}`. Now this
is valid EDN, and you get to see what you are dealing with. For a REPL-based
workflow, or when wading through test results being able to see the actual
values is invaluable. (pun intended) To make this work you need to teach both
the reader and the printer about the tag and the type.

Dealing with the reader is relatively straightforward, you do this by supplying
a `data_readers.cljc`, or by binding `*data-readers*`, see the [Tagged
Literals](https://clojure.org/reference/reader#tagged_literals) section in the
official Clojure docs.

When it comes to printing however the situation is more complex. There are
multiple printer implementations, and significant differences between Clojure
and ClojureScript. This is where this library comes in. It aims to make it easy
to define print handlers, and to have them set up across implementations, so you
always get consistent results.

Clojure and ClojureScript contain two built-in printers, the one that powers all
the `pr` functions (`pr`, `prn`, `pr-str`, etc), and `clojure.pprint`. On the
Clojure side you need to extend multimethods, for ClojureScript you implement a
protocol. We pave over these differences by providing a number of functions all
with the same signature.

``` clojure
(register-print CustomType 'my.ns/CustomType (fn [obj] {:x (.-x obj)}))
```

This takes the type (`java.lang.Class` or JavaScript constructor function), a
tag as a symbol, and a function returning a plain EDN representation of
instances of the type.

Available functions:

```
lambdaisland.data-printers/register-print
lambdaisland.data-printers/register-pprint
```

## Usage

<!-- contributing -->
## Contributing

Everyone has a right to submit patches to data-printers, and thus become a contributor.

Contributors MUST

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem. Start by stating the problem, then supply a minimal solution. `*`
- agree to license their contributions as MPL 2.0.
- not break the contract with downstream consumers. `**`
- not break the tests.

Contributors SHOULD

- update the CHANGELOG and README.
- add tests for new functionality.

If you submit a pull request that adheres to these rules, then it will almost
certainly be merged immediately. However some things may require more
consideration. If you add new dependencies, or significantly increase the API
surface, then we need to decide if these changes are in line with the project's
goals. In this case you can start by [writing a pitch](https://nextjournal.com/lambdaisland/pitch-template),
and collecting feedback on it.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves, then supply a minimal solution.

`**` As long as this project has not seen a public release (i.e. is not on Clojars)
we may still consider making breaking changes, if there is consensus that the
changes are justified.
<!-- /contributing -->

<!-- license -->
## License

Copyright &copy; 2021 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
<!-- /license -->
