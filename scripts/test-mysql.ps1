param(
    [Parameter(Mandatory=$true)][string]$JavaFxLib,
    [Parameter(Mandatory=$true)][string]$MySqlJar,
    [switch]$Vistas
)
$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
& (Join-Path $PSScriptRoot 'build.ps1') -JavaFxLib $JavaFxLib -Test
if (-not $env:ESPERANZA_TEST_URL -or $env:ESPERANZA_TEST_URL -notmatch '^jdbc:mysql://(127\.0\.0\.1|localhost)(:\d+)?/esperanza_test(\?|$)') {
    throw 'Define ESPERANZA_TEST_URL con la base local desechable esperanza_test y aplica el fixture y las migraciones. No se aceptan otras bases.'
}
if (-not (Test-Path -LiteralPath $MySqlJar)) { throw 'No se encontro MySQL Connector/J.' }
$taskCp="$(Join-Path $taskRoot 'build\sprint4\classes');$(Join-Path $taskRoot 'build\sprint4\test');$MySqlJar"
$taskFxArgs=@('--module-path',$JavaFxLib,'--add-modules','javafx.controls,javafx.fxml,javafx.swing')
& java @taskFxArgs -cp $taskCp PruebaIntegracionMySql
if ($LASTEXITCODE -ne 0) { throw 'Fallo la integracion MySQL.' }
if ($Vistas) {
    $env:ESPERANZA_DB_URL=$env:ESPERANZA_TEST_URL
    $env:ESPERANZA_DB_USER=$env:ESPERANZA_TEST_USER
    $env:ESPERANZA_DB_PASSWORD=$env:ESPERANZA_TEST_PASSWORD
    & java @taskFxArgs -cp $taskCp PruebaVistas (Join-Path $taskRoot 'build\sprint4\capturas')
    if ($LASTEXITCODE -ne 0) { throw 'Fallo la carga de las vistas.' }
}
