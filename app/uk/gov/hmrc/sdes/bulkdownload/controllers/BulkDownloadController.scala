/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.sdes.bulkdownload.connectors.SdesListFilesConnector

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton()
class BulkDownloadController @Inject() (sdesListFilesConnector: SdesListFilesConnector, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  private lazy val emptyHc = HeaderCarrier()

  def methodNotAllowed(fileType: String): Action[AnyContent] = Action(MethodNotAllowed)

  def list(fileType: String): Action[AnyContent] = HavingClientIdHeader.async { implicit request =>
    val (hc, maybeClientId) = headerCarrierWithClientId
    lazy val clientId: String = maybeClientId.fold(ifEmpty = "N/A")(identity)
    logger.debug(s"request headers from /list: ${request.headers.toSimpleMap}")
    logger.debug(s"HeaderCarrier headers from /list: ${hc.extraHeaders}")
    sdesListFilesConnector.listAvailableFiles(fileType)(hc) map { files =>
      Ok(Json.toJson(files))
    } recover {
      case e4xx: Upstream4xxResponse if e4xx.upstreamResponseCode == BAD_REQUEST =>
        logger.error(s"Status BadRequest received when listing available files of type $fileType with client id $clientId: $e4xx")
        BadRequest
      case e4xx: Upstream4xxResponse if e4xx.upstreamResponseCode == UNAUTHORIZED =>
        logger.error(s"Status Unauthorized received when listing available files of type $fileType with client id $clientId: $e4xx")
        Unauthorized
      case e4xx: Upstream4xxResponse if e4xx.upstreamResponseCode == FORBIDDEN =>
        logger.error(s"Status Forbidden received when listing available files of type $fileType with client id $clientId: $e4xx")
        Forbidden
      case NonFatal(e) =>
        logger.error(s"Could not list available files of type $fileType with client id $clientId: $e", e)
        InternalServerError
    }
  }

  private def headerCarrierWithClientId(implicit request: Request[_]): (HeaderCarrier, Option[String]) = {
    import HavingClientIdHeader.clientIdHeaderName
    request.headers.get(clientIdHeaderName).fold(ifEmpty = emptyHc -> Option.empty[String]) { clientId =>
      emptyHc.withExtraHeaders(clientIdHeaderName -> clientId) -> Some(clientId)
    }
  }

  private object HavingClientIdHeader extends ActionBuilder[Request, AnyContent] with ActionFilter[Request] {
    val clientIdHeaderName = "X-Client-ID"

    def executionContext: ExecutionContext       = cc.executionContext
    def parser:           BodyParser[AnyContent] = cc.parsers.defaultBodyParser

    override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
      val maybeError = request.headers.get(clientIdHeaderName) match {
        case None =>
          logger.error(s"Header '$clientIdHeaderName' not found in request.")
          Some(Unauthorized)
        case Some(blank) if blank.trim.isEmpty =>
          logger.error(s"Request header '$clientIdHeaderName' has a blank value '$blank'.")
          Some(Unauthorized)
        case _ => None
      }
      Future.successful(maybeError)
    }
  }

}
