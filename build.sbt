name := "ldap-mon"

organization := "de.uni-ulm"

scalaVersion := "2.11.6"


libraryDependencies += "org.apache.directory.api" % "api-ldap-client-api" % "1.0.0-M28"

libraryDependencies += "org.apache.directory.api" % "api-ldap-codec-standalone" % "1.0.0-M28"


libraryDependencies += "io.reactivex" %% "rxscala" % "0.24.0"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.10"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"
