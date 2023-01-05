/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.sdes.bulkdownload.acceptance

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import uk.gov.hmrc.sdes.bulkdownload.utils.TestUtils
import uk.gov.hmrc.sdes.bulkdownload.utils.wiremock.{MockSdesProxyListFilesEndpoint, WireMockRunner}

class BulkDownloadListSpec extends PlaySpec with GuiceOneAppPerSuite with WireMockRunner with MockSdesProxyListFilesEndpoint {

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.service-locator.enabled" -> false,
      "auditing.enabled"                              -> false,
      "metrics.enabled"                               -> false,
      "microservice.services.sdes-list-files.host"    -> TestUtils.host,
      "microservice.services.sdes-list-files.port"    -> TestUtils.port
    )
    .build()

  val headerNameClientId = "X-Client-ID"
  val clientId           = "some_Client_Id"
  val fileType           = "FILE_TYPE"
  val endpoint           = s"/list/$fileType"

  val validRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(Helpers.GET, endpoint).withHeaders(headerNameClientId -> clientId)

  val expectedResponse: JsValue = Json.parse(defaultListFilesResponseBody)

  "Bulk Download List endpoint" when {
    "request is valid" should {
      "call SDES proxy with ClientId header and file type, passing back the response" in {
        stubSdesProxyListFilesEndpoint(fileType)

        val Some(result) = route(app, validRequest)

        status(result) mustBe OK
        contentAsJson(result) mustBe expectedResponse
        verifySdesProxyListFilesCalled(fileType, clientId)
      }

      "recognise incoming ClientId header in a case insensitive manner and pass it downstream" in {
        stubSdesProxyListFilesEndpoint(fileType)

        val request      = FakeRequest(Helpers.GET, endpoint).withHeaders(invertLettersCase(headerNameClientId) -> clientId)
        val Some(result) = route(app, request)

        status(result) mustBe OK
        verifySdesProxyListFilesCalled(fileType, clientId)
      }
    }

    "Invalid POST Request" should {
      "respond with 405 MethodNotAllowed" in {
        val Some(result) = route(app, FakeRequest(Helpers.POST, endpoint))
        status(result) mustBe METHOD_NOT_ALLOWED
      }
    }

    "no files are found i.e. SDES proxy returns an empty array" should {
      "respond with 200 OK with empty list" in {
        stubSdesProxyListFilesEndpoint(fileType, responseBody = Json.stringify(JsArray()))

        val Some(result) = route(app, validRequest)

        status(result) mustBe OK
        contentAsJson(result) mustBe JsArray()
      }
    }

    "SDES proxy responds with 400 BAD_REQUEST" should {
      "respond with 400 BAD_REQUEST too" in {
        verifyErroneousProxyResponseStatusPropagated(BAD_REQUEST)
      }
    }

    "SDES proxy responds with 401 UNAUTHORIZED" should {
      "respond with 401 UNAUTHORIZED too" in {
        verifyErroneousProxyResponseStatusPropagated(UNAUTHORIZED)
      }
    }

    "SDES proxy responds with 403 FORBIDDEN" should {
      "respond with 403 FORBIDDEN too" in {
        verifyErroneousProxyResponseStatusPropagated(FORBIDDEN)
      }
    }

    s"$headerNameClientId header is not provided in request" should {
      "respond with 401 Unauthorized without calling SDES proxy" in {
        val Some(result) = route(app, FakeRequest(Helpers.GET, endpoint))

        status(result) mustBe UNAUTHORIZED
        verifySdesProxyListFilesNotCalled(fileType)
      }
    }

    s"$headerNameClientId header has a blank value" should {
      "respond with 401 Unauthorized without calling SDES proxy" in {
        val Some(result) = route(app, FakeRequest(Helpers.GET, endpoint).withHeaders(headerNameClientId -> "   "))

        status(result) mustBe UNAUTHORIZED
        verifySdesProxyListFilesNotCalled(fileType)
      }
    }
  }

  private def verifyErroneousProxyResponseStatusPropagated(statusCode: Int) = {
    stubSdesProxyListFilesEndpoint(fileType, statusCode, responseBody = "")

    val Some(result) = route(app, validRequest)

    status(result) mustBe statusCode
  }

  private def invertLettersCase(s: String): String =
    for (c <- s) yield if (c.isLower) c.toUpper else c.toLower
}
