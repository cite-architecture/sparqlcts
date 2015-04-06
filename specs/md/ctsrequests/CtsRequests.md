
# CTS Requests `sparqlcts` #

## GetValidReff

`GetValidReff` returns valid citations for a given URN.

The URN <strong concordion:set="#point">urn:cts:greekLit:tlg0085.tlg003.fuSyn:</strong> points to an edition of Aeschylus' *Prometheus Bound* cited at <strong concordion:set="#levelStr">1</strong> level of citation, with <strong concordion:assertEquals="countCitations(#point,#levelStr)">1097</strong> cited lines.

The URN <strong concordion:set="#point">urn:cts:greekLit:tlg0085.tlg003.fuSyn:1-10</strong> specifies a range of citations in an edition of Aeschylus' *Prometheus Bound*, cited at <strong concordion:set="#levelStr">1</strong> level of citation; this range includes <strong concordion:assertEquals="countCitations(#point,#levelStr)">10</strong> cited lines.

The URN <strong concordion:set="#point">urn:cts:greekLit:tlg0012.tlg001.fuPers:1</strong> specifies a book of the *Iliad*, cited at <strong concordion:set="#levelStr">2</strong> levels of citation; this book includes <strong concordion:assertEquals="countCitations(#point,#levelStr)">611</strong> cited lines. If we request `GetValidReff` with a `level` parameter of <strong concordion:set="#levelStr">1</strong>, we would get only  <strong concordion:assertEquals="countCitations(#point,#levelStr)">1</strong> citation.

The exemplar-level URN  <strong concordion:set="#point">urn:cts:greekLit:tlg0011.tlg004.fu02.tok:1-2</strong> points to a text cited by line and syntactic token.  If we request `GetValidReff` with a `level` parameter of <strong concordion:set="#levelStr">1</strong>, we would get only  <strong concordion:assertEquals="countCitations(#point,#levelStr)">2</strong> citations.

 If we request `GetValidReff` with a `level` parameter of <strong concordion:set="#levelStr">2</strong>, we would get <strong concordion:assertEquals="countCitations(#point,#levelStr)">15</strong> citations.




