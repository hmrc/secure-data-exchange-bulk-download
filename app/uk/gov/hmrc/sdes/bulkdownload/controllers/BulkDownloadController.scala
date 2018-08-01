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
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.sdes.bulkdownload.connectors.SdesListFilesConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton()
class BulkDownloadController @Inject()(sdesListFilesConnector: SdesListFilesConnector) extends BaseController {

  def list(fileType: String): Action[AnyContent] = HavingClientIdHeader.async { implicit request =>
    Logger.debug(s"request headers from /list: ${request.headers.toSimpleMap}")
    sdesListFilesConnector.listAvailableFiles(fileType) map {
      case Nil => NotFound
      case nonEmptyList => Ok(Json.toJson(nonEmptyList))
    } recover {
      case bre: uk.gov.hmrc.http.BadRequestException =>
        Logger.warn(s"BadRequest received when listing available files for $fileType: $bre")
        BadRequest
      case NonFatal(e) =>
        Logger.error(s"Could not list available files for $fileType: $e", e)
        InternalServerError
    }
  }

  private object HavingClientIdHeader extends ActionBuilder[Request] with ActionFilter[Request] {
    val clientIdHeaderName = "X-Client-ID"

    override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
      val maybeError = request.headers.get(clientIdHeaderName) match {
        case None => Some(Unauthorized)
        case Some(empty) if empty.trim.isEmpty => Some(Unauthorized)
        case _ => None
      }
      Future.successful(maybeError)
    }
  }

}
