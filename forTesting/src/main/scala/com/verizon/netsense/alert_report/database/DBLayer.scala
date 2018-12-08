package com.verizon.netsense.alert_report.database

import java.sql.{PreparedStatement, ResultSet, Statement}
import java.time.Instant
import com.verizon.netsense.alert_report.utils.ObjectMapperUtil._
import java.util.UUID

import com.verizon.netsense.alert_report.utils._

import scala.util.Random


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

    log.debug(s"Aurora Get Report Query: $getQuery")
    val report: Option[Report] = try {
      val rs: ResultSet = mySQL.executeQuery(getQuery)
      if (rs.first()) { Option(Report.apply(rs)) } else None
    } catch {
      case ex: Exception => log.error("ERROR: Error in getReport DB Call: " + ex.getMessage)
        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
    }
    report
  }

  def getAllReports(orgId: String, siteId: String): List[Report] = {
    val getAllQuery =
      s"""
         |SELECT * FROM ${reportTable}
         | WHERE siteid='${siteId}';
      """.stripMargin

    log.debug(s"Aurora Get All Reports Query: $getAllQuery")
    val resp: List[Report] = try {
      val rs: ResultSet = mySQL.executeQuery(getAllQuery)
      val rsIterator: Iterator[ResultSet] = new Iterator[ResultSet] {
        def hasNext = rs.next()
        def next() = rs
      }
      rsIterator.foldLeft(List[Report]())((a, c) => {
        a.+:(Report.apply(c))
      })
    } catch {
      case ex: Exception => log.error("ERROR: Error in getAllReports DB Call: " + ex.getMessage)
        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  def deleteReport(reportId: String, orgId: String, siteId: String): Boolean = {
    val deleteQuery =
      s"""
         |DELETE FROM ${reportTable}
         | WHERE report_id ='${reportId}' AND siteid = '${siteId}';
       """.stripMargin

    log.debug(s"Aurora Delete Report Query: $deleteQuery")
    val resp: Boolean = try {
      val rs: Int = mySQL.executeUpdate(deleteQuery)
      if (rs > 0) true else false
    } catch {
      case ex: Exception => log.error("ERROR: Error in deleteReport DB Call: " + ex.getMessage)
        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
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

    log.debug(s"Aurora Delete Report Query: $activateQuery")
    val resp: Boolean = try {
      val rs: Int = mySQL.executeUpdate(activateQuery)
      if (rs > 0) true else false
    } catch {
      case ex: Exception => log.error("ERROR: Error in activateReport DB Call: " + ex.getMessage)
        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
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

    log.debug(s"Aurora Delete Report Query: $deActivateQuery")
    val resp: Boolean = try {
      val rs: Int = mySQL.executeUpdate(deActivateQuery)
      if (rs > 0) true else false
    } catch {
      case ex: Exception => log.error("ERROR: Error in deActivateReport DB Call: " + ex.getMessage)
        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  def createReport(report: Report): Option[Report] = {
    val insertColumns =
      s"""(report_id,report_name,report_type,orgid,siteid,
         |time_period,schedule,last_sent,next_scheduled,
         |active,rules,recipients,created,updated)""".stripMargin

    val insertQuery =
      s"""
         |INSERT INTO ${reportTable} $insertColumns
         | VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
      """.stripMargin

    val recipients = if(report.recipients == null || report.recipients.isEmpty) null else toJson(report.recipients)
    val mySQLP: PreparedStatement = sqlConnection.prepareStatement(insertQuery)
    mySQLP.setObject(1, report.reportId)
    mySQLP.setObject(2, report.reportName.getOrElse(null))
    mySQLP.setObject(3, report.reportType.getOrElse(null))
    mySQLP.setObject(4, report.orgId)
    mySQLP.setObject(5, report.siteId)
    mySQLP.setObject(6, report.timePeriod.getOrElse(null))
    mySQLP.setObject(7, report.schedule.getOrElse(null))
    mySQLP.setObject(8, report.lastSent.getOrElse(null))
    mySQLP.setObject(9, report.nextScheduled.getOrElse(null))
    mySQLP.setObject(10, report.active)
    mySQLP.setObject(11, report.rules.getOrElse(null))
    mySQLP.setObject(12, recipients)
    mySQLP.setObject(13, report.created.getOrElse(null))
    mySQLP.setObject(14, report.updated.getOrElse(null))

    log.debug(s" Aurora Create Report Query: $insertQuery")
    val resp: Option[Report] = try {
      val applied: Int = mySQLP.executeUpdate
      if (applied > 0) Option(report) else None

    } catch {
      case ex: Exception => log.error("ERROR: Error in createReport DB Call: " + ex.getMessage)
        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  def updateReport(report: Report): Option[Report] = {
    val updateQuery =
      s"""
         |UPDATE ${reportTable}
         | SET report_name = ?, report_type = ?, time_period = ?, schedule = ?, next_scheduled = ?, active = ?,
         | rules = ?, recipients = ?, updated = ?
         | WHERE report_id = ? AND siteid = ?;
       """.stripMargin

    val recipients = if(report.recipients == null || report.recipients.isEmpty) null else toJson(report.recipients)
    val mySQLP: PreparedStatement = sqlConnection.prepareStatement(updateQuery)
    mySQLP.setObject(1, report.reportName.getOrElse(null))
    mySQLP.setObject(2, report.reportType.getOrElse(null))
    mySQLP.setObject(3, report.timePeriod.getOrElse(null))
    mySQLP.setObject(4, report.schedule.getOrElse(null))
    mySQLP.setObject(5, report.nextScheduled.getOrElse(null))
    mySQLP.setObject(6, report.active)
    mySQLP.setObject(7, report.rules.getOrElse(null))
    mySQLP.setObject(8, recipients)
    mySQLP.setObject(9, report.updated.getOrElse(null))
    mySQLP.setString(10, report.reportId)
    mySQLP.setString(11, report.siteId)

    log.debug(s"Aurora Update Report Query: $updateQuery")
    val resp: Option[Report] = try {
      val applied: Int = mySQLP.executeUpdate
      if (applied > 0) Option(report) else None

    } catch {
      case ex: Exception => log.error("ERROR: Error in updateReport DB Call: " + ex.getMessage)
        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  private def formatList(l: Set[String]): String =
    l.map(x => s"'${x}'").mkString("(", ", ", ")")

  def getAlertsForReport(rule: ReportRules, tPeriod: Int): List[Any] = {
    val alertStatus: String       = rule.alertstatus.getOrElse("all").toLowerCase
    val selectionColumns: String  = if(rule.reportcolumns.isEmpty)
      "nodeid, nodeHw, action, alarmType, ufname, severity, active, created, updated, updatedBy"
    else rule.reportcolumns.mkString(",")

    val (minTime, maxTime) = (Instant.now.toEpochMilli - (tPeriod * 24 * 60 * 60 * 1000) , Instant.now.toEpochMilli)

    val getAllQuery = alertStatus match {
      case "active" =>
        s"""
           |SELECT ${selectionColumns} FROM ${alertsTable}
           | WHERE nodeid IN ${formatList(rule.nodeids)} AND alarmtype IN ${formatList(rule.alarmtypes)} AND active = 1
           | AND updated BETWEEN ${minTime} AND ${maxTime};
        """.stripMargin
      case "inactive" =>
        s"""
           |SELECT ${selectionColumns} FROM ${alertsTable}
           | WHERE nodeid IN ${formatList(rule.nodeids)} AND alarmtype IN ${formatList(rule.alarmtypes)} AND active = 0
           | AND updated BETWEEN ${minTime} AND ${maxTime};
        """.stripMargin
      case _ =>
        s"""
           |SELECT ${selectionColumns} FROM ${alertsTable}
           | WHERE nodeid IN ${formatList(rule.nodeids)} AND alarmtype IN ${formatList(rule.alarmtypes)}
           | AND updated BETWEEN ${minTime} AND ${maxTime};
        """.stripMargin
    }

    log.debug(s"Aurora Get All Reports Query: $getAllQuery")
    val resp = try {
      val rs: ResultSet = mySQL.executeQuery(getAllQuery)
      val rsIterator: Iterator[ResultSet] = new Iterator[ResultSet] {
        def hasNext = rs.next()
        def next() = rs
      }
      val rsmd = rs.getMetaData
      def getMap(rs: ResultSet): Map[String, AnyRef] = {
        {for {
          i <- (1 to rsmd.getColumnCount())
          key = rsmd.getColumnName(i).toLowerCase
          value = rs.getObject(key)
        } yield (key,value)}.toMap[String, AnyRef]
      }

      rsIterator.foldLeft(List[Any]())((a, c) => { a.+:(getMap(c)) })
    } catch {
      case ex: Exception => log.error("ERROR: Error in getAlertsForReport DB Call: " + ex.getMessage)
        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
    }
    resp
  }

  //  def loadAlerts = {
  //    val nodeIds = Seq("N1", "N2", "N3", "N4", "N5", "N6", "N7", "N8")
  //
  //    def loadAlertsQuery(i: Int) =
  //        s"""
  //           INSERT INTO alerts_historic (recordid, alertid, orgid, siteid, nodeid, lat, lon, group_name, fixture_name, alarmtype, severity, category, nodehw, active, updated_by, created, updated, ufname, description, displaytopartner, displaytocustomer, action) VALUES
  //           ("${UUID.randomUUID().toString + i}",
  //           "988460-98c4-11e7-a10f-0f5228981g9c54",
  //           "org1",
  //           "site1",
  //           "${nodeIds(Random.nextInt(nodeIds.size))}",
  //           123.456,
  //           123.456,
  //           "G1",
  //           "F1",
  //           "Disconnect",
  //           "Major",
  //           "Network",
  //           "unode-v3",
  //           1,
  //           "System Update",
  //           1544152931663,
  //           1544152931680,
  //           "${"Node Disconnected" + i}",
  //           "Node Disconnected from Netsense Platform",
  //           1,
  //           1,
  //           "Check Node Connectivity"
  //           );
  //        """.stripMargin
  //
  //
  //    val resp = try {
  //      for(i <- 1 to 20000) {
  //        log.debug(s"Load Reports Query: ${loadAlertsQuery(i)}")
  //        mySQL.executeUpdate(loadAlertsQuery(i))
  //      }
  //      true
  //    } catch {
  //      case ex: Exception => log.error("ERROR: Error in getAlertsForReport DB Call: " + ex.getMessage)
  //        throw new ResponseException("Unable to fetch data from DB: " + ex.getMessage)
  //    }
  //    resp
  //  }
}