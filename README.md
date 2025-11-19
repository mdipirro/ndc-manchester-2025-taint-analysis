# Trust No Input: Taint Analysis at Compile Time

This repository contains the Scala Play! code shown during the "Trust No Input: Taint Analysis at Compile Time" presentation at NDC [Manchester 2025](https://ndcmanchester.com/agenda/trust-no-input-taint-analysis-at-compile-time-0jxc/00pfthq9cus).

In particular, the repository contains the code of the [Play Scala forms](https://github.com/playframework/play-samples/tree/3.0.x/play-scala-forms-example) example, "decorated" with the `TaintTracked` type from the [scala-secure-types](https://github.com/mdipirro/scala-secure-types) library.

The example was slightly edited to introduce an HTML injection vulnerability. In particular, the `listWidgtes` form uses the `@Html` directive to avoid escaping special characters. Therefore, any HTML code entered as part of the `Name` form field will be rendered without validation.

`scala-secure-types` is imported as an SBT project dependency, as no artifacts were published for it.
