import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt._

object AppDependencies {
  val play                 = "play-30"
  val bootstrapPlayVersion = "8.6.0"

  val compile = Seq(
    "uk.gov.hmrc" %% s"bootstrap-backend-$play" % bootstrapPlayVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test-$play" % bootstrapPlayVersion % Test,
    "org.pegdown"       % "pegdown"                % "1.6.0"              % Test,
    "org.playframework" %% "play-test"             % current              % Test,
    "org.mockito"       %% "mockito-scala"         % "1.17.31"            % Test
  )
}
