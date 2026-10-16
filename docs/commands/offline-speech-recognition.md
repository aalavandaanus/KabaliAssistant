# Offline Speech Recognition Verification

## Purpose

Verify whether the Android device's current speech-recognition provider can transcribe English, Tamil, and spoken Tamil expressed as Tanglish **with internet access disabled**. This is a device/provider test, not a command-interpreter test.

## Current implementation snapshot

- Speech API: Android `SpeechRecognizer` with `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`.
- Requested locale: `en-US`.
- `RecognizerIntent.EXTRA_PREFER_OFFLINE`: currently `false`.
- The current implementation does not yet provide a language selector.
- The app has an `INTERNET` permission; however, the user's test confirmed English recognition works with internet both enabled and disabled.

Source: `app/src/main/java/com/aalavandaanus/kabaliassistant/features/listening/SpeechRecognitionService.kt`.

## Test procedure

1. Test while connected to Wi-Fi or mobile data.
2. Enable Airplane mode and confirm both Wi-Fi and mobile data are off.
3. Open Kabali, tap **Listen**, and speak a short phrase.
4. Record the exact transcript or error.
5. Repeat for Tamil and Tanglish only after language selection is available, or if the installed provider allows selecting those languages through system settings.

## Results log

| Test | Internet disabled? | Requested language | Result | Notes |
|---|---|---|---|---|
| Connected baseline | No | English (en-US) | **Passed (user-reported)** | Recognition works with internet enabled. |
| Offline baseline | Yes | English (en-US) | **Passed (user-reported)** | Recognition works with internet disabled. Exact transcript and attempt count not recorded. |
| Offline language check | Yes | Tamil | Pending | App currently requests only `en-US`; Tamil support not verified. |
| Offline language check | Yes | Tanglish / Latin-script Tamil | Pending | Must test after an appropriate locale/language selection approach is available. |

## Interpretation and limitations

- The user's successful offline English test is evidence that the current device/provider setup can recognize English without internet.
- This result applies to the tested device, provider, locale, and installed language resources; it does not establish offline Tamil or Tanglish support.
- `EXTRA_PREFER_OFFLINE` is only a request to the recognition provider, not proof that recognition happened offline.
- The current app requests `en-US` and sets `EXTRA_PREFER_OFFLINE=false`. A later implementation may request offline recognition explicitly and add language selection; we should verify the behavior again after that change.
- Keep speech transcription results separate from command-interpreter results. A transcript can be wrong even if the interpreter is correct, and an accurate transcript can still map to an unknown command.

## Verification status

- **English offline recognition: passed on user's device (user-reported).**
- **Tamil offline recognition: not yet tested.**
- **Tanglish offline recognition: not yet tested.**
