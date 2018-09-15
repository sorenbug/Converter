organization := "com.olvind.scalablytyped"
name := "with-theme"
version := "0.0-unknown-98699b"
scalaVersion := "2.12.6"
enablePlugins(ScalaJSPlugin)
libraryDependencies ++= Seq(
  "com.olvind" %%% "runtime" % "1.0.0-M1",
  "com.olvind.scalablytyped" %%% "react" % "0.0-unknown-2be2df",
  "com.olvind.scalablytyped" %%% "std" % "0.0-unknown-7aeb49",
  "org.scala-js" %%% "scalajs-dom" % "0.9.5")
publishArtifact in packageDoc := false
scalacOptions += "-P:scalajs:sjsDefinedByDefault"
        