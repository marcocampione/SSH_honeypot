@set CLASS_DIR=classes
@IF not exist %CLASS_DIR% (mkdir %CLASS_DIR%)
javac -d %CLASS_DIR% -classpath "lib/*" src\*.java src\util\*.java src\filesystem\*.java
jar -cf sshd.jar -C %CLASS_DIR% filesystem -C %CLASS_DIR% util -C %CLASS_DIR% DummyCommand.class -C %CLASS_DIR% SshServerMain.class