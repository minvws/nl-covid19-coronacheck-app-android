# COVID-19 CoronaCheck - Android

## Introduction

This repository contains the Android release of the Dutch COVID-19 CoronaCheck project.

* The Android app is located in the repository you are currently viewing.
* The iOS app can be found [here](https://github.com/minvws/nl-covid19-coronacheck-app-ios)

See minvws/**[nl-covid19-coronacheck-app-coordination](https://github.com/minvws/nl-covid19-coronacheck-app-coordination)** for further technical documentation.

---

## About the Apps

The codebase builds two different app products:

### [CoronaCheck](https://play.google.com/store/apps/details?id=nl.rijksoverheid.ctr.holder)

**CoronaCheck** (referred to internally as the *Holder* app) is the official app of the Netherlands for showing coronavirus entry passes. With this digital tool, you can create a certificate with QR code of your negative test, vaccination, or recovery. This allows access to certain venues and activities abroad. Or at the border.

### [Scanner voor CoronaCheck](https://play.google.com/store/apps/details?id=nl.rijksoverheid.ctr.verifier)

**CoronaCheck Scanner** (referred to internally as the *Verifier* app) is the official scanner app of the Netherlands for coronavirus entry passes. With this digital tool, you can verify if visitors have a valid certificate of their negative test, vaccination, or recovery. You do this by scanning their QR code. This way, you can safely give access to your venue or activity.

### App Requirements

The apps can run on devices that meet the following requirements.

- Operating System: Android API 6.0+
- Internet connection

### Feature Overview

#### CoronaCheck

The app works like this:

*First make sure you are vaccinated or have tested for coronavirus.*

- With the CoronaCheck app you can create a certificate based on your vaccination or coronavirus test results. You do this by retrieving your details via DigiD or via the retrieval code you received if you got tested at a test location other than the GGD.

- You can create a vaccination certificate from your vaccination, create a test certificate from your negative coronavirus test result, or create a recovery certificate from your positive coronavirus test result.

- The QR code of your certificate may be checked at the entrance of venues and activities. And also at international borders. This is proof that you have been vaccinated, have had coronavirus or did not have coronavirus at the time of testing

This is a general overview of the features that are available in the app:

* **Onboarding**: When the app starts or the first time, the user is informed of the functionality of the app and views the privacy declaration.

* **Dashboard**: an overview of the user's certificates. Depending on the active [disclosure policy](#disclosure-policies), there can a tab switcher to change between Domestic and International certificates, or else it's hidden and the user can only view International certificates.

* **View QR codes**: View the QR code(s) for a selected certificate.

* **Fuzzy-matching**: For when a user has multiple names in the app, which the backend no longer permits. Before the implementation of server-side fuzzy matching they existed together, but now there needs to be a way for the user to choose which name permutations (+ associated events) to keep, and which to discard. This feature also has an "onboarding" flow explaining the choice the user has to make. This feature is presented to the user as-needed, and is not otherwise accessible.

* **Menu**:

  * **Add Certificate**:

    * Vaccination: can be retrieved via authentication with DigiD
    * Positive Test: can be retrieved via authentication with DigiD
    * Negative Test: can be added via authentication with DigiD or by entering a retrieval code from a third-party test provider.

    For those without a DigiD account, the user can request a certificate from the CoronaCheck Helpdesk or (if the user doesn't have a BSN) directly from the GGD.

  * **Scan to add certificate**: the user can import a paper copy of a various types of certificate by scanning it using the phone's camera.

  * **Add visitor pass**: for users who were vaccinated outside the EU and are visiting the Netherlands, they can obtain a "vaccine approval" code and use it - together with a negative test - to create a visitor pass in the app.

  * **Frequently asked questions**: a webview

  * **About this app**:

    * Privacy statement: a webview
    * Accessibility: a webview
    * Colophon: a webview
    * Stored data: shows the event data imported from GGD, RIVM, from commerical test providers, or scanned manually by the user. They can be deleted from here.
    * Reset the app: wipes the app's encrypted database and user preferences, restoring the app to a "first-run" state. The user has to manually start the app afterwards.

    To aid in development/testing of the app, there are some extra menu items when compiling for Test/Acceptance:

    * ***Open Scanner**: open the CoronaCheck Scanner app via a universal-link*

    * ***A list of disclosure policies** (1G, 3G, etc) which can be manually activated to override the remote configuration disclosure policy.*

##### Disclosure policies

Depending on the active disclosure policy (which is set by the remote config), the Dutch certificates are handled differently in the app:

* **0G**: no Dutch certificates displayed, only international certificates.

* **1G** access: the user can only use a negative test to enter places which require a coronavirus pass. The app only displays the QRs of negative test certificates.
* **3G** access: the user can enter anywhere (that requires a coronavirus pass) with a proof of vaccination, recovery, or a negative test. So all certificates are available.
* **1G + 3G**: some venues are operating with 1G rules, others with 3G. Thus the app displays separate certificates for 3G and for 1G access.

#### CoronaCheck Scanner

The app works like this:

- With CoronaCheck Scanner you can scan visitors' QR codes with your smartphone
  camera. Visitors can show their QR code in the CoronaCheck app, or on paper. Tourists can
  use an app or a printed QR code from their own country.
- A number of details appear on your screen, allowing you to verify - using their proof of
  identity - if the QR code really belongs to this visitor.
- If the QR code is valid, and the details are the same as on the proof of identity, a check
  mark will appear on the screen and you can give access to the visitor.

This is how the app uses personal details:

* Visitors' details may only be used to verify the coronavirus entry pass

* Visitors' details are not centrally stored anywhere
* Visitors' location details are neither used nor saved

This is a general overview of the features that are available in the app:

* **Onboarding**: When the app starts or the first time, the user is informed of the purpose of the app and accepts the Acceptable Use Policy.
* **Landing screen**: explains briefly what the app does.
* **"About Scanning" onboarding**: informs the user how to scan & verify a certificate.
* **New Policy screen**: explains the current disclosure policy
* **Scan QR code**: camera view which allows the user to scan a QR code. The device's flashlight can be toggled.
* **Result screen**: shows a green checkmark or a red cross depending on the result of the scan.

* **Menu**:
  * **How it works**: replay the "About Scanning" onboarding.
  * **Support**: opens a webview.
  * **Scan Setting**: allows the user to choose the active verification policy (feature is only available when multiple verification policies are permitted by the remote config).
  * **About this app:**
    * Acceptable use policy: opens a webview
    * Accessibility: a webview
    * Colophon: a webview
    * Reset the app: wipes the database, user preferences and keychain entries, restoring the app to a "first-run" state. This is only available when compiling for Test/Acceptance.
    * Scan setting log: keeps track of the type of access used during scanning. A civil enforcement officer may request access to this log. For privacy reasons, only scan settings used in the last 60 minutes are saved on this phone. No personal information is saved. (feature is only available when multiple verification policies are permitted by the remote config).

##### Verification Policies

Related to Disclosure Policies above, but for the Scanner. The verification policy set (by the remote configuration) determines which types of QR codes can be scanned.

* **1G** access: the scanner can only approve QR codes representing valid negative tests.
* **3G** access: the scanner can approve QR codes representing valid vaccination, recovery, and negative tests.
* **1G + 3G**: some venues are operating with 1G rules, others with 3G. The scanner can be set to 1G or 3G mode, depending on the venue where it's intended to be used.

### Remote Configuration

Feature flags / configuration values are loaded dynamically from the backend ([CoronaCheck](https://holder-api.coronacheck.nl/v8/holder/config), [CoronaCheck Scanner](https://holder-api.coronacheck.nl/v8/verifier/config)). The `payload` value is base64 encoded.

*Note: the API is versioned: /v8/, /v9/ etc. Check the [holder build.gradle](/holder/build.gradle) and the [scanner build gradle](/verifier/build.gradle) for the currently used version.*

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
* [fladle](https://runningcode.github.io/fladle/) gradle plugin for Firebase test lab and flank
* [flank](https://github.com/Flank/flank) for massively running ui tests on Firebase test lab

### In-house dependencies

#### CLCore

The Android and iOS apps share a [core library](https://github.com/minvws/nl-covid19-coronacheck-mobile-core), written in Go, which is responsible for producing the QR-code image, and for validating scanned QR-codes.
Build its .aar according to the [instructions](/mobilecore)
The library is also built by the [project's github actions workflow](/.github/workflows/ci.yml), to be transparent it is used uncompromised

#### RDO modules

* [rdo modules](https://github.com/minvws/nl-rdo-app-android-modules): modules that were extracted from CoronaCheck for reuse.

## Development

### Getting started

Like mentioned above, the app is dependent on a library written in Go. An artifact named `mobilecore.aar` must be present [in this path](/mobilecore) in order for the project to compile.
The project is also dependent to [rdo modules](https://github.com/minvws/nl-rdo-app-android-modules) in order to compile.
All of them are added as git submodules.

Steps to add them:
1. `git submodule init`
2. `git submodule update` 
3. [Install latest go](https://go.dev/doc/install)
4. `./gradlew :mobilecore:buildCore`

### Project structure

CoronaCheck can be built using the `holder` module. 
Scanner can be built using the `verifier` module.

Both come in 5 flavours:

- tst (test environment)
- acc (acceptance environment)
- fdroidAcc (acceptance environment for fdroid builds, not depending in google play services)
- fdroidProd (production environment for fdroid builds)
- prod (production environment)

Note that test and acceptance environments are accessible only inside VWS trusted networks.

Other project modules used by both apps:
- [api](/api) http client setup
- [appconfig](/appconfig) remote configuration management
- [design](/design) common styles and components
- [introduction](/introduction) onboarding and privacy consent components
- [qrscanner](/qrscanner) camera scanning qr codes
- [shared](/shared) common models and utility classes

Finally, there are couple of bash and python [scripts](/scripts):
- The original development team used [Lokalise](https://lokalise.com/) to manage the dutch and english copies of the apps. A [download script](/scripts/download_copy.py) is used to sync the local copies to the remote ones in Lokalise and an [upload script](/scripts/upload_copy.py) can be used to sync the remote ones to the local ones. To use them add an environment variable named `LOKALISE_API_KEY` with value a [lokalise api token](https://docs.lokalise.com/en/articles/1929556-api-tokens)
- [Update the current screenshots](/scripts/record_screenshots.sh) used in the CoronaCheck screenshot testing [folder](/holder/screenshots)
- [Sync the public repo to the private repo](/scripts/sync_public_repo.sh) used by the internal development team

## Release Procedure

The release process is the same for CoronaCheck and for CoronaCheck Scanner.

`apk` and `aab` artifacts are generated by github actions every time `main` or `release/*` branches have a new commit.

We release test, acceptance and production-like builds internally to Firebase App Distribution. These are triggered whenever there is a commit made to the main branch (ie by merging a pull request).
To use firebase, a [firebase service account json key](https://firebase.google.com/docs/admin/setup#:~:text=To%20authenticate%20a%20service%20account,confirm%20by%20clicking%20Generate%20Key.) must be added in github's secrets, named `FIREBASE_SERVICE_ACCOUNT`

Initial releases (4.6, 4.7 etc) are released from artifacts generated by the main branch. Hotfix releases artifacts are generated by release branches (eg `release/4.7.1`)
Once the team is satisfied with the quality of the builds on Firebase, a production build can be sent to the play store.

We perform a manual regression test on the build to make sure the production-ready binary performs as expected.

Once the build is approved by Google, we release the approved build manually using a phased rollout to give us the opportunity to spot any crashes that might be detected, or bugs that might be reported. The rollout starts at 5% and goes to 10%, then 25%, then 50% and finally 100%
At this point  a final tag should be made, with this format:

`Holder-4.7.0`

`Verifier-3.0.2`

Now that the release is completed, the private git repository should be "synced" with the public repository by running [this script](scripts/sync_public_repo.sh). It also pushes the releases tags.

## Contribution process

The development team works on the repository in a private fork (for reasons of compliance with existing processes) and shares its work as often as possible.

If you plan to make non-trivial changes, we recommend to open an issue beforehand where we can discuss your planned changes.
This increases the chance that we might be able to use your contribution (or it avoids doing work if there are reasons why we wouldn't be able to use it).

Note that all commits should be signed using a gpg key.
