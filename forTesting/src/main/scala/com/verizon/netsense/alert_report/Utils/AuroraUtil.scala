package com.verizon.netsense.alert_report.utils

import java.sql.{Connection, DriverManager, SQLException}

import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}

object AuroraUtil {
  implicit lazy val log: Logger = LoggerFactory.getLogger(this.getClass)

  val dbconfig        = ConfigFactory.load()
  val auroraHost      = dbconfig.getString("db.aurora.auroraHost")
  val auroraPort      = dbconfig.getString("db.aurora.auroraPort")
  val auroraDatabase  = dbconfig.getString("db.aurora.auroraDatabase")
  val auroraUser      = dbconfig.getString("db.aurora.auroraUser")
  val auroraPassword  = dbconfig.getString("db.aurora.auroraPassword")
  val auroraDriver    = dbconfig.getString("db.aurora.auroraDriver")

  @volatile var connection: Connection = _
  val netsenseUrl = "jdbc:mysql://" + auroraHost + ":" + auroraPort + "/" + auroraDatabase

  def createConnection(): Connection =
    try {
      Class.forName(auroraDriver)
      log.info("Created DB connection: " + auroraDatabase)

      if (connection == null) {
        connection =
          DriverManager.getConnection(netsenseUrl, auroraUser, auroraPassword)
        log.info("Created DB connection: " + connection.isValid(0))
        connection

      } else {
        log.info("Reusing DB connection: " + connection.isValid(0))
        connection
      }

    } catch {
      case ex: SQLException =>
        log.error("Unable to establish DB connection " + ex.getMessage); throw ex
      case ex: Exception =>
        log.error("Failed creating connection " + ex.getMessage); throw ex

    }

  def closeConnection(): Unit = {
    try { connection.close()
    } catch {
      case ex: Exception => log.warn("Failed closing DB connection: " + ex.getMessage)
    }
  }
}