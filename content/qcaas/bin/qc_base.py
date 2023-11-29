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
import random

def has_arg(args, flag):
    return flag in args
    
def get_arg(args, flag):
    idx = args.index(flag) + 1
    if idx >= len(args):
        raise Exception("No argument provided for " + flag) 
    return args[idx]
    
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
        
class qc_base:
    
    def __init__(self, args):
        self.qcrequest = \
            read_file(get_arg(args, '-f')) \
            if ( has_arg(args, '-f')) \
            else read_input()
        
    def get_flag(self, var, value):
        return random.randint(0,9)
    
    def do_qc(self):
        qcresponse = {}
        qcresponse['request_id'] = self.qcrequest['request_id']
        req_data = self.qcrequest['data']
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
                flag = self.get_flag(var, value)
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
    qc = qc_base(args)
    
    qcresponse = qc.do_qc()
    if '-o' in args:
        idx = args.index('-o') + 1
        if idx >= len(args):
            raise Exception("No file specified.") 
        outfile = args[idx]
        print(f'writing to file: {outfile}', file=sys.stderr)
        write_file(qcresponse, outfile)
    else:
        print(json.dumps(qcresponse))
    
