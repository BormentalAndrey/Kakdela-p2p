@echo off
where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle %*
) else (
  echo Gradle CLI not found. Run 'gradle wrapper' locally to create wrapper.
  exit /b 1
)
