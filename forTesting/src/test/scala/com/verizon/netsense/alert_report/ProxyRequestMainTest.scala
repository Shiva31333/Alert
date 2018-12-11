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

  var request = new APIGatewayProxyRequestEvent()
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

  it should "test for createReport success case" in {

    request.setHeaders(REQUESTHEADER)
    request.setBody(Constants.REQUESTbODY)
    request.setHttpMethod(Constants.POSTREQUESTMETHOD)
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" createReport body = ${request.getBody}")
    println(s" createReport headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")
    /*
     report pay load:
    request body: Report(95843b55-7bc6-44a8-ae34-032bf28c0337,Some(MyReport1),Some(Alert Summary Report),,,None,None,None,
    Some(1544518800000),false,Some({"nodeids":["N1","N2","N3"],"alarmtypes":["T1","T2","T3"],"alertstatus":"active",
    "reportcolumns":["nodeid","alarmtype"]}),Set(Users(Some(Lakshman),
    Some(lakshman.kolliapra@hcl.com)), Users(Some(John Wood),Some(john.wood@hcl.com))),Some(1544476359128),Some(1544476359129))
  */

    //assert(response.getStatusCode.equals(200) && reportPayload.orgId.equals(PATHPARAMETERSREQUESTBODY.get("orgId")) &&
    //reportPayload.siteId.equals(PATHPARAMETERSREQUESTBODY.get("siteid")))
    assert(response.getStatusCode.equals(200))
  }


  it should "test for delete report success case" in {

    request.setHttpMethod(Constants.DELETEREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "716bc2a3-094b-4ea7-a3fa-8d1307fdf357")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" delete report body = ${request.getBody}")
    println(s" delete report headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(200) && response.getBody.equals(
      """{"message":"Success: Report """+  PATHPARAMETERSREQUESTBODY.get("reportid")+""" deleted successfully"}"""))
  }

  it should "test for activate report record success case" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "3d3896da-ddfb-4037-b526-83331dac0dc9")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")
    request.setPath("/alert-reports/activate/")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" activate report body = ${request.getBody}")
    println(s" activate report headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(200) && response.getBody.equals("""{"message":"Success: Report """+
      PATHPARAMETERSREQUESTBODY.get("reportid")+""" activated successfully"}"""))
  }

  it should "test for deActivate report record success case" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "0efbb8fc-cd4d-4cd5-9fea-ae0503daad1d")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")
    request.setPath("/alert-reports/deactivate/")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" deActivate report record body = ${request.getBody}")
    println(s" deActivate report record headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(200) && response.getBody.equals("""{"message":"Success: Report """+
      PATHPARAMETERSREQUESTBODY.get("reportid")+""" de-activated successfully"}"""))

  }

  it should "test for get only one report record success case- reportid,orgid,siteid" in {

    request.setHttpMethod(Constants.GETREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "0efbb8fc-cd4d-4cd5-9fea-ae0503daad1d")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" get report record body = ${request.getBody}")
    println(s" get report record  headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")

    assert(response.getStatusCode.equals(200) /*&& reportPayload.orgId.equals(PATHPARAMETERSREQUESTBODY.get("orgId")) &&
      reportPayload.siteId.equals(PATHPARAMETERSREQUESTBODY.get("siteid"))*/)
  }


  it should "test for get all report records success case" in {

    request.setHttpMethod(Constants.GETREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" get all report records body = ${request.getBody}")
    println(s" get all report records headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")

    assert(response.getStatusCode.equals(200) /*&& reportPayload.orgId.equals(PATHPARAMETERSREQUESTBODY.get("orgId")) &&
      reportPayload.siteId.equals(PATHPARAMETERSREQUESTBODY.get("siteid"))*/)
  }

  it should "test for update report record success case" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "0efbb8fc-cd4d-4cd5-9fea-ae0503daad1d")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")

    request.setBody(Constants.UpdateREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" update report record body = ${request.getBody}")
    println(s" update report record headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: RawReport = fromJson[RawReport](response.getBody)

    assert(response.getStatusCode.equals(200))
  }

  it should "test for activate report record failure case - report id not found" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "Dummyreportid")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" activate report body = ${request.getBody}")
    println(s" activate report headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(404) && response.getBody.equals("""{"message":"Error: No Report found for reportId: """ + PATHPARAMETERSREQUESTBODY.get("reportid")+""""}"""))
  }

  it should "test for deActivate report record failure case - report id not found" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "Dummyreportid")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" deActivate report record body = ${request.getBody}")
    println(s" deActivate report record headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(404) && response.getBody.equals("""{"message":"Error: No Report found for reportId: """ + PATHPARAMETERSREQUESTBODY.get("reportid")+""""}"""))
  }

  it should "test for delete report failure case - invalid report id" in {

    request.setHttpMethod(Constants.DELETEREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "DummyReportid")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")

    request.setBody(Constants.EMPTYREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" delete report body = ${request.getBody}")
    println(s" delete report headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    assert(response.getStatusCode.equals(404) && response.getBody.equals("""{"message":"Error: No Report found for reportId: """ + PATHPARAMETERSREQUESTBODY.get("reportid")+""""}"""))
  }

  it should "test for update report record failure case - report id not found" in {

    request.setHttpMethod(Constants.PUTREQUESTMETHOD)

    PATHPARAMETERSREQUESTBODY.put("reportid", "DummyReportid")
    PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
    PATHPARAMETERSREQUESTBODY.put("siteid", "ss")

    request.setBody(Constants.UpdateREQUESTBODY)
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)

    println(s" update report record body = ${request.getBody}")
    println(s" update report record headers = ${request.getHeaders}")

    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: RawReport = fromJson[RawReport](response.getBody)

    assert(response.getStatusCode.equals(404) && response.getBody.equals("""{"message":"Error: No Report found for reportId: """ + PATHPARAMETERSREQUESTBODY.get("reportid")+""""}"""))
  }

  /* it should "test for get only one report record failure case- reportid,orgid,siteid" in {
     request.setHttpMethod(Constants.GETREQUESTMETHOD)
     PATHPARAMETERSREQUESTBODY.put("reportid", "DummyReportid")
     PATHPARAMETERSREQUESTBODY.put("customerid", "cc")
     PATHPARAMETERSREQUESTBODY.put("siteid", "ss")
     request.setBody(Constants.EMPTYREQUESTBODY)
     request.setPathParameters(PATHPARAMETERSREQUESTBODY)
    // println(s" get report record body = ${request.getBody}")
     println(s" get report record  headers = ${request.getHeaders}")
     val response = ProxyRequestMain.handleRequest(request, testContext)
     val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")
     assert(response.getStatusCode.equals(404) && response.getBody.equals("""{"message":"Error: No Report found for reportId: """ + PATHPARAMETERSREQUESTBODY.get("reportid")+""""}"""))
   }*/

  /*it should "test for createReport failure case - path parameters empty" in {
    request.setHeaders(REQUESTHEADER)
    request.setBody(Constants.REQUESTbODY)
    request.setHttpMethod(Constants.POSTREQUESTMETHOD)
   // PATHPARAMETERSREQUESTBODY.put("reportid", "")
    PATHPARAMETERSREQUESTBODY.put("customerid", "")
    PATHPARAMETERSREQUESTBODY.put("siteid", "")
    request.setPathParameters(PATHPARAMETERSREQUESTBODY)
    println(s" createReport body = ${request.getBody}")
    println(s" createReport headers = ${request.getHeaders}")
    val response = ProxyRequestMain.handleRequest(request, testContext)
    val reportPayload: Report = Report.apply(fromJson[RawReport](response.getBody), "", "")
    assert(response.getStatusCode.equals(400) && response.getBody.equals(s"Unable to create Report"))
  }
*/

}