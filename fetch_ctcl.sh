git clone git@github.com:minvws/nl-covid19-coronatester-ctcl-core-private.git tmp-ctcl
cd tmp-ctcl
git checkout app-integration
gomobile bind -target android -o ctcl.aar github.com/minvws/nl-covid19-coronatester-ctcl-core/clmobile
cd ../
cp tmp-ctcl/ctcl.aar app/libs 
rm -rf tmp-ctcl