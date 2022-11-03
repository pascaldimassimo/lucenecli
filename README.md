## Introduction

lucenecli is a command line tool to explore Lucene indexes. It can read Lucene versions 5, 6 and 7 indexes.

Here are some of the available commands:
 - list all fields
 - list all documents
 - basic search functionality
 - show term vectors
 - show facets
 - and more...
 
See the documentation for more details and examples.

## Installation

First, you need to have Java 8 and [sbt installed](http://www.scala-sbt.org/1.0/docs/Setup.html). Under Mac OS X, you can use [brew](http://brew.sh/) to install sbt.

Then, you need to clone this repository and build the jar by using this command from within the project's folder:

    sbt assembly

Once it is done, you can start lucenecli with this command:

    java -jar target/scala-2.11/lucenecli-assembly-1.1.0-SNAPSHOT.jar

## Commands

### terms

The ```terms``` command allow to list terms matching a prefix. To provide that prefix, it is needed to use an options block:

    terms body _omv {"prefix":"a"}
