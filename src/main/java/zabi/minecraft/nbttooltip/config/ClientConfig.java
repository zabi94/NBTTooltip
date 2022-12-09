package zabi.minecraft.nbttooltip.config;

import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.compress.utils.Lists;
import zabi.minecraft.nbttooltip.NBTTooltipClient;
import zabi.minecraft.nbttooltip.parse_engine.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientConfig {
	public static final ClientConfig INSTANCE = new ClientConfig();

	private final List<ConfigData<?>> data = Lists.newArrayList();

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
	
	private ClientConfig() {
		this.maxLinesShown = 10;
		this.compress = false;
		this.triggerType = TriggerType.F3H;
		this.showDelimiters = true;
		this.showSeparator = true;
		this.ticksBeforeScroll = 20;
		this.ctrlSuppressesRest = true;
		this.splitLongLines = true;
		this.tooltipEngine = TooltipEngine.FRIENDLY;
		this.copyingEngine = CopyingEngine.JSON;
	}

	public ForgeConfigSpec buildSpec() {
		this.data.clear();
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		accept(builder.comment("Prints '- NBTTooltip -' before the NBT tag.", "This tag doesn't scroll with the text.").define("Show separator", this.showSeparator), v -> this.showSeparator = v);
		accept(builder.comment("The text won't have indentation and will.", "all align to the left margin.").define("Compress text horizontally", this.compress), v -> this.compress = v);
		accept(builder.comment("Determines when the tooltip should be shown. The key used for", "key-based triggers is configurable through the controls menu.").defineEnum("Trigger Type", this.triggerType), v -> this.triggerType = v);
		accept(builder.comment("Prints '- NBT start -' and '- NBT end -'.", "inside the tag, scrolling with the rest.").define("Show delimiters", this.showDelimiters), v -> this.showDelimiters = v);
		accept(builder.comment("Allows to show just the NBT part of a tooltip", "when holding CTRL down, hiding the rest.").define("Just NBT with CTRL", this.ctrlSuppressesRest), v -> this.ctrlSuppressesRest = v);
		accept(builder.comment("Divides long lines into multiple ones and", "places a sing at the beginning of each part.").define("Split long lines", this.splitLongLines), v -> this.splitLongLines = v);
		accept(builder.comment("The maximum amount of lines shown in a", "tooltip before it starts scrolling.").define("Maximum NBT lines", this.maxLinesShown), v -> this.maxLinesShown = v);
		accept(builder.comment("The amount of ticks after which a new line", "is shown when the tooltip is too long.").define("Scroll delay", this.ticksBeforeScroll), v -> this.ticksBeforeScroll = v);
		accept(builder.comment("FRIENDLY uses a human-readable format for tooltips", "JSON uses the standard JSON format for files.").defineEnum("Tooltip presentation", this.tooltipEngine), v -> this.tooltipEngine = v);
		accept(builder.comment("FRIENDLY uses a human-readable format to copy nbt", "JSON uses the standard JSON format for files.").defineEnum("Copy format", this.copyingEngine), v -> this.copyingEngine = v);
		return builder.build();
	}

	private static <T> void accept(ForgeConfigSpec.ConfigValue<T> value, Consumer<T> consumer) {
		INSTANCE.data.add(new ConfigData<>(value, consumer));
	}

	public void sync() {
		this.data.forEach(ConfigData::sync);
	}

	private record ConfigData<T>(ForgeConfigSpec.ConfigValue<T> value, Consumer<T> consumer) {

		public void sync() {
			this.consumer.accept(this.value.get());
		}
	}
	
	public enum TooltipEngine {
		
		FRIENDLY(new ColoredHumanReadableParser()), 
		FRIENDLY_NO_COLOR(new BWHumanReadableParser()), 
		JSON(new JsonParser());
		
		private final NbtTagParser parser;
		
		TooltipEngine(NbtTagParser parser) {
			this.parser = parser;
		}
		
		public NbtTagParser getEngine() {
			return this.parser;
		}
	}
	
	public enum CopyingEngine {
		
		FRIENDLY(new BWHumanReadableParser()), 
		MC_GIVE_CMD(new NativeParser()),
		JSON(new JsonParser());
		
		private final NbtTagParser parser;
		
		CopyingEngine(NbtTagParser parser) {
			this.parser = parser;
		}
		
		public NbtTagParser getEngine() {
			return this.parser;
		}
	}

	public enum TriggerType {

		F3H(TooltipFlag::isAdvanced),
		ALWAYS_ON(ctx -> true),
		TOGGLE_ON_KEY(ctx -> NBTTooltipClient.nbtKeyToggled),
		SHOW_ON_KEY(ctx -> NBTTooltipClient.nbtKeyPressed);

		private final Function<TooltipFlag, Boolean> test;

		TriggerType(Function<TooltipFlag, Boolean> check) {
			this.test = check;
		}

		public boolean shouldShowTooltip(TooltipFlag context) {
			return this.test.apply(context);
		}

	}
}
