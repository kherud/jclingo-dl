![Clingo Version 5.6.2](https://img.shields.io/badge/clingo-5.6.2-informational)
![Clingo-dl Version 1.4.0](https://img.shields.io/badge/clingo--dl-1.4.0-informational)

# Java Bindings for Clingo-dl: A grounder and solver for solving ASP modulo Difference Constraints

**This non-official repository contains Java bindings for Clingo-dl.**

Clingo-dl is part of the [Potassco] project for *Answer Set Programming* (ASP).
It extends ASP with constraints over difference logic and extends the ASP grounder and solver [clingo].

Please consult the following resources for further information:

- [**Downloading source and binary releases**][download]
- [**Installation and software requirements**][install]
- [Potassco clingo-dl page][home]

This Java binding makes use of [JClingo](https://github.com/kherud/jclingo).

# Installation

First, make sure to install the Clingo shared library (see the [official Clingo repo](https://github.com/potassco/clingo/blob/master/INSTALL.md)).
- linux: libclingo.so (pre-compiled x86-64 provided)
- macos: libclingo.dylib (pre-compiled arm64 provided)
- windows: clingo.dll

Second, install the Clingo-dl shared library (see [official Clingo-dl repo](https://github.com/potassco/clingo-dl/blob/master/INSTALL.md)).
- linux: libclingo-dl.so (pre-compiled x86-64 provided)
- macos: libclingo-dl.dylib (pre-compiled arm64 provided)
- windows: clingo-dl.dll

You can then use this API via Maven:

```
<dependencies>
    <dependency>
        <groupId>org.potassco</groupId>
        <artifactId>clingo.dl</artifactId>
        <version>1.0-des-rc1</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>des-releases-public</id>
        <name>denkbares Public Releases Repository</name>
        <url>https://repo.denkbares.com/releases-public/</url>
    </repository>
</repositories>
```

### Usage
Please have a look at the [demonstration directory (src/test/demo)](src/test/java/org/potassco/clingodl/demo) .

[clingo]: https://potassco.org/clingo/
[Potassco]: https://potassco.org/
[home]: https://potassco.org/labs/clingodl/
[download]: https://github.com/potassco/clingoDL/releases/
[install]: https://github.com/potassco/clingo-dl/blob/master/INSTALL.md
