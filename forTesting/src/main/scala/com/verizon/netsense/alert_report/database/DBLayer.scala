package com.verizon.netsense.alert_report.database

import java.sql.{ResultSet, Statement}
import java.time.Instant

import com.verizon.netsense.alert_report.utils._


class DBLayer extends Logging {

  lazy val reportTable = "summary_report"
  lazy val alertsTable = "alerts_historic"
  lazy val sqlConnection = AuroraUtil.createConnection()
  lazy val mySQL: Statement = sqlConnection.createStatement()

  def getReport(reportId: String,
                orgId: String,
                siteId: String): Option[Report] = {
    val getQuery =
      s"""
         |SELECT * FROM ${reportTable}
         | WHERE report_id='${reportId}' AND siteid='${siteId}' LIMIT 1;
      """.stripMargin

    log.trace(s"Aurora Get Report Query: $getQuery")
    val report: Option[Report] = try {
      val rs: ResultSet = mySQL.executeQuery(getQuery)
      log.info("Result from DB: " + rs)
      if (rs.first()) { Option(Report.apply(rs)) } else None
    } catch {
      case ex: Exception =>
        throw new ResponseException(
          "Unable to fetch data from DB: " + ex.getMessage)
    }
    report
  }

  def getAllReports(orgId: String, siteId: String): List[Report] = {
    val getAllQuery =
      s"""
         |SELECT * FROM ${reportTable}
         | WHERE siteid='${siteId}';
      """.stripMargin

    log.trace(s"Aurora Get All Reports Query: $getAllQuery")
    val resp: List[Report] = try {
      val rs: ResultSet = mySQL.executeQuery(getAllQuery)
      log.info("Result from DB: " + rs)
      val rsIterator: Iterator[ResultSet] = new Iterator[ResultSet] {
        def hasNext = rs.next()
        def next() = rs
      }
      rsIterator.foldLeft(List[Report]())((a, c) => {
        a.+:(Report.apply(c))
      })
    } catch {
      case ex: Exception =>
        throw new ResponseException(
          "Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  def deleteReport(reportId: String, orgId: String, siteId: String): Boolean = {
    val deleteQuery =
      s"""
         |DELETE FROM ${reportTable}
         | WHERE report_id ='${reportId}' AND siteid = '${siteId}';
       """.stripMargin

    log.trace(s"Aurora Delete Report Query: $deleteQuery")
    val resp: Boolean = try {
      val rs: Int = mySQL.executeUpdate(deleteQuery)
      log.info("Result from DB: " + rs)
      if (rs > 0) true else false
    } catch {
      case ex: Exception =>
        throw new ResponseException(
          "Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  def activateReport(reportId: String,
                     orgId: String,
                     siteId: String): Boolean = {
    val activateQuery =
      s"""
         | UPDATE ${reportTable}
         | SET active = 1 , updated='${Instant.now.toEpochMilli}'
         | WHERE report_id ='$reportId' AND siteid = '${siteId}';
       """.stripMargin

    log.trace(s"Aurora Delete Report Query: $activateQuery")
    val resp: Boolean = try {
      val rs: Int = mySQL.executeUpdate(activateQuery)
      log.info("Result from DB: " + rs)
      if (rs > 0) true else false
    } catch {
      case ex: Exception =>
        throw new ResponseException(
          "Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  def deActivateReport(reportId: String,
                       orgId: String,
                       siteId: String): Boolean = {
    val deActivateQuery =
      s"""
         | UPDATE ${reportTable}
         | SET active = 0 , updated='${Instant.now.toEpochMilli}'
         | WHERE report_id ='$reportId' AND siteid = '${siteId}';
       """.stripMargin

    log.trace(s"Aurora Delete Report Query: $deActivateQuery")
    val resp: Boolean = try {
      val rs: Int = mySQL.executeUpdate(deActivateQuery)
      log.info("Result from DB: " + rs)
      if (rs > 0) true else false
    } catch {
      case ex: Exception =>
        throw new ResponseException(
          "Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  def createReport(report: Report): Option[Report] = {
    val active = if(report.active) 1 else 0
    val insertColumns =
      s"""(report_id,report_name,report_type,orgid,siteid,
         |time_period,schedule,last_sent,next_scheduled,
         |active,rules,user_emails,created,updated)""".stripMargin

    val insertQuery =
      s"""
         |INSERT INTO ${reportTable} $insertColumns
         | VALUES ('${report.reportId}', '${report.reportName.getOrElse(null)}', '${report.reportType.getOrElse(null)}',
         | '${report.orgId}', '${report.siteId}','${report.timePeriod.getOrElse(null)}',
         | '${report.schedule.getOrElse(null)}','${report.lastSent.getOrElse(null)}',
         | '${report.nextScheduled.getOrElse(null)}','${active}','${report.rules.getOrElse(null)}',
         | '${report.userEmails.getOrElse(null)}',
         | '${report.created.getOrElse(null)}','${report.updated.getOrElse(null)}'
         | );
      """.stripMargin

    log.info(s" Aurora Create Report Query: $insertQuery")
    val resp: Option[Report] = try {
      val applied: Int = mySQL.executeUpdate(insertQuery)
      log.info(" Response " + applied)
      if (applied > 0) Option(report) else None

    } catch {
      case ex: Exception =>
        throw new ResponseException(
          "Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  def updateReport(report: Report): Option[Report] = {
    val active = if(report.active) 1 else 0
    val updateQuery =
      s"""
         |UPDATE ${reportTable}
         | SET report_name='${report.reportName.getOrElse(null)}', time_period='${report.timePeriod.getOrElse(null)}',
         | schedule='${report.schedule.getOrElse(null)}', active='${active}',rules='${report.rules.getOrElse(null)}',
         | user_emails='${report.userEmails.getOrElse(null)}', updated='${report.updated.getOrElse(null)}'
         | WHERE report_id ='${report.reportId}' AND siteid = '${report.siteId}';
       """.stripMargin

    log.info(s"Aurora Update Report Query: $updateQuery")
    val resp: Option[Report] = try {
      val applied: Int = mySQL.executeUpdate(updateQuery)
      log.info(" Response " + applied)
      if (applied > 0) Option(report) else None

    } catch {
      case ex: Exception =>
        throw new ResponseException(
          "Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  private def formatList(l: Set[String]): String =
    l.map(x => s"'${x}'").mkString("(", ", ", ")")

  def getAlertsForReport(rule: ReportRules, timePeriod: Int): List[Any] = {
    val alertStatus: String       = rule.alertstatus.getOrElse("all").toLowerCase
    val selectionColumns: String  =
      if(rule.reportcolumns.isEmpty)
        "nodeid, nodeHw, action, alarmType, ufname, severity, active, created, updated, updatedBy"
      else rule.reportcolumns.mkString(",")
    val (minTime, maxTime) = (Instant.now.toEpochMilli - (timePeriod * 24 * 60 * 60 * 1000) , Instant.now.toEpochMilli)

    val getAllQuery = alertStatus match {
      case "active" =>
        s"""
           |SELECT ${selectionColumns} FROM ${alertsTable}
           | WHERE nodeid IN ${formatList(rule.nodeids)} AND alarmType IN ${formatList(rule.alarmtypes)} AND active = 1
           | AND updated BETWEEN ${minTime} AND ${maxTime};
        """.stripMargin
      case "inactive" =>
        s"""
           |SELECT ${selectionColumns} FROM ${alertsTable}
           | WHERE nodeid IN ${formatList(rule.nodeids)} AND alarmType IN ${formatList(rule.alarmtypes)} AND active = 0
           | AND updated BETWEEN ${minTime} AND ${maxTime};
        """.stripMargin
      case _ =>
        s"""
           |SELECT ${selectionColumns} FROM ${formatList(rule.nodeids)}
           | WHERE nodeid IN ${rule.nodeids} AND alarmType IN ${formatList(rule.alarmtypes)}'
           | AND updated BETWEEN ${minTime} AND ${maxTime};
        """.stripMargin
    }

    log.trace(s"Aurora Get All Reports Query: $getAllQuery")
    val resp = try {
      val rs: ResultSet = mySQL.executeQuery(getAllQuery)
      log.info("Result from DB: " + rs)
      val rsIterator: Iterator[ResultSet] = new Iterator[ResultSet] {
        def hasNext = rs.next()
        def next() = rs
      }
      rsIterator.foldLeft(List[Any]())((a, c) => {
        a.+:(Report.apply(c))
      })
    } catch {
      case ex: Exception =>
        throw new ResponseException(
          "Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }
}