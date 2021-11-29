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
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
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
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false,
    Test / parallelExecution := false,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    scoverageSettings
  )
  .settings(scoverageSettings)
  .settings(scalastyleSettings)
  .settings(scalafmtSettings)

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := """"<empty>"; .*(BuildInfo|Routes|Reverse).*""",
  coverageMinimum := 95,
  coverageFailOnMinimum := true,
  coverageHighlighting := true
)

lazy val scalastyleSettings = Seq(
  scalastyleConfig := baseDirectory.value / "scalastyle-config.xml"
)

lazy val scalafmtSettings = Seq(
  Compile / compile := (Compile / compile)
        .dependsOn(Compile / scalafmtSbt)
        .dependsOn(scalafmtAll)
        .value
)
