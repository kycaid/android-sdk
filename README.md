# Oficcial KYCAID Android SDK

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

* Android API level 21+

## Integration
To use Kycaid SDK you should do three simple steps:
1. Add maven repository under your allprojects closure in project's build.gradle file:
```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url  "https://kycaid.bintray.com/maven"
        }
    }
}
```
2. Add dependency to module's build.gradle file:
```gradle
implementation('com.kycaid:kycaid-sdk:x.y.z')
```
where x.y.z - latest version that can be checked in Releases section of Github.

3. Kycaid SDK requires at minimum Java 8+, so you need to add following lines to your module's build.gradle file under ```android``` closure:
```gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
kotlinOptions {
    jvmTarget = '1.8'
}
```
## Usage

### Setup SDK

First of all, you should add ```KycaidActivity``` to app's Manifest.xml  file.
Place the following under application tag of your Manifest.xml:
```xml
<activity android:name="com.kycaid.sdk.ui.KycaidActivity" />
```

You can can initialize Kycaid SDK flow via ```KycaidIntent``` class. It has inner ```Builder``` class for additional configuration of appearance of the SDK. ```Builder``` constructor has two arguments: ```apiToken``` and ```formId``` - both of them should be obtained from your dashboard.
```kotlin
val builder = KycaidIntent.Builder(<API Token>, <Form Id>).build()
```

### Run verification flow

Once you created ```Builder``` object via ```build``` method you can start verification flow using ```startActivityForResult``` method and pass your ```Activity``` of ```Fragment``` class as parameter.
```kotlin
builder.startActivityForResult(<Activity or Fragment instance>)
```

### Handle verification result

To handle result from SDK you should override ```onActivityResult``` method in your Activity/Fragment class. The result could be obtained via static ```onActivityResult``` method of ```KycaidIntent``` class.
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    KycaidIntent.onActivityResult(requestCode, resultCode, data) { result ->
        when(result) {
            is KycaidResult.Success -> // obtain verification id and applicant id
            is KycaidResult.Failure -> // handle error
        }
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
```

### Additional configuration

You can apply additional configurations to SDK via ```Builder``` class. For example, you can pass callback url or pass an existing applicant id to update some information.
```kotlin
val builder = KycaidIntent.Builder(<API Token>, <Form Id>)
    .callbackUrl("https://some-url.com")
    .applicantId(<Applicant Id>)
.build()
```
You can specify a huge amount of color configurations for UI elements of SDK, there are some of them:
```kotlin
builder
    .backgroundColor(Color.GRAY)
    .colorPrimary(Color.YELLOW)
    .textColorPrimary(Color.BLUE)
    .textColorSecondary(Color.CYAN)
    .buttonTextColor(Color.BLACK)
```

## Localization

* English
* Ukrainian
* Russian

## TODO

* Add example project
* Support Activity Result API
* Color configuration explanation
* Fix limitations

## Limitations

* You can update existing document's images but cannot add new ones if start verification with existing applicant id.

## Links

API documentation:
https://docs.kycaid.com
