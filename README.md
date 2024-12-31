# clerk-playground

This is a [Clerk](https://book.clerk.vision/) notebook which allows you to enter a [Java regular expression](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html) 
and a text. It will then show you if and how the regular expression can be matches against the text.

> It's your own local https://regex101.com/

## Usage

Clone the repo and then run with [`clj`](https://clojure.org/reference/clojure_cli#use_main)

```
clj -M:run-notebook
```

Then point your browser to http://localhost:8888/

## Windows 

Windows users may find [deps.clj](https://github.com/borkdude/deps.clj) useful if they have trouble installing `clj`.

