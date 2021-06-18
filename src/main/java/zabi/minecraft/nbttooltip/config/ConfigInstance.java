package zabi.minecraft.nbttooltip.config;

import zabi.minecraft.nbttooltip.parse_engine.BWHumanReadableParser;
import zabi.minecraft.nbttooltip.parse_engine.ColoredHumanReadableParser;
import zabi.minecraft.nbttooltip.parse_engine.JsonParser;
import zabi.minecraft.nbttooltip.parse_engine.NativeParser;
import zabi.minecraft.nbttooltip.parse_engine.NbtTagParser;

public class ConfigInstance {

	public TriggerType triggerType;
	public boolean showSeparator;
	public int maxLinesShown;
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
		triggerType = TriggerType.F3H;
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
		
		TooltipEngine(NbtTagParser parser) {
			this.parser = parser;
		}
		
		public NbtTagParser getEngine() {
			return parser;
		}
	}
	
	public static enum CopyingEngine {
		
		FRIENDLY(new BWHumanReadableParser()), 
		MC_GIVE_CMD(new NativeParser()),
		JSON(new JsonParser());
		
		private NbtTagParser parser;
		
		CopyingEngine(NbtTagParser parser) {
			this.parser = parser;
		}
		
		public NbtTagParser getEngine() {
			return parser;
		}
	}
	
}
