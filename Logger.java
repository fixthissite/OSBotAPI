package lemons.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;

public class Logger extends TaskScriptEmulator<TaskScript> {
	
	private LogLevel level = LogLevel.DEBUG;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS"),
			 file_sdf = new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss");

	private File file;

	private String filename;

	private PrintWriter out;

	private boolean useOSBotDebug = false;

	public Logger(TaskScript s) {
		super(s);
		try {
			out = new PrintWriter(filename(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setLevel(LogLevel level) {
		this.level = level;
	}
	
	private void log(LogLevel level, String method, String message) {
		// Format
		String str = "["+getBot().getId()+"-"
						+level.toString()
						+ "-"+sdf.format(new Date())
						+ " >"+method
						+"] "
					+message;
		out.println(str);
		out.flush();
		if (useOSBotDebug && level.priority() >= this.level.priority()) {
			switch (level) {
			case DEBUG:
				getScript().logger.debug("["+method+"] "+message);
				break;
			case ERROR:
				getScript().logger.error("["+method+"] "+message);
				break;
			case EXCEPTION:
				getScript().logger.error("["+method+"] "+message);
				break;
			case INFO:
				getScript().logger.info("["+method+"] "+message);
				break;
			case WARNING:
				getScript().logger.warn("["+method+"] "+message);
				break;
			default:
				break;
			}
		} else {
			System.out.println(str);
		}
	}

	private String filename() {
		if (filename == null) {
			filename = getScript().getClass().getName()+"_"+file_sdf.format(new Date())+".txt";
		}
		return getFilePath()+ File.separator + filename;
	}
	
	private String getFilePath() {
        file = new File(System.getProperty("user.home")  + File.separator + "OSBot"
                + File.separator + "dudeami" + File.separator + "logs" + File.separator);
        if(!file.exists())
            file.mkdirs();
        return file.getPath();
	}

	public void debug(String string) {
		log(LogLevel.DEBUG, string);
	}

	public void info(String string) {
		log(LogLevel.INFO, string);
	}

	public void warning(String string) {
		log(LogLevel.WARNING, string);
	}

	public void error(String string) {
		log(LogLevel.ERROR, string);
	}
	
	private void log(LogLevel l, String string) {
		String name = "???";
		try {
			throw new Exception();
		} catch (Exception e) {
			for (int i = 2; i < 4; i++) {
				if (e.getStackTrace()[i].getMethodName().equals(l.toString().toLowerCase()))
					continue;
				name = e.getStackTrace()[i].getClassName();
				name = name.substring(name.lastIndexOf(".")+1)+"."+e.getStackTrace()[i].getMethodName();
				break;
			}
		}
		log(l, name, string);
	}
	
	public void exception(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		log(LogLevel.EXCEPTION, sw.toString()); // stack trace as a string
	}
	
	public boolean isDebug() {
		return level == LogLevel.DEBUG;
	}
	
	public boolean isInfo() {
		return isDebug() || level == LogLevel.INFO;
	}
	
	public boolean isWarning() {
		return isDebug() || isInfo() || level == LogLevel.WARNING;
	}
	
	public boolean isError() {
		return isDebug() || isInfo() || isError() || level == LogLevel.ERROR;
	}
	
	private enum LogLevel {
		DEBUG(0),
		INFO(1),
		WARNING(2),
		ERROR(3),
		EXCEPTION(4);

	    private final int priority; // in meters
	    LogLevel(int order) {
	        this.priority = order;
	    }
	    public int priority() { return priority; }
	}
	
}
