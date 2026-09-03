@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows with intelligent wrapper detection
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto checkWrapper

if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    set "JAVA_EXE=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    goto checkWrapper
)

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto checkWrapper

if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    set "JAVA_EXE=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    goto checkWrapper
)

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
goto fail

:checkWrapper
set "WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar"
if exist "%WRAPPER_JAR%" goto execute

if exist "C:\Users\ResTIC55\AndroidStudioProjects\CadeiraPDM\gradle\wrapper\gradle-wrapper.jar" (
    set "WRAPPER_JAR=C:\Users\ResTIC55\AndroidStudioProjects\CadeiraPDM\gradle\wrapper\gradle-wrapper.jar"
    goto execute
)

for /d %%G in ("%USERPROFILE%\.gradle\wrapper\dists\gradle-*\*\gradle-*\bin\gradle.bat") do (
    if exist "%%G" (
        call "%%G" --project-cache-dir "%USERPROFILE%\.gradle\project-cache" %*
        goto end
    )
)

:execute
set CLASSPATH=
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" -jar "%WRAPPER_JAR%" --project-cache-dir "%USERPROFILE%\.gradle\project-cache" %*

:end
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal
