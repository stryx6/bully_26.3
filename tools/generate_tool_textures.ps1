Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem

$textureDirectory = Join-Path $PSScriptRoot '..\src\main\resources\assets\bully\textures\item'
$scytheReferenceDirectory = Join-Path $PSScriptRoot 'reference'
$toolTypes = @('axe', 'pickaxe', 'shovel', 'hoe', 'scythe', 'spear')
$vanillaClientJar = Join-Path $env:USERPROFILE '.gradle\caches\fabric-loom\1.21.11\minecraft-client.jar'

$materials = @('antimatter', 'aetherald', 'auronyx', 'feralyth', 'fluxite', 'mystralite', 'nebulazul', 'voidcrist')

# These four colors form the wooden grip on every existing sword. Tool handles always
# use this same ramp; only the metal head is recolored from the matching sword sprite.
$woodPalette = @(
    [System.Drawing.ColorTranslator]::FromHtml('#281E0B'),
    [System.Drawing.ColorTranslator]::FromHtml('#493615'),
    [System.Drawing.ColorTranslator]::FromHtml('#684E1E'),
    [System.Drawing.ColorTranslator]::FromHtml('#896727')
)
$woodColorKeys = [System.Collections.Generic.HashSet[string]]::new([string[]]@('281E0B', '493615', '684E1E', '896727'))
# Vanilla 1.21.11 spears use a broader orange-brown wooden shaft palette than swords.
$diamondHandleColorKeys = [System.Collections.Generic.HashSet[string]]::new([string[]]@('281E0B', '493615', '684E1E', '896727', '411805', '67290B', 'A34B22', 'BA5F34'))

function Get-ColorKey([System.Drawing.Color]$color) {
    return '{0:X2}{1:X2}{2:X2}' -f $color.R, $color.G, $color.B
}

function Get-Brightness([System.Drawing.Color]$color) {
    return (0.2126 * $color.R) + (0.7152 * $color.G) + (0.0722 * $color.B)
}

function Get-SwordPalette([string]$material) {
    $sword = Get-CustomTexture (Join-Path $textureDirectory "$material`_sword.png")
    $colors = @{}
    for ($y = 0; $y -lt $sword.Height; $y++) {
        for ($x = 0; $x -lt $sword.Width; $x++) {
            $color = $sword.GetPixel($x, $y)
            if ($color.A -gt 0 -and -not $woodColorKeys.Contains((Get-ColorKey $color))) {
                $colors[(Get-ColorKey $color)] = $color
            }
        }
    }
    $sword.Dispose()
    return @($colors.Values | Sort-Object { Get-Brightness $_ })
}

function Get-FluxiteToolPalette {
    return @(
        [System.Drawing.ColorTranslator]::FromHtml('#220d0d'),
        [System.Drawing.ColorTranslator]::FromHtml('#421819'),
        [System.Drawing.ColorTranslator]::FromHtml('#7f1b1d'),
        [System.Drawing.ColorTranslator]::FromHtml('#f05f51')
    )
}

function Get-ScythePalette([string]$material) {
    if ($material -eq 'fluxite') {
        return Get-FluxiteToolPalette
    }

    $palette = Get-SwordPalette $material
    if ($palette.Count -ge 4) {
        $mid1 = [int][Math]::Floor(($palette.Count - 1) * 0.33)
        $mid2 = [int][Math]::Floor(($palette.Count - 1) * 0.66)
        return @($palette[0], $palette[$mid1], $palette[$mid2], $palette[$palette.Count - 1])
    }

    if ($palette.Count -eq 3) {
        return @($palette[0], $palette[1], $palette[2], $palette[2])
    }

    return @($palette[0], $palette[0], $palette[1], $palette[$palette.Count - 1])
}

