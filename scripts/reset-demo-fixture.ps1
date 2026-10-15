[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-MongoDatabaseName {
    param([Parameter(Mandatory = $true)][string]$MongoUri)

    try {
        if ($MongoUri -match '[\r\n]' -or $MongoUri -match '%(?![0-9A-Fa-f]{2})') {
            throw 'invalid URI encoding'
        }
        $parsedUri = [System.Uri]::new($MongoUri, [System.UriKind]::Absolute)
        if ($parsedUri.Scheme -notin @('mongodb', 'mongodb+srv') -or [string]::IsNullOrWhiteSpace($parsedUri.Host)) {
            throw 'unsupported MongoDB URI scheme or host'
        }
        if ($parsedUri.Fragment) {
            throw 'URI fragments are not supported'
        }

        $escapedPath = $parsedUri.GetComponents([System.UriComponents]::Path, [System.UriFormat]::UriEscaped)
        if ([string]::IsNullOrEmpty($escapedPath)) {
            throw 'database name is missing'
        }
        $databaseName = [System.Uri]::UnescapeDataString($escapedPath)
        if ([string]::IsNullOrWhiteSpace($databaseName) -or $databaseName.Contains('/')) {
            throw 'database name is missing or ambiguous'
        }
        return $databaseName
    }
    catch {
        throw 'URI MongoDB invalide ou impossible à analyser de manière sûre. Vérifiez sa forme sans partager ses identifiants.'
    }
}

function Test-FixtureConfirmation {
    param([AllowNull()][string]$Answer)
    return $Answer -ceq 'RESET_DEMO_DATA'
}

function Write-CapturedProcessStream {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$StreamName
    )

    $content = [System.IO.File]::ReadAllText($Path)
    $content = [regex]::Replace(
        $content,
        '(?i)(mongodb(?:\+srv)?://)[^\s/]+@',
        '$1[IDENTIFIANTS MASQUÉS]@'
    )
    if ([string]::IsNullOrEmpty($content)) {
        return
    }
    Write-Host "----- Début $StreamName Java -----"
    if ($StreamName -ceq 'stderr') {
        [Console]::Error.Write($content)
    }
    else {
        [Console]::Out.Write($content)
    }
    if (-not $content.EndsWith("`n")) {
        [Console]::WriteLine()
    }
    Write-Host "----- Fin $StreamName Java -----"
}

