name := "tramodana"

scalaVersion := "2.12.4"

version := "0.1.0"


libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.4" % "test")

lazy val root = project.in(file(".")).aggregate(module1, module2)

lazy val module1 = project.in(file("module1"))
  .settings(
libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.4" % "test")
  ).dependsOn(module2)

lazy val module2 = project.in(file("module2"))
  .settings(
    libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "3.0.4" % "test")
  )