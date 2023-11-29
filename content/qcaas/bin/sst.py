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
import sys
import copy
import json
import math
import random

min_questionable = 10
min_acceptable = 20
max_acceptable = 30
max_questionable = 40

missing_value = -999.
args
FLAG_GOOD = '2'
FLAG_QUESTIONABLE = '3'
FLAG_BAD = '4'
FLAG_MISSING = '9'

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
    print(f'value: {value}',file=sys.stderr)
    vf=double(value)
    if math.isnan(vf) or missing_value == value:
        print("isnan",file=sys.stderr)
        return FLAG_MISSING
    if vf < min_acceptable or vf > max_min_acceptable:
        print(f'questionable: {value}',file=sys.stderr)
        return FLAG_QUESTIONABLE
    if vf < min_questionable or vf > max_questionable:
        print(f'not acceptable: {value}',file=sys.stderr)
        return FLAG_BAD
    return FLAG_GOOD #random.randint(0,8)

def do_qc(qcrequest):
    qcresponse = {}
    qcresponse['request_id'] = qcrequest['request_id']
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
        qcrequest = read_file(filename)
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
    
