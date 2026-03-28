$ErrorActionPreference = 'Stop'

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
. (Join-Path $PSScriptRoot 'common.ps1')

Use-JavaToolchain
Set-Location $projectRoot

Invoke-Maven -Arguments @('javafx:run')
