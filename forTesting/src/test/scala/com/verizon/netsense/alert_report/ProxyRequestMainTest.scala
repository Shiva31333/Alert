package com.verizon.netsense.alert_report

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.verizon.netsense.alert_report.Constants.{PATHPARAMETERSREQUESTBODY, REQUESTHEADER}
import com.verizon.netsense.alert_report.aws_lambda.ProxyRequestMain
import com.verizon.netsense.alert_report.utils.ObjectMapperUtil.fromJson
import com.verizon.netsense.alert_report.utils.{RawReport, Report}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

class ProxyRequestMainTest extends FlatSpec with BeforeAndAfterEach with Matchers {


  private var subject = null
  private var testContext = null

  var request = new APIGatewayProxyRequestEvent();
  request.setResource("/")
  request.setPath("/")
  REQUESTHEADER.put("Accept", "*/*")
  REQUESTHEADER.put("accept-encoding", "gzip) deflate")
  REQUESTHEADER.put("Content-Type", "application/json")
  REQUESTHEADER.put("Host", "nsn-lcodev.sensity.com")
  REQUESTHEADER.put("User-Agent", "PostmanRuntime/7.4.0")
  REQUESTHEADER.put("X-Amzn-Trace-Id", "Root=1-5c05c63d-dbfcfbecfd0be35431fc25b8")
  REQUESTHEADER.put("X-Forwarded-For", "68.128.164.93")
  REQUESTHEADER.put("X-Forwarded-Port", "443")
  REQUESTHEADER.put("X-Forwarded-Proto", "https")

  it should "testing createReport success case" in {

    request.setHeaders(REQUESTHEADER)
    request.setBody(Constants.REQUESTbODY)
    request.setHttpMethod(Constants.POSTREQUESTMETHOD)
    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" createReport body = ${request.getBody}")
    println(s" createReport headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")

    assert(response.getStatusCode.equals(200) && reportPayload.orgId.equals(PATHPARAMETERSREQUESTBODY.get("orgId")) &&
      reportPayload.siteId.equals(PATHPARAMETERSREQUESTBODY.get("siteid")))
  }

  it should "testing createReport failure case - path parameters empty" in {

    request.setHeaders(REQUESTHEADER)
    request.setBody(Constants.REQUESTbODY)
    request.setHttpMethod(Constants.POSTREQUESTMETHOD)
    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" createReport body = ${request.getBody}")
    println(s" createReport headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")

    assert(response.getStatusCode.equals(400) && reportPayload.orgId.equals(PATHPARAMETERSREQUESTBODY.get("orgId")) &&
      reportPayload.siteId.equals(PATHPARAMETERSREQUESTBODY.get("siteid")))
  }

  it should "testing delete report success case" in {

    request.setHttpMethod(Constants.DELETEREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" delete report body = ${request.getBody}")
    println(s" delete report headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(200) && response.getBody.equals(s"Report " + PATHPARAMETERSREQUESTBODY.get("reportid") + " deleted successfully"))
  }

  it should "testing delete report failure case - invalid report id" in {

    request.setHttpMethod(Constants.DELETEREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" delete report body = ${request.getBody}")
    println(s" delete report headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(404) && response.getBody.equals(s"No Report found for reportId: " + PATHPARAMETERSREQUESTBODY.get("reportid")))
  }

  it should "testing get only one report record success case- reportid,orgid,siteid" in {

    request.setHttpMethod(Constants.GETREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "1")
    PATHPARAMETERSREQUESTBODY.put("customerid", "123")
    PATHPARAMETERSREQUESTBODY.put("siteid", "1")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" get report record body = ${request.getBody}")
    println(s" get report record  headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")

    assert(response.getStatusCode.equals(200) && reportPayload.orgId.equals(PATHPARAMETERSREQUESTBODY.get("orgId")) &&
      reportPayload.siteId.equals(PATHPARAMETERSREQUESTBODY.get("siteid")))
  }

  it should "testing get all report records success case" in {

    request.setHttpMethod(Constants.GETREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" get all report records body = ${request.getBody}")
    println(s" get all report records headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")

    assert(response.getStatusCode.equals(200) && reportPayload.orgId.equals(PATHPARAMETERSREQUESTBODY.get("orgId")) &&
      reportPayload.siteId.equals(PATHPARAMETERSREQUESTBODY.get("siteid")))
  }

  it should "testing activate report record success case" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" activate report body = ${request.getBody}")
    println(s" activate report headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(200) && response.getBody.equals(s"Report " + PATHPARAMETERSREQUESTBODY.get("reportid") + " activated successfully"))
  }

  it should "testing activate report record failure case - report id not found" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" activate report body = ${request.getBody}")
    println(s" activate report headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(404) && response.getBody.equals(s"No Report found for reportId: " + PATHPARAMETERSREQUESTBODY.get("reportid")))
  }

  it should "testing deActivate report record success case" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" deActivate report record body = ${request.getBody}")
    println(s" deActivate report record headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(200) && response.getBody.equals(s"Report " + PATHPARAMETERSREQUESTBODY.get("reportid") + " de-activated successfully"))

  }
  it should "testing deActivate report record failure case - report id not found" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" deActivate report record body = ${request.getBody}")
    println(s" deActivate report record headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(200) && response.getBody.equals(s"No Report found for reportId: " + PATHPARAMETERSREQUESTBODY.get("reportid")))

  }

  it should "testing update report record success case" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" update report record body = ${request.getBody}")
    println(s" update report record headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: RawReport = fromJson[RawReport](response.getBody)

    assert(response.getStatusCode.equals(200))
  }

  it should "testing update report record failure case - report id not found" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" update report record body = ${request.getBody}")
    println(s" update report record headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: RawReport = fromJson[RawReport](response.getBody)

    assert(response.getStatusCode.equals(404) && response.getBody.equals(s"No Report found for reportId: ${PATHPARAMETERSREQUESTBODY.get("reportid")}"))
  }

}

