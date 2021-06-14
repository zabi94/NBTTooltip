package zabi.minecraft.nbttooltip.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import zabi.minecraft.nbttooltip.config.ConfigInstance.CopyingEngine;
import zabi.minecraft.nbttooltip.config.ConfigInstance.TooltipEngine;


public class ModConfig {

    private static final File folder = new File("config");
    private static File configFile;
    private static final Gson config = new GsonBuilder().setPrettyPrinting().create();
    public static ConfigInstance INSTANCE;

    public static void init() {
        loadDefaults();
        generateFoldersAndFiles();
        readJson();
        if (INSTANCE.triggerType == null) {
            INSTANCE.triggerType = TriggerType.F3H;
        }
        if (INSTANCE.tooltipEngine == null) {
            INSTANCE.tooltipEngine = TooltipEngine.FRIENDLY;
        }
        if (INSTANCE.copyingEngine == null) {
            INSTANCE.copyingEngine = CopyingEngine.FRIENDLY;
        }
        writeJson(); //Write to file new config options
    }

    public static void loadDefaults() {
        INSTANCE = new ConfigInstance();
    }

    private static void generateFoldersAndFiles() {
        if (!folder.exists()) {
            folder.mkdir();
        }
        if (folder.isDirectory()) {
            configFile = new File(folder, "nbttooltip.json");

            File legacyConfigFile = new File(folder, "nbttooltip.config");
            if (legacyConfigFile.exists() && legacyConfigFile.isFile() && ! configFile.exists()) {
                if (!legacyConfigFile.renameTo(configFile)) {
                    System.err.format("Could not rename legacy config file %s to %s\n", legacyConfigFile.getAbsolutePath(), configFile.getAbsolutePath());
                } else {
                    System.out.println("Successfully converted legacy config file");
                }
            }


            if (!configFile.exists()) {
                System.out.println("Creating new config file");
                try {
                    configFile.createNewFile();
                    loadDefaults();
                    String json = config.toJson(INSTANCE);
                    FileWriter writer = new FileWriter(configFile);
                    writer.write(json);
                    writer.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Can't create config file", e);
                }
            } else if (configFile.isDirectory()) {
                throw new IllegalStateException("'nbttooltip.json' must be a file!");
            }
        } else {
            throw new IllegalStateException("'config' must be a folder!");
        }
    }

    public static void readJson() {
        try {
            INSTANCE = config.fromJson(new FileReader(configFile), ConfigInstance.class);
            if (INSTANCE == null) {
                throw new IllegalStateException("Null configuration");
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid configuration!");
            e.printStackTrace();
        } catch (JsonIOException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // No op
        }
    }

    public static void writeJson() {
        try {
            String json = config.toJson(INSTANCE);
            FileWriter writer = new FileWriter(configFile, false);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Can't update config file", e);
        }
    }

}
