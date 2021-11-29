import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.16.0"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-28" % "5.16.0"  % Test,
    "org.pegdown"       % "pegdown"                 % "1.6.0"   % Test,
    "com.typesafe.play" %% "play-test"              % current   % Test,
    "org.mockito"       %% "mockito-scala"          % "1.16.37" % Test
  )

}
