/*
 * Copyright 2020 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.sdes.bulkdownload.connectors.SdesListFilesConnector
import uk.gov.hmrc.sdes.bulkdownload.model.FileItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BulkDownloadControllerSpec extends UnitSpec with MockitoSugar {

  private trait Setup {
    val mockSdesListFilesConnector: SdesListFilesConnector = mock[SdesListFilesConnector]

    val controller = new BulkDownloadController(mockSdesListFilesConnector, stubControllerComponents())

    val fileType = "FILE_TYPE"
    val validRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("X-Client-ID" -> "someId")

    val emulatedError = new RuntimeException("emulated service error")
  }

  "BulkDownloadController" should {
    "return a successful response OK when listAvailableFiles is empty" in new Setup {
      when(mockSdesListFilesConnector.listAvailableFiles(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Nil))

      private val result = await(controller.list(fileType)(validRequest))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe JsArray()

      verify(mockSdesListFilesConnector).listAvailableFiles(meq(fileType))(any[HeaderCarrier])
    }

    "return successful ok response with json data when listAvailableFiles returns valid data" in new Setup {
      val fileItems = List(FileItem("name", "Url", 10))

      when(mockSdesListFilesConnector.listAvailableFiles(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(fileItems))

      private val result = await(controller.list(fileType)(validRequest))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe Json.toJson(fileItems)

      verify(mockSdesListFilesConnector).listAvailableFiles(meq(fileType))(any[HeaderCarrier])
    }

    "intercept an exception from connector call" in new Setup {
      when(mockSdesListFilesConnector.listAvailableFiles(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(emulatedError))

      private val result = await(controller.list(fileType)(validRequest))
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 405(MethodNotAllowed) when trying to post to endpoint" in new Setup {
      status(await(controller.methodNotAllowed("")(FakeRequest()))) shouldBe Status.METHOD_NOT_ALLOWED
    }

  }

}
