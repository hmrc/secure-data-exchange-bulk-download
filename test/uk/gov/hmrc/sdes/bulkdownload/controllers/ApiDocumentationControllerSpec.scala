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

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.http.ContentTypes
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.sdes.bulkdownload.config.SdesServicesConfig
import uk.gov.hmrc.sdes.bulkdownload.utils.TestData._

class ApiDocumentationControllerSpec extends UnitSpec with MockitoSugar {

  "ApiDocumentationController" should {
    "render definition.json with access type and whitelisted application ids" in {
      val mockConfig = mock[SdesServicesConfig]
      val controller = new ApiDocumentationController(mockConfig)

      when(mockConfig.apiAccessType).thenReturn(ApiAccessTypes.PUBLIC)
      when(mockConfig.apiAccessWhitelistedApplicationIds).thenReturn(applicationIds)

      val result = controller.definition()(FakeRequest())

      status(result) shouldBe OK
      contentType(result) shouldBe Some(ContentTypes.JSON)
      contentAsJson(result) shouldBe expectedDefinitionJson(ApiAccessTypes.PUBLIC, applicationIds)

    }
  }

}
