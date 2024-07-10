@echo off
"C:\\Program Files\\Android\\Android Studio\\jbr\\bin\\java" ^
  --class-path ^
  "C:\\Users\\alexei\\.gradle\\caches\\modules-2\\files-2.1\\com.google.prefab\\cli\\2.1.0\\aa32fec809c44fa531f01dcfb739b5b3304d3050\\cli-2.1.0-all.jar" ^
  com.google.prefab.cli.AppKt ^
  --build-system ^
  cmake ^
  --platform ^
  android ^
  --abi ^
  x86 ^
  --os-version ^
  30 ^
  --stl ^
  c++_static ^
  --ndk-version ^
  26 ^
  --output ^
  "C:\\Users\\alexei\\AppData\\Local\\Temp\\agp-prefab-staging3609502565101330744\\staged-cli-output" ^
  "C:\\Users\\alexei\\.gradle\\caches\\transforms-4\\eb7d0302ac381986475583af9519a0e6\\transformed\\games-activity-1.2.2\\prefab"
