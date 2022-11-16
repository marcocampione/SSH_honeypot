@set CLASS_DIR=classes
jar -cf sshd.jar -C %CLASS_DIR% filesystem -C %CLASS_DIR% util -C %CLASS_DIR% DummyCommand.class -C %CLASS_DIR% SshServerMain.class