@echo off
rem -----------------------------------------------------------------------------
rem ofbiz.bat - OFBIZ Wrapper for the Start/Stop Script for the CATALINA Server
rem
rem Environment Variable Prequisites
rem
rem   OFBIZ_HOME    (Optional) May point at your Catalina "build" directory.
rem                 If not present, %CATALINA_HOME%\..\ofbiz is assumed, in
rem                 other words %CATALINA_HOME% and the top 'ofbiz' directory
rem                 are sibling directories.
rem
rem   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
rem                 If not present, the parent directory of the current working
rem                 directory is assumed.
rem
rem   JAVA_HOME     Must point at your Java Development Kit installation.
rem
rem $Id$
rem -----------------------------------------------------------------------------

rem ----- Save Environment Variables That May Change --------------------------

set _OFBIZ_HOME=%OFBIZ_HOME%
set _CATALINA_HOME=%CATALINA_HOME%
set _CATALINA_OPTS=%CATALINA_OPTS%


rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJava
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJava

if not "%CATALINA_HOME%" == "" goto gotHome
set CATALINA_HOME=.
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
set CATALINA_HOME=..
:gotHome
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
echo Cannot find catalina.bat in %CATALINA_HOME%\bin
echo Please check your CATALINA_HOME setting
goto cleanup
:okHome

if not "%OFBIZ_HOME%" == "" goto gotOfbizHome
set OFBIZ_HOME=%CATALINA_HOME%\..\ofbiz
:gotOfbizHome


rem ----- Set CATALINA_OPTS and Start Catalina -----------------------------------------

set CATALINA_OPTS=%CATALINA_OPTS% -Dofbiz.home=%OFBIZ_HOME%

echo ofbiz.bat - Running Catalina with the following options:
echo  JAVA_HOME=%JAVA_HOME%
echo  CATALINA_HOME=%CATALINA_HOME%
echo  OFBIZ_HOME=%OFBIZ_HOME%
echo  CATALINA_OPTS=%CATALINA_OPTS%
echo  -- RUNNING %CATALINA_HOME%\bin\catalina.bat %1 --

call %CATALINA_HOME%\bin\catalina.bat %1


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set OFBIZ_HOME=%_OFBIZ_HOME%
set _OFBIZ_HOME=
set CATALINA_HOME=%_CATALINA_HOME%
set _CATALINA_HOME=
set CATALINA_OPTS=%_CATALINA_OPTS%
set _CATALINA_OPTS=
:finish
