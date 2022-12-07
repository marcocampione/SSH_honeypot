@set CLASS_DIR=classes
"C:\Program Files\Java\jdk-17.0.1\bin\jar" -cf sshd.jar -C %CLASS_DIR% filesystem -C %CLASS_DIR% util -C %CLASS_DIR% DummyCommand.class -C %CLASS_DIR% SshServerMain.class