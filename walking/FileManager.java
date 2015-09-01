package lemons.api.walking;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import lemons.api.org.json.JSONArray;
import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.utils.Base64;

public class FileManager extends TaskScriptEmulator<TaskScript> {
	
	private final String URL_DOWNLOAD = "https://raw.githubusercontent.com/Lem0ns/webwalk/master/web.txt",
						 IMAGE_DOWNLOAD = "http://i.imgur.com/bY7GPOl.jpg",
						 LIVE_DOWNLOAD = "http://me.lemons2.cf:8080/web.txt";
	
	public static final int FILE_VERSION = 10;
	
	private String[] tmpLoadString;
	private int[] tmpLoadInt;
	private int[][] paths = null;
	private boolean triedDownload = false;

	private HashMap<String, Integer> cachedFlags = new HashMap<String, Integer>();

	private boolean cacheTiles = false;

	private File imageFile;

	private URL website;

	private ReadableByteChannel rbc;

	private FileOutputStream fos;

	private String filename;

	private BufferedReader br;

	private ArrayList<int[]> list;

	private String line;

	private PrintWriter writer;

	private String filename2;

	private BufferedReader br2;

	private StringBuilder b;

	private String line2;

	private StringBuilder sb;

	private InputStream is;

	private BufferedReader rd;

	private String jsonText;

	private JSONArray json;

	private File file;

	private BufferedReader br3;

	private String line3;

	private int fileVersion;

	private PrintWriter writer2;

	private boolean isDev = false, gotDev = false, isRender = false, gotRender = false;

	private PrintWriter printer;

	private HashMap<Integer, String> flagCache = new HashMap<Integer, String>();
	
	public FileManager(TaskScript s) {
		super(s);
	}
	
	public int[][] getWeb() {
		return paths;
	}

	public void loadWeb() {
		checkForWebUpdates(); 
		try {
			filename = getFilename("webwalker.txt");
			br = new BufferedReader(new FileReader(filename));
			list = new ArrayList<int[]>();
			
	        line = br.readLine();

	        while (line != null) {
	        	if (!line.isEmpty()) {
		        	tmpLoadString = line.split(",");
		        	tmpLoadInt = new int[tmpLoadString.length];
		        	
		        	for (int i = 0; i < tmpLoadString.length; i++) {
		        		if (tmpLoadString[i] == null || tmpLoadString[i].equals("null"))
		        			continue;
		        		else
		        			tmpLoadInt[i] = Integer.parseInt(tmpLoadString[i]);
		        	}
		        	
		        	list.add(tmpLoadInt);
	        	}
	        	
	            line = br.readLine();
	        }
	        
	        debug("Loaded "+list.size()+" points in getWalker()...");
	        
	        paths = list.toArray(new int[list.size()][0]);
	        
	        br.close();
		} catch (FileNotFoundException e) {
			if (!triedDownload) {
				info("Unable to read webwalker config, downloading...");
				triedDownload  = true;
				checkForWebUpdates();
				loadWeb();
			} else {
				error("Unable to download Tiles, webwalker will fail!");
				//exception(e);;
			}
		} catch (IOException e) {
			exception(e);;
	    }
	}

	public void checkForWebUpdates() {
		if (isDev()) {
			info("Downloading latest developer Tiles...");
			downloadWebTiles(LIVE_DOWNLOAD);
		} else if (!new File(getFilename("webwalker.txt")).exists()
				|| !gitHash().equalsIgnoreCase(fileHash())) {
	        info("Update found! Downloading...");
        	downloadWebTiles(URL_DOWNLOAD);
        } else {
        	info("Tiles are up to date.");
        }
	}

	private void downloadWebTiles(String url) {
		try {
			info("Downloading from "+url);
			website = new URL(url);
			rbc = Channels.newChannel(website.openStream());
			fos = new FileOutputStream(getFilename("webwalker.txt"));
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			
			writer = new PrintWriter(getFilename("version.txt"), "UTF-8");
			writer.print(gitHash());
			writer.close();
		} catch (MalformedURLException e) {
			exception(e);;
		} catch (IOException e) {
			exception(e);;
		}
	}

	public boolean isDev() {
		if (!gotDev) {
			gotDev = true;
			isDev = new File(getFilename("dev.txt")).exists();
		}
		return isDev;
	}

	private String gitHash() {
		try {
			return readJsonFromUrl("https://api.github.com/repos/Lem0ns/webwalk/commits")
					.getJSONObject(0).getString("sha");
		} catch (MalformedURLException e) {
			exception(e);;
		} catch (IOException e) {
			exception(e);;
		}
		return "Cannot connect to github!";
	}

