import json
import qaws
  
def lambda_handler(event, context):
    print("event:"+json.dumps(event))
    print(f"context: {context}")
    msg = 'Hello from Lambda!'
    if 'body' in event:
        headers = event['headers']
        requestFrom = headers['X-Forwarded-For']
        host = headers['Host']
        reqBody = json.loads(event['body'])
        print('body:' + json.dumps(reqBody))
        qcresponse = qaws.do_qc(reqBody)
        msg = 'Hello to ' + requestFrom
        msg += ' from ' + host
        msg += ' regarding request_id:' + reqBody['request_id']
        print(msg)
    return {
        'statusCode': 200,
        'body': json.dumps(qcresponse)
    }

