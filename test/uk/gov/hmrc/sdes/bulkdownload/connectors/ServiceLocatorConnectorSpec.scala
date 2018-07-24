/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.sdes.bulkdownload.config.SdesServicesConfig
import uk.gov.hmrc.sdes.bulkdownload.domain.Registration

import scala.concurrent.Future

class ServiceLocatorConnectorSpec extends UnitSpec with MockitoSugar {

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val serviceLocatorException = new RuntimeException

    val mockServicesConfig: SdesServicesConfig = mock[SdesServicesConfig]
    val mockHttp: HttpClient = mock[HttpClient]

    val connector = new ServiceLocatorConnector(mockServicesConfig, mockHttp)

    val serviceLocatorUrl = "https://SERVICE_LOCATOR"
    val appName = "api-microservice"
    val appUrl = "http://example.com"
    val registration = Registration(serviceName = appName, serviceUrl = appUrl, metadata = Some(Map("third-party-api" -> "true")))

    when(mockServicesConfig.getString("appName")).thenReturn(appName)
    when(mockServicesConfig.getString("appUrl")).thenReturn(appUrl)
    when(mockServicesConfig.baseUrl("service-locator")).thenReturn(serviceLocatorUrl)

    def stubServiceLocatorResponse(response: Future[HttpResponse]): Unit =
      when(mockHttp.POST(any[String](), any[Registration](), any[Seq[(String, String)]]())(any[Writes[Registration]](),
        any[HttpReads[HttpResponse]](), any(), any())).thenReturn(response)

    def verifyRegistrationCalled(): Unit =
      verify(mockHttp).POST(Matchers.eq(s"$serviceLocatorUrl/registration"), Matchers.eq(registration),
        Matchers.eq(Seq("Content-Type" -> "application/json")))(any[Writes[Registration]](),
        any[HttpReads[HttpResponse]](), any(), any())
  }

  "register" should {
    "register the JSON API Definition into the Service Locator" in new Setup {
      stubServiceLocatorResponse(Future.successful(HttpResponse(200)))

      await(connector.register) shouldBe true

      verifyRegistrationCalled()
    }


    "fail registering in service locator" in new Setup {
      stubServiceLocatorResponse(Future.failed(serviceLocatorException))

      await(connector.register) shouldBe false

      verifyRegistrationCalled()
    }

  }
}
