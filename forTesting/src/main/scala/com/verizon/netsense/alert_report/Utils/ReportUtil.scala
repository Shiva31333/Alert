package com.verizon.netsense.alert_report.utils

import java.sql.ResultSet
import java.time.Instant
import java.util.{Date, TimeZone, UUID}

import org.quartz.CronExpression._
import com.verizon.netsense.alert_report.utils.ObjectMapperUtil._
import org.quartz.CronExpression
import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  implicit lazy val log: Logger = LoggerFactory.getLogger(this.getClass)
}

class ResponseException(message: String = "Unable to process Request: ",
                        cause: Throwable = None.orNull)
  extends Exception(message, cause)

case class ReportRules(nodeids: Set[String] = Set.empty,
                       alarmtypes: Set[String] = Set.empty,
                       alertstatus: Option[String] = None,
                       reportcolumns: Set[String] = Set.empty)

case class Users(userName: Option[String] = None, userEmail: Option[String] = None)

case class RawReport(
                      reportName: Option[String] = None,
                      reportType: Option[String] = Some("Alert Summary"),
                      timePeriod: Option[String] = None,
                      schedule: Option[String] = None,
                      active: Boolean = false,
                      rules: Option[String] = None,
                      recipients: Set[Users] = Set.empty)


object RawReport {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  val validStatus = Set("all", "active", "inactive")
  val validTime = Set("1day", "2days", "7days", "30days", "60days", "90days")
  val validColumns = Set("nodeid",
    "nodehw",
    "action",
    "alarmtype",
    "ufname",
    "severity",
    "active",
    "created",
    "updated",
    "updated_by")

  def validateRules(str: Option[String]): ReportRules = {
    val rules: ReportRules = fromJson[ReportRules](
      str.getOrElse(throw new ResponseException("Rules cannot be empty")))

    val validatedRules =
      if (rules.nodeids == null || rules.nodeids.isEmpty || rules.alarmtypes == null || rules.alarmtypes.isEmpty)
        throw new ResponseException(
          "Invalid Rules. NodeIds & AlarmTypes cannot be empty")
      else if (!validStatus.contains(rules.alertstatus.getOrElse("all")))
        throw new ResponseException(
          s"Invalid Rules. Alert Status should be one of ${validStatus.mkString(",")}")
      else if (rules.reportcolumns != null && !rules.reportcolumns.forall(validColumns.contains))
        throw new ResponseException(
          s"Invalid Report Columns. Valid Report Columns: ${validColumns.mkString(",")}")
      else if(rules.reportcolumns == null || rules.reportcolumns.isEmpty) rules.copy(reportcolumns = validColumns)
      else rules

    validatedRules.copy(alertstatus = Option(rules.alertstatus.getOrElse("all")))
  }

  def validateReport(r: RawReport): RawReport = {
    val repName = r.reportName.getOrElse(throw new ResponseException("Report Name cannot be empty"))
    val repType = r.reportType.getOrElse("Alert Summary")
    //    TODO: Validations for Scheduling in Future
    //    if (!validTime.contains(r.timePeriod.getOrElse("")))
    //      throw new ResponseException(s"Time Period should be one of ${validTime.mkString(",")}")
    //      if(!isValidExpression(r.schedule.getOrElse("")))
    //        throw new ResponseException(s"Invalid Cron: ${r.schedule.getOrElse("")}")

    val tPeriod = None
    val schedule = None
    val rules = Option(toJson(validateRules(r.rules)))

    RawReport(Some(repName),
      Some(repType),
      tPeriod,
      schedule,
      r.active,
      rules,
      r.recipients)
  }
}

case class Report(
                   reportId: String = UUID.randomUUID().toString,
                   reportName: Option[String] = None,                      //"My Test Report"
                   reportType: Option[String] = Some("Alert Summary"),     //Default: "Alert Summary" (or something else in Future)
                   orgId: String,                                          //orgid
                   siteId: String,                                         //siteid
                   timePeriod: Option[String] = None,                      //default: "1day", "2days", "7days", "30 days", "60 days", "90days"
                   schedule: Option[String] = None,                        //Cron Entry based on Site Timezone //0 17 * * *
                   lastSent: Option[Long] = None,                          //Epoch (Spark/Lamda Have to update it)
                   nextScheduled: Option[Long] = None,                     //Epoch (Spark/Lamda Have to update it)
                   active: Boolean = false,                                //Boolean
                   rules: Option[String] = None,                           //{"nodeids": ["N1","N2"], "alarmtypes": ["T1","T2"], "alertstatus": "active/inactive/all", "reportcolumns": ["nodeid","alarmtype"] }
                   recipients: Set[Users] = Set.empty,                      //Comma seperated list of email
                   created: Option[Long] = Some(Instant.now.toEpochMilli), //Epoch Millis
                   updated: Option[Long] = Some(Instant.now.toEpochMilli)  //Epoch Millis
                 )

object Report extends Logging {
  //  TODO: Needed for Scheduling in Future
  //  def getNextScheduled(schedule: Option[String]): Option[Long] = {
  //    try { Option(new CronExpression(schedule.getOrElse("0 0 4 * * ?")).getNextValidTimeAfter(new Date()).getTime) }
  //    catch { case ex: Exception => log.error(s"Error parsing Cron: ${schedule}"); None }
  //  }

  val nextReportAt = Option(new CronExpression("0 0 4 * * ?").getNextValidTimeAfter(new Date()).getTime)

  def apply(rs: ResultSet): Report = {
    val recipients: Set[Users] = try { fromJson[Set[Users]](rs.getString("recipients")) }
    catch { case ex: Exception => Set.empty }

    Report(
      rs.getString("report_id"),
      Option(rs.getString("report_name")),
      Option(rs.getString("report_type")),
      rs.getString("orgid"),
      rs.getString("siteid"),
      Option(rs.getString("time_period")),
      Option(rs.getString("schedule")),
      Option(rs.getLong("last_sent")),
      Option(rs.getLong("next_scheduled")),
      rs.getBoolean("active"),
      Option(rs.getString("rules")),
      recipients,
      Option(rs.getLong("created")),
      Option(rs.getLong("updated"))
    )
  }

  def apply(report: RawReport, orgId: String, siteId: String): Report = {
    val r = RawReport.validateReport(report)
    Report(
      UUID.randomUUID().toString,
      r.reportName,
      r.reportType,
      orgId,
      siteId,
      r.timePeriod,
      r.schedule,
      None,
      nextReportAt,
      r.active,
      r.rules,
      r.recipients,
      Some(Instant.now.toEpochMilli),
      Some(Instant.now.toEpochMilli)
    )
  }

  def apply(r: Report, rawReport: RawReport): Report = {
    val raw = RawReport.validateReport(rawReport)
    r.copy(
      reportName    = raw.reportName,
      reportType    = raw.reportType,
      timePeriod    = raw.timePeriod,
      schedule      = raw.schedule,
      nextScheduled = nextReportAt,
      active        = raw.active,
      rules         = raw.rules,
      recipients    = raw.recipients,
      updated       = Some(Instant.now.toEpochMilli)
    )
  }
}

case class ResponseMsg(message: Option[String] = None)

object ResponseMsg {
  def errorMsg(msg: String): ResponseMsg = ResponseMsg(Some("Error: " + msg))
  def successMsg(msg: String): ResponseMsg =
    ResponseMsg(Some("Success: " + msg))
}