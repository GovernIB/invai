########################################
## EJECUCIÓN DESDE CONSOLA POWERSHELL ##
########################################
# Lanzar todos los scripts que contiene la carpeta >> C:\Repositorios\CAIB\invai\scripts\bbdd\oracle\
# Ejemplo sin settings:
# .\LaunchOracleScriptsVersion.ps1 'V.1.0.0' 'C:\Repositorios\CAIB\invai\scripts\bbdd\oracle' 'INVAI_USER' 'password_aqui' '10.1.8.36' '30002' 'XE'
# Ejemplo con settings:
# .\LaunchOracleScriptsVersion.ps1 'V.1.0.0' 'C:\Repositorios\CAIB\invai\scripts\bbdd\oracle' 'INVAI_USER' 'password_aqui' '10.1.8.36' '30002' 'XE' $true 'c.sql'

# Datos conexión BD Oracle remota configurados en standalone.xml
# <connection-url>jdbc:oracle:thin:@10.1.8.36:30002/XE</connection-url>

param (
    [string]$version,             # Versión de los archivos SQL (ej: 'V.1.0.0')
    [string]$pathSqlFolderPath,   # Ruta local base de la carpeta con archivos SQL
    [string]$dbUser,              # Usuario de la BD Oracle (INVAI)
    [string]$dbPassword,          # Contraseña de la BD Oracle
    [string]$dbHost,              # IP/Host de la BD Oracle (ej: '10.1.8.36')
    [string]$dbPort,              # Puerto de la BD Oracle (ej: '30002')
    [string]$dbService,           # SID o Servicio de la BD Oracle (ej: 'XE')
    [bool]$useSettings = $false,  # Parámetro opcional para inyectar scripts de configuración previa
    [string]$settingsFile = ""    # Nombre del archivo de configuración (ej: 'c.sql')
)

# Forzar que SQL*Plus use UTF-8 (evita corrupción de caracteres, eñes y tildes en los LOGS)
$env:NLS_LANG = ".AL32UTF8"

# Validación estricta de parámetros requeridos
$requiredParams = @{
    'version'           = $version;
    'pathSqlFolderPath' = $pathSqlFolderPath;
    'dbUser'            = $dbUser;
    'dbPassword'        = $dbPassword;
    'dbHost'            = $dbHost;
    'dbPort'            = $dbPort;
    'dbService'         = $dbService
}

foreach ($param in $requiredParams.Keys) {
    if (-not $requiredParams[$param]) {
        Write-Error "El parámetro '$param' es totalmente obligatorio para la ejecución."
        exit 1
    }
}

# Validación del archivo de settings si el flag está activo
if ($useSettings) {
    $settingsFilePath = Join-Path $pathSqlFolderPath $settingsFile
    if (-not (Test-Path -Path $settingsFilePath)) {
        Write-Error "El fichero de pre-configuración indicado no existe en la ruta: $settingsFilePath"
        exit 1
    }
}

# Construcción y validación de la ruta de la versión actual
$sqlFolderPath = Join-Path $pathSqlFolderPath $version

if (-not (Test-Path -Path $sqlFolderPath)) {
    Write-Error "La carpeta específica de la versión no existe: $sqlFolderPath"
    exit 1
}

# Asegurar la existencia de la carpeta de logs para la auditoría de la ejecución
$logFolderPath = Join-Path -Path $sqlFolderPath -ChildPath "logs"
if (-not (Test-Path -Path $logFolderPath)) {
    New-Item -ItemType Directory -Path $logFolderPath | Out-Null
}

# Cadena de conexión estandarizada tipo Oracle Net Services URL
$dbConnectString = "//" + $dbHost + ":" + $dbPort + "/" + $dbService

$errorsFound = $false
$sqlFiles = Get-ChildItem -Path $sqlFolderPath -Filter "*.sql" -File | Sort-Object Name

Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host "Iniciando despliegue de scripts de Base de Datos para INVAI ($version)" -ForegroundColor Cyan
Write-Host "Destino: $dbConnectString" -ForegroundColor Cyan
Write-Host "=========================================================================" -ForegroundColor Cyan

foreach ($file in $sqlFiles) {
    $localFilePath = $file.FullName
    $sqlFileName = $file.Name
    $logFilePath = Join-Path -Path $logFolderPath -ChildPath "$sqlFileName.log"

    Write-Host "Ejecutando script: $sqlFileName ..." -NoNewline

    if ($useSettings) {
        # Generamos un script temporal dinámico por iteración para evitar colisiones
        $tempScript = Join-Path $env:TEMP "temp_invai_sqlplus_script.sql"
        # Forzar la inclusión de @ al inicio para la llamada SQLPlus externa
        Set-Content -Path $tempScript -Value "@$settingsFilePath`n@$localFilePath"

        & echo 'quit' | sqlplus -L -S "${dbUser}/${dbPassword}@${dbConnectString}" @$tempScript > $logFilePath 2>&1

        if (Test-Path -Path $tempScript) { Remove-Item -Path $tempScript -Force }
    } else {
        & echo 'quit' | sqlplus -L -S "${dbUser}/${dbPassword}@${dbConnectString}" @$localFilePath > $logFilePath 2>&1
    }

    # Análisis de logs buscando errores típicos de sintaxis o restricciones de Oracle
    $errorLines = Select-String -Path $logFilePath -Pattern "ORA-|ERR-|PLS-"
    if ($errorLines) {
        Write-Host " [ERROR]" -ForegroundColor Red
        Write-Host " -> Errores detectados en la consola de Oracle. Ver bitácora en: $logFilePath" -ForegroundColor Yellow
        $errorsFound = $true
    } else {
        Write-Host " [OK]" -ForegroundColor Green
    }
}

Write-Host "=========================================================================" -ForegroundColor Cyan
if ($errorsFound) {
    Write-Error "El despliegue finalizó con errores parciales. Por favor comprueba los archivos de log en: $logFolderPath"
} else {
    Write-Host "ÉXITO: Todos los scripts SQL se han ejecutado correctamente sobre el entorno INVAI." -ForegroundColor Green
}
Write-Host "=========================================================================" -ForegroundColor Cyan