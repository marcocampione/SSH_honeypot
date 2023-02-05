/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import java.io.IOException;
import java.io.PrintStream;


import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;


import org.apache.sshd.cli.CliLogger;
import org.apache.sshd.cli.server.SshServerCliSupport;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.PropertyResolver;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.config.ConfigFileReaderSupport;
import org.apache.sshd.common.keyprovider.FileHostKeyCertificateProvider;
import org.apache.sshd.common.keyprovider.HostKeyCertificateProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.config.SshServerConfigFileReader;
import org.apache.sshd.server.config.keys.ServerIdentity;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ShellFactory;
import org.apache.sshd.server.subsystem.SubsystemFactory;
import org.slf4j.Logger;

import util.DataLog;
/**
 * TODO Add javadoc
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class SshServerMain extends SshServerCliSupport {
    public SshServerMain() {
        super(); // in case someone wants to extend it
    }
    
    // Password for root and pi used fror authentication
    static String[] rootPwds= {"123456", "root", "admin", "123", "0", "1"};
    static String[] piPwds= {"raspberry", "pi"};


    public static void main(String[] args) throws Exception {
        int port = 22;
        boolean error = false;
        String hostKeyType = AbstractGeneratorHostKeyProvider.DEFAULT_ALGORITHM;
        int hostKeySize = 0;
        Collection<String> keyFiles = null;
        Collection<String> certFiles = null;
        Map<String, Object> options = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


        int numArgs = GenericUtils.length(args);
        for (int i = 0; i < numArgs; i++) {
            String argName = args[i];
            if ("-p".equals(argName)) {
                i++;
                if (i >= numArgs) {
                    System.err.println("option requires an argument: " + argName);
                    error = true;
                    break;
                }
                port = Integer.parseInt(args[i]);
            } else if ("-key-type".equals(argName)) {
                i++;
                if (i >= numArgs) {
                    System.err.println("option requires an argument: " + argName);
                    error = true;
                    break;
                }

                if (keyFiles != null) {
                    System.err.println("option conflicts with -key-file: " + argName);
                    error = true;
                    break;
                }
                hostKeyType = args[i].toUpperCase();
            } else if ("-key-size".equals(argName)) {
                i++;
                if (i >= numArgs) {
                    System.err.println("option requires an argument: " + argName);
                    error = true;
                    break;
                }

                if (keyFiles != null) {
                    System.err.println("option conflicts with -key-file: " + argName);
                    error = true;
                    break;
                }

                hostKeySize = Integer.parseInt(args[i]);
            } else if ("-key-file".equals(argName)) {
                i++;
                if (i >= numArgs) {
                    System.err.println("option requires an argument: " + argName);
                    error = true;
                    break;
                }

                String keyFilePath = args[i];
                if (keyFiles == null) {
                    keyFiles = new LinkedList<>();
                }
                keyFiles.add(keyFilePath);
            } else if ("-o".equals(argName)) {
                i++;
                if (i >= numArgs) {
                    System.err.println("option requires and argument: " + argName);
                    error = true;
                    break;
                }

                String opt = args[i];
                int idx = opt.indexOf('=');
                if (idx <= 0) {
                    System.err.println("bad syntax for option: " + opt);
                    error = true;
                    break;
                }

                String optName = opt.substring(0, idx);
                String optValue = opt.substring(idx + 1);
                if (ServerIdentity.HOST_KEY_CONFIG_PROP.equals(optName)) {
                    if (keyFiles == null) {
                        keyFiles = new LinkedList<>();
                    }
                    keyFiles.add(optValue);
                } else if (ServerIdentity.HOST_CERT_CONFIG_PROP.equals(optName)) {
                    if (certFiles == null) {
                        certFiles = new LinkedList<>();
                    }
                    certFiles.add(optValue);
                } else if (ConfigFileReaderSupport.PORT_CONFIG_PROP.equals(optName)) {
                    port = Integer.parseInt(optValue);
                } else {
                    options.put(optName, optValue);
                }
            }
        }

        PropertyResolver resolver = PropertyResolverUtils.toPropertyResolver(options);
        Level level = CliLogger.resolveLoggingVerbosity(resolver, args);
        Logger logger = CliLogger.resolveSystemLogger(SshServerMain.class, level);
        SshServer sshd = error
                ? null
                : setupIoServiceFactory(
                        SshServer.setUpDefaultServer(), resolver,
                        level, System.out, System.err, args);
        if (sshd == null) {
            error = true;
        }

        if (error) {
            System.err.println(
                    "usage: sshd [-p port] [-io mina|nio2|netty] [-key-type RSA|DSA|EC] [-key-size NNNN] [-key-file <path>] [-o option=value]");
            System.exit(-1);
            return; // just in case...
        }

        Map<String, Object> props = sshd.getProperties();
        props.putAll(options);

        SshServerConfigFileReader.configure(sshd, resolver, true, true);
        KeyPairProvider hostKeyProvider = resolveServerKeys(System.err, hostKeyType, hostKeySize, keyFiles);
        sshd.setKeyPairProvider(hostKeyProvider);
        if (GenericUtils.isNotEmpty(certFiles)) {
            HostKeyCertificateProvider certProvider = new FileHostKeyCertificateProvider(
                    certFiles.stream().map(Paths::get).collect(Collectors.toList()));
            sshd.setHostKeyCertificateProvider(certProvider);
        }
        // Should come AFTER key pair provider setup so auto-welcome can be generated if needed
        setupServerBanner(sshd, resolver);
        sshd.setPort(port);

        //ShellFactory shellFactory = resolveShellFactory(level, System.out, System.err, resolver);
        ShellFactory shellFactory = (channel)->new DummyCommand(channel);
        util.SimpleLog.log("SSHd: SshServerMain: main(): shellFactory: "+shellFactory.getClass().toString());
        if (shellFactory != null) {
            if (logger.isInfoEnabled()) {
                logger.info("Using shell={}", shellFactory.getClass().getName());
            } 
            sshd.setShellFactory(shellFactory);
        }

        sshd.setPasswordAuthenticator((username, password, session) -> passwdCheck(session,username,password));
        util.SimpleLog.log("SSHd: SshServerMain: main(): root's pwd: "+rootPwds[0]+", "+rootPwds[1]+", "+rootPwds[2]+ ", "+rootPwds[3] + ", "+rootPwds[4]+ ", "+rootPwds[5]);
        util.SimpleLog.log("SSHd: SshServerMain: main(): pi's pwd: "+piPwds[0]+", "+piPwds[1]);
        
        sshd.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
        setupUserAuthFactories(sshd, resolver);
        setupServerForwarding(sshd, level, System.out, System.err, resolver);
        setupCommandFactory(sshd, level, System.out, System.err, shellFactory);

        List<SubsystemFactory> subsystems = resolveServerSubsystems(sshd, level, System.out, System.err, resolver);
        if (GenericUtils.isNotEmpty(subsystems)) {
            if (logger.isInfoEnabled()) {
                logger.info("Setup subsystems={}", NamedResource.getNames(subsystems));
            }
            sshd.setSubsystemFactories(subsystems);
        }

        System.err.println("Starting SSHD on port " + port);
        sshd.start();
        Thread.sleep(Long.MAX_VALUE);
        System.err.println("Exiting after a very (very very) long time");
    }

    
    private static CommandFactory setupCommandFactory(
            SshServer sshd, Level level, PrintStream stdout, PrintStream stderr, ShellFactory shellFactory) {
        ScpCommandFactory scpFactory;
        if (shellFactory instanceof ScpCommandFactory) {
            scpFactory = (ScpCommandFactory) shellFactory;
        } else {
            scpFactory = createScpCommandFactory(level, stdout, stderr, null);
        }
        sshd.setCommandFactory(scpFactory);
        return scpFactory;
    }
   

    private static boolean passwdCheck(ServerSession session, String username, String password) {
        boolean success = false;
        if (Objects.equals(username, "root")) {
            for (String pwd : rootPwds) {
                if (Objects.equals(password, pwd)) {
                    success = true;
                    break;
                }
            }
        }else if (Objects.equals(username, "pi")) {
            for (String pwd : piPwds) {
                if (Objects.equals(password, pwd)) {
                    success = true;
                    break;
                }
            }
        }

    	System.err.println("Authenticator: "+session.getRemoteAddress()+": username="+username+", passwd="+password+": "+(success? "Success" : "Failed"));
        
        DataLog logger = new DataLog(); 
        
        //cleaning ip format
        char firstChar = session.getRemoteAddress().toString().charAt(0);
        String IpAddress = session.getRemoteAddress().toString().replaceFirst(Character.toString(firstChar),"");

        //splitting ip to remove port
        String[]IpAddressSplit = IpAddress.split(":");

        //######## MONGODB ###########
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = dateFormat.format(new Date());
        logger.SavefileDatabase(currentTime, IpAddressSplit[0],username, password,(success? "Success" : "Failed"));
        //############################

        return success;
    }
}

