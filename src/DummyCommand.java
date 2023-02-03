import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import util.DataLogTxt;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;


import filesystem.File;
import filesystem.Directory;


public class DummyCommand implements Command {
	
	public static boolean VERBOSE= true;
	

	private void log(String msg) {
		System.out.println("Test SSHd: "+DummyCommand.class.getSimpleName()+": "+channel.getSession().getRemoteAddress()+": "+msg);
		DataLogTxt logger = new DataLogTxt();
		char firstChar = channel.getSession().getRemoteAddress().toString().charAt(0);
		logger.logToFileDummyCommand("Session IP: "+channel.getSession().getRemoteAddress().toString().replaceFirst(Character.toString(firstChar), "")+": "+msg);
	}
	

	public static String PROMPT= "$ ";
	
	private ChannelSession channel;
	private InputStream in;
	private OutputStream out, err;
	private ExitCallback callback;
	private StringBuffer commandLine= new StringBuffer();
	private Directory localDir= Directory.createRootDirectory();
	private String username;

	
	public DummyCommand(ChannelSession channel) {
		this.channel= channel;
		log("DummyCommand()");
		username= channel.getSessionContext().getUsername();
		try {
			localDir.createDirectory("etc");
			localDir.createDirectory("bin");
			localDir.createDirectory("lib");
			Directory usr= localDir.createDirectory("usr");
			usr.createDirectory("bin");
			usr.createDirectory("lib");
			localDir.createDirectory("var");
			localDir.createDirectory("root");
			Directory home= localDir.createDirectory("home");
			home.createDirectory("alice");
			home.createDirectory("bob");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void run() {
		try {
			while(in!=null) {
				// echo all characters
				int c= in.read();
				if (c>0) {
					if (c == 0x08 || c == 0x7F){
						if(commandLine.length() > 0){
							out.write(c);
							out.flush();
							commandLine.deleteCharAt(commandLine.length()-1);
						}
					}
					else{
						out.write(c);
						out.flush();
						commandLine.append((char)c);
						if(c=='\r') {
							out.write('\n');
							out.flush();
							String command= commandLine.toString().trim();
							commandLine.delete(0,commandLine.length());
							handleCommand(command);
						}	
					}			
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleCommand(String command) {
		log("command: "+command);
		if (command.length()==0) {
			printPrompt();
		}
		else
		if (command.equals("exit")) {
			if (callback!=null) callback.onExit(0,"Requested to exit"); 
		}
		

		// list files and directories command
		else
		if (command.equals("ls")) {
			StringBuffer sb= new StringBuffer();		
			for (Directory d : localDir.getDirectories()) sb.append(d).append(' ');
			for (File f : localDir.getFiles()) sb.append(f).append(' ');
			String commandOutput= sb.toString();
			log("output: "+commandOutput);
			printOut(commandOutput+"\r\n");
			printPrompt();
		}
		else
		if (command.startsWith("cd")) {
			String[] params= command.split(" ");
			if (params.length>1) {
				String name= params[1].trim();
				if (name.equals(".")) {
					// do nothing
				}
				else
				if (name.equals("..")) {
					if (!localDir.isRoot()) localDir= localDir.getParentDirectory();
				}
				else {
					Directory newDir= localDir.getDirectory(name);
					if (newDir!=null) localDir= newDir;
					else printOut("Directory '"+name+"' not found\r\n");
				}
			}
			printPrompt();
		}
		
		// clear command  
		else
		if(command.equals("clear")){
			printOut("\033[H\033[2J");
			log("output: page cleared");
			printPrompt();    
		}
		
		// mkdir command
		else
		if(command.startsWith("mkdir")){
			String[] params= command.split(" ");
			if (params.length>1) {
				String name= params[1].trim();
				if (name.equals("-h")) {
					printOut("Usage: mkdir [OPTIONS]... DIRECTORY...\r\n");
					printOut("Create the DIRECTORY, if they do not already exist.\r\n");
					printOut("\t-m MODE Mode;\r\n");
					printOut("\t-p no error if existing, make parent directories as needed;\r\n");
					printOut("\t-v print a message for each created directory;\r\n");
					printOut("\t-h display this help and exit;\r\n\n");
					printOut("Full documentation <https://www.gnu.org/software/coreutils/mkdir>\r\n");
					log("output: "+ command + " description");
				}
				else
				if (name.equals("--help")) {
					printOut("Usage: mkdir [OPTIONS]... DIRECTORY...\r\n");
					printOut("Create the DIRECTORY, if they do not already exist.\r\n");
					printOut("\t-m MODE Mode;\r\n");
					printOut("\t-p no error if existing, make parent directories as needed;\r\n");
					printOut("\t-v print a message for each created directory;\r\n");
					printOut("\t--help display this help and exit;\r\n\n");
					printOut("Full documentation <https://www.gnu.org/software/coreutils/mkdir>\r\n");
					log("output: "+ command + " description");
				}
				else {
					try {
						localDir.createDirectory(name);
					} catch (IOException e) {
						e.printStackTrace();
					}
					printOut("Directory '"+name+"' created \r\n");
					log("output: Directory '"+name+"' created");
				}
			}
			printPrompt();
		}
		
		// rm command
		else
		if(command.startsWith("rm")){
			String[] params= command.split(" ");
			if (params.length>1) {
				String name= params[1].trim();
				if (name.equals("-h")) {
					printOut("Usage: rm [OPTIONS]... DIRECTORY...\r\n");
					printOut("Create the DIRECTORY, if they do not already exist.\r\n");
					printOut("\t-p remove DIRECTORY and its ancestors;\r\n");
					printOut("\t-v output a diagnostic for every directory processed;\r\n");
					printOut("\t-h display this help and exit;\r\n\n");
					printOut("Full documentation <https://www.gnu.org/software/coreutils/rmdir>\r\n");
					log("output: "+ command + " description");
				}
				else
				if (name.equals("--help")) {
					printOut("Usage: rm [OPTIONS]... DIRECTORY...\r\n");
					printOut("Create the DIRECTORY, if they do not already exist.\r\n");
					printOut("\t-p remove DIRECTORY and its ancestors;\r\n");
					printOut("\t-v output a diagnostic for every directory processed;\r\n");
					printOut("\t--help display this help and exit;\r\n\n");
					printOut("Full documentation <https://www.gnu.org/software/coreutils/rmdir>\r\n");
					log("output: "+ command + " description");
				}
				else {
					try {
						localDir.delDirectory(name);
					} catch (IOException e) {
						e.printStackTrace();
					}
					printOut("Directory '"+name+"' removed \r\n");
					log("output: Directory '"+name+"' removed ");
				}
			}
			printPrompt();
		}
		
		// pwd command
		else 
		if(command.equals("pwd")){
			printOut(localDir.getPath()+"\r\n");
			log("output: "+ localDir.getPath());
			printPrompt();
		}
		
		// whoami command
		else
		if(command.startsWith("whoami")){
			String[] params= command.split(" ");
			if (params.length>1) {
				String name= params[1].trim();
				if (name.equals("-h")) {
					printOut("Usage: whoami [OPTIONS]...\r\n");
					printOut("Print the user name associated with the current effective user ID.\r\n");
					printOut("\t-h display this help and exit;\r\n\n");
					printOut("Full documentation <https://www.gnu.org/software/coreutils/whoami>\r\n");
					log("output: "+ command + " description");
				}
				else
				if (name.equals("--help")) {
					printOut("Usage: whoami [OPTIONS]...\r\n");
					printOut("Print the user name associated with the current effective user ID.\r\n");
					printOut("\t-h display this help and exit;\r\n\n");
					printOut("Full documentation <https://www.gnu.org/software/coreutils/whoami>\r\n");
					log("output: "+ command + " description");
				}
			}
			else {
				printOut(channel.getSession().getUsername().toString() + "\r\n");
				log("output: " + channel.getSession().getUsername().toString());
			}
			printPrompt();
		}
		
		// echo command 
		else
		if(command.startsWith("echo")){
			String[] params= command.split("echo ");
			String text= params[1].trim();
			if (params.length>1) {
				printOut(text + "\r\n");
				log("output: "+ text);
			}
			printPrompt();
		}
		
		//list of commands that are not usable by the user 
		else
		if (command.startsWith("passwd") || command.startsWith("iptables") || command.startsWith("cat") || 
			command.startsWith("grep") || command.startsWith("sudo") || command.startsWith("halt")) {
			printOut("Permission denied! You can't use the command " + command + "\r\n");	
			log("output: " + command + " Permission denied");	
			printPrompt();
		}	

		//help command
		else 
		if(command.equals("help")){
			printOut("\r\n\texit\tsudo\r\n");
			printOut("\thalt\tcat\r\n");
			printOut("\tls\tgrep\r\n");
			printOut("\tcd\tpasswd\r\n");
			printOut("\tclear\tiptables\r\n");
			printOut("\tmkdir\r\n");
			printOut("\trm\r\n");
			printOut("\tpwd\r\n");
			printOut("\twhoami\r\n");
			printOut("\techo\r\n\n");

			log("output: list of available command");
			printPrompt();
		}

		else{
			String commandOutput= command+": command not found.";
			log("output: "+commandOutput);
			printOut(commandOutput+"\r\n");
			printPrompt();
		}
		
	}
	
	@Override
	public void start(ChannelSession channel, Environment env) throws IOException {
		log("start()");
		new Thread(this::run).start();
		this.channel= channel;
	}

	@Override
	public void destroy(ChannelSession channel) throws Exception {
		log("destroy()");
		in= null;
	}

	@Override
	public void setInputStream(InputStream in) {
		log("setInputStream()");
		this.in= in;
	}

	@Override
	public void setOutputStream(OutputStream out) {
		log("setOutputStream()");
		this.out= out;
		printOut("\r\nWelcome to Ubuntu 22.04.1 LTS (GNU/Linux 5.15.0-57-generic x86_64)\r\n");
		printOut(" * Documentation:  https://help.ubuntu.com\r\n");
		printOut(" * Management:     https://landscape.canonical.com\r\n");
		printOut(" * Support:        https://ubuntu.com/advantage\r\n");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = dateFormat.format(new Date());
		printOut("\r\n System information as " + currentTime + " UTC\r\n");
		printOut("\r\n  System load:           0.080078125\r\n");
		printOut("  Usage of /:            23.5% of 24.04GB\r\n");
		printOut("  Memory usage:          34%\r\n");
		printOut("  Swap usage:            6%\r\n");
		printOut("  Processes:             101\r\n");
		printOut("  Users logged in:       1\r\n");
		printOut("  IPv4 address for eth0: 172.104.249.194\r\n");
		printOut("  IPv6 address for eth0: 2a01:7e01::f03c:93ff:feca:a2fc\r\n");
		char firstChar = channel.getSession().getRemoteAddress().toString().charAt(0);
		printOut("\r\nLast login: "+ currentTime + " from "+ channel.getSession().getRemoteAddress().toString().replaceFirst(Character.toString(firstChar), "") + "\r\n");

		printPrompt();
	}
	
	private void printPrompt() {
		printOut(username+"@myhomeserver:"+localDir.getPath()+PROMPT);
	}

	private void printOut(String str) {
		try {
			out.write(str.getBytes());
			out.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setErrorStream(OutputStream err) {
		log("setErrorStream()");
		this.err= err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		log("setExitCallback()");
		this.callback= callback;
	}

}
