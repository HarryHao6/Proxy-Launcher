$ErrorActionPreference = 'Stop'

function Get-JdkCandidateRoots {
    $candidateRoots = @()
    if ($env:JAVA_HOME) {
        $candidateRoots += $env:JAVA_HOME
    }

    $candidateRoots += Get-ChildItem 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -ExpandProperty FullName

    return $candidateRoots | Select-Object -Unique
}

function Set-JavaToolchain {
    param(
        [Parameter(Mandatory = $true)]
        [string]$CandidateRoot
    )

    $env:JAVA_HOME = $CandidateRoot
    $env:Path = "$CandidateRoot\bin;$env:Path"
}

function Get-JdkMajorVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$CandidateRoot
    )

    $releaseFile = Join-Path $CandidateRoot 'release'
    if (-not (Test-Path $releaseFile)) {
        return $null
    }

    $javaVersionLine = Get-Content $releaseFile | Where-Object { $_ -like 'JAVA_VERSION=*' } | Select-Object -First 1
    if (-not $javaVersionLine) {
        return $null
    }

    $javaVersion = ($javaVersionLine -replace 'JAVA_VERSION="', '' -replace '"', '').Trim()
    if ($javaVersion.StartsWith('1.')) {
        return [int]$javaVersion.Split('.')[1]
    }

    return [int]$javaVersion.Split('.')[0]
}

function Use-JavaToolchain {
    $candidateRoots = Get-JdkCandidateRoots

    foreach ($candidateRoot in $candidateRoots) {
        $javacPath = Join-Path $candidateRoot 'bin\javac.exe'
        $majorVersion = Get-JdkMajorVersion -CandidateRoot $candidateRoot
        if ((Test-Path $javacPath) -and $majorVersion -ge 17) {
            Set-JavaToolchain -CandidateRoot $candidateRoot
            return
        }
    }

    throw 'No JDK 17+ with javac.exe was found. Install JDK 17 or above and set JAVA_HOME before running this script.'
}

function Get-MavenCommand {
    $wrapperCmd = Join-Path (Get-Location) 'mvnw.cmd'
    if (Test-Path $wrapperCmd) {
        return $wrapperCmd
    }

    $mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mavenCommand) {
        return $mavenCommand.Source
    }

    throw 'Neither mvnw.cmd nor mvn was found.'
}

function Invoke-Maven {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $mavenCommand = Get-MavenCommand
    & $mavenCommand @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Maven command failed: $mavenCommand $($Arguments -join ' ')"
    }
}

function Get-ProjectVersion {
    $pomPath = Join-Path (Get-Location) 'pom.xml'
    if (-not (Test-Path $pomPath)) {
        throw "pom.xml was not found at $pomPath."
    }

    [xml]$pom = Get-Content $pomPath
    $version = $pom.project.version
    if ([string]::IsNullOrWhiteSpace($version)) {
        throw 'The project version could not be read from pom.xml.'
    }

    return $version.Trim()
}

function Get-NativeAppVersion {
    $projectVersion = Get-ProjectVersion
    $baseVersion = ($projectVersion -replace '-SNAPSHOT$', '')

    if (-not $projectVersion.EndsWith('-SNAPSHOT')) {
        return $baseVersion
    }

    $versionParts = $baseVersion.Split('.')
    if ($versionParts.Count -lt 2) {
        throw "The project version '$projectVersion' must contain at least major and minor segments."
    }

    $major = [int]$versionParts[0]
    $minor = [int]$versionParts[1]
    $timestamp = Get-Date
    $dayStamp = [int]($timestamp.ToString('yy') + $timestamp.DayOfYear.ToString('000'))
    $minuteOfDay = [int][Math]::Floor($timestamp.TimeOfDay.TotalMinutes)

    return "$major.$minor.$dayStamp.$minuteOfDay"
}

function Remove-PathWithRetry {
    param(
        [Parameter(Mandatory = $true)]
        [string]$LiteralPath,
        [int]$Attempts = 8
    )

    for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
        try {
            if (-not (Test-Path $LiteralPath)) {
                return
            }

            $item = Get-Item -LiteralPath $LiteralPath -Force
            if (-not $item.PSIsContainer) {
                [System.IO.File]::SetAttributes($item.FullName, [System.IO.FileAttributes]::Normal)
            }

            Remove-Item -LiteralPath $LiteralPath -Recurse -Force -ErrorAction Stop
            return
        } catch {
            if ($attempt -eq $Attempts) {
                throw "Failed to remove '$LiteralPath'. Close any Explorer window, installer, or process using it and try again. $($_.Exception.Message)"
            }

            Start-Sleep -Milliseconds (250 * $attempt)
        }
    }
}

function Reset-DirectoryContents {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if (-not (Test-Path $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
        return
    }

    Get-ChildItem $Path -Force | ForEach-Object {
        Remove-PathWithRetry -LiteralPath $_.FullName
    }
}
