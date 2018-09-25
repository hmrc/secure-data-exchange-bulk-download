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

package uk.gov.hmrc.sdes.bulkdownload.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.sdes.bulkdownload.connectors.SdesListFilesConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton()
class BulkDownloadController @Inject()(sdesListFilesConnector: SdesListFilesConnector) extends BaseController {

  private lazy val emptyHc = HeaderCarrier()

  def methodNotAllowed(fileType: String): Action[AnyContent] = Action { implicit request =>  MethodNotAllowed }

  def list(fileType: String): Action[AnyContent] = HavingClientIdHeader.async { implicit request =>
    val (hc, maybeClientId) = headerCarrierWithClientId
    lazy val clientId: String = maybeClientId.fold(ifEmpty = "N/A")(identity)
    Logger.debug(s"request headers from /list: ${request.headers.toSimpleMap}")
    Logger.debug(s"HeaderCarrier headers from /list: ${hc.headers}")
    sdesListFilesConnector.listAvailableFiles(fileType)(hc) map {
      case Nil => NotFound
      case nonEmptyList => Ok(Json.toJson(nonEmptyList))
    } recover {
      case bre: uk.gov.hmrc.http.BadRequestException =>
        Logger.error(s"Status BadRequest received when listing available files of type $fileType with client id $clientId: $bre")
        BadRequest
      case e4xx: uk.gov.hmrc.http.Upstream4xxResponse if e4xx.upstreamResponseCode == UNAUTHORIZED =>
        Logger.error(s"Status Unauthorized received when listing available files of type $fileType with client id $clientId: $e4xx")
        Unauthorized
      case e4xx: uk.gov.hmrc.http.Upstream4xxResponse if e4xx.upstreamResponseCode == FORBIDDEN =>
        Logger.error(s"Status Forbidden received when listing available files of type $fileType with client id $clientId: $e4xx")
        Forbidden
      case NonFatal(e) =>
        Logger.error(s"Could not list available files of type $fileType with client id $clientId: $e", e)
        InternalServerError
    }
  }

  private def headerCarrierWithClientId(implicit request: Request[_]): (HeaderCarrier, Option[String]) = {
    import HavingClientIdHeader.clientIdHeaderName
    request.headers.get(clientIdHeaderName).fold(ifEmpty = emptyHc -> Option.empty[String]) { clientId =>
      emptyHc.withExtraHeaders(clientIdHeaderName -> clientId) -> Some(clientId)
    }
  }

  private object HavingClientIdHeader extends ActionBuilder[Request] with ActionFilter[Request] {
    val clientIdHeaderName = "X-Client-ID"

    override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
      val maybeError = request.headers.get(clientIdHeaderName) match {
        case None =>
          Logger.error(s"Header '$clientIdHeaderName' not found in request.")
          Some(Unauthorized)
        case Some(blank) if blank.trim.isEmpty =>
          Logger.error(s"Request header '$clientIdHeaderName' has a blank value '$blank'.")
          Some(Unauthorized)
        case _ => None
      }
      Future.successful(maybeError)
    }
  }

}
