# How to build mobilecore.aar

## Prerequisites

This project depends on the mobilecore go project, which is a submodule of this repository. The
submodule should be initialised using `git submodule init` and `git submodule update`.
To build the library make sure you have go installed and added `$GOPATH/bin` to your path.

To build `mobilecore.aar` run `./gradlew mobilecore:buildCore` since the library is
(no longer) included in this repository this has to be done before the project can be synced.

## Updating mobilecore

Update the `mobilecore` submodule and check out the appropriate mobilecore version tag. Be sure to
commit `mobilecore` from the root project to ensure this version is recorded when the submodule is
checked out. Then run`./gradew mobilecore:buildCore` from the project root to (re)build the aar.





