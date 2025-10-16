# PowerShell: build all target MC micro-versions and copy each produced jar to a per-project archive folder
param(
    [string[]]$Versions = @('1.21','1.21.1','1.21.2','1.21.3','1.21.4','1.21.5','1.21.6','1.21.7','1.21.8'),
    #[string[]]$Versions = @('1.21','1.21.1','1.21.2','1.21.3'),
    # Added 1.21-1.21.3 and 1.21.9+ for compatibility testing
    #[string[]]$Versions = @('1.21','1.21.1','1.21.2','1.21.3','1.21.4','1.21.5','1.21.6','1.21.7','1.21.8','1.21.9','1.21.10'),
    [string]$Dest = 'build-archive'
)
 
# Ensure destination exists
New-Item -ItemType Directory -Force -Path $Dest | Out-Null
 
foreach ($v in $Versions) {
    Write-Host "=== Building $v ==="
    $args = "-PtargetProps=$v","clean","remapAndArchive","--no-daemon","--stacktrace"
    $process = Start-Process -FilePath ".\gradlew.bat" -ArgumentList $args -NoNewWindow -Wait -PassThru
    if ($process.ExitCode -ne 0) {
        Write-Error "Build failed for $v (exit $($process.ExitCode)). Stopping."
        exit $process.ExitCode
    }
 
    # Prefer the archived remapped jar (build/archives/<version>), fallback to build/libs
    $archiveDir = Join-Path -Path "build/archives" -ChildPath $v
    $jar = Get-ChildItem -Path $archiveDir -Filter *.jar -ErrorAction SilentlyContinue | Select-Object -First 1
 
    if (-not $jar) {
        # Try to find a jar in build/libs that contains the version string
        $jar = Get-ChildItem -Path "build/libs" -Filter *.jar -ErrorAction SilentlyContinue | Where-Object { $_.Name -like "*${v}*" } | Select-Object -First 1
    }
 
    if (-not $jar) {
        # Final fallback: any jar in build/libs
        $jar = Get-ChildItem -Path "build/libs" -Filter *.jar -ErrorAction SilentlyContinue | Select-Object -First 1
    }
 
    if ($jar) {
        # Ensure destination subfolder exists (keeps archives per-run safe)
        $versionDest = Join-Path -Path $Dest -ChildPath $v
        New-Item -ItemType Directory -Force -Path $versionDest | Out-Null
        $destFile = Join-Path $versionDest ($jar.Name)
        Copy-Item -Path $jar.FullName -Destination $destFile -Force
        Write-Host "Copied $($jar.Name) -> $destFile"
    } else {
        Write-Warning "No jar found to copy for $v"
    }
 
    Write-Host "Completed $v; archived at build/archives/$v and copied to $Dest\$v"
}
 
Write-Host "All versions processed. Per-version jars are in $Dest (each version in its own subfolder)"