package zabi.minecraft.nbttooltip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public class ModConfig {

	public static boolean showSeparator;
	public static int maxLinesShown;
	public static boolean requiresf3;
	public static boolean showDelimiters;
	public static boolean compress;
	public static int ticksBeforeScroll;
	public static boolean ctrlSuppressesRest;
	
	private static File folder = new File("config");
	private static Gson config = new GsonBuilder().setPrettyPrinting().create();
	
	public static void init() {
		loadDefaults();
		generateFoldersAndFiles();
		readJson();
	}

	public static void loadDefaults() {
//		System.out.println("Loading default config");
		ModConfig.maxLinesShown = 10;
		ModConfig.compress = false;
		ModConfig.requiresf3 = true;
		ModConfig.showDelimiters = true;
		ModConfig.showSeparator = true;
		ModConfig.ticksBeforeScroll = 20;
		ModConfig.ctrlSuppressesRest = true;
	}
	
	private static void generateFoldersAndFiles() {
//		System.out.println("Generating config tree");
		if (!folder.exists()) {
			folder.mkdir();
//			System.out.println("Created 'config' folder");
		}
		if (folder.isDirectory()) {
//			System.out.println("Found configs folder");
			File configFile = new File(folder, "NBTTooltip.config");
			if (!configFile.exists()) {
				try {
//					System.out.println("Creating new config file");
					configFile.createNewFile();
					ConfigInstance def = new ConfigInstance(showSeparator, maxLinesShown, requiresf3, showDelimiters, compress, ticksBeforeScroll, ctrlSuppressesRest);
					String json = config.toJson(def);
					FileWriter writer = new FileWriter(configFile);
					writer.write(json);
					writer.close();
//					System.out.println("Written default settings: "+json);
				} catch (IOException e) {
					throw new IllegalStateException("Can't create config file", e);
				}
			} else if (configFile.isDirectory()) {
				throw new IllegalStateException("'NBTTooltip.config' must be a file!");
			} else {
//				System.out.println("Found existing config file");
			}
		} else {
			throw new IllegalStateException("'config' must be a folder!");
		}
	}
		
	public static void readJson() {
//		System.out.println("Reading config file");
		File configFile = new File(folder, "NBTTooltip.config");
		try {
			ConfigInstance instance = config.fromJson(new FileReader(configFile), ConfigInstance.class);
			
			if (instance == null) {
				throw new IllegalStateException("Null configuration");
			}
			
			showDelimiters = instance.showDelimiters;
			showSeparator = instance.showSeparator;
			ticksBeforeScroll = instance.ticksBeforeScroll;
			maxLinesShown = instance.maxLinesShown;
			compress = instance.compress;
			requiresf3 = instance.requiresf3;
			ctrlSuppressesRest = instance.ctrlSuppressesRest;
			
		} catch (JsonSyntaxException e) {
			System.err.println("Invalid configuration!");
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// No op
		}
	}
		
}
