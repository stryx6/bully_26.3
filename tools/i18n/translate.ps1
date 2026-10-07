Param(
	[ValidateSet("azure", "deepl", "libretranslate")]
	[string]$Provider = "azure",

	# Azure Translator:
	#   AZURE_TRANSLATOR_KEY, AZURE_TRANSLATOR_REGION (optional depending on resource), AZURE_TRANSLATOR_ENDPOINT (optional)
	# DeepL:
	#   DEEPL_KEY, DEEPL_ENDPOINT (optional; defaults to api-free)
	# LibreTranslate:
	#   LIBRETRANSLATE_ENDPOINT (required), LIBRETRANSLATE_KEY (optional)
	[switch]$OverwriteExisting = $false,
	[switch]$WhatIf = $false,
	[string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\\..")).Path
)

$ErrorActionPreference = "Stop"

function Get-TargetLanguageTag([string]$locale) {
	# Vanilla locales are mostly like "de_de" or "pt_br". Many providers accept BCP-47 tags.
	# We'll map a few special cases, otherwise use "ll-RR".
	$loc = $locale.ToLowerInvariant()

	# Fun/constructed languages: keep English (machine translation makes them worse).
	if ($loc -in @("en_ud", "enp", "enws", "lol_us")) { return "en" }

	# Common special cases
	if ($loc -eq "pt_br") { return "pt-BR" }
	if ($loc -eq "pt_pt") { return "pt-PT" }
	if ($loc -eq "zh_cn") { return "zh-Hans" }
	if ($loc -eq "zh_tw") { return "zh-Hant" }

	$parts = $loc.Split("_")
	if ($parts.Length -eq 1) { return $parts[0] }
	if ($parts.Length -ge 2) { return ("{0}-{1}" -f $parts[0], $parts[1].ToUpperInvariant()) }
	return $loc
}

function Protect-Placeholders([string]$s) {
	# Replace printf-style placeholders with tokens so translators don't "helpfully" rewrite them.
	# Handles: %s, %1$s, %2$s, etc.
	$placeholders = New-Object System.Collections.Generic.List[string]
	$out = [System.Text.RegularExpressions.Regex]::Replace($s, "%\\d*\\$?s", {
		param($m)
		$idx = $placeholders.Count
		$placeholders.Add($m.Value) | Out-Null
		return ("__PH_{0}__" -f $idx)
	})
	return [pscustomobject]@{ text = $out; placeholders = $placeholders }
}

function Unprotect-Placeholders([string]$s, $placeholders) {
	$out = $s
	for ($i = 0; $i -lt $placeholders.Count; $i++) {
		$out = $out.Replace(("__PH_{0}__" -f $i), $placeholders[$i])
	}
	return $out
}

function Translate-Azure([string]$text, [string]$toTag) {
	$key = $env:AZURE_TRANSLATOR_KEY
	if ([string]::IsNullOrWhiteSpace($key)) { throw "Missing env var AZURE_TRANSLATOR_KEY" }

	$region = $env:AZURE_TRANSLATOR_REGION
	$endpoint = $env:AZURE_TRANSLATOR_ENDPOINT
	if ([string]::IsNullOrWhiteSpace($endpoint)) {
		$endpoint = "https://api.cognitive.microsofttranslator.com"
	}

	$uri = "$endpoint/translate?api-version=3.0&to=$([uri]::EscapeDataString($toTag))"
	$headers = @{
		"Ocp-Apim-Subscription-Key" = $key
	}
	if (!([string]::IsNullOrWhiteSpace($region))) {
		$headers["Ocp-Apim-Subscription-Region"] = $region
	}

	$body = @(@{ Text = $text }) | ConvertTo-Json
	$r = Invoke-RestMethod -Method Post -Uri $uri -Headers $headers -ContentType "application/json; charset=utf-8" -Body $body
	return [string]$r[0].translations[0].text
}

function Translate-DeepL([string]$text, [string]$toTag) {
	$key = $env:DEEPL_KEY
	if ([string]::IsNullOrWhiteSpace($key)) { throw "Missing env var DEEPL_KEY" }

	$endpoint = $env:DEEPL_ENDPOINT
	if ([string]::IsNullOrWhiteSpace($endpoint)) {
		$endpoint = "https://api-free.deepl.com/v2/translate"
	}

	# DeepL wants language codes like DE, FR, PT-BR. It doesn't support every vanilla locale.
	$to = $toTag.ToUpperInvariant()
	if ($to.StartsWith("ZH-HANS")) { $to = "ZH" }
	if ($to.StartsWith("ZH-HANT")) { $to = "ZH" }
	if ($to -match "^[A-Z]{2}-[A-Z]{2}$") { } # ok
	elseif ($to -match "^[A-Z]{2}$") { } # ok
	else { $to = $to.Substring(0, [Math]::Min(2, $to.Length)) }

	$body = @{
		auth_key = $key
		text = $text
		target_lang = $to
	} # form urlencoded

	$r = Invoke-RestMethod -Method Post -Uri $endpoint -Body $body
	return [string]$r.translations[0].text
}

function Translate-LibreTranslate([string]$text, [string]$toTag) {
	$endpoint = $env:LIBRETRANSLATE_ENDPOINT
	if ([string]::IsNullOrWhiteSpace($endpoint)) { throw "Missing env var LIBRETRANSLATE_ENDPOINT" }

	$key = $env:LIBRETRANSLATE_KEY
	$to = $toTag.Split("-")[0].ToLowerInvariant()

	$payload = @{
		q = $text
		source = "en"
		target = $to
		format = "text"
	}
	if (!([string]::IsNullOrWhiteSpace($key))) {
		$payload.api_key = $key
	}

	$r = Invoke-RestMethod -Method Post -Uri $endpoint -ContentType "application/json; charset=utf-8" -Body ($payload | ConvertTo-Json)
	return [string]$r.translatedText
}

function Translate-One([string]$text, [string]$toTag) {
	switch ($Provider) {
		"azure" { return Translate-Azure $text $toTag }
		"deepl" { return Translate-DeepL $text $toTag }
		"libretranslate" { return Translate-LibreTranslate $text $toTag }
		default { throw "Unknown provider: $Provider" }
	}
}

$langDir = Join-Path $ProjectRoot "src\\main\\resources\\assets\\bully\\lang"
$baseFile = Join-Path $langDir "en_us.json"

if (!(Test-Path $baseFile)) { throw "Missing base lang file: $baseFile" }
if (!(Test-Path $langDir)) { throw "Missing lang dir: $langDir" }

$base = Get-Content -Raw -Path $baseFile | ConvertFrom-Json -AsHashtable
$keys = $base.Keys | Sort-Object

$files = Get-ChildItem -Path $langDir -Filter "*.json" | Where-Object { $_.Name -ne "en_us.json" } | Sort-Object Name
Write-Host "Translating $($files.Count) locales via $Provider"

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

foreach ($f in $files) {
	$locale = $f.BaseName
	$toTag = Get-TargetLanguageTag $locale

	$dest = Get-Content -Raw -Path $f.FullName | ConvertFrom-Json -AsHashtable
	$willWrite = $OverwriteExisting

	# If locale file is identical to en_us (placeholder), treat as "needs translation".
	$identical = $true
	foreach ($k in $keys) {
		if (!($dest.ContainsKey($k))) { $identical = $false; break }
		if ([string]$dest[$k] -ne [string]$base[$k]) { $identical = $false; break }
	}
	if ($identical) { $willWrite = $true }

	if (!$willWrite) {
		Write-Host "Skip $locale (already has translations; pass -OverwriteExisting to replace)"
		continue
	}

	Write-Host "Translate $locale -> $toTag"
	$out = [ordered]@{}
	foreach ($k in $keys) {
		$srcText = [string]$base[$k]
		$p = Protect-Placeholders $srcText
		$translated = Translate-One $p.text $toTag
		$translated = Unprotect-Placeholders $translated $p.placeholders
		$out[$k] = $translated
		Start-Sleep -Milliseconds 120
	}

	$json = ($out | ConvertTo-Json -Depth 10)
	if ($WhatIf) {
		Write-Host "WhatIf: would write $($f.FullName)"
		continue
	}

	[System.IO.File]::WriteAllText($f.FullName, $json + "`n", $utf8NoBom)
}

Write-Host "Done."

