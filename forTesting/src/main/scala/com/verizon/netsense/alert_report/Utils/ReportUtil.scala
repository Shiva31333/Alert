package com.verizon.netsense.alert_report.utils

import java.sql.ResultSet
import java.time.Instant
import java.util.UUID
import org.quartz.CronExpression._

import com.verizon.netsense.alert_report.utils.ObjectMapperUtil._
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

case class RawReport(
                      reportName: Option[String] = None,
                      reportType: Option[String] = Some("Alert Summary"),
                      timePeriod: Option[String] = None,
                      schedule: Option[String] = None,
                      active: Boolean = false,
                      rules: Option[String] = None,
                      userEmails: Option[String] = None
                    )

object RawReport {

  def validateRules(str: Option[String]): ReportRules = {
    val validStatus = Set("all", "active", "inactive")
    val validColumns = Set("nodeid", "nodeHw", "action", "alarmType", "ufname", "severity", "active", "created", "updated", "updatedBy")
    val rules: ReportRules = fromJson[ReportRules](str.getOrElse(throw new ResponseException("Rules cannot be empty")))
    if (rules.nodeids.isEmpty || rules.alarmtypes.isEmpty)
      throw new ResponseException("Invalid Rules. NodeIds & AlarmTypes cannot be empty")
    else if(validStatus.contains(rules.alertstatus.getOrElse("all")))
      throw new ResponseException("Invalid Rules. Alert Status should be 'active' or 'inactive' or 'all'")
    else if(rules.reportcolumns.forall(validColumns.contains))
      throw new ResponseException(s"Invalid Report Columns. Valid Report Columns: ${validColumns.mkString(",")}")
    else rules
  }

  def validateReport(r: RawReport) = {
    val repName = r.reportName.getOrElse(throw new ResponseException("Report Name cannot be empty"))
    val repType = r.reportName.getOrElse("Alert Summary")
    //TODO: Validations for Scheduling in Future
    //    val tPeriod = r.timePeriod.getOrElse(throw new ResponseException("Time Period cannot be empty"))
    //    val schedule =
    //      if(isValidExpression(r.schedule.getOrElse(""))) r.schedule
    //      else throw new ResponseException("Invalid Cron for Schedule")
    val tPeriod = None
    val schedule = None
    val rules = Option(toJson(validateRules(r.rules)))

    RawReport(Some(repName), Some(repType), tPeriod, schedule, r.active, rules, r.userEmails)
  }
}

case class Report(
                   reportId: String = UUID.randomUUID().toString,
                   reportName: Option[String] = None, //"My Test Report"
                   reportType: Option[String] = Some("Alert Summary"), //Default: "Alert Summary" (or something else in Future)
                   orgId: String, //orgid
                   siteId: String, //siteid
                   timePeriod: Option[String] = None, //default: "1day", "2days", "7days", "30 days", "60 days", "90days"
                   schedule: Option[String] = None, //Cron Entry based on Site Timezone //0 17 * * *
                   lastSent: Option[String] = None, //UTC String (Spark/Lamda Have to update it)
                   nextScheduled: Option[String] = None, //UTC String (Spark/Lamda Have to update it)
                   active: Boolean = false, //Boolean
                   rules: Option[String] = None, //{"nodeids": ["N1","N2"], "alarmtypes": ["T1","T2"], "alertstatus": "active/inactive", "reportcolumns": ["nodeid","alarmtype"] }
                   userEmails: Option[String] = None, //Comma seperated list of email
                   created: Option[Long] = Some(Instant.now.toEpochMilli), //Epoch Millis
                   updated: Option[Long] = Some(Instant.now.toEpochMilli) //Epoch Millis
                 )

object Report extends Logging {

  def apply(rs: ResultSet): Report = Report(
    rs.getString("report_id"),
    Option(rs.getString("report_name")),
    Option(rs.getString("report_type")),
    rs.getString("orgid"),
    rs.getString("siteid"),
    Option(rs.getString("time_period")),
    Option(rs.getString("schedule")),
    Option(rs.getString("last_sent")),
    Option(rs.getString("next_scheduled")),
    rs.getBoolean("active"),
    Option(rs.getString("rules")),
    Option(rs.getString("user_emails")),
    Option(rs.getLong("created")),
    Option(rs.getLong("updated"))
  )

  def apply(report: RawReport, orgId: String, siteId: String): Report = {
    val r = RawReport.validateReport(report)
    Report(
      UUID.randomUUID().toString,
      r.reportName,
      Some(r.reportType.getOrElse("Alert Summary")),
      orgId,
      siteId,
      Some(r.timePeriod.getOrElse("1day")),
      r.schedule,
      None,
      None,
      r.active,
      r.rules,
      r.userEmails,
      Some(Instant.now.toEpochMilli),
      Some(Instant.now.toEpochMilli)
    )
  }

  def apply(r: Report, rawReport: RawReport): Report = {
    val raw = RawReport.validateReport(rawReport)
    r.copy(
      reportName = raw.reportName,
      reportType = Some(raw.reportType.getOrElse("Alert Summary")),
      timePeriod = Some(raw.timePeriod.getOrElse("1day")),
      schedule = raw.schedule,
      active = raw.active,
      rules = r.rules,
      userEmails = raw.userEmails,
      updated = Some(Instant.now.toEpochMilli)
    )
  }
}

case class ResponseMsg(msg: Option[String] = None)

object ResponseMsg {
  def errorMsg(msg: String): ResponseMsg = ResponseMsg(Some("Error: " + msg))
  def successMsg(msg: String): ResponseMsg =
    ResponseMsg(Some("Success: " + msg))
}