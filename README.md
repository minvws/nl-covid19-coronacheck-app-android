# COVID-19 CoronaCheck Prototype - Android

## Introduction
This repository contains the Android prototype of the Dutch COVID-19 CoronaCheck project.

* The Android app is located in the repository you are currently viewing.
* The iOS app can be found here: https://github.com/minvws/nl-covid19-coronacheck-app-ios

The project is currently an experimental prototype to explore technical possibilities.   

## Development & Contribution process

The development team works on the repository in a private fork (for reasons of compliance with existing processes) and shares its work as often as possible.

If you plan to make non-trivial changes, we recommend to open an issue beforehand where we can discuss your planned changes.
This increases the chance that we might be able to use your contribution (or it avoids doing work if there are reasons why we wouldn't be able to use it).

Note that all commits should be signed using a gpg key.

## Dependencies

This project has at the moment two external dependencies:
- Mobilecore library
- Luhncheck https://github.com/minvws/nl-covid19-coronacheck-app-android/releases

You need to add their .aar files manually in the project to be able to compile.

Instructions for the Mobilecore library can be found at https://github.com/minvws/nl-covid19-coronacheck-app-android/tree/main/mobilecore

The other libraries' .aar files have to placed in the libs folder of the holder module for the CoronaCheck app and in the libs folder of the verifier module for the Scanner app. You can either build their .aar yourself from their repo or grab directly the prebuilt artifacts from their github releases.

