# Official KYCAID Android SDK

![GitHub Logo](/art/logo_new_entry.png).

## Contents

* [Integration](#integration)
* [Usage](#usage)
    - [Setup SDK](#setup-sdk)
    - [Run verification flow](#run-verification-flow)
    - [Handle verification Result](#handle-verification-result)
    - [Additional configuration](#additional-configuration)
* [Localization](#localization)
* [TODO](#todo)
* [Limitations](#limitations)
* [Links](#links)

## Requirements

* Android API level 22+

## Integration
To use Kycaid SDK you should do three simple steps:
1. Add maven repository under your allprojects closure in project's build.gradle file:
```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            allowInsecureProtocol = true
            url  "http://nexus.kycaid.com/repository/android"
        }
    }
}
```
2. Add dependency to module's build.gradle file:
```gradle
implementation('com.kycaid:kycaid-sdk:x.y.z')
```
where x.y.z - latest version that can be checked in [Releases](https://github.com/kycaid/android-sdk/releases) section of Github.

3. Kycaid SDK requires at minimum Java 8+, so you need to add following lines to your module's build.gradle file under ```android``` closure:
```gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_11
    targetCompatibility JavaVersion.VERSION_11
}
kotlinOptions {
    jvmTarget = JavaVersion.VERSION_11.toString()
}
```

BE ADVISED!!!
Kycaid SDK uses Google Play Services so it won't work on devices without them. Also to be able to properly test it on the emulator you should use an emulator with up-to-date Google Services(use proper emulator image with Google Services and Play Store installed). We recommend to use at least Pixel 3 for this purposes.
## Usage

### Setup SDK

First of all, you should add ```KycaidActivity``` to app's Manifest.xml  file.
Place the following under application tag of your Manifest.xml:
```xml
 <activity android:name="com.kycaid.sdk.ui.KycaidActivity"
        android:windowSoftInputMode="adjustResize"/>
```

You can can initialize Kycaid SDK flow via ```KycaidConfiguration``` class. It has inner ```Builder``` class for additional configuration of appearance of the SDK. ```Builder``` constructor has three arguments: ```apiToken```, ```formId``` and ```customHost``` - first two could be obtained from your dashboard, the third one is used to configure custom host for the api, pass ```null``` if you don't use self hosted api.
```kotlin
val builder = KycaidConfiguration.Builder(/*API Token*/, /*Form Id*/, /*custom host*/).build()
```

### Run verification flow

Once you created ```Builder``` object via ```build``` method you can start verification flow using ```startActivityForResult``` method and pass your ```Activity``` of ```Fragment``` class as parameter.
```kotlin
builder.startActivityForResult(<Activity or Fragment instance>)
```
Or use Activity Result Api as following:
```kotlin
// Declare result launcher as Activity or Fragment class level property
private val kycaidSdkLauncher = registerForActivityResult(KycaidConfiguration.CreateVerification()) { kycaidResult ->
    // Handle result(see below)
}

// Launch sdk
kycaidSdkLauncher.launch(/*KycaidConfiguration instance*/)
```

### Handle verification result

#### Legacy onActivityResult option

To handle result from SDK you should override ```onActivityResult``` method in your Activity/Fragment class. The result could be obtained via static ```onActivityResult``` method of ```KycaidIntent``` class.
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    KycaidConfiguration.onActivityResult(requestCode, resultCode, data) { result ->
        when(result) {
            is KycaidResult.Success -> // obtain verification id and applicant id
            is KycaidResult.Failure -> // handle error
            is KycaidResult.Cancelled -> // user just closed verification flow
        }
    }
}
```

#### Activity Result API option

First of all you should declare ```ActivityResultLauncher``` at top level of your Activity/Fragment like so:
```kotlin
private val kycaidSdkLauncher = registerForActivityResult(KycaidConfiguration.CreateVerification()) { result ->
    when (result) {
        is KycaidResult.Success -> // obtain verification id and applicant id
        is KycaidResult.Failure -> // handle error
        is KycaidResult.Cancelled -> // user just closed verification flow
    }
}
```

The result of SDK flow is presented via ```KycaidResult``` sealed class. ```KycaidResult.Success``` contains verification id and applicant id in case of successful result, ```KycaidResult.Failure``` contains an error code and optional message explaining the reason error happened.
There are several error codes that you can check for and handle correspondingly:
```kotlin
const val KYCAID_ERROR_FORM_ID_MISSING = 10
const val KYCAID_ERROR_API_TOKEN_MISSING = 11
const val KYCAID_ERROR_NOT_VALID_REQUEST = 12
const val KYCAID_ERROR_FAILED_TO_CREATE_VERIFICATION = 13
const val KYCAID_ERROR_UNAUTHORIZED = 14
const val KYCAID_ERROR_INACTIVE_ACCOUNT = 15
const val KYCAID_ERROR_INSUFFICIENT_FUNDS = 16
const val KYCAID_ERROR_NOT_FOUND = 17
const val KYCAID_ERROR_REQUEST_TIMEOUT = 18
const val KYCAID_ERROR_DUPLICATE_DATA = 19
const val KYCAID_ERROR_FLOW = 20
const val KYCAID_ERROR_EDIT_DENIED = 21
const val KYCAID_ERROR_DELETE_DENIED = 22
const val KYCAID_ERROR_VALIDATION = 23
const val KYCAID_ERROR_APPLICANT_EXISTS = 24
const val KYCAID_ERROR_VERIFICATION_EXISTS = 25
const val KYCAID_ERROR_INSUFFICIENT_DATA = 26
const val KYCAID_ERROR_LIMIT_EXCEEDED = 27
const val KYCAID_ERROR_INTERNAL_SERVER = 28
```

You can find explanation of every error in API documentation here: https://docs.kycaid.com/#errors

### Additional configuration

You can apply additional configurations to SDK via ```Builder``` class. For example, you can pass an existing applicant id to create verification for existing applicant.
```kotlin
val builder = KycaidIntent.Builder(/*API Token*/, /*Form Id*/)
    .applicantId(/*Applicant Id*/)
    .build()
```
You can specify a huge amount of color configurations for UI elements of SDK, there are some of them:
```kotlin
builder
    .backgroundColor(Color.GRAY)
    .colorPrimary(Color.YELLOW)
    .textColorPrimary(Color.BLUE)
    .textColorSecondary(Color.CYAN)
    // etc
```

## Localization

* English
* Ukrainian
* Russian
* Kazakh

## TODO

* Add example project
* Color configuration tutorial
* Jetpack Compose support

## Links

API documentation:
https://docs.kycaid.com
