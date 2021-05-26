import requests
import time
import hashlib 
import os

if os.getenv('CORONACHECK_ONESKY_SECRET') is not None:
	publickey = "dyCXIZ0qBOqSaGiyqHJkcXdiDO7LPAJS"
	secretkey = os.getenv('CORONACHECK_ONESKY_SECRET')
	timestamp = time.time()
	devhash = hashlib.md5((str(timestamp) + secretkey).encode('utf-8')).hexdigest()

	print("Uploading NL copy for Holder")
	r = requests.post("https://platform.api.onesky.io/1/projects/380524/files", data = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "nl", "file_format": "ANDROID_XML", "is_keeping_all_strings": "false"} , files = {'file': open('../holder/src/main/res/values/strings.xml','rb')})

	print("Uploading EN copy for Holder")
	r = requests.post("https://platform.api.onesky.io/1/projects/380524/files", data = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "en", "file_format": "ANDROID_XML", "is_keeping_all_strings": "false"} , files = {'file': open('../holder/src/main/res/values-en/strings.xml','rb')})

	print("Uploading NL copy for Verifier")
	r = requests.post("https://platform.api.onesky.io/1/projects/380530/files", data = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "nl", "file_format": "ANDROID_XML", "is_keeping_all_strings": "false"}, files = {'file': open('../verifier/src/main/res/values/strings.xml','rb')})

	print("Uploading EN copy for Verifier")
	r = requests.post("https://platform.api.onesky.io/1/projects/380530/files", data = {'api_key': publickey, 'timestamp': timestamp, 'dev_hash': devhash, 'locale': "en", "file_format": "ANDROID_XML", "is_keeping_all_strings": "false"}, files = {'file': open('../verifier/src/main/res/values-en/strings.xml','rb')})

	print("Finished uploading copy")
else:
	print("Please set CORONACHECK_ONESKY_SECRET environment variable")