/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.sdes.bulkdownload.utils.wiremock

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status.OK

trait MockSdesProxyListFilesEndpoint {
  val defaultListFilesResponseBody: String =
    s"""
       |[
       |  { "filename": "xyz.xml.gz", "downloadURL": "https://<sdes.domain>?token=jwt.goes.here", "fileSize":1234 }
       |]
     """.stripMargin

  private def mockEndpoint(fileType: String) = s"/files-available/list/$fileType"

  def stubSdesProxyListFilesEndpoint(fileType: String, status: Int = OK, responseBody: String = defaultListFilesResponseBody) {
    stubFor(
      get(urlEqualTo(mockEndpoint(fileType)))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(responseBody)
        )
    )
  }

  def verifySdesProxyListFilesNotCalled(fileType: String): Unit = verify(0, getUrlRequestedFor(fileType))

  def verifySdesProxyListFilesCalled(fileType: String, clientIdHeader: String) {
    verify(
      getUrlRequestedFor(fileType)
        .withHeader("X-Client-ID", equalTo(clientIdHeader))
    )
  }

  private def getUrlRequestedFor(fileType: String) =
    getRequestedFor(urlEqualTo(mockEndpoint(fileType)))
}
