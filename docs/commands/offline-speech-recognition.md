# Offline Speech Recognition Verification

## Purpose

Verify whether the Android device's current speech-recognition provider can transcribe English, Tamil, and spoken Tamil expressed as Tanglish **with internet access disabled**. This is a device/provider test, not a command-interpreter test.

## Current implementation snapshot

- Speech API: Android `SpeechRecognizer` with `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`.
- Requested locale: `en-US`.
- `RecognizerIntent.EXTRA_PREFER_OFFLINE`: currently `false`.
- The current implementation does not yet provide a language selector.
- The app has an `INTERNET` permission, so a normal online test does not prove offline capability.
- No language has been verified offline yet.

Source: `app/src/main/java/com/aalavandaanus/kabaliassistant/features/listening/SpeechRecognitionService.kt`.

## Before testing

1. Pull and build the current feature branch on the Android device.
2. While connected to Wi-Fi or mobile data, open Kabali's listening screen and confirm the **Listen** action can return a transcript. This is the connected baseline.
3. In Android system settings, check the installed speech-recognition provider and any available offline speech/language downloads. The exact menu names vary by Android version and provider.
4. Download the relevant language packs if the provider offers them. Do not assume they are installed just because online recognition works.

## Offline test procedure

1. Enable Airplane mode, then confirm Wi-Fi and mobile data are both off. If Wi-Fi is re-enabled manually, turn it off again for this test.
2. Open Kabali and tap **Listen**.
3. Speak one short phrase at a time at normal volume, waiting for the displayed **Heard:** transcript or error after each attempt.
4. Repeat each phrase three times in a quiet room. Record the exact transcript and whether recognition succeeded.
5. Test English first:
   - Show my alarms.
   - Call [a real contact name available on the test device].
6. Tamil and Tanglish cannot yet be fairly evaluated through the app's current fixed `en-US` locale. First test them only if the provider permits selecting a Tamil locale at the system level; record that limitation. Do not mark Tamil/Tanglish as supported until Kabali can request the relevant locale and the offline test succeeds.
7. Turn Airplane mode off after recording the results.

## Results log

Do not fill in results until the user has run the tests on their device.

| Test | Internet disabled? | Requested language | Attempts | Successful transcripts | Exact transcripts / errors | Result |
|---|---|---|---:|---:|---|---|
| Connected baseline | No | English (en-US) | Pending | Pending | Pending | Not tested |
| Offline baseline | Yes | English (en-US) | Pending | Pending | Pending | Not tested |
| Offline language check | Yes | Tamil | Pending | Pending | Pending | Not tested |
| Offline language check | Yes | Tanglish / Latin-script Tamil | Pending | Pending | Pending | Not tested |

## Interpretation

- A successful connected test does not establish offline support.
- A successful test with Wi-Fi and mobile data disabled is evidence for that specific device, provider, locale, and installed language pack.
- `EXTRA_PREFER_OFFLINE` is only a request to the recognition provider; it is not proof that recognition happened offline.
- The current app requests `en-US` and sets `EXTRA_PREFER_OFFLINE=false`. A later implementation step may add explicit offline preference and language selection, followed by another real device test.
- Keep speech transcription results separate from command-interpreter results. A transcript can be wrong even if the interpreter is correct, and an accurate transcript can still map to an unknown command.

## Verification status

**Pending user device test.** No offline language support is currently confirmed.
