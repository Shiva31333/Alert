package com.verizon.netsense.alert_report

import java.util

object Constants {

  val DELETEREQUESTMETHOD = "DELETE"
  val GETREQUESTMETHOD = "GET"
  val PUTREQUESTMETHOD = "PUT"
  val POSTREQUESTMETHOD = "POST"
  val REQUESTHEADERS =
    """
      |"Accept": "*/*",
      |"accept-encoding": "gzip, deflate",
      |"Content-Type": "application/json",
      |"Host": "nsn-lcodev.sensity.com",
      |"User-Agent": "PostmanRuntime/7.4.0",
      |"X-Amzn-Trace-Id": "Root=1-5c05c63d-dbfcfbecfd0be35431fc25b8",
      |"X-Forwarded-For": "68.128.164.93",
      |"X-Forwarded-Port": "443",
      |"X-Forwarded-Proto": "https"
    """.stripMargin

  val EMPTYREQUESTBODY =
    """{
      |}
    """.stripMargin

  val REQUESTbODY =
    """
      |{
      |  "reportName": "MyReport1",
      |  "reportType": "Alert Summary Report",
      |  "timePeriod": "1day",
      |  "active": false,
      |  "recipients": [{"userName": "Lakshman", "userEmail": "lakshman.kolliapra@hcl.com"}, {"userName": "John Wood", "userEmail": "john.wood@hcl.com"}],
      |  "rules": "{\"nodeids\":[\"N1\", \"N2\", \"N3\"],\"alarmtypes\": [\"T1\", \"T2\", \"T3\"],\"alertstatus\": \"active\",\"reportcolumns\": [\"nodeid\", \"alarmtype\"]}"
      |}
    """.stripMargin
  val UpdateREQUESTBODY =
    """
      |{
      |  "reportName": "Updated MyReport",
      |  "reportType": "Alert Summary Report",
      |  "timePeriod": "1day",
      |  "active": false,
      |  "recipients": [{"userName": "Lakshman", "userEmail": "lakshman.kolliapra@hcl.com"}, {"userName": "John Wood", "userEmail": "john.wood@hcl.com"}],
      |  "rules": "{\"nodeids\":[\"N1\", \"N2\", \"N3\"],\"alarmtypes\": [\"T1\", \"T2\", \"T3\"],\"alertstatus\": \"active\",\"reportcolumns\": [\"nodeid\", \"alarmtype\"]}"
      |}
    """.stripMargin
  val REQUESTbODY_backup =
    """
      |"reportId":"12345",
      |"reportType":"Test",
      |"siteId" :"siteId",
      |"orgId":"orgId",
      |"active":"TRUE"
    """.stripMargin
  var REQUESTHEADER: java.util.Map[String, String] = new util.HashMap();
  var PATHPARAMETERSREQUESTBODY: java.util.Map[String, String] = new util.HashMap();

  val RequestString =
    """{
      |    "resource": "/",
      |    "path": "/sensity",
      |    "httpMethod": "POST",
      |    "headers": {
      |        "Accept": "*/*",
      |        "accept-encoding": "gzip, deflate",
      |        "Content-Type": "application/json",
      |        "Host": "nsn-lcodev.sensity.com",
      |        "User-Agent": "PostmanRuntime/7.4.0",
      |        "X-Amzn-Trace-Id": "Root=1-5c05c63d-dbfcfbecfd0be35431fc25b8",
      |        "X-Forwarded-For": "68.128.164.93",
      |        "X-Forwarded-Port": "443",
      |        "X-Forwarded-Proto": "https"
      |    },
      |    "multiValueHeaders": {
      |        "Accept": [
      |            "*/*"
      |        ],
      |        "accept-encoding": [
      |            "gzip, deflate"
      |        ],
      |        "Content-Type": [
      |            "application/json"
      |        ],
      |        "Host": [
      |            "nsn-lcodev.sensity.com"
      |        ],
      |        "User-Agent": [
      |            "PostmanRuntime/7.4.0"
      |        ],
      |        "X-Amzn-Trace-Id": [
      |            "Root=1-5c05c63d-dbfcfbecfd0be35431fc25b8"
      |        ],
      |        "X-Forwarded-For": [
      |            "68.128.164.93"
      |        ],
      |        "X-Forwarded-Port": [
      |            "443"
      |        ],
      |        "X-Forwarded-Proto": [
      |            "https"
      |        ]
      |    },
      |    "queryStringParameters": null,
      |    "multiValueQueryStringParameters": null,
      |    "pathParameters": null,
      |    "stageVariables": null,
      |    "requestContext": {
      |        "resourceId": "gt1hpp84c2",
      |        "resourcePath": "/",
      |        "httpMethod": "POST",
      |        "extendedRequestId": "RWvppGHFvHcFzMQ=",
      |        "requestTime": "04/Dec/2018:00:11:41 +0000",
      |        "path": "/sensity",
      |        "accountId": "173021575303",
      |        "protocol": "HTTP/1.1",
      |        "stage": "lco_dev_test",
      |        "domainPrefix": "nsn-lcodev",
      |        "requestTimeEpoch": 1543882301713,
      |        "requestId": "2d30e891-f759-11e8-9324-b5f0d3b0cbcd",
      |        "identity": {
      |            "cognitoIdentityPoolId": null,
      |            "accountId": null,
      |            "cognitoIdentityId": null,
      |            "caller": null,
      |            "sourceIp": "68.128.164.93",
      |            "accessKey": null,
      |            "cognitoAuthenticationType": null,
      |            "cognitoAuthenticationProvider": null,
      |            "userArn": null,
      |            "userAgent": "PostmanRuntime/7.4.0",
      |            "user": null
      |        },
      |        "domainName": "nsn-lcodev.sensity.com",
      |        "apiId": "5bw3bgmlg3"
      |    },
      |    "body": "{\"reportId\":\"12345\", \"reportType\":\"Test\", \"siteId\":\"siteid1\", \"orgId\": \"orgid1\", \"active\" : \"true\"}",
      |    "isBase64Encoded": false
      |}""".stripMargin
}
