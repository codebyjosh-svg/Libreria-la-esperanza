param(
    [string]$JavaFxLib = $(if ($env:JAVAFX_HOME) { Join-Path $env:JAVAFX_HOME 'lib' } else { '' }),
    [string]$MySqlJar = $env:MYSQL_CONNECTOR_JAR,
    [switch]$Test,
    [switch]$Run
)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
if (-not $JavaFxLib -or -not (Test-Path -LiteralPath (Join-Path $JavaFxLib 'javafx.controls.jar'))) {
    throw 'Indica -JavaFxLib con la carpeta lib de JavaFX 21, o define JAVAFX_HOME con la carpeta del SDK.'
}
foreach ($taskTool in @('java','javac','jar')) { Get-Command $taskTool -ErrorAction Stop | Out-Null }
$taskClasses = Join-Path $taskRoot 'build\sprint4\classes'
$taskTests = Join-Path $taskRoot 'build\sprint4\test'
$taskDist = Join-Path $taskRoot 'dist'
New-Item -ItemType Directory -Force -Path $taskClasses,$taskTests,$taskDist | Out-Null
$taskSources = @(Get-ChildItem (Join-Path $taskRoot 'src') -Filter '*.java' -Recurse | ForEach-Object { $_.FullName })
$taskFxArgs = @('--module-path',$JavaFxLib,'--add-modules','javafx.controls,javafx.fxml,javafx.swing')
& javac -encoding UTF-8 --release 21 @taskFxArgs -d $taskClasses @taskSources
if ($LASTEXITCODE -ne 0) { throw 'La compilacion no termino correctamente.' }
$taskSourceRoot = Join-Path $taskRoot 'src'
Get-ChildItem $taskSourceRoot -Recurse -File | Where-Object { $_.Extension -in @('.fxml','.css') } | ForEach-Object {
    $taskRelative = $_.FullName.Substring($taskSourceRoot.Length + 1)
    $taskOut = Join-Path $taskClasses $taskRelative
    New-Item -ItemType Directory -Force -Path (Split-Path $taskOut) | Out-Null
    Copy-Item -LiteralPath $_.FullName -Destination $taskOut -Force
}
# La configuracion privada se lee desde src en el classpath, nunca se incluye en el JAR.
$taskJar = Join-Path $taskDist 'Libreria-la-esperanza.jar'
& jar --create --file $taskJar --main-class org.esperanza.main.Main -C $taskClasses org
if ($LASTEXITCODE -ne 0) { throw 'No se pudo generar el JAR.' }
if ($Test) {
    $taskTestSources = @(Get-ChildItem (Join-Path $taskRoot 'test') -Filter '*.java' -File | ForEach-Object { $_.FullName })
    & javac -encoding UTF-8 --release 21 @taskFxArgs -cp $taskClasses -d $taskTests @taskTestSources
    if ($LASTEXITCODE -ne 0) { throw 'No se pudieron compilar las pruebas.' }
    foreach ($taskTest in @('PruebaStockCritico','PruebaSalidaInventario','PruebaVentas','PruebaAdministracion','PruebaProveedores','PruebaDevoluciones')) {
        & java @taskFxArgs -cp "$taskClasses;$taskTests" $taskTest
        if ($LASTEXITCODE -ne 0) { throw "Fallo la prueba $taskTest" }
    }
}
Write-Host "Compilacion completa: $taskJar"
if ($Run) {
    if (-not $MySqlJar -or -not (Test-Path -LiteralPath $MySqlJar)) { throw 'Indica -MySqlJar con MySQL Connector/J, o define MYSQL_CONNECTOR_JAR.' }
    if (-not (Test-Path -LiteralPath (Join-Path $taskSourceRoot 'db.properties')) -and -not $env:ESPERANZA_DB_URL) {
        throw 'Copia db.properties.example como src/db.properties y configura tu base, o define las variables ESPERANZA_DB_*.'
    }
    & java @taskFxArgs -cp "$taskJar;$taskSourceRoot;$MySqlJar" org.esperanza.main.Main
    if ($LASTEXITCODE -ne 0) { throw 'La aplicacion termino con un error.' }
}
