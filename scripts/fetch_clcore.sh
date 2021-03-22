cd ../
git clone git@github.com:minvws/nl-covid19-coronacheck-cl-core-private tmp-clcore
cd tmp-clcore
gomobile bind -target android -o clcore.aar github.com/minvws/nl-covid19-coronacheck-cl-core/clmobile
cd ../
cp tmp-clcore/clcore.aar clcore
rm -rf tmp-clcore