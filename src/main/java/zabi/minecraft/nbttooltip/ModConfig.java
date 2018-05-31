package zabi.minecraft.nbttooltip;

import net.minecraftforge.common.config.Config;

@Config(modid=NBTTooltip.MOD_ID, category="Clientside")
public class ModConfig {
	
	@Config.Comment("How many ticks have to pass before the next line is shown")
	@Config.RangeInt(min=1, max=400)
	public static int ticksBeforeScroll = 20; 
	
	@Config.Comment("How many lines are shown at once. Anything greater than this will scroll")
	@Config.RangeInt(min=1, max=100)
	public static int maxLinesShown = 10; 
	
	@Config.Comment("The opening method for the reader window. BOTH means both sides (will open 2 windows when playing SP), CLIENT means client only (will do nothing in servers), SERVER means server only, DISABLED disables the interaction entirely")
	public static EnumFetchType fetchType = EnumFetchType.BOTH;
	
	@Config.Comment("If set to false it will show the NBT tag regardless of the F3+H status")
	public static boolean requiresf3 = true;
	
	@Config.Comment("Set this to false to hide the purple delimiters in the item tags")
	public static boolean showDelimiters = true;
	
	@Config.Comment("Set this to true to see the TAG all in the same line")
	public static boolean compress = false;

	@Config.Comment("Set this to true to add an introductory line in the tooltip")
	public static boolean showSeparator = true;
	
	public static enum EnumFetchType {
		DISABLED, SERVER, CLIENT, BOTH
	}
}
