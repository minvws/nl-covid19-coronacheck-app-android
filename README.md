# COVID-19 CoronaCheck - Android

## Introduction

This repository contains the Android release of the Dutch COVID-19 CoronaCheck project.

* The Android app is located in the repository you are currently viewing.
* The iOS app can be found [here](https://github.com/minvws/nl-covid19-coronacheck-app-ios)

See [minvws](https://github.com/minvws)/**[nl-covid19-coronacheck-app-coordination](https://github.com/minvws/nl-covid19-coronacheck-app-coordination)** for further technical documentation.

---

## About the Apps

The codebase was building two different app products:

### [CoronaCheck](https://play.google.com/store/apps/details?id=nl.rijksoverheid.ctr.holder)

**CoronaCheck** (referred to internally as the *Holder* app) was the official app of the Netherlands for showing coronavirus entry passes. With this digital tool, you could create a certificate with QR code of your negative test, vaccination, or recovery. This allowed access to certain venues and activities abroad. Or at the border.

### [Scanner voor CoronaCheck](https://play.google.com/store/apps/details?id=nl.rijksoverheid.ctr.verifier)

**CoronaCheck Scanner** (referred to internally as the *Verifier* app) was the official scanner app of the Netherlands for coronavirus entry passes. With this digital tool, you could verify if visitors have a valid certificate of their negative test, vaccination, or recovery. You did this by scanning their QR code. This way, you could safely give access to your venue or activity.

### App Requirements

The apps can run on devices that meet the following requirements.

* Operating System: Android API 6.0+
* Internet connection

### Feature Overview

#### CoronaCheck

The app does not work anymore, it just opens informing the user about the current deactivated status, with a link to a website offering the last available information for the corona passes.
To check previous features of the app, check out one of the previous releases/tags.

### Third party dependencies

Dependencies management is handled with [Gradle's Version Catalog](https://docs.gradle.org/current/userguide/platforms.html). Find the app's dependencies [here](/gradle/libs.versions.toml)

* [Bouncy castle crypto apis](https://www.bouncycastle.org/java.htmls) for validating data signatures.
* [certificatetransparency-android](https://github.com/appmattus/certificatetransparency): for protection against man-in-the-middle attacks.
* [groupie](https://github.com/lisawray/groupie) for recyclerview layouts
* [koin](https://insert-koin.io/) for dependency injection
* [lottie](https://github.com/airbnb/lottie-ios): natively renders vector-based animations.
* [moshi](https://github.com/krzyzanowskim/OpenSSL) for parsing JSON into Kotlin classes
* [retrofit](https://square.github.io/retrofit/): http client
* [rootbeer](https://github.com/scottyab/rootbeer) for checking if the device got root
* [sqlcipher](https://github.com/sqlcipher/android-database-sqlcipher) for encrypting the database

#### Development only

* [crashlytics](https://firebase.google.com/docs/crashlytics/) crash reporting for internal testing builds
* [spotless](https://github.com/diffplug/spotless) for consistent code formatting
* [timber](https://github.com/JakeWharton/timber) for easier logging
* [versionCatalogUpdate](https://github.com/littlerobots/version-catalog-update-plugin) for keeping up to date the dependencies declared in the [version catalog toml file](/gradle/libs.versions.toml)
* [versions](https://github.com/ben-manes/gradle-versions-plugin) for detecting dependencies updates

#### Testing only

* [barista](https://github.com/AdevintaSpain/Barista) for ui tests assertions
* [junit](https://github.com/junit-team/junit4) for unit testing
* [mockk](https://mockk.io/) for mocking kotlin classes in tests
* [shot](https://github.com/pedrovgs/Shot) for screenshot testing
* [robolectric](https://robolectric.org/) for testing faster components depended on Android framework classes

#### Continuous Integration only

* [firebase-action](https://github.com/littlerobots/firebase-action) for sending builds to firebase for internal testing, setting up credentials via a service account and disabling firebase analytics

### Project structure

CoronaCheck can be built using the `holder` module.

Both come in 5 flavours:

* tst (test environment)
* acc (acceptance environment)
* fdroidAcc (acceptance environment for F-Droid builds, not depending in google play services)
* fdroidProd (production environment for F-Droid builds)
* prod (production environment)

Note that test and acceptance environments are accessible only inside VWS trusted networks.

Other project modules used by both apps:

* [appconfig](/appconfig) remote configuration management
* [design](/design) common styles and components
* [shared](/shared) common models and utility classes

Finally, there are couple of bash and python [scripts](/scripts):

* [Sync the public repo to the private repo](/scripts/sync_public_repo.sh) used by the internal development team.

## Contribution process

The development team used to work on the repository in a private fork (for reasons of compliance with existing processes) and was sharing its work as often as possible.

No development is taking place anymore so contribution is not possible.

Note that all commits were signed using a GPG key.
