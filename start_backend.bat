@echo off
echo  Starting Local AI Backend Server...
echo =====================================
cd local_backend
echo  Changed to local_backend directory
echo.
echo  Installing dependencies...
call npm install
echo.
echo  Starting server on http://localhost:3000
echo.
echo  Server will be available at:
echo    - Health Check: http://localhost:3000/health
echo    - Moderation: http://localhost:3000/api/moderate  
echo    - Suggestions: http://localhost:3000/api/suggest
echo.
echo  Press Ctrl+C to stop the server
echo =====================================
echo.
call npm start
pause
