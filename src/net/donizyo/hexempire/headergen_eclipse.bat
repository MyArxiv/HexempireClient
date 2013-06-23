@echo off
cd ..
cd ..
cd ..
cd hexempire
cd hexempireclient
cd bin

echo 正在生成头文件...

%JAVA_HOME%\bin\javah net.donizyo.hexempire.Client
%JAVA_HOME%\bin\javah net.donizyo.hexempire.ClientEntry
%JAVA_HOME%\bin\javah net.donizyo.hexempire.util.AudioEntity

echo 头文件已成功生成.