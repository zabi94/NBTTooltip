package zabi.minecraft.nbttooltip;

public class ConfigInstance {

	public boolean showSeparator;
	public int maxLinesShown;
	public boolean requiresf3;
	public boolean showDelimiters;
	public boolean compress;
	public int ticksBeforeScroll;
	public boolean ctrlSuppressesRest;
	public boolean splitLongLines;
	
	public ConfigInstance(boolean showSeparator, int maxLinesShown, boolean requiresf3, boolean showDelimiters, boolean compress, int ticksBeforeScroll, boolean ctrlSuppressesRest, boolean splitLongLines) {
		super();
		this.showSeparator = showSeparator;
		this.maxLinesShown = maxLinesShown;
		this.requiresf3 = requiresf3;
		this.showDelimiters = showDelimiters;
		this.compress = compress;
		this.ticksBeforeScroll = ticksBeforeScroll;
		this.ctrlSuppressesRest = ctrlSuppressesRest;
		this.splitLongLines = splitLongLines;
	}
	
}
