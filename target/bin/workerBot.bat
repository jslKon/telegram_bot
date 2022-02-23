@REM ----------------------------------------------------------------------------
@REM Copyright 2001-2004 The Apache Software Foundation.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM ----------------------------------------------------------------------------
@REM

@echo off

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto WinNTGetScriptDir

@REM The 4NT Shell from jp software
:4NTArgs
set CMD_LINE_ARGS=%$
goto WinNTGetScriptDir

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto Win9xGetScriptDir
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

:Win9xGetScriptDir
set SAVEDIR=%CD%
%0\
cd %0\..\.. 
set BASEDIR=%CD%
cd %SAVEDIR%
set SAVE_DIR=
goto repoSetup

:WinNTGetScriptDir
set BASEDIR=%~dp0\..

:repoSetup


if "%JAVACMD%"=="" set JAVACMD=java

if "%REPO%"=="" set REPO=%BASEDIR%\repo

set CLASSPATH="%BASEDIR%"\etc;"%REPO%"\org\springframework\boot\spring-boot-starter-web\2.6.3\spring-boot-starter-web-2.6.3.jar;"%REPO%"\org\springframework\boot\spring-boot-starter\2.6.3\spring-boot-starter-2.6.3.jar;"%REPO%"\org\springframework\boot\spring-boot-starter-logging\2.6.3\spring-boot-starter-logging-2.6.3.jar;"%REPO%"\ch\qos\logback\logback-classic\1.2.10\logback-classic-1.2.10.jar;"%REPO%"\ch\qos\logback\logback-core\1.2.10\logback-core-1.2.10.jar;"%REPO%"\org\apache\logging\log4j\log4j-to-slf4j\2.17.1\log4j-to-slf4j-2.17.1.jar;"%REPO%"\org\apache\logging\log4j\log4j-api\2.17.1\log4j-api-2.17.1.jar;"%REPO%"\org\slf4j\jul-to-slf4j\1.7.33\jul-to-slf4j-1.7.33.jar;"%REPO%"\jakarta\annotation\jakarta.annotation-api\1.3.5\jakarta.annotation-api-1.3.5.jar;"%REPO%"\org\yaml\snakeyaml\1.29\snakeyaml-1.29.jar;"%REPO%"\org\springframework\boot\spring-boot-starter-json\2.6.3\spring-boot-starter-json-2.6.3.jar;"%REPO%"\com\fasterxml\jackson\core\jackson-databind\2.13.1\jackson-databind-2.13.1.jar;"%REPO%"\com\fasterxml\jackson\core\jackson-core\2.13.1\jackson-core-2.13.1.jar;"%REPO%"\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.13.1\jackson-datatype-jdk8-2.13.1.jar;"%REPO%"\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.13.1\jackson-datatype-jsr310-2.13.1.jar;"%REPO%"\com\fasterxml\jackson\module\jackson-module-parameter-names\2.13.1\jackson-module-parameter-names-2.13.1.jar;"%REPO%"\org\springframework\boot\spring-boot-starter-tomcat\2.6.3\spring-boot-starter-tomcat-2.6.3.jar;"%REPO%"\org\apache\tomcat\embed\tomcat-embed-core\9.0.56\tomcat-embed-core-9.0.56.jar;"%REPO%"\org\apache\tomcat\embed\tomcat-embed-el\9.0.56\tomcat-embed-el-9.0.56.jar;"%REPO%"\org\apache\tomcat\embed\tomcat-embed-websocket\9.0.56\tomcat-embed-websocket-9.0.56.jar;"%REPO%"\org\springframework\spring-web\5.3.15\spring-web-5.3.15.jar;"%REPO%"\org\springframework\spring-beans\5.3.15\spring-beans-5.3.15.jar;"%REPO%"\org\springframework\spring-webmvc\5.3.15\spring-webmvc-5.3.15.jar;"%REPO%"\org\springframework\spring-aop\5.3.15\spring-aop-5.3.15.jar;"%REPO%"\org\springframework\spring-context\5.3.15\spring-context-5.3.15.jar;"%REPO%"\org\springframework\spring-expression\5.3.15\spring-expression-5.3.15.jar;"%REPO%"\org\springframework\cloud\spring-cloud-starter-openfeign\3.1.1\spring-cloud-starter-openfeign-3.1.1.jar;"%REPO%"\org\springframework\cloud\spring-cloud-starter\3.1.1\spring-cloud-starter-3.1.1.jar;"%REPO%"\org\springframework\cloud\spring-cloud-context\3.1.1\spring-cloud-context-3.1.1.jar;"%REPO%"\org\springframework\security\spring-security-rsa\1.0.10.RELEASE\spring-security-rsa-1.0.10.RELEASE.jar;"%REPO%"\org\bouncycastle\bcpkix-jdk15on\1.68\bcpkix-jdk15on-1.68.jar;"%REPO%"\org\bouncycastle\bcprov-jdk15on\1.68\bcprov-jdk15on-1.68.jar;"%REPO%"\org\springframework\cloud\spring-cloud-openfeign-core\3.1.1\spring-cloud-openfeign-core-3.1.1.jar;"%REPO%"\org\springframework\boot\spring-boot-starter-aop\2.6.3\spring-boot-starter-aop-2.6.3.jar;"%REPO%"\org\aspectj\aspectjweaver\1.9.7\aspectjweaver-1.9.7.jar;"%REPO%"\io\github\openfeign\form\feign-form-spring\3.8.0\feign-form-spring-3.8.0.jar;"%REPO%"\io\github\openfeign\form\feign-form\3.8.0\feign-form-3.8.0.jar;"%REPO%"\commons-fileupload\commons-fileupload\1.4\commons-fileupload-1.4.jar;"%REPO%"\org\springframework\cloud\spring-cloud-commons\3.1.1\spring-cloud-commons-3.1.1.jar;"%REPO%"\org\springframework\security\spring-security-crypto\5.6.1\spring-security-crypto-5.6.1.jar;"%REPO%"\io\github\openfeign\feign-core\11.8\feign-core-11.8.jar;"%REPO%"\io\github\openfeign\feign-slf4j\11.8\feign-slf4j-11.8.jar;"%REPO%"\org\projectlombok\lombok\1.18.22\lombok-1.18.22.jar;"%REPO%"\jakarta\xml\bind\jakarta.xml.bind-api\2.3.3\jakarta.xml.bind-api-2.3.3.jar;"%REPO%"\jakarta\activation\jakarta.activation-api\1.2.2\jakarta.activation-api-1.2.2.jar;"%REPO%"\org\springframework\spring-core\5.3.15\spring-core-5.3.15.jar;"%REPO%"\org\springframework\spring-jcl\5.3.15\spring-jcl-5.3.15.jar;"%REPO%"\org\telegram\telegrambots-spring-boot-starter\5.7.1\telegrambots-spring-boot-starter-5.7.1.jar;"%REPO%"\org\telegram\telegrambots\5.7.1\telegrambots-5.7.1.jar;"%REPO%"\org\telegram\telegrambots-meta\5.7.1\telegrambots-meta-5.7.1.jar;"%REPO%"\com\google\guava\guava\30.0-jre\guava-30.0-jre.jar;"%REPO%"\com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar;"%REPO%"\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;"%REPO%"\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar;"%REPO%"\org\checkerframework\checker-qual\3.5.0\checker-qual-3.5.0.jar;"%REPO%"\com\google\errorprone\error_prone_annotations\2.3.4\error_prone_annotations-2.3.4.jar;"%REPO%"\com\google\j2objc\j2objc-annotations\1.3\j2objc-annotations-1.3.jar;"%REPO%"\com\fasterxml\jackson\core\jackson-annotations\2.13.1\jackson-annotations-2.13.1.jar;"%REPO%"\com\fasterxml\jackson\jaxrs\jackson-jaxrs-json-provider\2.13.1\jackson-jaxrs-json-provider-2.13.1.jar;"%REPO%"\com\fasterxml\jackson\jaxrs\jackson-jaxrs-base\2.13.1\jackson-jaxrs-base-2.13.1.jar;"%REPO%"\com\fasterxml\jackson\module\jackson-module-jaxb-annotations\2.13.1\jackson-module-jaxb-annotations-2.13.1.jar;"%REPO%"\org\glassfish\jersey\inject\jersey-hk2\2.35\jersey-hk2-2.35.jar;"%REPO%"\org\glassfish\jersey\core\jersey-common\2.35\jersey-common-2.35.jar;"%REPO%"\org\glassfish\hk2\osgi-resource-locator\1.0.3\osgi-resource-locator-1.0.3.jar;"%REPO%"\org\glassfish\hk2\hk2-locator\2.6.1\hk2-locator-2.6.1.jar;"%REPO%"\org\glassfish\hk2\external\aopalliance-repackaged\2.6.1\aopalliance-repackaged-2.6.1.jar;"%REPO%"\org\glassfish\hk2\hk2-api\2.6.1\hk2-api-2.6.1.jar;"%REPO%"\org\glassfish\hk2\hk2-utils\2.6.1\hk2-utils-2.6.1.jar;"%REPO%"\org\javassist\javassist\3.25.0-GA\javassist-3.25.0-GA.jar;"%REPO%"\org\glassfish\jersey\media\jersey-media-json-jackson\2.35\jersey-media-json-jackson-2.35.jar;"%REPO%"\org\glassfish\jersey\ext\jersey-entity-filtering\2.35\jersey-entity-filtering-2.35.jar;"%REPO%"\org\glassfish\jersey\containers\jersey-container-grizzly2-http\2.35\jersey-container-grizzly2-http-2.35.jar;"%REPO%"\org\glassfish\hk2\external\jakarta.inject\2.6.1\jakarta.inject-2.6.1.jar;"%REPO%"\org\glassfish\grizzly\grizzly-http-server\2.4.4\grizzly-http-server-2.4.4.jar;"%REPO%"\org\glassfish\grizzly\grizzly-http\2.4.4\grizzly-http-2.4.4.jar;"%REPO%"\org\glassfish\grizzly\grizzly-framework\2.4.4\grizzly-framework-2.4.4.jar;"%REPO%"\jakarta\ws\rs\jakarta.ws.rs-api\2.1.6\jakarta.ws.rs-api-2.1.6.jar;"%REPO%"\org\glassfish\jersey\core\jersey-server\2.35\jersey-server-2.35.jar;"%REPO%"\org\glassfish\jersey\core\jersey-client\2.35\jersey-client-2.35.jar;"%REPO%"\jakarta\validation\jakarta.validation-api\2.0.2\jakarta.validation-api-2.0.2.jar;"%REPO%"\org\json\json\20180813\json-20180813.jar;"%REPO%"\org\apache\httpcomponents\httpclient\4.5.13\httpclient-4.5.13.jar;"%REPO%"\org\apache\httpcomponents\httpcore\4.4.15\httpcore-4.4.15.jar;"%REPO%"\org\apache\httpcomponents\httpmime\4.5.13\httpmime-4.5.13.jar;"%REPO%"\commons-io\commons-io\2.11.0\commons-io-2.11.0.jar;"%REPO%"\org\springframework\boot\spring-boot\2.6.3\spring-boot-2.6.3.jar;"%REPO%"\org\springframework\boot\spring-boot-autoconfigure\2.6.3\spring-boot-autoconfigure-2.6.3.jar;"%REPO%"\org\slf4j\slf4j-api\1.7.33\slf4j-api-1.7.33.jar;"%REPO%"\org\apache\poi\poi-ooxml\3.17\poi-ooxml-3.17.jar;"%REPO%"\org\apache\poi\poi\3.17\poi-3.17.jar;"%REPO%"\commons-codec\commons-codec\1.15\commons-codec-1.15.jar;"%REPO%"\org\apache\commons\commons-collections4\4.4\commons-collections4-4.4.jar;"%REPO%"\org\apache\poi\poi-ooxml-schemas\3.17\poi-ooxml-schemas-3.17.jar;"%REPO%"\org\apache\xmlbeans\xmlbeans\2.6.0\xmlbeans-2.6.0.jar;"%REPO%"\stax\stax-api\1.0.1\stax-api-1.0.1.jar;"%REPO%"\com\github\virtuald\curvesapi\1.04\curvesapi-1.04.jar;"%REPO%"\it\tdlight\tdlight-java\2.8.0.5\tdlight-java-2.8.0.5.jar;"%REPO%"\it\tdlight\tdlight-api-legacy\4.0.217\tdlight-api-legacy-4.0.217.jar;"%REPO%"\it\unimi\dsi\fastutil\8.5.6\fastutil-8.5.6.jar;"%REPO%"\org\reactivestreams\reactive-streams\1.0.3\reactive-streams-1.0.3.jar;"%REPO%"\net\harawata\appdirs\1.2.1\appdirs-1.2.1.jar;"%REPO%"\net\java\dev\jna\jna-platform\5.6.0\jna-platform-5.6.0.jar;"%REPO%"\net\java\dev\jna\jna\5.6.0\jna-5.6.0.jar;"%REPO%"\com\google\zxing\core\3.4.1\core-3.4.1.jar;"%REPO%"\it\tdlight\tdlight-natives-linux-amd64\4.0.219\tdlight-natives-linux-amd64-4.0.219.jar;"%REPO%"\net\objecthunter\exp4j\0.4.8\exp4j-0.4.8.jar;"%REPO%"\com\example\telegram-bot-hungmb\0.0.1-SNAPSHOT\telegram-bot-hungmb-0.0.1-SNAPSHOT.jar
set EXTRA_JVM_ARGUMENTS=
goto endInit

@REM Reaching here means variables are defined and arguments have been captured
:endInit

%JAVACMD% %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="workerBot" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" com.example.telegrambothungmb.TelegramBotHungmbApplication %CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=1

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set CMD_LINE_ARGS=
goto postExec

:endNT
@endlocal

:postExec

if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
