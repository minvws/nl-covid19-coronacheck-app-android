import requests, zipfile
from io import BytesIO
import os
import lokalise

if os.getenv('CORONACHECK_LOKALISE_TOKEN') is not None:
	apikey = os.getenv('CORONACHECK_LOKALISE_TOKEN')

	print("Initializing Lokalise client...")
	client = lokalise.Client(apikey)

	# Download holder copy
	print("Downloading holder copy...")
	response = client.download_files('5229025261717f4fcb81c1.73606773', {
		"format": "xml",
		"original_filenames": True,
		"replace_breaks": False,
		"filter_langs" : ["en","nl"],
		"export_sort" : "first_added"
	})
	req = requests.get(response['bundle_url'])
	# Extract copy from zip
	print("Extracting holder copy...")
	zipHolder = zipfile.ZipFile(BytesIO(req.content))
	zipHolder.extractall('../holder/src/main/res/')
	print("Finished downloading holder copy!")
	print()

	# Download verifier copy
	print("Downloading verifier copy...")
	response = client.download_files('243601816196631318a279.00348152', {
		"format": "xml",
		"original_filenames": True,
		"replace_breaks": False,
		"filter_langs" : ["en","nl"],
		"export_sort" : "first_added"
	})
	req = requests.get(response['bundle_url'])
	# Extract copy from zip
	print("Extracting verifier copy...")
	zipVerifier = zipfile.ZipFile(BytesIO(req.content))
	zipVerifier.extractall('../verifier/src/main/res/')
	print("Finished verifier copy!")
	print()

	print("All done!")
else:
	print("Please set CORONACHECK_LOKALISE_TOKEN")

