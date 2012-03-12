If you want a stand-alone uSDLC installation, drop 'uSDLC-full.jar' in a
prepared directory (on the same level as your other projects) and run as

java -jar uSDLC-full [userId=anon] [port=9000]

If you are a developer you can get it running straight away using using
the uSDLC.sh or uSDLC.bat script. Options are clean, build or run. This
triggers an ant run using build.xml. The only external requirement is a
Java jdk installation.

If you want to integrate uSDLC within your IDE, read the comments at the
top of build.xml.
