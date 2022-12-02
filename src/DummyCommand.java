import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
		//System.out.println("Test SSHd: "+DummyCommand.class.getSimpleName()+": "+msg);
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
					out.write(c);
					out.flush();
					commandLine.append((char)c);
					if (c=='\r') {
						out.write('\n');
						out.flush();
						String command= commandLine.toString().trim();
						commandLine.delete(0,commandLine.length());
						handleCommand(command);
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
		else
		if (command.equals("halt")) {
			if (callback!=null) callback.onExit(0,"Requested to halt");
			//da rimuovere BUTTA GIU' IL SERVER 
			System.exit(0);
		}
		else
		if (command.equals("ls")) {
			StringBuffer sb= new StringBuffer();
			//sb.append(". ");
			//if (!localDir.isRoot()) sb.append(".. ");			
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
			//printOut("\r\n");
			printPrompt();
		}
		
		// Clear command  
		else
		if(command.equals("clear")){
			log("output: "+ command);
			printOut("\033[H\033[2J");
			printPrompt();    
		}
		
		// MKDIR command
		else
		if(command.startsWith("mkdir")){
			String[] params= command.split(" ");
			if (params.length>1) {
				String name= params[1].trim();
				if (name.equals("-h")) {
					printOut("Usage: mkdir [OPTIONS] DIRECTORY...\n");
					printOut("Create DIRECTORY\n");
					printOut("-m MODE Mode\t");
					printOut("-p no error if existing, make parent directories as needed\t");
					printOut("-v print a message for each created directory\n");
					printPrompt();
				}
				else
				if (name.equals("--help")) {
					printOut("Usage: mkdir [OPTIONS] DIRECTORY...\n");
					printOut("Create DIRECTORY\n\t");
					printOut("-m MODE Mode");
					printOut("-p no error if existing, make parent directories as needed\n");
					printOut("-v print a message for each created directory\n");
					printPrompt();
				}
				else {
					String commandOutput= command+": command not found.";
					log("output: "+commandOutput);
					printOut(commandOutput+"\r\n");
				}
			}
			/* 
			//mkdir function
			try {
				Directory home= localDir.createDirectory("home");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
		// RMDIR command
		else
		if(command.equals("rmdir")){
			log("output: "+ command);
			
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
		printOut("Wellcome to this honeypot ;)\r\n");
		printPrompt();
	}
	
	private void printPrompt() {
		printOut(username+"@honeypot:"+localDir.getPath()+PROMPT);
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
