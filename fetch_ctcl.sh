git clone git@github.com:minvws/nl-covid19-coronatester-ctcl-core.git tmp-ctcl
cd tmp-ctcl
gomobile bind -target android -o ctcl.aar github.com/minvws/nl-covid19-coronatester-ctcl-core/clmobile
cd ../
cp tmp-ctcl/ctcl.aar shared/libs 
rm -rf tmp-ctcl