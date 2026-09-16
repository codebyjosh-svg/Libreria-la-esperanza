@echo off
setlocal
cd /d "%~dp0"
if not exist build\classes\org\esperanza\dao\impl\LibroDAOImpl.class (
    echo Primero ejecute Clean and Build en NetBeans.
    pause
    exit /b 1
)
if not exist build\test-stock mkdir build\test-stock
javac -encoding UTF-8 -cp "build\classes;test\lib\h2-2.3.232.jar" -d build\test-stock test\PruebaStockCritico.java
if errorlevel 1 (
    pause
    exit /b 1
)
java -cp "build\classes;build\test-stock;test\lib\h2-2.3.232.jar" PruebaStockCritico
pause
