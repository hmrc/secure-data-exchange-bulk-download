import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-26" % "5.10.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc"            %% "hmrctest"           % "3.9.0-play-26" % scope,
    "org.scalatest"          %% "scalatest"          % "3.0.8"         % scope,
    "org.pegdown"            % "pegdown"             % "1.6.0"         % scope,
    "com.typesafe.play"      %% "play-test"          % current         % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3"         % scope,
    "org.mockito"            %% "mockito-scala"      % "1.16.37"       % scope,
    "com.github.tomakehurst" % "wiremock-jre8"       % "2.27.1"        % scope
  )

}
