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

package uk.gov.hmrc.sdes.bulkdownload.utils.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.http.Port
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient
import uk.gov.hmrc.sdes.bulkdownload.utils.TestUtils

trait WireMockManager {
  lazy val wireMockPort: Int = TestUtils.port
  lazy val wireMockHost: String = TestUtils.host

  lazy val wireMockUrl: String = TestUtils.url

  protected lazy val wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(wireMockPort))

  def startMockServer() {
    if (!wireMockServer.isRunning) wireMockServer.start()
    WireMock.configureFor(wireMockHost, wireMockPort)
  }

  def resetMockServer() {
    WireMock.reset()
  }

  def stopMockServer() {
    wireMockServer.stop()
  }
}

object WireMockManager extends WireMockManager

trait WireMockRunner extends BeforeAndAfterEach with WireMockManager { self: Suite =>
  override def beforeEach(): Unit = {
    super.beforeEach()
    startMockServer()
    resetMockServer()
  }

  override def afterEach(): Unit = {
    stopMockServer()
    super.afterEach()
  }
}

object WireMockRunner extends WireMockManager {
  def withWireMockServer[T](func: WSClient => T): T = {
    try {
      WireMockManager.startMockServer()
      WireMockManager.resetMockServer()
      WsTestClient.withClient(func(_))(new Port(wireMockPort))
    } finally {
      WireMockManager.stopMockServer()
    }
  }

  def withWireMockServer[T](block: => T): T = withWireMockServer(_ => block)
}
