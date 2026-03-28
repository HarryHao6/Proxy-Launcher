$ErrorActionPreference = 'Stop'

& (Join-Path $PSScriptRoot 'package-native.ps1') -Type 'app-image'
