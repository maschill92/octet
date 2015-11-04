# Changelog #

## Version 0.2.0 ##

Date: 2015-11-04

- Add `into` helper that has similar semantics with clojure into. Given a spec and
  appropriate data it creates the buffer of exact size for the spec and writes the
  data to it.
- Add platform independent way se the buffer capacity.
- Fix cljs compatibility issues in string specs.
- Add support to write and read from a collection of byte buffers.
- Add `repeat` composition type spec.
- Add dynamic `vector*` type spec.
- Add unsigned version of primitives typespecs: uint16, uint32 uint64, ubyte.
- Convert to conditional readers (clojure 1.7 is now the minimum clojure
  version required).
- Add proper externs for enable advanced compilation.


## Version 0.1.0 ##

Date: 2015-03-01

- Initial version