	private String fileHash() {
		try {
			filename2 = getFilename("version.txt");
			br2 = new BufferedReader(new FileReader(filename2));
			b = new StringBuilder();
			
	        line2 = br2.readLine();
	
	        while (line2 != null) {
	        	b.append(line2);
	        	
		        line2 = br2.readLine();
	        }
	        
	        br2.close();
	        return b.toString();
		} catch (FileNotFoundException e) {
			exception(e);;
		} catch (IOException e) {
			exception(e);;
		}
		return "";
	}
		
	
	private String readAll(Reader rd) throws IOException {
	    sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	}

	public JSONArray readJsonFromUrl(String url) throws IOException {
	    is = new URL(url).openStream();
	    try {
	      rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      jsonText = readAll(rd);
	      json = new JSONArray(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	}

	private String getFilename(String file) {
		return getFilePath()+ File.separator + file;
	}
	
	private String getFilePath() {
        file = new File(System.getProperty("user.home")  + File.separator + "OSBot"
                + File.separator + "dudeami" + File.separator + "web" + File.separator);
        if(!file.exists())
            file.mkdirs();
        return file.getPath();
	}
	
	public HashMap<String, Integer> loadRegionCache() {
		if (!cacheTiles)
			return new HashMap<String, Integer>();
		try {
			br3 = new BufferedReader(new FileReader(getFilename("tilecache.txt")));
			
	        line3 = br3.readLine();
	        
	        fileVersion = -1;
	
	        while (line3 != null && !line3.isEmpty()) {
	        	if (fileVersion == -1) {
	        		try {
	        			fileVersion = Integer.parseInt(line3);
	        		} catch (NumberFormatException e1) { }
        			if (fileVersion != FILE_VERSION) {
        				info("Outdated cache, ignoring!");
        				br3.close();
        				return new HashMap<String, Integer>();
        			}
	        	} else {
		        	if (!cachedFlags.containsKey(line3.substring(0, 5)))
		        		cachedFlags.put(line3.substring(0, 5), Base64.decode(line3.substring(5, line3.length())));
	        	}
	            line3 = br3.readLine();
	        }
	        
	        br3.close();
	        return cachedFlags;
		} catch (FileNotFoundException e) {
			info("No cached data found!");
		} catch (Exception e) {
			exception(e);;
		}
		return new HashMap<String, Integer>();
	}
	
	public void saveRegionCache(HashMap<String, Integer> flags) {
		//if (lockFile.exists()) {
		//	info("ERROR - Could not save Region Cache!");
		//	return;
		//}
		try {
			//lockFile.createNewFile();
			
			writer2 = new PrintWriter(getFilename("tilecache.txt"), "UTF-8");
			writer2.println(FILE_VERSION);
			for (String key : flags.keySet()) {
				writer2.println(key+encodeFlag(flags.get(key)));
			}
			writer2.close();
			
			/*
		    OutputStream file = new FileOutputStream(getFilename("tilecache.ser"));
		    OutputStream buffer = new BufferedOutputStream(file);
		    ObjectOutput output = new ObjectOutputStream(buffer);
		    output.writeObject(flags);
		    output.close();
		    */
			//lockFile.delete();
		} catch (IOException e) {
			exception(e);;
		} catch (SecurityException e) {
			exception(e);;
		}
	}

	private String encodeFlag(Integer integer) {
		if (!flagCache.containsKey(integer))
			flagCache .put(integer, Base64.encode(integer));
		return flagCache.get(integer);
	}

	public BufferedImage getMapImage() {
		debug("Loading map...");
		try {
			imageFile = new File(getFilename("map.jpg"));
			if (!imageFile.exists()) {
				info("Downloading oldschool map...");
				website = new URL(IMAGE_DOWNLOAD);
				rbc = Channels.newChannel(website.openStream());
				fos = new FileOutputStream(getFilename("map.jpg"));
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
			return ImageIO.read(imageFile);
		} catch (IOException e) {
			exception(e);;
		}
		return null;
	}

	public boolean isRender() {
		if (!gotRender) {
			gotRender = true;
			isRender = new File(getFilename("render.txt")).exists();
		}
		return isRender;
	}

	public PrintWriter getLogPrinter() {
		if (printer == null)
			try {
				printer = new PrintWriter(getFilename("log.txt"), "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				exception(e);;
			}
		return printer;
	}


}
