# Official KYCAID Android SDK

![GitHub Logo](/images/logo_new_entry.png)

## Contents

* [Integration](#integration)
* [Usage](#usage)
    - [Setup SDK](#setup-sdk)
    - [Run verification flow](#run-verification-flow)
    - [Handle verification Result](#handle-verification-result)
    - [Additional configuration](#additional-configuration)
* [UI customization](#ui-customization)
* [Screenshots](#screenshots)
* [Localization](#localization)
* [Useful links](#links)

## Requirements

* Android API level 23+
* Java version 11+

## Integration
To use Kycaid SDK you should do three simple steps:
1. Add maven repository to your `allprojects` closure in project's build.gradle file:
```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url "https://nexus.kycaid.com/repository/android" }
    }
}
```
or
```Kotlin
maven(url = "https://nexus.kycaid.com/repository/android")
```
for Kotlin DSL.

2. Add dependency to module's build.gradle file:
```gradle
implementation('com.kycaid:kycaid-sdk:x.y.z')
```
where x.y.z - latest version that can be checked in [Releases](https://github.com/kycaid/android-sdk/releases) section of Github.

3. Kycaid SDK requires at minimum Java 11+, so you need to add following lines to your module's build.gradle file to `android` closure:
```gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_11
    targetCompatibility JavaVersion.VERSION_11
}
kotlinOptions {
    jvmTarget = JavaVersion.VERSION_11.toString()
}
```

## Usage

### Setup SDK
You can can initialize Kycaid SDK flow via `KycaidConfiguration` class. It has inner `Builder` class for additional configuration of the SDK. `Builder` constructor has two required arguments: `apiToken` and `formId`, which could be obtained from your dashboard.
```kotlin
val config = KycaidConfiguration.Builder(/*API Token*/, /*Form Id*/).build()
```

### Run verification flow

Once you created ```KycaidConfiguration``` object via ```build``` method you need to create `KycaidIntent` object. It takes `KycaidConfiguration` as an argument.
```kotlin
val intent = KycaidIntent(config)
```
Then you can start verification flow using ```startActivityForResult``` method and pass your ```Activity``` of ```Fragment``` class as parameter.
```kotlin
intent.startActivityForResult(<Activity or Fragment instance>)
```
Or use Activity Result Api as following:
```kotlin
// Declare result launcher as Activity or Fragment class level property
private val kycaidSdkLauncher = registerForActivityResult(KycaidIntent.CreateVerification()) { kycaidResult ->
    // Handle result (see below)
}

// Launch sdk
kycaidSdkLauncher.launch(/*KycaidIntent instance*/)
```

### Handle verification result

#### Legacy onActivityResult option

To handle result from SDK you should override ```onActivityResult``` method in your Activity/Fragment class. The result could be obtained via static ```onActivityResult``` method of ```KycaidIntent``` class.
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    KycaidIntent.onActivityResult(requestCode, resultCode, data) { result ->
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
private val kycaidSdkLauncher = registerForActivityResult(KycaidIntent.CreateVerification()) { result ->
    when (result) {
        is KycaidResult.Success -> // obtain verification id and applicant id
        is KycaidResult.Failure -> // handle error
        is KycaidResult.Cancelled -> // user just closed verification flow
    }
}
```

The result of SDK flow is represented by ```KycaidResult``` sealed class. ```KycaidResult.Success``` contains verification id and applicant id in case of successful result, ```KycaidResult.Failure``` contains an error code and optional message explaining the reason error happened.
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

You can apply additional configurations to the SDK via ```KycaidConfiguration.Builder``` class. For example, you can pass an existing applicant id to create verification for existing applicant.
```kotlin
val config = KycaidConfiguration.Builder(/*API Token*/, /*Form Id*/)
    .applicantId(/*Applicant Id*/)
    .build()
val intent = KycaidIntent(config)
kycaidSdkLauncher.launch(intent)
```
It's also possible to specify `externalApplicantId` to bind it to the applicant created in the KYCAID system, and `environment` to determine which API will be used, `stg-api` or `api`, that is staging or production environment (`KycaidEnvironment.PRODUCTION` by default).
```kotlin
config
    .externalApplicantId(/*External Applicant Id*/)
    .environment(/*KycaidEnvironment*/)
```
You can specify a huge amount of color configurations for UI elements of the SDK. See [UI customization](#ui-customization) to get more details about `ColorConfiguration`. Here are some of the colors:

```kotlin
config
    .backgroundColor(Color.GRAY)
    .colorPrimary(Color.YELLOW)
    .textColorPrimary(Color.BLUE)
    .textColorSecondary(Color.CYAN)
    // etc
```

You can specify the default language in which the form will be run by default.
```kotlin
config
    .language(/*KycaidConfiguration.Language*/)
```

## UI customization

KYCAID SDK supports basic UI customization. 
To change UI element colors you can use `KycaidConfiguration.Builder` methods. Here are all the colors you can change:
```kotlin
data class ColorConfiguration(
    @ColorInt val backgroundColor: Int? = null,
    @ColorInt val colorPrimary: Int? = null,
    @ColorInt val colorSecondary: Int? = null,
    @ColorInt val colorOnSecondary: Int? = null,
    @ColorInt val textFieldBackgroundColor: Int? = null,
    @ColorInt val inputBorderColor: Int? = null,
    @ColorInt val disabledInputBorderColor: Int? = null,
    @ColorInt val textColorPrimary: Int? = null,
    @ColorInt val textColorSecondary: Int? = null,
    @ColorInt val colorSurface: Int? = null,
    @ColorInt val colorOnSurface: Int? = null,
    @ColorInt val cardBackgroundColor: Int? = null,
    @ColorInt val buttonTextColor: Int? = null,
    @ColorInt val outlinedButtonBorderColor: Int? = null,
    @ColorInt val outlinedButtonTextColor: Int? = null,
    @ColorInt val textHintColor: Int? = null,
    @ColorInt val buttonRippleColor: Int? = null,
    @ColorInt val toolbarColor: Int? = null,
    @ColorInt val toolbarTextColor: Int? = null,
    @ColorInt val navigationBarColor: Int? = null,
    @ColorInt val pendingColor: Int? = null,
    @ColorInt val successColor: Int? = null,
    @ColorInt val errorColor: Int? = null,
    val appearanceLightStatusBars: Boolean = true,
    val appearanceLightNavigationBars: Boolean = false,
)
```
Note that each property has its default value, so you can change only those you need.

**Example**
<p float="center">
    <img src="/images/colors_1.PNG" width="240">
    <img src="/images/colors_2.PNG" width="240">
</p>
<p float="center">
    <img src="/images/colors_3.PNG" width="240">
    <img src="/images/colors_4.PNG" width="240">
</p>

## Screenshots

<p float="center">
  <img src="/images/screenshots/1.PNG" width="240" />
  <img src="/images/screenshots/2.PNG" width="240" /> 
  <img src="/images/screenshots/3.PNG" width="240" />
</p>
<p float="center">
  <img src="/images/screenshots/4.PNG" width="240" />
  <img src="/images/screenshots/5.PNG" width="240" /> 
  <img src="/images/screenshots/6.PNG" width="240" />
</p>
<p float="center">
  <img src="/images/screenshots/7.PNG" width="240" />
  <img src="/images/screenshots/8.PNG" width="240" /> 
  <img src="/images/screenshots/9.PNG" width="240" />
</p>
<p float="center">
  <img src="/images/screenshots/10.PNG" width="240" />
  <img src="/images/screenshots/11.PNG" width="240" /> 
  <img src="/images/screenshots/12.PNG" width="240" />
</p>

## Localization

KYCAID SDK supports following languages:

* English
* Azeybarjan
* Brunei
* German
* Spanish
* Spanish (Mexico)
* French
* French (Canada)
* Hindi
* Croatian
* Hebrew
* Yiddish
* Kazakh
* Dutch
* Polish
* Portuguese
* Portuguese (Brazil)
* Romanian
* Russian
* Serbian
* Tajik
* Turkish
* Ukrainian
* Uzbek
* Chinese
* Indonesian
* Georgian
* Malay
* Thai
* Vietnamese
* Finnish
* Japanese
* Korean
* Norwegian

## Links

API documentation:
https://docs.kycaid.com
