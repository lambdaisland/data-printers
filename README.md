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
- [Puget](https://github.com/greglook/puget) (Clojure only)
- [deep-diff](https://github.com/lambdaisland/deep-diff) (Clojure only)
- [deep-diff2](https://github.com/lambdaisland/deep-diff2)
- Puget inlined by cider-nrepl

<!-- installation -->
## Installation

deps.edn

```
lambdaisland/data-printers {:mvn/version "0.9.58"}
```

project.clj

```
[lambdaisland/data-printers "0.9.58"]
```
<!-- /installation -->

## Requirements

We test with Clojure 1.10. Clojure 1.9 should still work, assuming you're not
using deep-diff2, which requires Clojure 1.10.

All dependencies are marked as `<optional>true</optional>`, so you need to bring
the ones you plan to use yourself (Puget, deep-diff, transit-clj, transit-cljs).

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
printed version is no longer valid EDN. It will fail to be read back.

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

There are also libraries that implement their own printers, for instance Puget
is a colorizing pretty printer.

Transit is a separate data format, an alternative to EDN. It has a data model
very similar to EDN, but is more compact and faster to parse. It also has the
ability to mark values with tags, so you can reuse your print/read handlers with
Transit.

Currently we have these functions for registering handlers:

``` clojure
lambdaisland.data-printers/register-print
lambdaisland.data-printers/register-pprint
lambdaisland.data-printers.puget/register-puget
lambdaisland.data-printers.deep-diff/register-deep-diff
lambdaisland.data-printers.deep-diff2/register-deep-diff2
lambdaisland.data-printers.transit/register-write-handler
lambdaisland.data-printers.cider-puget/register-cider-puget
```

## Usage

You should create a wrapper in your own project where you call all the ones that
apply to you, depending on your project's dependencies. Here's a full example:

``` clojure
(ns lambdaisland.data-printers.example
  (:require [lambdaisland.data-printers :as dp]
            [lambdaisland.data-printers.deep-diff :as dp-ddiff]
            [lambdaisland.data-printers.deep-diff2 :as dp-ddiff2]
            [lambdaisland.data-printers.transit :as dp-transit]
            [lambdaisland.data-printers.puget :as dp-puget]
            [lambdaisland.data-printers.cider-puget :as dp-cider-puget]))

(defn register-printer [type tag to-edn]
  (dp/register-print type tag to-edn)
  (dp/register-pprint type tag to-edn)
  (dp-puget/register-puget type tag to-edn)
  (dp-ddiff/register-deep-diff type tag to-edn)
  (dp-ddiff2/register-deep-diff2 type tag to-edn)
  (dp-transit/register-write-handler type tag to-edn)
  (dp-cider-puget/register-cider-puget type tag to-edn))
```

This can be a `.cljc` file, the platform-specific handlers are all still
implemented as CLJC for your convenience, even though they may do nothing on a
given platform.

### Readers

This library only provides registration of write/print handlers, since Clojure
already comes with fairly convenient support for custom tagged literal readers,
but some caveats you should be aware of.

Reader functions are declared in a `data_readers.clj`, `data_readers.cljs`, or
`data_readers.cljc` file. This should contain a map from tag (symbol) to
function name. It needs to be on the classpath, it's commonly placed under
`resources`. Clojure will merge all the ones it finds on the classpath.

``` clojure
{my.ns/my-type my.ns/type-reader-fn}
```

When booting Clojure will create the `my.ns` namespace object, and the
`my.ns/type-reader-fn` var object, but it will not actually load the namespace.
The var will initially be undefined/empty, so you have to make sure to
`(:require [my.ns])` before Clojure encounters its first `#my.ns {}` tagged
literal.

Note that this may also confuse code that uses `requiring-resolve`. This will
return the undeclared/empty var, without requiring the namespace!

When only dealing with Clojure your reader function can just return the value it
needs to return and you're done, but when dealing with ClojureScript or
cross-platform code it gets a bit more tricky.

ClojureScript reader functions are still declared in Clojure (which makes sense,
the ClojureScript compiler is written in Clojure, and handles the reading,
compiling, and generating JS). In this case the function should return a
**form**. Think of it as a macro, but defined with `defn` instead of `defmacro`.

``` clojure
(defn my-reader [obj]
  `(->MyType (:x ~obj)))
```

If the same form is valid Clojure and ClojureScript then you are good to go, if
not then this helper can come in handy:

``` clojure
(defmacro platform-case [& {:keys [cljs clj]}]
  `(if (:ns ~'&env) ~cljs ~clj))
```

Used as such:

``` clojure
(defn my-reader [obj]
  (platform-case :clj `(->MyType (:x ~obj))
                 :cljs `(->MyCljsType (:x ~obj))))
```

### Transit

Transit is the odd one out here, since it's not EDN, and because it requires
some extra care and handling.

Transit does not come with a registry of handlers that you can easily add to,
instead you need to pass the handlers when creating a writer.

``` clojure
(require '[cognitect.transit :as transit])

(def writer (transit/writer :json {:handlers @dp-transit/write-handlers}))
```

Since in the case of Transit you will probably also want to read back your
serialized data, we include a macro to turn your `data_readers.cljc` into
transit readers. (currently only `.cljc` is supported)

``` clojure
(def reader (transit/writer :json {:handlers (dp-transit/data-reader-handlers)}))
```

The way this works is it will call your read handler functions, passing in a
symbol. It expects to get a valid form back which gets turned into a transit
read handler, so make sure your data readers are defined as described above, as
macros in disguise.

### Puget

Puget does not have a mechanism for globally registering new printers, it
expects you to pass in any additional handlers at the call site. This is not
always feasible or convenient, so we work around this by altering Puget's map of
default handlers with an `alter-var-root`. This generally works but can be
fragile, YMMV.

`deep-diff` (v1, clj-only) sits on top of Puget, but introduces its own
mechanism for registering global handlers, so
`lambdaisland.data-printers.deep-diff/register-deep-diff` leverages that. But
since this uses Puget under the hood, if you are already using
`lambdaisland.data-printers.puget` then this isn't strictly necessary, deep-diff
should pick up your Puget handlers.

`deep-diff2` (cljc version) uses a forked version of Puget, which required API
alterations to make it ClojureScript compatible, which is why we need to rely on
a fork rather than getting these changes upstream. Since these are different
namespaces from the original Puget deep-diff2 will not pick up your Puget
handlers, so you still need `lambdaisland.data-printers.deep-diff2`.

Finally you can instruct CIDER to use Puget for pretty printing (e.g. when
invoking `cider-pprint-eval-last-sexp`), by setting `cider-print-fn` to `puget`.
In this case CIDER uses its own version of Puget, inlined via MrAnderson. This
means another copy of the Puget namespace, which will look something like
`cider.nrepl.inlined-deps.puget.v1v3v1.puget.printer`. The
`lambdaisland.data-printers.cider-puget` allows you to register handlers with
this version of Puget, it will find this namespace by scanning the classpath,
which is why you need to add `clojure.java.classpath` as a dependency for this
to work.

### BYO (Bring-Your-Own) Dependencies

data-readers does not declare any dependencies explicitly, since it assumes that
you already have project-level dependency declarations for any printers that you
want to register handlers for, and so as not to impose extra dependencies on
people that don't need it.

Some of the dependencies you might want to include:

- org.clojure/clojure
- lambdaisland/deep-diff2
- lambdaisland/deep-diff
- com.cognitect/transit-clj
- com.cognitect/transit-cljs
- mvxcvi/puget
- org.clojure/java.classpath

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
