import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "1.7.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
    "org.scalatest" %% "scalatest" % "3.0.4" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,

    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
    "org.mockito" % "mockito-core" % "1.10.19" % scope,
    "com.github.tomakehurst" % "wiremock" % "2.17.0" % scope
  )

}
