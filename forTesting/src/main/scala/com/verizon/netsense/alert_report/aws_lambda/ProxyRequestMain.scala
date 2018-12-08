package com.verizon.netsense.alert_report.aws_lambda

import java.util.UUID

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.verizon.netsense.alert_report.database._
import com.verizon.netsense.alert_report.utils._
import com.verizon.netsense.alert_report.utils.ObjectMapperUtil._

import scala.collection.JavaConverters._
import scala.collection.mutable

object ProxyRequestMain
  extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent]
    with Logging {

  val db = new DBLayer()

  override def handleRequest(request: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    log.info("Request: " + request)
    val response = new APIGatewayProxyResponseEvent()
    try {

      val pathParameters: mutable.Map[String, String] = request.getPathParameters.asScala
      if (pathParameters.isEmpty || pathParameters == null) throw new RuntimeException("Path parameters are empty")

      val path = request.getPath
      if (path.isEmpty || path == null) throw new RuntimeException("Path is empty")

      val validTypes = Set("GET", "POST", "PUT", "DELETE")
      val httpMethod: String = if (!validTypes.contains(request.getHttpMethod))
        throw new RuntimeException("Invalid API Request")
      else request.getHttpMethod

      val activateReport: Boolean   = path.contains("/activate/")
      val deActivateReport: Boolean = path.contains("/deactivate/")
      val updateReport: Boolean     = !path.contains("activate")
      val previewReport: Boolean    = path.contains("/alerts")
      val reportId: String          = pathParameters.getOrElse("reportid", "")
      val orgId: String             = pathParameters.getOrElse("customerid", "")
      val siteId: String            = pathParameters.getOrElse("siteid", "")

      if (orgId.isEmpty || orgId == null || siteId.isEmpty || siteId == null)
        throw new ResponseException("OrgId & SiteId cannot be null or empty")


      def handleGetRequest: APIGatewayProxyResponseEvent = {
        try {
          if (reportId == null || reportId.isEmpty) {
            val reportList = db.getAllReports(orgId, siteId)
            response.setStatusCode(200)
            response.setBody(toJson(reportList))
          }
          else {
            db.getReport(reportId, orgId, siteId) match {
              case Some(r: Report) =>
                if(previewReport) {
                  val rules: ReportRules = RawReport.validateRules(r.rules)
                  val timePeriod: Int =
                    ("""\d+""".r findFirstIn(r.timePeriod.getOrElse("1day"))).getOrElse("1").trim.toInt
                  val alerts: List[Any] = db.getAlertsForReport(rules, timePeriod)
                  response.setStatusCode(200)
                  response.setBody(toJson(alerts))
                }
                else {
                  response.setStatusCode(200)
                  response.setBody(toJson(r))
                }
              case None =>
                response.setStatusCode(404)
                response.setBody(toJson(ResponseMsg.errorMsg(s"No Report found for reportId: $reportId")))
            }
          }
        } catch {
          case ex: Exception =>
            log.error("ERROR: Error in GET Request: " + ex.getMessage); ex.printStackTrace()
            response.setStatusCode(501)
            response.setBody(toJson(ResponseMsg.errorMsg(ex.getMessage)))
        }
        response
      }

      def handleDeleteRequest: APIGatewayProxyResponseEvent = {
        try {
          if (reportId == null || reportId.isEmpty) throw new RuntimeException("ReportId cannot be empty")
          db.deleteReport(reportId, orgId, siteId) match {
            case true =>
              response.setStatusCode(200)
              response.setBody(toJson(ResponseMsg.successMsg(s"Report $reportId deleted successfully")))
            case false =>
              response.setStatusCode(404)
              response.setBody(toJson(ResponseMsg.errorMsg(s"No Report found for reportId: $reportId")))
          }
        } catch {
          case ex: Exception =>
            log.error("ERROR: Error in DELETE Request: " + ex.getMessage); ex.printStackTrace()
            response.setStatusCode(501)
            response.setBody(toJson(ResponseMsg.errorMsg(ex.getMessage)))
        }
        response
      }

      def handlePostRequest: APIGatewayProxyResponseEvent = {
        try {
          val reportPayload: Report = Report.apply(fromJson[RawReport](request.getBody), orgId, siteId)
          log.info("Report Payload: " + reportPayload)
          db.createReport(reportPayload) match {
            case Some(r: Report) =>
              response.setStatusCode(200)
              response.setBody(toJson(r))
            case _ =>
              response.setStatusCode(400)
              response.setBody(toJson(ResponseMsg.errorMsg(s"Unable to create Report")))
          }
        } catch {
          case ex: Exception =>
            log.error("ERROR: Error in POST Request: " + ex.getMessage); ex.printStackTrace()
            response.setStatusCode(501)
            response.setBody(toJson(ResponseMsg.errorMsg(ex.getMessage)))
        }
        response
      }

      def handlePutRequest: APIGatewayProxyResponseEvent = {
        try {
          if (reportId == null || reportId.isEmpty) throw new RuntimeException("ReportId cannot be empty")
          if (activateReport) {
            db.activateReport(reportId, orgId, siteId) match {
              case true =>
                response.setStatusCode(200)
                response.setBody(toJson(ResponseMsg.successMsg(s"Report $reportId activated successfully")))
              case _ =>
                response.setStatusCode(404)
                response.setBody(toJson(ResponseMsg.errorMsg(s"No Report found for reportId: $reportId")))
            }
          } else if (deActivateReport) {
            db.deActivateReport(reportId, orgId, siteId) match {
              case true =>
                response.setStatusCode(200)
                response.setBody(toJson(ResponseMsg.successMsg(s"Report $reportId de-activated successfully")))
              case _ =>
                response.setStatusCode(404)
                response.setBody(toJson(ResponseMsg.errorMsg(s"No Report found for reportId: $reportId")))
            }
          } else if (updateReport) {
            val reportPayload: RawReport = fromJson[RawReport](request.getBody)
            db.getReport(reportId, orgId, siteId) match {
              case Some(r: Report) =>
                val newReport: Report = Report(r, reportPayload)
                log.info("Report Payload: " + newReport)
                db.updateReport(newReport) match {
                  case Some(r: Report) =>
                    response.setStatusCode(200)
                    response.setBody(toJson(r))
                  case _ =>
                    response.setStatusCode(400)
                    response.setBody(
                      toJson(ResponseMsg.errorMsg(s"Unable to update Report")))
                }
              case _ =>
                response.setStatusCode(404)
                response.setBody(toJson(ResponseMsg.errorMsg(s"No Report found for reportId: $reportId")))
            }
          } else {
            response.setStatusCode(501)
            response.setBody(toJson(ResponseMsg.errorMsg("Invalid Request")))
          }

        } catch {
          case ex: Exception =>
            log.error("ERROR: Error in PUT Request: " + ex.getMessage); ex.printStackTrace()
            response.setStatusCode(501)
            response.setBody(toJson(ResponseMsg.errorMsg(ex.getMessage)))
        }
        response
      }

      val apiResponse: APIGatewayProxyResponseEvent =
        httpMethod match {
          case "GET"    => handleGetRequest
          case "POST"   => handlePostRequest
          case "PUT"    => handlePutRequest
          case "DELETE" => handleDeleteRequest
        }
      log.info("Response: " + apiResponse)
      apiResponse
    } catch {
      case ex: Exception =>
        log.error("ERROR: " + ex.getMessage); ex.printStackTrace()
        response.setStatusCode(501)
        response.setBody(toJson(ResponseMsg.errorMsg(ex.getMessage)))
        log.info("Response: " + response)
        response
    }
  }

}