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

package uk.gov.hmrc.sdes.bulkdownload.controllers

import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Controller
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.sdes.bulkdownload.config.SdesModule

class RoutesSpec extends UnitSpec with BeforeAndAfterEach with GuiceOneAppPerSuite with MockitoSugar {

  val allControllers@(
    mockApiDocumentationController,
    mockBulkDownloadController) = (
    mock[ApiDocumentationController],
    mock[BulkDownloadController]
  )

  private def forEachController(f: Controller => Unit): Unit =
    allControllers.productIterator.foreach { case c: Controller => f(c) }

  override protected def beforeEach(): Unit = {
    forEachController(Mockito.reset(_))
  }

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(bind[ApiDocumentationController] toInstance mockApiDocumentationController)
    .overrides(bind[BulkDownloadController] toInstance mockBulkDownloadController)
    .configure("microservice.services.service-locator.enabled" -> false)
    .configure("auditing.enabled" -> false)
    .disable(classOf[SdesModule])
    .build()

  private val fileType = "FILETYPE"

  val routes = Seq(
    (GET, "/api/definition", () => verify(mockApiDocumentationController).definition()),
    (GET, s"/list/$fileType", () => verify(mockBulkDownloadController).list(fileType))
  )

  "routes" should {
    routes.foreach { case (method, url, verification) =>
      s"properly route $method $url" in {
        route(app, FakeRequest(method, url))
        verification.apply()
      }
    }

    "not route unknown urls" in {
      Seq(GET, POST) foreach { method =>
        route(app, FakeRequest(method, "/rubbish"))
        forEachController(verifyZeroInteractions(_))
      }
    }
  }

}
