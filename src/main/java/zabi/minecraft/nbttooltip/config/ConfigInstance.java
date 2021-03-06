package zabi.minecraft.nbttooltip.config;

import zabi.minecraft.nbttooltip.parse_engine.BWHumanReadableParser;
import zabi.minecraft.nbttooltip.parse_engine.ColoredHumanReadableParser;
import zabi.minecraft.nbttooltip.parse_engine.JsonParser;
import zabi.minecraft.nbttooltip.parse_engine.NbtTagParser;

public class ConfigInstance {

	public boolean showSeparator;
	public int maxLinesShown;
	public boolean requiresf3;
	public boolean showDelimiters;
	public boolean compress;
	public int ticksBeforeScroll;
	public boolean ctrlSuppressesRest;
	public boolean splitLongLines;
	
	public TooltipEngine tooltipEngine;
	public CopyingEngine copyingEngine;
	
	public ConfigInstance() {
		maxLinesShown = 10;
		compress = false;
		requiresf3 = true;
		showDelimiters = true;
		showSeparator = true;
		ticksBeforeScroll = 20;
		ctrlSuppressesRest = true;
		splitLongLines = true;
		tooltipEngine = TooltipEngine.FRIENDLY;
		copyingEngine = CopyingEngine.JSON;
	}
	
	public static enum TooltipEngine {
		
		FRIENDLY(new ColoredHumanReadableParser()), 
		FRIENDLY_NO_COLOR(new BWHumanReadableParser()), 
		JSON(new JsonParser());
		
		private NbtTagParser parser;
		
		private TooltipEngine(NbtTagParser parser) {
			this.parser = parser;
		}
		
		public NbtTagParser getEngine() {
			return parser;
		}
	}
	
	public static enum CopyingEngine {
		
		FRIENDLY(new BWHumanReadableParser()), 
		JSON(new JsonParser());
		
		private NbtTagParser parser;
		
		private CopyingEngine(NbtTagParser parser) {
			this.parser = parser;
		}
		
		public NbtTagParser getEngine() {
			return parser;
		}
	}
	
}
