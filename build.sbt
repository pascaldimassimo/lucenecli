// This file is subject to the terms and conditions defined in
// files 'LICENSE.txt' and 'NOTICE.txt', which is part of this
// source code package.

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.8"

val luceneVersion = "6.1.0"
val jacksonVersion = "2.7.4"

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11" % "test",
	"org.scalatest" % "scalatest_2.11" % "2.2.6" % "test",

	"org.apache.lucene" % "lucene-core" % luceneVersion,
	"org.apache.lucene" % "lucene-analyzers-common" % luceneVersion,
	"org.apache.lucene" % "lucene-queryparser" % luceneVersion,
	"org.apache.lucene" % "lucene-codecs" % luceneVersion,
	"org.apache.lucene" % "lucene-facet" % luceneVersion,
	"org.apache.lucene" % "lucene-grouping" % luceneVersion,
	"org.apache.lucene" % "lucene-backward-codecs" % luceneVersion,
	"org.apache.lucene" % "lucene-misc" % luceneVersion,

	"jline" % "jline" % "2.14.2",

	"com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
	"com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
	"com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
	"com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % jacksonVersion
)

mainClass in assembly := Some("lucenecli.CliApp")