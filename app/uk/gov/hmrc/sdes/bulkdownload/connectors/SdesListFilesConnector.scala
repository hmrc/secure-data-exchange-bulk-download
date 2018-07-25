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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.sdes.bulkdownload.config.SdesServicesConfig
import uk.gov.hmrc.sdes.bulkdownload.model.FileItem

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SdesListFilesConnector @Inject()(servicesConfig: SdesServicesConfig, http: HttpClient) {

  lazy val serviceUrl: String = {
    val serviceKey = "sdes-list-files"
    val contextKey = s"$serviceKey.context"
    val baseUrl = servicesConfig.baseUrl(serviceKey)
    val context = servicesConfig.getConfString(contextKey,
      throw new IllegalStateException(s"Service context missing: $contextKey"))
    s"$baseUrl$context"
  }

  def listAvailableFiles(fileType: String)(implicit hc: HeaderCarrier): Future[List[FileItem]] = {
    val url = s"$serviceUrl/$fileType"
    http.GET[List[FileItem]](url)
  }

}