function Get-DiamondHandlePalette {
    return @(
        [System.Drawing.ColorTranslator]::FromHtml('#281E0B'),
        [System.Drawing.ColorTranslator]::FromHtml('#67290B'),
        [System.Drawing.ColorTranslator]::FromHtml('#A34B22'),
        [System.Drawing.ColorTranslator]::FromHtml('#BA5F34')
    )
}

function Get-ScytheGripPalette {
    return @(
        [System.Drawing.ColorTranslator]::FromHtml('#2c0f10'),
        [System.Drawing.ColorTranslator]::FromHtml('#63191c'),
        [System.Drawing.ColorTranslator]::FromHtml('#a02b2c'),
        [System.Drawing.ColorTranslator]::FromHtml('#e05f52')
    )
}

function Get-PaletteColor([System.Drawing.Color[]]$palette, [double]$brightness, [double]$minimum, [double]$maximum, [int]$x, [int]$y) {
    $span = [Math]::Max(1.0, $maximum - $minimum)
    $normalized = [Math]::Max(0.0, [Math]::Min(1.0, ($brightness - $minimum) / $span))
    # Map each vanilla netherite shade directly to the material palette. This preserves the
    # original head's clean light/shadow pattern rather than adding independent speckle noise.
    $index = [int][Math]::Round($normalized * ($palette.Count - 1))
    $index = [Math]::Max(0, [Math]::Min($palette.Count - 1, $index))
    return $palette[$index]
}

function Set-PixelSafe([System.Drawing.Bitmap]$bitmap, [int]$x, [int]$y, [System.Drawing.Color]$color) {
    if ($x -ge 0 -and $x -lt $bitmap.Width -and $y -ge 0 -and $y -lt $bitmap.Height) {
        $bitmap.SetPixel($x, $y, $color)
    }
}

function Draw-Line([System.Drawing.Bitmap]$bitmap, [int]$x0, [int]$y0, [int]$x1, [int]$y1, [System.Drawing.Color]$color, [int]$thickness = 1) {
    $radius = [Math]::Max(0, [int][Math]::Floor(($thickness - 1) / 2))
    $dx = [Math]::Abs($x1 - $x0)
    $sx = if ($x0 -lt $x1) { 1 } else { -1 }
    $dy = -[Math]::Abs($y1 - $y0)
    $sy = if ($y0 -lt $y1) { 1 } else { -1 }
    $err = $dx + $dy
    while ($true) {
        for ($oy = -$radius; $oy -le $radius; $oy++) {
            for ($ox = -$radius; $ox -le $radius; $ox++) {
                Set-PixelSafe $bitmap ($x0 + $ox) ($y0 + $oy) $color
            }
        }
        if ($x0 -eq $x1 -and $y0 -eq $y1) {
            break
        }
        $e2 = 2 * $err
        if ($e2 -ge $dy) {
            $err += $dy
            $x0 += $sx
        }
        if ($e2 -le $dx) {
            $err += $dx
            $y0 += $sy
        }
    }
}

function Draw-Polyline([System.Drawing.Bitmap]$bitmap, [hashtable[]]$points, [System.Drawing.Color]$color, [int]$thickness = 1) {
    for ($i = 0; $i -lt $points.Count - 1; $i++) {
        Draw-Line $bitmap $points[$i].X $points[$i].Y $points[$i + 1].X $points[$i + 1].Y $color $thickness
    }
}

function Is-DiamondHandlePixel([System.Drawing.Bitmap]$diamondTexture, [int]$x, [int]$y) {
    $pixel = $diamondTexture.GetPixel($x, $y)
    return $pixel.A -gt 0 -and $diamondHandleColorKeys.Contains((Get-ColorKey $pixel))
}

function Set-WoodPixel([System.Drawing.Bitmap]$bitmap, [int]$x, [int]$y, [int]$shade) {
    if ($x -ge 0 -and $x -lt $bitmap.Width -and $y -ge 0 -and $y -lt $bitmap.Height) {
        $bitmap.SetPixel($x, $y, $woodPalette[$shade % $woodPalette.Count])
    }
}

