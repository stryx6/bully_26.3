Param(
	[Parameter(Mandatory = $true)]
	[string]$Locale,
	[Parameter(Mandatory = $true)]
	[string]$Csv,
	[string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\\..")).Path
)

$ErrorActionPreference = "Stop"

$langDir = Join-Path $ProjectRoot "src\\main\\resources\\assets\\bully\\lang"
$srcFile = Join-Path $langDir "en_us.json"
$destFile = Join-Path $langDir ("$Locale.json")

if (!(Test-Path $srcFile)) {
	throw "Missing source lang file: $srcFile"
}
if (!(Test-Path $Csv)) {
	throw "Missing CSV: $Csv"
}
if (!(Test-Path $destFile)) {
	throw "Missing destination locale file: $destFile"
}

$src = Get-Content -Raw -Path $srcFile | ConvertFrom-Json -AsHashtable
$dest = Get-Content -Raw -Path $destFile | ConvertFrom-Json -AsHashtable
$rows = Import-Csv -Path $Csv

# CSV format: key,en_us,<locale?>,note
$valueColumn = $Locale
if ($rows.Count -gt 0 -and $rows[0].PSObject.Properties.Name -contains $valueColumn) {
	# ok
} else {
	# fallback: try generic "value" column
	$valueColumn = "value"
	if (!($rows.Count -gt 0 -and $rows[0].PSObject.Properties.Name -contains $valueColumn)) {
		throw "CSV must contain a '$Locale' column or a 'value' column."
	}
}

foreach ($row in $rows) {
	$key = [string]$row.key
	if ([string]::IsNullOrWhiteSpace($key)) { continue }
	if (!($src.ContainsKey($key))) { continue } # ignore unknown keys

	$val = [string]$row.$valueColumn
	if ([string]::IsNullOrWhiteSpace($val)) {
		# Keep existing translation if blank.
		continue
	}
	$dest[$key] = $val
}

# Ensure keyset matches en_us (add any missing keys with English fallback).
foreach ($k in $src.Keys) {
	if (!($dest.ContainsKey($k))) {
		$dest[$k] = [string]$src[$k]
	}
}

# Remove keys that no longer exist in en_us.
foreach ($k in @($dest.Keys)) {
	if (!($src.ContainsKey($k))) {
		$dest.Remove($k) | Out-Null
	}
}

$ordered = [ordered]@{}
foreach ($k in ($src.Keys | Sort-Object)) {
	$ordered[$k] = [string]$dest[$k]
}

$json = ($ordered | ConvertTo-Json -Depth 10)

# Write UTF-8 without BOM to avoid mojibake issues.
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($destFile, $json + "`n", $utf8NoBom)

Write-Host "Updated $destFile"

