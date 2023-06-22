# Build with the latest stable node version

set -eux

commit_hash="1b03bb722b45970578854290a835e70069147257" # v4.1.5
destination_path="holder/src/main/res/raw/web_pdf_tools.js"
temp_path="tmp-web-pdf-tools"

git clone https://github.com/minvws/nl-covid19-coronacheck-web-pdf-tools.git "$temp_path"

pushd "$temp_path"
git checkout "$commit_hash"
sed -i.bak 's/output: { file: pkg.main },/output: { file: pkg.main, format: "iife", name: "pdfTools" },/g' rollup.config.js
npm install
npm run build
popd

rm -f "$destination_path"
cp "${temp_path}/dist/index.js" "$destination_path"
rm -rf "$temp_path"
