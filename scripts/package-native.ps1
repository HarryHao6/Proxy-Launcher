param(
    [ValidateSet('app-image', 'exe')]
    [string]$Type = 'app-image'
)

$ErrorActionPreference = 'Stop'

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
. (Join-Path $PSScriptRoot 'common.ps1')

function Find-JPackageCommand {
    $candidates = @()

    if ($env:JAVA_HOME) {
        $candidates += $env:JAVA_HOME
    }

    $candidates += Get-ChildItem 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -ExpandProperty FullName

    foreach ($candidate in $candidates) {
        $jpackagePath = Join-Path $candidate 'bin\jpackage.exe'
        if (Test-Path $jpackagePath) {
            return $jpackagePath
        }
    }

    throw 'No jpackage.exe was found. Install a JDK 17+ distribution that includes jpackage.'
}

function Use-WixToolsetIfNeeded {
    param(
        [Parameter(Mandatory = $true)]
        [string]$PackageType
    )

    if ($PackageType -ne 'exe') {
        return
    }

    $wixDirectories = @()
    $wixDirectories += Get-ChildItem 'C:\Program Files (x86)' -Directory -Filter 'WiX Toolset*' -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        ForEach-Object { Join-Path $_.FullName 'bin' }
    $wixDirectories += Get-ChildItem 'C:\Program Files' -Directory -Filter 'WiX Toolset*' -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        ForEach-Object { Join-Path $_.FullName 'bin' }

    foreach ($candidate in $wixDirectories | Select-Object -Unique) {
        if ((Test-Path (Join-Path $candidate 'candle.exe')) -and (Test-Path (Join-Path $candidate 'light.exe'))) {
            $env:Path = "$candidate;$env:Path"
            return
        }
    }

    $hasCandle = Get-Command candle.exe -ErrorAction SilentlyContinue
    $hasLight = Get-Command light.exe -ErrorAction SilentlyContinue
    if ($hasCandle -and $hasLight) {
        return
    }

    throw 'WiX Toolset 3.x was not found. Install WiX and ensure candle.exe and light.exe are available before building an EXE installer.'
}

Use-JavaToolchain
Set-Location $projectRoot

Invoke-Maven -Arguments @('clean', 'package')

$jpackage = Find-JPackageCommand
$appVersion = Get-NativeAppVersion
$releaseAssetVersion = Get-ReleaseAssetVersion
Use-WixToolsetIfNeeded -PackageType $Type
$nativeRoot = Join-Path $projectRoot 'native-dist'
$nativeDir = Join-Path $nativeRoot $Type
$winUpgradeUuid = 'f9d21ce0-5a7f-44d2-b00e-a91b2ff77e72'

if ($Type -eq 'app-image') {
    Reset-DirectoryContents -Path $nativeDir
} elseif (-not (Test-Path $nativeDir)) {
    New-Item -ItemType Directory -Path $nativeDir | Out-Null
} else {
    Get-ChildItem $nativeDir -Filter '*.exe' -File -ErrorAction SilentlyContinue | ForEach-Object {
        Remove-PathWithRetry -LiteralPath $_.FullName
    }
}

$packageInputDir = Join-Path $projectRoot 'target\jpackage-input'
Reset-DirectoryContents -Path $packageInputDir

$mainJar = Get-ChildItem 'target' -Filter 'proxy-launcher-*-jar-with-dependencies.jar' |
    Select-Object -First 1

if (-not $mainJar) {
    throw 'The fat jar was not found under target/.'
}

Copy-Item $mainJar.FullName $packageInputDir

$jpackageArguments = @(
    '--type', $Type,
    '--name', 'ProxyLauncher',
    '--dest', $nativeDir,
    '--input', $packageInputDir,
    '--main-jar', $mainJar.Name,
    '--main-class', 'com.proxylauncher.LauncherMain',
    '--app-version', $appVersion,
    '--vendor', 'Proxy Launcher'
)

if ($Type -eq 'exe') {
    $jpackageArguments += @(
        '--win-dir-chooser',
        '--win-menu',
        '--win-shortcut',
        '--win-upgrade-uuid', $winUpgradeUuid
    )
}

& $jpackage @jpackageArguments

if ($LASTEXITCODE -ne 0) {
    throw 'jpackage failed.'
}

if ($Type -eq 'exe') {
    $generatedInstaller = Join-Path $nativeDir ("ProxyLauncher-{0}.exe" -f $appVersion)
    $releaseInstaller = Join-Path $nativeDir ("ProxyLauncher-{0}.exe" -f $releaseAssetVersion)
    if ((Test-Path $generatedInstaller) -and ($generatedInstaller -ne $releaseInstaller)) {
        if (Test-Path $releaseInstaller) {
            Remove-PathWithRetry -LiteralPath $releaseInstaller
        }
        Move-Item -LiteralPath $generatedInstaller -Destination $releaseInstaller
    }
    Write-Host "Native EXE installer created under $nativeDir."
} else {
    Write-Host "Native app-image created under $nativeDir."
}
