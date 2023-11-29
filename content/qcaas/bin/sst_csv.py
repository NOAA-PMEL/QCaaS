#!/usr/bin/python3
# This allows the script to be run (at least on unix-based systems) stand-alone
# You may have to change this for your system.

# QCaaS test/example script
# Does random 0-9 flags for each specified data variable
# This script can read the JSON QcInvocationRequest either from StdIn 
# or from named file using -f flag
# Will write to StdOut or to an output file specified using the -o flag
#
# Note that I am just learning python...

import os
import csv
import sys
import copy
import json
import math
import random

from collections import OrderedDict 


min_acceptable = 10
min_questionable = 20
max_questionable = 30
max_acceptable = 40

def read_input():
    print(f'Reading JSON from stdin', file=sys.stderr)
    return json.load(sys.stdin)

def read_csv_file(filename, has_units=False):
    print(f'Reading from {filename}', file=sys.stderr)
    f = open(filename, mode='r')
    csv_reader = csv.DictReader(f)
    headers = csv_reader.fieldnames
    rows = []
    row_count = 0;
    for row in csv_reader:
        if row_count == 0:
            if looks_like_units(row,headers):
                units = row
            else:
                (units,headers) = pull_units_from_headers(headers)
                rows.append(row)
        else:
            rows.append(row)
        row_count += 1
        
    request = {}
    request["headers"] = headers
    request["units"] = units
    request["data"] = rows
    return request

def looks_like_units(row, headers):
    for header in headers:
        value = row[header]
        try:
            float(value)
            return False
        except:
            continue
    return True    
    
def pull_units_from_headers(headers):
    trimmed_headers = []
    units = OrderedDict()
    for header in headers:
        idx = header.index('_')
        if idx <= 0:
            raise Exception("bad unit header: " + header)
        trimmed = header[0:idx]
        unit = header[idx+1:]
        units[trimmed] = unit
        trimmed_headers.append(trimmed)
    return (units, trimmed_headers)

def write_file(flags, filename):
    print(f'Writing file {filename}', file=sys.stderr)
    with open(filename, 'w') as f:
        json.dump(flags, f)
    
def get_flag(var, value):
    print(f'value: {value}',file=sys.stderr)
    vf=float(value)
    if math.isnan(vf):
        print("isnan",file=sys.stderr)
        return 9
    if vf < min_questionable or vf > max_questionable:
        print(f'questionable: {value}',file=sys.stderr)
        return 3
    if vf < min_acceptable or vf > max_acceptable:
        print(f'not acceptable: {value}',file=sys.stderr)
        return 4
    return 2 # random.randint(0,8)

def do_qc(qcrequest):
    qcresponse = {}
    qcresponse['request_id'] = 'testing'
    req_data = qcrequest['data']
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
    for i in range(1, len(rows)): # skip units row.
    # for row in rows:
        row = rows[i]
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

if __name__ == '__main__':
    
    print('file:'+os.path.dirname(os.path.abspath(__file__)), file=sys.stderr)
    print('cwd:'+os.path.abspath(os.getcwd()), file=sys.stderr)
    
#     print('QcPy')
    args = sys.argv
    if '-f' in args:
        idx = args.index('-f') + 1
        if idx >= len(args):
            raise Exception("No file specified.") 
        filename = args[idx]
        print(f'reading from file: {filename}', file=sys.stderr)
        qcrequest = read_csv_file(filename)
    else:
        qcrequest = read_input()
#     print(qcrequest)
    
    qcresponse = do_qc(qcrequest)
    if '-o' in args:
        idx = args.index('-o') + 1
        if idx >= len(args):
            raise Exception("No file specified.") 
        outfile = args[idx]
        print(f'writing to file: {outfile}', file=sys.stderr)
        write_file(qcresponse, outfile)
    else:
        print(json.dumps(qcresponse))
    
