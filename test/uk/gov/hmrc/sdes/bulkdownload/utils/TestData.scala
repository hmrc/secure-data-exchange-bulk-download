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

package uk.gov.hmrc.sdes.bulkdownload.utils

import play.api.libs.json.{JsValue, Json}

object TestData {

  val applicationIds @ Seq(id1, id2, id3) = Seq("id1", "id2", "id3")

  def expectedDefinitionJson(whitelistedApplicationIds: Seq[String] = applicationIds): JsValue = Json.parse(
    s"""
       |{
       |  "scopes": [
       |    {
       |      "key": "read:bulk-data-download-list",
       |      "name": "List Available Files",
       |      "description": "List files available for download from SDES"
       |    }
       |  ],
       |  "api": {
       |    "name": "Bulk Data File List",
       |    "description": "An API informing about files available for download from Secure Data Exchange Services.",
       |    "context": "bulk-data-download",
       |    "versions": [
       |      {
       |        "version": "1.0",
       |        "status": "BETA",
       |        "access": {
       |          "type": "PRIVATE",
       |          "whitelistedApplicationIds":[ ${whitelistedApplicationIds.map(Json.toJson(_)).mkString(",")} ]
       |        },
       |        "endpointsEnabled": true
       |      }
       |    ]
       |  }
       |}
         """.stripMargin
  )

}
