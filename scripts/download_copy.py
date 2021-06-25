import requests
import time
import hashlib 
import os

if os.getenv('CORONACHECK_ONESKY_SECRET') is not None and os.getenv('CORONACHECK_ONESKY_PUBLIC') is not None:
	publickey = os.getenv('CORONACHECK_ONESKY_PUBLIC')
	secretkey = os.getenv('CORONACHECK_ONESKY_SECRET')
	timestamp = time.time()
	devhash = hashlib.md5((str(timestamp) + secretkey).encode('utf-8')).hexdigest()

	print("Downloading NL copy for Holder")
	r = requests.get("https://platform.api.onesky.io/1/projects/380524/translations", params = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "nl", "source_file_name": "strings.xml", "export_file_name": "strings.xml"})
	open('../holder/src/main/res/values/strings.xml', 'wb').write(r.content)

	print("Downloading EN copy for Holder")
	r = requests.get("https://platform.api.onesky.io/1/projects/380524/translations", params = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "en", "source_file_name": "strings.xml", "export_file_name": "strings.xml"})
	open('../holder/src/main/res/values-en/strings.xml', 'wb').write(r.content)

	print("Downloading NL copy for Verifier")
	r = requests.get("https://platform.api.onesky.io/1/projects/380530/translations", params = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "nl", "source_file_name": "strings.xml", "export_file_name": "strings.xml"})
	open('../verifier/src/main/res/values/strings.xml', 'wb').write(r.content)

	print("Downloading EN copy for Verifier")
	r = requests.get("https://platform.api.onesky.io/1/projects/380530/translations", params = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "en", "source_file_name": "strings.xml", "export_file_name": "strings.xml"})
	open('../verifier/src/main/res/values-en/strings.xml', 'wb').write(r.content)

	print("Finished downloading copy")
else:
	print("Please set CORONACHECK_ONESKY_SECRET and CORONACHECK_ONESKY_PUBLIC environment variable")