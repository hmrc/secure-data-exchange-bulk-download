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

package uk.gov.hmrc.sdes.bulkdownload.config

import java.io.File

import com.typesafe.config.ConfigFactory
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.sdes.bulkdownload.utils.TestData._

class SdesServicesConfigSpec extends UnitSpec with MockitoSugar {

  trait Setup {
    def configFileContent: String

    val runMode = Mode.Test

    lazy val servicesConfig = {
      val config = Configuration(ConfigFactory.parseString(configFileContent))
      val env = Environment(mock[File], mock[ClassLoader], runMode)

      new SdesServicesConfig(config, env)
    }
  }

  "SdesServicesConfigSpec" when {

    "reading whitelisted application ids from config" should {
      "read configured values" in new Setup {
        override val configFileContent =
          s"""
             |api.access.white-list.applicationIds.0 = $id1
             |api.access.white-list.applicationIds.1 = $id2
             |api.access.white-list.applicationIds.2 = $id3
        """.stripMargin

        servicesConfig.apiAccessWhitelistedApplicationIds shouldBe applicationIds
      }

      "by default give an empty list when ids aren't configured" in new Setup {
        override val configFileContent = ""
        servicesConfig.apiAccessWhitelistedApplicationIds shouldBe Nil
      }
    }

    "reading api access type from config" should {
      "read a configured value" in new Setup {
        val accessType = "SOME_ACCESS_TYPE"
        override val configFileContent = s"api.access.type = $accessType"

        servicesConfig.apiAccessType shouldBe accessType
      }

      "by default give PRIVATE when access type is not configured" in new Setup {
        override val configFileContent = ""

        servicesConfig.apiAccessType shouldBe ApiAccessTypes.PRIVATE
      }
    }
  }

}
