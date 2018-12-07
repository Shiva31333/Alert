name := "forTesting"

version := "0.1"

scalaVersion := "2.12.8"




import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

resolvers += Resolver.sonatypeRepo("public")

assemblyMergeStrategy in assembly := {
  case PathList("reference.conf")     => MergeStrategy.concat
  case PathList("META-INF", xs @ _ *) => MergeStrategy.discard
  case _                              => MergeStrategy.first
}
assemblyJarName in assembly := "Alert_Summary_Report.jar"

libraryDependencies ++= Seq(
  "com.typesafe"                    % "config"                        % "1.2.0",
  "commons-io"                      % "commons-io"                    % "2.3",
  "org.slf4j"                       % "slf4j-log4j12"                 % "1.7.25",
  "org.scala-lang.modules"          %% "scala-parser-combinators"     % "1.0.4",
  "mysql"                           % "mysql-connector-java"          % "8.0.13",
  "org.quartz-scheduler"            % "quartz"                        % "2.3.0",
  "com.amazonaws"                   % "aws-lambda-java-events"        % "2.2.4",
  "com.amazonaws"                   % "aws-lambda-java-core"          % "1.2.0",
  "com.fasterxml.jackson.module"    %% "jackson-module-scala"         % "2.9.0.pr1",
  "org.scalatest"                   %% "scalatest"                    % "3.0.1"         % "test",
  "com.amazonaws"                   % "aws-lambda-java-log4j"         % "1.0.0"

)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings")