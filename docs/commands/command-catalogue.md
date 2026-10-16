# English Voice Command Catalogue

## Scope

This catalogue records the English voice commands currently implemented in Kabali, their expected behavior, and verification results. Recognition and interpretation are tested separately. Test results marked user-reported were performed by the user on their Motorola device; they are not automated test results.

## Command: LIST_ALARMS

- **Intent ID:** `LIST_ALARMS`
- **Expected behavior:** Open the Kabali alarm list.
- **Supported language:** English only for voice commands at this stage.
- **Interpreter behavior:** Exact normalized phrase matching for currently supported phrases; unrelated or unsupported phrases should remain unknown and must not trigger the alarm list.

### Positive phrases

| Phrase | Expected behavior | Verification |
|---|---|---|
| Show my alarms | Open the alarm list | **Passed (user-reported, Motorola)** |
| List my alarms | Open the alarm list | **Passed (user-reported, Motorola)** |

### Negative phrase checks

| Phrase | Expected behavior | Observed result | Verification |
|---|---|---|---|
| I don't want to see my alarms | Do not open the alarm list; report an unrecognized command | Stayed on the listening screen and displayed “command not recognized” | **Passed (user-reported, Motorola)** |

### Remaining phrase coverage

These are candidate phrases, not supported or verified phrases yet.

- What alarms do I have?
- Do I have any alarms set?
- Display all my alarms.

Each phrase should be added only after a test-first change and device verification.

## Test notes

- The user reported that English speech recognition works both with and without internet on their Motorola.
- Tamil offline recognition was not available in the offline speech-recognition language list the user checked, so voice command support remains English-only for now.
- Do not infer intent from the presence of the words “my alarms” alone. Negation or a different intent must not accidentally open the alarm list.
- No code change was needed for the negative phrase test because the current behavior matched the expected result.

## Current status

- Positive phrase: “Show my alarms” — verified.
- Positive phrase: “List my alarms” — verified.
- Negative phrase: “I don't want to see my alarms” — verified as unrecognized and non-executing.
- Additional LIST_ALARMS phrase variants — not yet implemented or verified.