function Set-HeadPixel([System.Drawing.Bitmap]$bitmap, [System.Drawing.Color[]]$palette, [int]$x, [int]$y, [int]$shade) {
    if ($x -ge 0 -and $x -lt $bitmap.Width -and $y -ge 0 -and $y -lt $bitmap.Height) {
        $shade = [Math]::Max(0, [Math]::Min($palette.Count - 1, $shade))
        $bitmap.SetPixel($x, $y, $palette[$shade])
    }
}

function Draw-DiagonalHandle([System.Drawing.Bitmap]$bitmap, [int]$startX, [int]$startY, [int]$length) {
    for ($i = 0; $i -lt $length; $i++) {
        Set-WoodPixel $bitmap ($startX + $i) ($startY - $i) ($i % 4)
        Set-WoodPixel $bitmap ($startX + $i + 1) ($startY - $i) (($i + 1) % 4)
    }
}

function Lighten-Color([System.Drawing.Color]$color, [double]$amount) {
    $amount = [Math]::Max(0.0, [Math]::Min(1.0, $amount))
    $r = [int][Math]::Round($color.R + ((255 - $color.R) * $amount))
    $g = [int][Math]::Round($color.G + ((255 - $color.G) * $amount))
    $b = [int][Math]::Round($color.B + ((255 - $color.B) * $amount))
    return [System.Drawing.Color]::FromArgb($color.A, $r, $g, $b)
}

function Darken-Color([System.Drawing.Color]$color, [double]$amount) {
    $amount = [Math]::Max(0.0, [Math]::Min(1.0, $amount))
    $r = [int][Math]::Round($color.R * (1.0 - $amount))
    $g = [int][Math]::Round($color.G * (1.0 - $amount))
    $b = [int][Math]::Round($color.B * (1.0 - $amount))
    return [System.Drawing.Color]::FromArgb($color.A, $r, $g, $b)
}

function Get-ProfileY([hashtable[]]$points, [double]$x) {
    if ($x -le $points[0].X) {
        return [double]$points[0].Y
    }
    for ($i = 0; $i -lt $points.Count - 1; $i++) {
        $left = $points[$i]
        $right = $points[$i + 1]
        if ($x -le $right.X) {
            $span = [Math]::Max(1.0, $right.X - $left.X)
            $t = ($x - $left.X) / $span
            return $left.Y + (($right.Y - $left.Y) * $t)
        }
    }
    return [double]$points[$points.Count - 1].Y
}

