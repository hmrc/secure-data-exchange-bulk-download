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

package uk.gov.hmrc.sdes.bulkdownload.config

import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, verifyZeroInteractions}
import org.scalatest.mockito.MockitoSugar
import play.api.Mode.Mode
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.api.{Application, Mode}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.sdes.bulkdownload.connectors.ServiceLocatorConnector

class AppStartupSpec extends UnitSpec with MockitoSugar {

  val mockServiceLocatorConnector: ServiceLocatorConnector = mock[ServiceLocatorConnector]

  def app(registrationEnabled: Option[Boolean], mode: Mode = Mode.Test): Application = {
    reset(mockServiceLocatorConnector)
    val config = for (flag <- registrationEnabled) yield "microservice.services.service-locator.enabled" -> flag
    GuiceApplicationBuilder()
      .in(mode)
      .configure(config.toSeq :_*)
      .bindings(bind[ServiceLocatorConnector].to(mockServiceLocatorConnector))
      .build()
  }

  "Application startup" when {
    "registration is not configured" should {
      "register the microservice in service locator" in running(app(registrationEnabled = None)){
        verify(mockServiceLocatorConnector).register(any[HeaderCarrier])
      }
    }

    "registration is enabled" should {
      "register the microservice in service locator" in running(app(registrationEnabled = Some(true))){
        verify(mockServiceLocatorConnector).register(any[HeaderCarrier])
      }
    }

    "registration is disabled" should {
      "not register with service locator" in running(app(registrationEnabled = Some(false))){
        verifyZeroInteractions(mockServiceLocatorConnector)
      }
    }

    "running locally in Dev mode" should {
      "not register with service locator" in running(app(registrationEnabled = None, mode = Mode.Dev)){
        verifyZeroInteractions(mockServiceLocatorConnector)
      }
    }
  }

}
