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

class range_test(qc_base):
    
    def __init__(self, args,
                       min_acceptable, min_questionable = min_acceptable, 
                       max_questionable = max_acceptable, max_acceptable):
        self.min_acceptable = min_acceptable
        self.min_questionable = min_questionable
        self.max_questionable = max_questionable
        self.max_acceptable = max_acceptable
        
    def get_flag(var, value):
        print(f'value: {value}',file=sys.stderr)
        vf=float(value)
        print(f'vf: {vf}',file=sys.stderr)
        if math.isnan(vf):
            print("isnan",file=sys.stderr)
            return 9
        return random.randint(0,8)
    
if __name__ == '__main__':
    
    print('file:'+os.path.dirname(os.path.abspath(__file__)), file=sys.stderr)
    print('cwd:'+os.path.abspath(os.getcwd()), file=sys.stderr)
    
    print('range_test', file=sys.stderr)
    args = sys.argv
    # min_acceptable -mna
    # min_questionable -mnq || min_acceptable
    # max_questionable -mxq || max_acceptable
    # max_acceptable -mxa
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
    
