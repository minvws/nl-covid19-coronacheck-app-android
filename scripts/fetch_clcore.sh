set -eux
cd ../
git clone git@github.com:minvws/nl-covid19-coronacheck-mobile-core-private.git tmp-mobilecore
cd tmp-mobilecore
git checkout v0.4.5
git submodule init
git submodule update
go get -d golang.org/x/mobile/cmd/gobind@latest
gomobile init
gomobile bind -target android -o mobilecore.aar github.com/minvws/nl-covid19-coronacheck-mobile-core
cd ../
cp tmp-mobilecore/mobilecore.aar mobilecore
rm -rf tmp-mobilecore