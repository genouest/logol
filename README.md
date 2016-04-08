
# INTRODUCTION 

## About

*Logol* is a pattern matching grammar language and a set of tools to search a pattern in a sequence (nucleic or proteic)

Website (http://logol.genouest.org) contains documentation, tutorials and examples.

## Setup

Logol program requires following structure:

-- install directory
      |
      -- LogolExec.sh : main execution program
      |
      |----- lib   : contains JAR libraries and log property file
      |
      |----- prolog : contains templates required for prolog generation as well as shells used during program execution
      |
      |----- Doc (optional) : Javadoc and prolog documentation
      |
      |----- tools : contains user defined specific cost function programs and logol scripts

Logol code is distributed under Affero GPL v3+ license. Libraries licenses are
included in Doc directory.

# Build from source

To build logol, one need:
 - Ruby (1.9+) and the gem cassiopee (gem install cassiopee or ruby-cassiopee package)
 - Java JDK (Oracle or OpenJDK)
 - swi-prolog.
 - A compiler (gcc for linux, Visual Studio for Windows)
 
Run:
ant -f build.xml dist_swi for swi-prolog support.

On Windows, all tools must be in the user PATH.


# INSTALLATION

Needs Ruby 1.9+

Debian/RPM/MacOS/Windows packages are available on web site (http://logol.genouest.org).

WARNING: on Windows, installation and work directories MUST NOT contain whitespaces not accents.

The software requires one of Cassiopee /Cassiopee Ruby gem / VMatch for suffix array searches. VMatch is an external software with its own license (http://www.vmatch.de/, S. Kurtz).
Cassiopee and Cassiopee ruby gem are available for other packages.


If software has been manually installed :

Uncompress the package in a directory.
- edit prolog/logol.properties and update dir.result and workingDir. workingDir is a temporary directory used for file creation and result storage.
- in LogolMultiExec.sh, update path for LD_LIBRARY_PATH to reflect path to DRM .so library (for sge only, not needed for local execution)
- if using vmatch and suffix search tool is not in path for user running queries, edit prolog/suffixSearch.rb and set path to tool (vmatch), and suffix.path in prolog.properties


Properties (prolog/logol.properties) can be modified depending on requirements. See documentation for further info or edit the file.

In case of cluster usage:
Edit prolog/mail.tpl to set up the mail template sent at the end of the jobs.

Nota Bene:
Compilation of prolog program if binary is not available: (requires swi-prolog)

Read prolog/SWI-README

# AUTOTEST

Go to install path and execute logolTest.sh without parameter. All tests should pass.

# EXECUTION

Execute LogolExec.sh (1 sequence) or LogolMultiexec.sh (for banks) with appropriate parameters (see shell file)


# Documentation generation

Go in tools directory and run generate-doc.sh. SWI-Prolog and TexLive are required.
PDF docs will be generated in Doc.

For grammar documentation, go in Doc/grammar and run generate.sh. Programs needs antlr-works and tex4ht.
It will produce html document with each grammar rule.

# Windows

Ruby is required and must be in the PATH of the user. Ruby for Windows is available at http://rubyinstaller.org/.

One should use LogolExec.rb and LogolMultiExec.rb instead of LogolExec.sh and LogolMultiExec.sh.

To install Cassiopee gem: gem install cassiopee

DRMAA (cluster) is not supported as well as custom external and cost functions.
VMatch is not (yet) supported (Cassiopee only)
