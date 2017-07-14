@rem loop protoc all folder
cd /d %~dp0

@for /d %%i in (proto) do protoc.exe --proto_path=proto --java_out=./src/main/java  %%i/*.proto

pause