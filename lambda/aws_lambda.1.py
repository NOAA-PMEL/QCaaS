import os
import sys
import copy
import json
import random

ranges = {}
sst = {
    "min_questionable" : -2.0,
    "min_acceptable" : -1.0,
    "max_acceptable" : 32.0,
    "max_questionable" : 38.0
}

min_questionable=42.0
min_acceptable=44.0
max_acceptable=48.0
max_questionable=50.0

def lambda_handler(event, context):
    print(f"event: {event}")
    if 'body' in event.keys():
        request = event['body']
    else:
        request = event
    print("request: ", type(request), request)
    if request and type(request) is str:
        print("converting to json")
        request = json.loads(request)
        print("json:", request)
    print("request: ", type(request), request)
    if not check_request(request):
        return {
            'statusCode': 400,
            'body': '{"request":' + json.dumps(request) + '}'
        }
    the_response = do_qc(request)
    print("the_response:", type(the_response), the_response)
    return {
        'statusCode': 200,
        'body': json.dumps(the_response)
    }

def check_request(checkRequest):
    if not checkRequest:
        print(f"No request: {checkRequest}")
        return False
    print("checkRequest: ", type(checkRequest), checkRequest)
    if not ( 'request_id' in checkRequest and
             'data' in checkRequest ):
        print("missing fields")
        return False
    data = checkRequest['data']
    if not ( 'data_variables' in data and
             'rows' in data ):
        print("No data")
        return False
    return True

def read_input():
    print(f'Reading JSON from stdin', file=sys.stderr)
    return json.load(sys.stdin)

def read_file(filename):
    print(f'Reading from {filename}', file=sys.stderr)
    f = open(filename)
    return json.load(f)

def write_file(flags, filename):
    print(f'Writing file {filename}', file=sys.stderr)
    with open(filename, 'w') as f:
        json.dump(flags, f)

def get_flag(var, value):
    fval = float(value)
    flag=2
    if not value:
        flag=9
    elif fval < min_acceptable or fval > max_acceptable:
        flag=3
    elif fval < min_questionable or fval > max_questionable:
        flag=4
    return flag

def do_qc(qcrequest):
    print("qcrequest type:", type(qcrequest), qcrequest)
    if type(qcrequest) is str:
        print("converting to json")
        qcrequest = json.loads(qcrequest)
        print("json:", qcrequest)
    qcresponse = {}
    qcresponse['request_id'] = qcrequest['request_id']
    req_data = qcrequest['data']
    print("req_data type:", type(req_data), req_data)
    vars = req_data['data_variables']
    supl = req_data['supplemental_variables']
    flag_vars = copy.deepcopy(vars)
    # if data['supplemental_variables']:
        # flag_vars.extend(data['supplemental_variables'])
    for var in flag_vars:
        qc_var = var # copy.deepcopy(var)
        varname = qc_var['standard_name']['name']
        qc_flagname = varname + "_QC"
        qc_var['standard_name']['name'] = qc_flagname
        # flag_vars.append(qc_var)
    rows = req_data['rows']
    for row in rows:
        idx = 0
        values = row['values']
        for var in vars:
            value = values[idx]
            flag = get_flag(var, value)
            values.append(flag)
    resp_data = {}
    resp_data['data_variables'] = vars
    resp_data['supplemental_variables'] = supl
    resp_data['flag_variables'] = flag_vars
    resp_data['rows'] = rows
    qcresponse['data'] = resp_data
    return qcresponse


