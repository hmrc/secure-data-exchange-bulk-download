import TestPhases.oneForkedJvmPerTest
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import org.scalastyle.sbt.ScalastylePlugin._

val appName = "secure-data-exchange-bulk-download"

val silencerVersion = "1.7.0"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    scalaVersion := "2.12.11",
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=views;routes",
    libraryDependencies ++= Seq(
          compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
          "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
        )
    // ***************
  )
  .settings(majorVersion := 0)
  .settings(
    publishingSettings: _*
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false,
    parallelExecution in Test := false,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    scoverageSettings
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )

val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
        "<empty>",
        "Reverse.*",
        ".*(BuildInfo|Routes).*"
      ).mkString(";"),
  coverageMinimum := 95,
  coverageFailOnMinimum := true,
  coverageHighlighting := true
)

scalastyleConfig := baseDirectory.value / "scalastyle-config.xml"

Compile / compile := (Compile / compile)
  .dependsOn(Compile / scalafmtSbt)
  .dependsOn(scalafmtAll)
  .value
