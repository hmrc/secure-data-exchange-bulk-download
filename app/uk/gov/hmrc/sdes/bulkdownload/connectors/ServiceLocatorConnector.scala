/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.sdes.bulkdownload.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.ContentTypes.JSON
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.sdes.bulkdownload.config.SdesServicesConfig

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ServiceLocatorConnector @Inject()(servicesConfig: SdesServicesConfig, http: HttpClient)
                                       (implicit ec: ExecutionContext) {

  protected lazy val appName: String = servicesConfig.getString("appName")
  protected lazy val appUrl: String = servicesConfig.getString("appUrl")
  protected lazy val serviceUrl: String = servicesConfig.baseUrl("service-locator")

  protected lazy val handlerOK: () => Unit = () => Logger.info("Service is registered on the service locator")
  protected lazy val handlerError: Throwable => Unit = e => Logger.error(s"Service could not register on the service locator", e)

  protected lazy val metadata: Option[Map[String, String]] = Some(Map("third-party-api" -> "true"))

  def register(implicit hc: HeaderCarrier): Future[Boolean] = {
    val registration = Registration(appName, appUrl, metadata)
    http.POST(s"$serviceUrl/registration", registration, Seq(CONTENT_TYPE -> JSON)) map {
      _ =>
        handlerOK()
        true
    } recover {
      case e: Throwable =>
        handlerError(e)
        false
    }
  }

}

case class Registration(serviceName: String, serviceUrl: String, metadata: Option[Map[String, String]] = None)

object Registration {
  implicit val format: OFormat[Registration] = Json.format[Registration]
}
