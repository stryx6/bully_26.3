# Translations (Human)

This repo contains language files at:

`src/main/resources/assets/bully/lang/*.json`

`en_us.json` is the source of truth for translation keys.

## What "Human Translations" Means

If you want real human translations for every vanilla locale, you need one of:

- Professional translators (paid), ideally with a reviewer per language.
- Community contributions (volunteer), ideally with native-speaker review.
- A hosted translation platform (Crowdin / Weblate / Transifex) to manage workflow, review, and PRs.

This repo includes tooling so translations can be edited safely without breaking keys.

## Rules For Translators

- Do not change keys. Only translate values.
- Keep formatting codes and placeholders exactly:
  - `%s`, `%1$s`, `%2$s`, etc must stay the same.
  - Keep punctuation/spacing around placeholders sensible for the language.
- If you cannot translate a string, keep the English value (do not delete keys).

## Supported Locales

Locale files are generated to match the set of locales present in your local vanilla assets index.

## Tooling

### Validate

Runs as part of Gradle `check`:

`./gradlew validateLang`

Ensures:

- Every `assets/bully/lang/*.json` is valid JSON.
- Every locale has the exact same key set as `en_us.json`.
- Every value is a string.

### Export (for translators)

`powershell -ExecutionPolicy Bypass -File tools/i18n/export.ps1`

Writes: `tools/i18n/out/translations.csv`

### Import (from translators)

`powershell -ExecutionPolicy Bypass -File tools/i18n/import.ps1 -Locale de_de -Csv tools/i18n/in/de_de.csv`

### Machine Translate (Optional)

If you want machine translations for every locale file, use:

Azure Translator (recommended for coverage):

```powershell
$env:AZURE_TRANSLATOR_KEY="..."
$env:AZURE_TRANSLATOR_REGION="..."   # if your Azure resource requires it
powershell -ExecutionPolicy Bypass -File tools/i18n/translate.ps1 -Provider azure -OverwriteExisting
```

DeepL:

```powershell
$env:DEEPL_KEY="..."
powershell -ExecutionPolicy Bypass -File tools/i18n/translate.ps1 -Provider deepl -OverwriteExisting
```

LibreTranslate:

```powershell
$env:LIBRETRANSLATE_ENDPOINT="https://your-libretranslate/translate"
$env:LIBRETRANSLATE_KEY="..."  # optional
powershell -ExecutionPolicy Bypass -File tools/i18n/translate.ps1 -Provider libretranslate -OverwriteExisting
```
