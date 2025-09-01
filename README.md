# Official KYCAID Android SDK

![GitHub Logo](/images/logo.png)

## Contents

* [Integration](#integration)
* [Usage](#usage)
    - [Setup SDK](#setup-sdk)
    - [Run verification flow](#run-verification-flow)
    - [Handle verification Result](#handle-verification-result)
    - [Get verification status](#get-verification-status)
    - [Additional configuration](#additional-configuration)
* [UI customization](#ui-customization)
* [Screenshots](#screenshots)
* [Localization](#localization)
* [Useful links](#links)

## Requirements

* Android API level 23+
* Java version 11+

## Integration
> If you are working with [Flutter](https://docs.flutter.dev/), you can take a look at this [quick guide](https://github.com/kycaid/android-sdk/blob/master/Flutter%20Integration%20Guide.md) to integrating the KYCAID SDK into a Flutter app.

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
or for Kotlin DSL:
```Kotlin
maven(url = "https://nexus.kycaid.com/repository/android")
```

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

#### By API Token and Form ID:
You can initialize Kycaid SDK with `KycaidConfiguration` class. It has inner `Builder` class for additional configuration of the SDK. `Builder` constructor has two required arguments: `apiToken` and `formId`, which can be obtained from your dashboard.
```kotlin
val config = KycaidConfiguration.Builder(apiToken = /*API Token*/, formId = /*Form Id*/).build()
```

#### By Form Token:
Or if you don't want to hold `apiToken` and `formId` in your app, then you can build `KycaidConfiguration` with `formToken` only that you have to first generate yourself (see this endpoint https://docs.kycaid.com/api/forms/form-get-url). And make sure that you pass applicant ID to the builder when `formToken` is generated with applicant binding (you passed `applicantId` to the `form-get-url` request body).
```kotlin
val config = KycaidConfiguration.Builder(formToken = /*Form Token*/)
    .applicantId(/*Applicant ID*/)
    .build()
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

The result of SDK flow is represented by ```KycaidResult``` sealed class. ```KycaidResult.Success``` contains verification id, applicant id and the current verification status in case of successful result, ```KycaidResult.Failure``` contains an error code and optional message explaining the reason error happened.
There are several error codes that you can check for and handle correspondingly:
```kotlin
const val KYCAID_ERROR_FORM_ID_MISSING = 10
const val KYCAID_ERROR_API_TOKEN_MISSING = 11
const val KYCAID_ERROR_NOT_VALID_REQUEST = 12
const val KYCAID_ERROR_FAILED_TO_CREATE_VERIFICATION = 13
const val KYCAID_ERROR_UNAUTHORIZED = 14
const val KYCAID_ERROR_INACTIVE_ACCOUNT = 15
const val KYCAID_ERROR_INSUFFICIENT_FUNDS = 16
const val KYCAID_ERROR_FORBIDDEN_DUPLICATES_RETRIES = 17
const val KYCAID_ERROR_FORBIDDEN_AGE_RESTRICTED_RETRIES = 18
const val KYCAID_ERROR_FORBIDDEN_COUNTRY_RESTRICTED_RETRIES = 19
const val KYCAID_ERROR_FORBIDDEN_COMPROMISED_PERSON_RETRIES = 20
const val KYCAID_ERROR_FORBIDDEN_FAKE_DOCUMENT_RETRIES = 21
const val KYCAID_ERROR_NOT_FOUND = 22
const val KYCAID_ERROR_REQUEST_TIMEOUT = 23
const val KYCAID_ERROR_DUPLICATE_DATA = 24
const val KYCAID_ERROR_FLOW = 25
const val KYCAID_ERROR_EDIT_DENIED = 26
const val KYCAID_ERROR_DELETE_DENIED = 27
const val KYCAID_ERROR_VALIDATION = 28
const val KYCAID_ERROR_APPLICANT_EXISTS = 29
const val KYCAID_ERROR_VERIFICATION_EXISTS = 30
const val KYCAID_ERROR_INSUFFICIENT_DATA = 31
const val KYCAID_ERROR_LIMIT_EXCEEDED = 32
const val KYCAID_ERROR_INTERNAL_SERVER = 33
const val KYCAID_ERROR_NETWORK_ERROR = 34
```

You can find an explanation for every error in the API documentation here: https://docs-v1.kycaid.com/#errors

### Get verification status

Once you have `verificationId` it's possible to check the verification status using `KycaidApi` class. You just need to initialise it with the same `KycaidConfiguration` you used during the verification flow and then call following method:
```kotlin
val kycaidApi = KycaidApi(config)
kycaidApi.getVerificationState(/*Verification Id*/)
```
Note that `getVerificationState` is a suspend function that returns `Result<VerificationState>`.
`VerificationState` contains the verification status and the verification steps with their particular statuses and decline reasons.
```kotlin
data class VerificationState(
    val verificationId: String,
    val applicantId: String,
    val verifications: List<Verification>,
    val status: VerificationStatus,
)

enum class VerificationStatus {
    PENDING, APPROVED, DECLINED
}

data class Verification(
    val type: Type,
    val status: VerificationStatus,
    val comment: String?,
    val declineReasons: List<DeclineReason>
) {
    enum class Type {
        PROFILE,
        DOCUMENT,
        FACIAL,
        ADDRESS,
        DATABASE_SCREENING,
        QUESTIONNAIRE,
        UNDEFINED,
    }

    enum class DeclineReason {
        OTHER,
        WRONG_NAME,
        WRONG_DOB,
        AGE_RESTRICTION,
        EXPIRED_DOCUMENT,
        BAD_QUALITY,
        FAKE_DOCUMENT,
        WRONG_INFO,
        PROHIBITED_JURISDICTION,
        NO_SELFIE,
        DIFFERENT_FACES,
        WRONG_DOCUMENT,
        DUPLICATE,
        DOCUMENT_DAMAGED,
        DOCUMENT_INCOMPLETE,
        FRAUDULENT,
        TAX_ID_REQUIRED,
        COMPROMISED_PERSON,
        EDITED_DOCUMENT,
        MULTIPLE_PERSON,
        COMPULSION,
        LIMIT_REACHED_OTP,
        IP_MISMATCH,
        ANONYMIZING_NETWORK,
        DEBTOR,
        QES_MISMATCH,
        UNDEFINED,
    }
}
```
You can find a description for every decline reason in the API documentation: https://docs.kycaid.com/decline-reasons

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
You can specify the default language in which the form will be run by default.
```kotlin
config
    .language(/*KycaidLanguage*/)
```

## UI customization

KYCAID SDK supports basic UI customization. You can specify a huge amount of colors for UI elements of the SDK. All you need is build `KycaidTheme` with help of `KycaidTheme.Builder` like this:

```kotlin
val theme = KycaidTheme.Builder()
    .colorScheme(
        KycaidTheme.ColorScheme(
            backgroundColor = Color.GRAY,
            primaryColor = Color.YELLOW,
            textPrimaryColor = Color.BLUE,
            textSecondaryColor = Color.CYAN
        )
    )
    .build()
val config = KycaidConfiguration.Builder(/*API Token*/, /*Form Id*/)
    .theme(theme)
    .build()
```
Here are all the colors you can change:
```kotlin
/**
 * Defines the main color palette for the SDK.
 *
 * These colors represent the core theme roles (backgrounds, accent colors, surfaces, text colors and status colors)
 * used throughout the user interface.
 *
 * Most component-specific color sets (e.g., CardColors, ButtonColors) will use these as their defaults
 * if not explicitly overridden.
 *
 * Unset colors (COLOR_UNSPECIFIED) will be resolved to their defaults internally.
 */
@Serializable
data class ColorScheme(
    /** The main background color used for screens. */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /**
     * The primary brand color used for major UI elements and key actions such as:
     * - action buttons (e.g., “Continue”, “Submit”)
     * - active elements (e.g., selected and focused elements, checked radio buttons and check boxes)
     * - progress indicators
     * - hyperlinks (unless overridden by hyperlinkColor)
     */
    @ColorInt val primaryColor: Int = COLOR_UNSPECIFIED,

    /** The color used for text and icons displayed on top of primaryColor backgrounds. */
    @ColorInt val onPrimaryColor: Int = COLOR_UNSPECIFIED,

    /**
     * The secondary accent color, used to highlight secondary actions or elements,
     * such as selection indication in dropdown lists.
     */
    @ColorInt val secondaryColor: Int = COLOR_UNSPECIFIED,

    /** The color used for text and icons displayed on top of secondaryColor backgrounds. */
    @ColorInt val onSecondaryColor: Int = COLOR_UNSPECIFIED,

    /** The tertiary accent color, intended for decorative elements such as the stars in the instructions. */
    @ColorInt val tertiaryColor: Int = COLOR_UNSPECIFIED,

    /** Background color for surfaces, containers and elevated elements. Usually used for dropdown lists. */
    @ColorInt val surfaceColor: Int = COLOR_UNSPECIFIED,

    /** The color used for text and icons displayed on top of surfaceColor backgrounds. */
    @ColorInt val onSurfaceColor: Int = COLOR_UNSPECIFIED,

    /**
     * The color used for strokes around primary elements such as:
     * - cards
     * - text inputs
     * - radio buttons
     * - check boxes
     * - other elements that have stroke
     *
     * unless it's not overridden by component-specific colors.
     */
    @ColorInt val primaryStrokeColor: Int = COLOR_UNSPECIFIED,

    /** The primary text color used for most text in the SDK. */
    @ColorInt val textPrimaryColor: Int = COLOR_UNSPECIFIED,

    /**
     * The secondary text color used for supporting or less prominent text,
     * such as instructions, subtitles.
     */
    @ColorInt val textSecondaryColor: Int = COLOR_UNSPECIFIED,

    /** The color used for hyperlink text and interactive links. */
    @ColorInt val hyperlinkColor: Int = COLOR_UNSPECIFIED,

    /** The color used to indicate pending statuses. */
    @ColorInt val pendingColor: Int = COLOR_UNSPECIFIED,

    /** The color used to indicate successful statuses. */
    @ColorInt val successColor: Int = COLOR_UNSPECIFIED,

    /** The color used to indicate errors, failures, or negative statuses. */
    @ColorInt val errorColor: Int = COLOR_UNSPECIFIED,

    /**
     * "The color used for divider lines between content and control elements,
     * such as the divider between the document type selector and the "Continue" button."
     */
    @ColorInt val dividerColor: Int = COLOR_UNSPECIFIED,

    /** The color used for the status bar background. */
    @ColorInt val statusBarColor: Int = COLOR_UNSPECIFIED,

    /**
     * Determines whether the status bar content (icons and text) should be light or dark.
     * If you use a light color for the status bar background,
     * `appearanceLightStatusBars` should be set to `true` in order to
     * indicate the system that the status bar content should be dark.
     */
    val appearanceLightStatusBars: Boolean = false,

    /** The color used for the navigation bar background. */
    @ColorInt val navigationBarColor: Int = COLOR_UNSPECIFIED,

    /**
     * Determines whether the navigation buttons should be light or dark.
     * If you use a light color for the navigation bar background,
     * `appearanceLightNavigationBars` should be set to `true` in order to
     * indicate the system that the navigation buttons should be dark.
     */
    val appearanceLightNavigationBars: Boolean = true,
)

@Serializable
data class CardColors(
    /** The background color of cards and card-like surfaces. */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of cards. */
    @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

    /** The color used for text and icons displayed on cards. */
    @ColorInt val onCardColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class TextInputColors(
    /** The background color of text input fields. */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of text input fields. */
    @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

    /** The color of entered text in text input fields. */
    @ColorInt val textColor: Int = COLOR_UNSPECIFIED,

    /** The color of hint text (or placeholder) in text input fields. */
    @ColorInt val hintTextColor: Int = COLOR_UNSPECIFIED,

    /** The color of the text cursor in text input fields. */
    @ColorInt val cursorColor: Int = COLOR_UNSPECIFIED,

    /** The color of the label above text fields, dropdowns, check boxes, radio buttons and other input fields */
    @ColorInt val labelColor: Int = COLOR_UNSPECIFIED,

    /** The background color of disabled text input fields. */
    @ColorInt val disabledBackgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of disabled text input fields. */
    @ColorInt val disabledStrokeColor: Int = COLOR_UNSPECIFIED,

    /** The color of entered text in disabled text input fields. */
    @ColorInt val disabledTextColor: Int = COLOR_UNSPECIFIED,

    /** The color of hint text (or placeholder) in disabled text input fields. */
    @ColorInt val disabledHintTextColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of focused text input fields. */
    @ColorInt val focusedStrokeColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class DropdownColors(
    /** The background color of the dropdown button (the view that triggers the list showing). */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of the dropdown button. */
    @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

    /** The color of text in the dropdown button. */
    @ColorInt val textColor: Int = COLOR_UNSPECIFIED,

    /** The color of hint text (or placeholder) in the dropdown button. */
    @ColorInt val hintTextColor: Int = COLOR_UNSPECIFIED,

    /** The tint color for the dropdown arrow icon. */
    @ColorInt val arrowTintColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of the focused dropdown button. */
    @ColorInt val focusedStrokeColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class RadioButtonColors(
    /** The background color of radio buttons. */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of radio buttons. */
    @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

    /** The color of text labels associated with radio buttons. */
    @ColorInt val textColor: Int = COLOR_UNSPECIFIED,

    /** The tint color applied to the radio button indicator. */
    @ColorInt val buttonTintColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class CheckBoxColors(
    /** The background color of checkboxes. */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of checkboxes. */
    @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

    /** The color of text labels associated with checkboxes. */
    @ColorInt val textColor: Int = COLOR_UNSPECIFIED,

    /** The tint color applied to the checkbox checkmark (when it's checked). */
    @ColorInt val buttonTintColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class ToolbarColors(
    /** The background color of the toolbar (or top bar). */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of the toolbar. */
    @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

    /** The color of the title (step label) in the toolbar. */
    @ColorInt val titleColor: Int = COLOR_UNSPECIFIED,

    /** The color of the subtitle (step description) in the toolbar. */
    @ColorInt val subtitleColor: Int = COLOR_UNSPECIFIED,

    /** The tint color for the back button icon in the toolbar. */
    @ColorInt val backButtonTintColor: Int = COLOR_UNSPECIFIED,

    /** The tint color for the language icon in the toolbar. */
    @ColorInt val languageButtonTintColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class ButtonColors(
    /** The background color of normal buttons. */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The color of text and icons in normal buttons. */
    @ColorInt val textColor: Int = COLOR_UNSPECIFIED,

    /** The ripple (touch feedback) color for normal buttons. */
    @ColorInt val rippleColor: Int = COLOR_UNSPECIFIED,

    /** The background color of disabled buttons. */
    @ColorInt val disabledBackgroundColor: Int = COLOR_UNSPECIFIED,

    /** The color of text and icons in disabled buttons. */
    @ColorInt val disabledTextColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class OutlinedButtonColors(
    /** The background color of outlined buttons (usually transparent). */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of outlined buttons. */
    @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

    /** The color of text and icons in outlined buttons. */
    @ColorInt val textColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class DocumentTypeButtonColors(
    /** The background color of document type selection buttons. */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color of document type selection buttons. */
    @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

    /** The color of text in document type selection buttons. */
    @ColorInt val textColor: Int = COLOR_UNSPECIFIED,

    /** The background color of the document icon in selection buttons. */
    @ColorInt val iconBackgroundColor: Int = COLOR_UNSPECIFIED,

    /** The tint color applied to the document icon in selection buttons. */
    @ColorInt val iconTintColor: Int = COLOR_UNSPECIFIED,

    /** The background color when a document type button is selected. */
    @ColorInt val selectedBackgroundColor: Int = COLOR_UNSPECIFIED,

    /** The stroke color when a document type button is selected. */
    @ColorInt val selectedStrokeColor: Int = COLOR_UNSPECIFIED,

    /** The text color when a document type button is selected. */
    @ColorInt val selectedTextColor: Int = COLOR_UNSPECIFIED,
)

@Serializable
data class AlertDialogColors(
    /** The background color of the alert dialog.
     * Defaults to `ColorScheme.surfaceColor` unless explicitly specified. */
    @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

    /** The color of the title in the alert dialog.
     * Defaults to `ColorScheme.onSurfaceColor` unless explicitly specified. */
    @ColorInt val titleColor: Int = COLOR_UNSPECIFIED,

    /** The color of the message in the alert dialog.
     * Defaults to `ColorScheme.onSurfaceColor` unless explicitly specified. */
    @ColorInt val messageColor: Int = COLOR_UNSPECIFIED,

    /** Colors for the cancel button in the alert dialog */
    val cancelButton: CancelButton = CancelButton(),

    /** Colors for the action button in the alert dialog */
    val actionButton: ActionButton = ActionButton()

) {
    @Serializable
    data class CancelButton(
        /** The background color of the cancel button.
         * Defaults to `OutlinedButtonColors.backgroundColor` unless explicitly specified. */
        @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

        /** The stroke color of the cancel button.
         * Defaults to `OutlinedButtonColors.strokeColor` unless explicitly specified. */
        @ColorInt val strokeColor: Int = COLOR_UNSPECIFIED,

        /** The text color in the cancel button.
         * Defaults to `OutlinedButtonColors.textColor` unless explicitly specified. */
        @ColorInt val textColor: Int = COLOR_UNSPECIFIED,
    )

    @Serializable
    data class ActionButton(
        /** The background color of the action button.
         * Defaults to `ButtonColors.backgroundColor` unless explicitly specified. */
        @ColorInt val backgroundColor: Int = COLOR_UNSPECIFIED,

        /** The text color in the action button.
         * Defaults to `ButtonColors.textColor` unless explicitly specified. */
        @ColorInt val textColor: Int = COLOR_UNSPECIFIED,
    )
}
```
Note that each property has its default value, so you can change only those you need.

## Screenshots

<p float="center">
  <img src="/images/screenshots/1.jpg" width="240" />
  <img src="/images/screenshots/2.jpg" width="240" /> 
  <img src="/images/screenshots/3.jpg" width="240" />
</p>
<p float="center">
  <img src="/images/screenshots/4.jpg" width="240" />
  <img src="/images/screenshots/5.jpg" width="240" /> 
  <img src="/images/screenshots/6.jpg" width="240" />
</p>
<p float="center">
  <img src="/images/screenshots/7.jpg" width="240" />
  <img src="/images/screenshots/8.jpg" width="240" /> 
  <img src="/images/screenshots/9.jpg" width="240" />
</p>
<p float="center">
  <img src="/images/screenshots/10.jpg" width="240" />
  <img src="/images/screenshots/11.jpg" width="240" /> 
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
* Serbian (Cyrillic)
* Serbian (Latin)
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
* Hungarian
* Nepali
* Sinhala
* Tamil

## Links

API documentation:
https://docs.kycaid.com
