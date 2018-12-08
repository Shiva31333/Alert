package com.verizon.netsense.alert_report

import com.verizon.netsense.alert_report.utils.ObjectMapperUtil

import org.scalatest.{BeforeAndAfterEach, FunSuite}

class ProxyRequestMainTest extends FunSuite with BeforeAndAfterEach {


  private var subject = null
  private var testContext = null


  test("testHandleRequest") {


    val str = """{
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



    var inputStream = new java.io.ByteArrayInputStream(str.getBytes)





    val request = Handler.convertInputStreamToReq(inputStream)

    println(s"body = ${request.getBody}")
    println(s"headers = ${request.getHeaders}")


    val getProxyResponse = ProxyRequestMain.handleRequest(request, testContext)

  }



}