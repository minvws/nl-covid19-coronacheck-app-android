import requests, zipfile
from io import BytesIO
import os

try:
	import lokalise
except ModuleNotFoundError:
	print("\033[91mYou forgot to install the Lokalise library. Run 'pip install python-lokalise-api' first")
	exit()


if os.getenv('LOKALISE_API_KEY') is not None:
	apikey = os.getenv('LOKALISE_API_KEY')

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

	print("\033[92mAll done!")
else:
	print("\033[91mPlease set LOKALISE_API_KEY before running the script again. ")
	print("export LOKALISE_API_KEY=......")