function Invoke-DemoFixtureReset {
    $projectRoot = Split-Path -Parent $PSScriptRoot
    $requiredFiles = @(
        'mvnw.cmd',
        'pom.xml',
        'src/main/java/fr/dawan/fixture/DemoFixtureApplication.java',
        'src/main/resources/application-demo-fixture.yaml',
        'src/main/java/fr/dawan/cc_java_mongo/fixture/DemoFixtureRunner.java'
    )

    foreach ($relativePath in $requiredFiles) {
        if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $relativePath) -PathType Leaf)) {
            [Console]::Error.WriteLine("Prérequis manquant : $relativePath")
            return 2
        }
    }

    $mongoUri = if (Test-Path Env:MONGODB_URI) {
        [Environment]::GetEnvironmentVariable('MONGODB_URI', 'Process')
    }
    else {
        'mongodb://localhost:27017/medicaldb'
    }
    if ([string]::IsNullOrWhiteSpace($mongoUri)) {
        [Console]::Error.WriteLine('MONGODB_URI est vide; arrêt sans lancement.')
        return 2
    }

    try {
        $databaseName = Get-MongoDatabaseName -MongoUri $mongoUri
    }
    catch {
        [Console]::Error.WriteLine($_.Exception.Message)
        return 2
    }
    if ($databaseName -cne 'medicaldb') {
        [Console]::Error.WriteLine('Réinitialisation refusée : la base de l’URI doit être exactement medicaldb.')
        return 2
    }

    Write-Host "Base MongoDB ciblée : $databaseName"
    Write-Host 'Les collections suivantes seront vidées puis remplies : patients, medecins, medicaments, consultations, utilisateurs.'
    Write-Host 'Les collections GridFS fs.files et fs.chunks seront préservées.'
    Write-Host 'Cette opération remplace les données métier présentes dans ces cinq collections.'
    $answer = Read-Host 'Pour confirmer, saisissez exactement RESET_DEMO_DATA'
    if (-not (Test-FixtureConfirmation -Answer $answer)) {
        Write-Host 'Confirmation incorrecte : arrêt avant le packaging et toute connexion MongoDB.'
        return 2
    }

    $originalLocation = Get-Location
    Push-Location -LiteralPath $projectRoot
    try {
        & .\mvnw.cmd -DskipTests package
        $mavenExitCode = $LASTEXITCODE
        if ($mavenExitCode -ne 0) {
            [Console]::Error.WriteLine("Le packaging Maven a échoué (code $mavenExitCode).")
            return $mavenExitCode
        }

        $jarPath = Join-Path $projectRoot 'target/cc_java_mongo-0.0.1-SNAPSHOT.jar'
        if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
            [Console]::Error.WriteLine('Le JAR attendu est absent après le packaging.')
            return 2
        }

        $stdoutLog = [System.IO.Path]::GetTempFileName()
        $stderrLog = [System.IO.Path]::GetTempFileName()
        $hadMongoUri = Test-Path Env:MONGODB_URI
        $oldMongoUri = [Environment]::GetEnvironmentVariable('MONGODB_URI', 'Process')
        try {
            [Environment]::SetEnvironmentVariable('MONGODB_URI', $mongoUri, 'Process')
            $javaCommand = Get-Command java -ErrorAction Stop
            $jarArgument = '"{0}"' -f $jarPath
            $javaProcess = Start-Process -FilePath $javaCommand.Source `
                -ArgumentList @('-Dapp.demo-fixture.confirmation=RESET_DEMO_DATA', '-jar', $jarArgument, '--spring.profiles.active=demo-fixture', '--app.demo-fixture.enabled=true') `
                -WorkingDirectory $projectRoot -Wait -PassThru -NoNewWindow `
                -RedirectStandardOutput $stdoutLog -RedirectStandardError $stderrLog
            $javaExitCode = $javaProcess.ExitCode
        }
        catch {
            [Console]::Error.WriteLine('Impossible de démarrer Java. Détail du processus :')
            [Console]::Error.WriteLine(($_ | Format-List * -Force | Out-String))
            Remove-Item -LiteralPath $stdoutLog, $stderrLog -Force -ErrorAction SilentlyContinue
            return 1
        }
        finally {
            if ($hadMongoUri) {
                [Environment]::SetEnvironmentVariable('MONGODB_URI', $oldMongoUri, 'Process')
            }
            else {
                [Environment]::SetEnvironmentVariable('MONGODB_URI', $null, 'Process')
            }
        }

        try {
            Write-CapturedProcessStream -Path $stdoutLog -StreamName 'stdout'
            Write-CapturedProcessStream -Path $stderrLog -StreamName 'stderr'
        }
        catch {
            [Console]::Error.WriteLine('Lecture des journaux Java impossible :')
            [Console]::Error.WriteLine(($_ | Format-List * -Force | Out-String))
        }
        finally {
            Remove-Item -LiteralPath $stdoutLog, $stderrLog -Force -ErrorAction SilentlyContinue
        }

        if ($javaExitCode -ne 0) {
            [Console]::Error.WriteLine("Le fixture Java a échoué (code $javaExitCode). Vérifiez l’état des collections avant de réessayer.")
            return $javaExitCode
        }
        Write-Host 'Le fixture de démonstration s’est terminé avec succès.'
        return 0
    }
    catch {
        [Console]::Error.WriteLine('Erreur pendant la préparation ou le lancement du fixture.')
        return 1
    }
    finally {
        Pop-Location
        Set-Location -LiteralPath $originalLocation
    }
}

if ($MyInvocation.InvocationName -ne '.') {
    exit (Invoke-DemoFixtureReset)
}
