import sys
import json

if __name__ == '__main__':
    
    eventStr = """
{
    "version": "1.0",
    "resource": "/myLambdaPy2",
    "path": "/default/myLambdaPy2",
    "httpMethod": "POST",
    "headers": {
        "Content-Length": "21",
        "Content-Type": "application/json",
        "Host": "zoe5ltsc1d.execute-api.us-west-1.amazonaws.com",
        "User-Agent": "curl/7.64.1",
        "X-Amzn-Trace-Id": "Root=1-61e847b1-494cd38678b2377539b0a228",
        "X-Forwarded-For": "161.55.46.139",
        "X-Forwarded-Port": "443",
        "X-Forwarded-Proto": "https",
        "accept": "*/*"
    },
    "multiValueHeaders": {
        "Content-Length": [
            "21"
        ],
        "Content-Type": [
            "application/json"
        ],
        "Host": [
            "zoe5ltsc1d.execute-api.us-west-1.amazonaws.com"
        ],
        "User-Agent": [
            "curl/7.64.1"
        ],
        "X-Amzn-Trace-Id": [
            "Root=1-61e847b1-494cd38678b2377539b0a228"
        ],
        "X-Forwarded-For": [
            "161.55.46.139"
        ],
        "X-Forwarded-Port": [
            "443"
        ],
        "X-Forwarded-Proto": [
            "https"
        ],
        "accept": [
            "*/*"
        ]
    },
    "queryStringParameters": null,
    "multiValueQueryStringParameters": null,
    "requestContext": {
        "accountId": "928198976571",
        "apiId": "zoe5ltsc1d",
        "domainName": "zoe5ltsc1d.execute-api.us-west-1.amazonaws.com",
        "domainPrefix": "zoe5ltsc1d",
        "extendedRequestId": "MNAjwjsdSK4EMPw=",
        "httpMethod": "POST",
        "identity": {
            "accessKey": null,
            "accountId": null,
            "caller": null,
            "cognitoAmr": null,
            "cognitoAuthenticationProvider": null,
            "cognitoAuthenticationType": null,
            "cognitoIdentityId": null,
            "cognitoIdentityPoolId": null,
            "principalOrgId": null,
            "sourceIp": "161.55.46.139",
            "user": null,
            "userAgent": "curl/7.64.1",
            "userArn": null
        },
        "path": "/default/myLambdaPy2",
        "protocol": "HTTP/1.1",
        "requestId": "MNAjwjsdSK4EMPw=",
        "requestTime": "19/Jan/2022:17:17:37 +0000",
        "requestTimeEpoch": 1642612657424,
        "resourceId": "ANY /myLambdaPy2",
        "resourcePath": "/myLambdaPy2",
        "stage": "default"
    },
    "pathParameters": null,
    "stageVariables": null,
    "body": "{\"request_id\":\"1639413797239\",\"data\":{\"variables\":[{\"standard_name\":{\"name\":\"CTDPRS\",\"vocabulary\":null}},{\"standard_name\":{\"name\":\"SALNTY\",\"vocabulary\":null}},{\"standard_name\":{\"name\":\"CTDOXY\",\"vocabulary\":null}}],\"rows\":[{\"values\":[\"      3.1\",\"  35.6527\",\"    194.1\"]},{\"values\":[\"     20.1\",\"  35.6570\",\"    195.7\"]},{\"values\":[\"     41.7\",\"  35.7167\",\"    195.6\"]},{\"values\":[\"     66.4\",\"  35.8540\",\"    204.5\"]},{\"values\":[\"     91.6\",\"  36.1715\",\"    196.9\"]},{\"values\":[\"    117.0\",\"  36.1159\",\"    186.9\"]},{\"values\":[\"    158.0\",\"  35.9876\",\"    179.0\"]},{\"values\":[\"    224.9\",\"  35.7616\",\"    190.6\"]},{\"values\":[\"    300.7\",\"  35.4747\",\"    187.6\"]},{\"values\":[\"    375.2\",\"  35.0739\",\"    164.8\"]},{\"values\":[\"    465.9\",\"  34.5756\",\"    147.2\"]},{\"values\":[\"    599.8\",\"  34.4415\",\"    140.8\"]},{\"values\":[\"    749.5\",\"  34.4651\",\"    137.5\"]},{\"values\":[\"    900.3\",\"  34.4824\",\"    140.0\"]},{\"values\":[\"   1049.6\",\"-999.0000\",\"    137.9\"]},{\"values\":[\"   1232.1\",\"  34.5468\",\"    140.0\"]}]}}",
    "isBase64Encoded": false
}
    
    """
    eventJson = json.loads(eventStr)
    print(eventJson)
    