import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt._

object AppDependencies {
  val play                 = "play-28"
  val bootstrapPlayVersion = "7.19.0"

  val compile = Seq(
    "uk.gov.hmrc" %% s"bootstrap-backend-$play" % bootstrapPlayVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test-$play" % bootstrapPlayVersion % Test,
    "org.pegdown"       % "pegdown"                % "1.6.0"              % Test,
    "com.typesafe.play" %% "play-test"             % current              % Test,
    "org.mockito"       %% "mockito-scala"         % "1.16.37"            % Test
  )
}
