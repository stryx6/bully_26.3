param(
    [Parameter(Mandatory = $true)][string]$OriginalJar,
    [string]$BuiltJar = 'build/libs/bully-1.4.2.2.jar'
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

function Get-EntryHash($Entry) {
    $stream = $Entry.Open()
    $digest = [Security.Cryptography.SHA256]::Create()
    try { return [BitConverter]::ToString($digest.ComputeHash($stream)) }
    finally { $digest.Dispose(); $stream.Dispose() }
}

$original = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $OriginalJar))
$built = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $BuiltJar))
try {
    $count = 0
    foreach ($entry in $original.Entries) {
        # The original namespace was renamed to utilitymenu; the lang file was edited for the rename.
        if (!$entry.FullName.StartsWith('assets/hackermenu/') -or $entry.FullName.EndsWith('/') -or $entry.FullName.EndsWith('/lang/en_us.json')) { continue }
        $target = $built.GetEntry($entry.FullName.Replace('assets/hackermenu/', 'assets/utilitymenu/'))
        if (!$target -or (Get-EntryHash $entry) -ne (Get-EntryHash $target)) {
            throw "Missing or modified original resource: $($entry.FullName)"
        }
        $count++
    }
    if ($count -eq 0) { throw 'No Utility Menu assets in the original JAR.' }
    foreach ($name in @(
        'net/kraftattacke/bully/utilitymenu/UtilityMenus.class',
        'net/kraftattacke/bully/utilitymenu/UtilityMenu.class',
        'net/kraftattacke/bully/utilitymenu/UtilityMenuPayload.class',
        'net/kraftattacke/bully/utilitymenu/UtilityMenuOpenPayload.class',
        'net/kraftattacke/bully/client/UtilityMenuClient.class',
        'net/kraftattacke/bully/client/UtilityMenuScreen.class',
        'META-INF/HACKER_MENU_NOTICE.txt'
    )) {
        if (!$built.GetEntry($name)) { throw "Missing production entry: $name" }
    }
    foreach ($entry in $built.Entries) {
        if ($entry.FullName -match 'UtilityMenuGameTest|net/bennysmith/|neoforge.mods.toml') {
            throw "Unexpected test or original NeoForge entry in release: $($entry.FullName)"
        }
        if ($entry.FullName.EndsWith('.class') -and $entry.FullName -match '(utilitymenu/|UtilityMenu)') {
            $stream = $entry.Open()
            $memory = [IO.MemoryStream]::new()
            try {
                $stream.CopyTo($memory)
                $text = [Text.Encoding]::UTF8.GetString($memory.ToArray())
                if ($text.Contains('net/neoforged/')) { throw "NeoForge dependency: $($entry.FullName)" }
                if ($entry.FullName.Contains('/utilitymenu/') -and $text.Contains('net/minecraft/client/')) {
                    throw "Client dependency in common menu code: $($entry.FullName)"
                }
            } finally { $stream.Dispose(); $memory.Dispose() }
        }
    }
    $reader = [IO.StreamReader]::new($built.GetEntry('fabric.mod.json').Open())
    try { $metadata = $reader.ReadToEnd() | ConvertFrom-Json }
    finally { $reader.Dispose() }
    if ($metadata.id -ne 'bully' -or $metadata.version -ne '1.4.2.2' -or $metadata.depends.minecraft -ne '>=26.3 <26.4') {
        throw 'Unexpected release metadata.'
    }
    Write-Output "PASS: $count original Utility Menu assets are byte-identical."
    Write-Output 'PASS: common/client port classes and third-party notice are packaged.'
    Write-Output 'PASS: no test classes, original NeoForge classes or NeoForge dependencies in the port.'
    Write-Output 'PASS: common port code has no Minecraft client references; Fabric 26.3 metadata is valid.'
} finally { $built.Dispose(); $original.Dispose() }
