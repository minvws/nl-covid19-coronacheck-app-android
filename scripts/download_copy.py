import requests
import time
import hashlib 
import os

if os.getenv('CORONACHECK_ONESKY_SECRET') is not None:
	publickey = "dyCXIZ0qBOqSaGiyqHJkcXdiDO7LPAJS"
	secretkey = os.getenv('CORONACHECK_ONESKY_SECRET')
	timestamp = time.time()
	devhash = hashlib.md5((str(timestamp) + secretkey).encode('utf-8')).hexdigest()
	payload = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "nl", "source_file_name": "strings.xml", "export_file_name": "strings.xml"} 

	print("Downloading copy for Holder")
	r = requests.get("https://platform.api.onesky.io/1/projects/380524/translations", params = payload)
	open('../holder/src/main/res/values/strings.xml', 'wb').write(r.content)

	print("Downloading copy for Verifier")
	r = requests.get("https://platform.api.onesky.io/1/projects/380530/translations", params = payload)
	open('../verifier/src/main/res/values/strings.xml', 'wb').write(r.content)

	print("Finished downloading copy")
else:
	print("Please set CORONACHECK_ONESKY_SECRET environment variable")