import sbt._
import Keys._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._

object Common extends Build {

  val ORGANIZATION = "com.gensler"
  val PROJECT_NAME = "scalavro"
  val PROJECT_VERSION = "0.7.0-SNAPSHOT"

  val SCALA_VERSION = "2.11.7"
  val SCALA_XML_VERSION = "1.0.2"

  val SPRAY_JSON_VERSION = "1.3.2"
  val AVRO_VERSION = "1.7.6"
  val SCALATEST_VERSION = "2.2.0"
  val REFLECTIONS_VERSION = "0.9.9-RC1"
  val TYPESAFE_CONFIG_VERSION = "1.0.2"
  val SLF4J_VERSION   = "1.7.12"
  val SCALA_LOGGING_VERSION = "3.1.0"

  val commonSettings =
    net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    scalariformSettings ++
	Seq(
      ScalariformKeys.preferences :=
        FormattingPreferences()
          .setPreference(AlignParameters, false)
          .setPreference(AlignSingleLineCaseStatements, true)
          .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
          .setPreference(CompactControlReadability, true)
          .setPreference(CompactStringConcatenation, false)
          .setPreference(DoubleIndentClassDeclaration, true)
          .setPreference(FormatXml, true)
          .setPreference(IndentLocalDefs, false)
          .setPreference(IndentPackageBlocks, true)
          .setPreference(IndentSpaces, 2)
          .setPreference(IndentWithTabs, false)
          .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
          .setPreference(PreserveDanglingCloseParenthesis, true)
          .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
          .setPreference(PreserveSpaceBeforeArguments, true)
          .setPreference(RewriteArrowSymbols, false)
          .setPreference(SpaceBeforeColon, false)
          .setPreference(SpaceInsideBrackets, false)
          .setPreference(SpaceInsideParentheses, false)
          .setPreference(SpacesWithinPatternBinders, true)
	    )




lazy val root = project.in(file(".")).settings(commonSettings: _*)
.settings(
libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % "2.11.7",
      "org.scalatest" %% "scalatest" % SCALATEST_VERSION % "test"
    )
).dependsOn(util,core).aggregate(util,core)


lazy val util = project.in(file("util")).
settings(commonSettings: _*).settings(
libraryDependencies ++= Seq(
      "org.reflections" % "reflections" % REFLECTIONS_VERSION,
      "org.scala-lang" % "scala-reflect" % "2.11.7",
      "com.typesafe" % "config" % TYPESAFE_CONFIG_VERSION,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5"
    ))


lazy val core = project.in(file("core")).settings(commonSettings: _*).settings(
libraryDependencies ++= Seq(
      "io.spray" %% "spray-json" % SPRAY_JSON_VERSION,
      "org.apache.avro" % "avro" % AVRO_VERSION,
      "com.jayway.jsonpath" % "json-path" % "2.0.0"
    )
).dependsOn(util)

}
