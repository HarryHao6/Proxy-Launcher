$ErrorActionPreference = 'Stop'

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
. (Join-Path $PSScriptRoot 'common.ps1')

Use-JavaToolchain
Set-Location $projectRoot

$distDir = Join-Path $projectRoot 'dist'
Reset-DirectoryContents -Path $distDir

Invoke-Maven -Arguments @('clean', 'package')

Get-ChildItem 'target' -Filter 'proxy-launcher-*.jar' | ForEach-Object {
    Copy-Item $_.FullName $distDir
}
Copy-Item 'README.md' $distDir

Write-Host 'Artifacts copied to dist/.'
Write-Host 'Note: this project targets Java 17+, and the fat jar includes the JavaFX runtime dependencies for the current platform.'
