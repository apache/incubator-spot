#!/bin/env python


import os
import subprocess
import sys

class Kerberos(object):

    def __init__(self):

        self._kinit =  os.getenv('KINITPATH')
        self._kinitopts =  os.getenv('KINITOPTS')
        self._keytab =  os.getenv('KEYTABPATH')
        self._krb_user =  os.getenv('KRB_USER')

        if self._kinit == None or self._kinitopts == None or self._keytab == None or self._krb_user == None:
            print "Please verify kerberos configuration, some environment variables are missing."
            sys.exit(1)

        self._kinit_args = [self._kinit,self._kinitopts,self._keytab,self._krb_user]

	def authenticate(self):

		kinit = subprocess.Popen(self._kinit_args, stderr = subprocess.PIPE)
		output,error = kinit.communicate()
		if not kinit.returncode == 0:
			if error:
				print error.rstrip()
				sys.exit(kinit.returncode)
		print "Successfully authenticated!"
