Param(
	[string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\\..")).Path,
	[string]$OutDir = (Join-Path $PSScriptRoot "out")
)

$ErrorActionPreference = "Stop"

$langDir = Join-Path $ProjectRoot "src\\main\\resources\\assets\\bully\\lang"
$srcFile = Join-Path $langDir "en_us.json"

if (!(Test-Path $srcFile)) {
	throw "Missing source lang file: $srcFile"
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$outCsv = Join-Path $OutDir "translations.csv"

$src = Get-Content -Raw -Path $srcFile | ConvertFrom-Json -AsHashtable

$rows = foreach ($k in ($src.Keys | Sort-Object)) {
	[pscustomobject]@{
		key = $k
		en_us = [string]$src[$k]
		note = ""
	}
}

$rows | Export-Csv -Path $outCsv -NoTypeInformation -Encoding utf8
Write-Host "Wrote $outCsv ($($rows.Count) keys)"