function New-ScytheTexture([System.Drawing.Color[]]$palette) {
    # Scythes use the reference silhouette at 64x64 so the weapon reads clearly while still
    # keeping hand-painted blade shading and a diamond-style wooden shaft.
    $texture = [System.Drawing.Bitmap]::new(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $bladeMask = @(
        '.......###......',
        '......####......',
        '...##########...',
        '...###.#######..',
        '..........#####.',
        '...........####.',
        '.........######.',
        '......#########.',
        '.....##########.',
        '....#####.##....',
        '...###.##.......',
        '..####..#.......',
        '.####...........',
        '####............',
        '####............',
        '.##.............'
    )

    $bladeOutline = Darken-Color $palette[0] 0.75
    $bladeShadow = Darken-Color $palette[0] 0.35
    $bladeMid = $palette[[Math]::Min(1, $palette.Count - 1)]
    $bladeLight = $palette[[Math]::Min(2, $palette.Count - 1)]
    $bladeGlint = Lighten-Color $palette[$palette.Count - 1] 0.60
    $handlePalette = Get-DiamondHandlePalette
    $gripPalette = Get-ScytheGripPalette

    for ($by = 0; $by -lt $bladeMask.Count; $by++) {
        for ($bx = 0; $bx -lt $bladeMask[$by].Length; $bx++) {
            if ($bladeMask[$by][$bx] -ne '#') {
                continue
            }

            $isHandle = ($by -ge 9 -and $bx -le 4)
            $isGrip = $isHandle -and ($by -ge 11)
            $cellX = $bx * 4
            $cellY = $by * 4

            for ($py = 0; $py -lt 4; $py++) {
                for ($px = 0; $px -lt 4; $px++) {
                    $color = [System.Drawing.Color]::FromArgb(0, 0, 0, 0)

                    if ($isHandle) {
                        $paletteForHandle = if ($isGrip) { $gripPalette } else { $handlePalette }
                        $progress = (($bx + ($by - 9) * 0.75) + ($px * 0.25) + ($py * 0.10))
                        $index = [int][Math]::Floor($progress) % $paletteForHandle.Count
                        if ($isGrip) {
                            $index = [Math]::Min($paletteForHandle.Count - 1, $index + 1)
                        }
                        $color = $paletteForHandle[$index]
                        if ($px -eq 0 -or $py -eq 0) {
                            $color = Darken-Color $color 0.20
                        }
                        if ($px -eq 3 -or $py -eq 3) {
                            $color = Lighten-Color $color 0.12
                        }
                    } else {
                        $topEdge = $by -eq 0 -or ($by -gt 0 -and $bladeMask[$by - 1][$bx] -ne '#')
                        $bottomEdge = $by -eq ($bladeMask.Count - 1) -or ($bladeMask[$by + 1][$bx] -ne '#')
                        $leftEdge = $bx -eq 0 -or ($bladeMask[$by][$bx - 1] -ne '#')
                        $rightEdge = $bx -eq ($bladeMask[$by].Length - 1) -or ($bladeMask[$by][$bx + 1] -ne '#')

                        $gradient = ($by / 15.0) * 0.7 + ($bx / 15.0) * 0.3
                        if ($by -le 3) {
                            $color = $bladeGlint
                        } elseif ($by -le 6) {
                            $color = if ($gradient -gt 0.5) { $bladeLight } else { $bladeMid }
                        } elseif ($by -le 10) {
                            $color = if ($gradient -gt 0.55) { $bladeMid } else { $bladeShadow }
                        } else {
                            $color = $bladeShadow
                        }

                        if ($topEdge -or $leftEdge -or $rightEdge -or $bottomEdge) {
                            $color = $bladeOutline
                        }
                        if ($by -le 2 -and $bx -ge 5 -and $bx -le 10) {
                            $color = $bladeGlint
                        } elseif ($by -le 5 -and $bx -ge 8) {
                            $color = Lighten-Color $color 0.18
                        } elseif ($by -ge 8 -and $bx -le 6) {
                            $color = Darken-Color $color 0.18
                        }

                        if (($by -eq 2 -and $bx -ge 4 -and $bx -le 7) -or ($by -eq 7 -and $bx -ge 10 -and $bx -le 12)) {
                            $color = $bladeGlint
                        }
                    }

                    # Add a little structure inside each 4x4 block so the blade does not read flat.
                    if (-not $isHandle) {
                        if ($py -eq 0 -or $px -eq 0) {
                            $color = Lighten-Color $color 0.05
                        } elseif ($py -eq 3 -or $px -eq 3) {
                            $color = Darken-Color $color 0.08
                        }
                    } elseif ($py -eq 1 -and $px -eq 2) {
                        $color = Lighten-Color $color 0.10
                    }

                    Set-PixelSafe $texture ($cellX + $px) ($cellY + $py) $color
                }
            }
        }
    }

    return $texture
}

function Get-VanillaTexture([System.IO.Compression.ZipArchive]$archive, [string]$tool) {
    $entry = $archive.GetEntry("assets/minecraft/textures/item/netherite_$tool.png")
    if ($null -eq $entry) {
        throw "Vanilla netherite texture not found for $tool"
    }
    $stream = $entry.Open()
    $image = [System.Drawing.Image]::FromStream($stream)
    $bitmap = [System.Drawing.Bitmap]::new($image)
    $image.Dispose()
    $stream.Dispose()
    return $bitmap
}

function Get-CustomTexture([string]$path) {
    $image = [System.Drawing.Image]::FromFile($path)
    $bitmap = [System.Drawing.Bitmap]::new($image)
    $image.Dispose()
    return $bitmap
}

function Get-ScytheTemplateTexture {
    return Get-CustomTexture (Join-Path $scytheReferenceDirectory 'antimatter_scythe_source.png')
}

if (-not (Test-Path $vanillaClientJar)) {
    throw "Minecraft client jar not found: $vanillaClientJar"
}

# Axes through hoes and spears take their shape directly from vanilla's netherite tools.
# Scythes have no vanilla counterpart, so they use the custom sprite above.
$archive = [System.IO.Compression.ZipFile]::OpenRead($vanillaClientJar)
$baseTextures = @{}
$diamondTextures = @{}
foreach ($tool in @('axe', 'pickaxe', 'shovel', 'hoe', 'spear')) {
    $baseTextures[$tool] = Get-VanillaTexture $archive $tool
    $entry = $archive.GetEntry("assets/minecraft/textures/item/diamond_$tool.png")
    $stream = $entry.Open()
    $image = [System.Drawing.Image]::FromStream($stream)
    $diamondTextures[$tool] = [System.Drawing.Bitmap]::new($image)
    $image.Dispose()
    $stream.Dispose()
}
$archive.Dispose()

foreach ($material in $materials) {
    $materialPalette = Get-ScythePalette $material
    if ($materialPalette.Count -lt 2) {
        throw "Unable to derive a usable palette from $material`_sword.png"
    }

    foreach ($tool in $toolTypes) {
        $outputPath = Join-Path $textureDirectory "$material`_$tool.png"
        if ($tool -eq 'scythe') {
            if ($material -eq 'antimatter') {
                Copy-Item -Force (Join-Path $scytheReferenceDirectory 'antimatter_scythe_source.png') $outputPath
                continue
            }
            $output = New-ScytheTexture $materialPalette
            $output.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
            $output.Dispose()
            continue
        }
        $source = $baseTextures[$tool]
        $diamondSource = $diamondTextures[$tool]
        $output = [System.Drawing.Bitmap]::new($source.Width, $source.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $headBrightness = @()
        for ($sampleY = 0; $sampleY -lt $source.Height; $sampleY++) {
            for ($sampleX = 0; $sampleX -lt $source.Width; $sampleX++) {
                $sample = $source.GetPixel($sampleX, $sampleY)
                if ($sample.A -gt 0 -and -not (Is-DiamondHandlePixel $diamondSource $sampleX $sampleY)) {
                    $headBrightness += Get-Brightness $sample
                }
            }
        }
        $minimumHeadBrightness = ($headBrightness | Measure-Object -Minimum).Minimum
        $maximumHeadBrightness = ($headBrightness | Measure-Object -Maximum).Maximum

        for ($y = 0; $y -lt $source.Height; $y++) {
            for ($x = 0; $x -lt $source.Width; $x++) {
                $pixel = $source.GetPixel($x, $y)
                if ($pixel.A -eq 0) {
                    $output.SetPixel($x, $y, $pixel)
                    continue
                }

                $brightness = Get-Brightness $pixel
                if (Is-DiamondHandlePixel $diamondSource $x $y) {
                    # Copy vanilla diamond's exact handle pixels, preserving its authentic wood shading.
                    $output.SetPixel($x, $y, $diamondSource.GetPixel($x, $y))
                } else {
                    $output.SetPixel($x, $y, (Get-PaletteColor $materialPalette $brightness $minimumHeadBrightness $maximumHeadBrightness $x $y))
                }
            }
        }

        $output.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $output.Dispose()
    }
}

foreach ($texture in $baseTextures.Values) {
    $texture.Dispose()
}
foreach ($texture in $diamondTextures.Values) {
    $texture.Dispose()
}
