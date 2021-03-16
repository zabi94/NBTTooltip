package zabi.minecraft.nbttooltip.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.text.TranslatableText;
import zabi.minecraft.nbttooltip.config.ConfigInstance.CopyingEngine;
import zabi.minecraft.nbttooltip.config.ConfigInstance.TooltipEngine;

public class ConfigScreenProvider implements ModMenuApi {
	
	
	public static ConfigBuilder builder() {
		
		ConfigBuilder configBuilder = ConfigBuilder.create()
				.setTitle(new TranslatableText("key.category.nbttooltip"))
				.setEditable(true)
				.setSavingRunnable(() -> ModConfig.writeJson());
		
		ConfigCategory general = configBuilder.getOrCreateCategory(new TranslatableText("nbttooltip.config.general"));
		
		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(new TranslatableText("nbttooltip.config.showseparator") , ModConfig.INSTANCE.showSeparator)
					.setDefaultValue(true)
					.setTooltip(
							new TranslatableText("nbttooltip.config.showseparator.line1"),
							new TranslatableText("nbttooltip.config.showseparator.line2") 
					)
					.setSaveConsumer(val -> {ModConfig.INSTANCE.showSeparator = val;})
					.build()
		);
		
		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(new TranslatableText("nbttooltip.config.compress") , ModConfig.INSTANCE.compress)
					.setDefaultValue(false)
					.setTooltip(
							new TranslatableText("nbttooltip.config.compress.line1"),
							new TranslatableText("nbttooltip.config.compress.line2") 
					)
					.setSaveConsumer(val -> {ModConfig.INSTANCE.compress = val;})
					.build()
		);
		
		general.addEntry(configBuilder.entryBuilder()
				.startEnumSelector(new TranslatableText("nbttooltip.config.triggerType"), TriggerType.class, ModConfig.INSTANCE.triggerType)
					.setDefaultValue(TriggerType.F3H)
					.setTooltip(
							new TranslatableText("nbttooltip.config.triggerType.line1"),
							new TranslatableText("nbttooltip.config.triggerType.line2") 
					)
					.setSaveConsumer(val -> {ModConfig.INSTANCE.triggerType = val;})
					.build()
		);
		
		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(new TranslatableText("nbttooltip.config.showDelimiters") , ModConfig.INSTANCE.showDelimiters)
					.setDefaultValue(true)
					.setTooltip(
							new TranslatableText("nbttooltip.config.showDelimiters.line1"),
							new TranslatableText("nbttooltip.config.showDelimiters.line2") 
					)
					.setSaveConsumer(val -> {ModConfig.INSTANCE.showDelimiters = val;})
					.build()
		);
		
		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(new TranslatableText("nbttooltip.config.ctrlSuppressesRest") , ModConfig.INSTANCE.ctrlSuppressesRest)
					.setDefaultValue(true)
					.setTooltip(
							new TranslatableText("nbttooltip.config.ctrlSuppressesRest.line1"),
							new TranslatableText("nbttooltip.config.ctrlSuppressesRest.line2") 
					)
					.setSaveConsumer(val -> {ModConfig.INSTANCE.ctrlSuppressesRest = val;})
					.build()
		);
		
		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(new TranslatableText("nbttooltip.config.splitLongLines") , ModConfig.INSTANCE.splitLongLines)
					.setDefaultValue(true)
					.setTooltip(
							new TranslatableText("nbttooltip.config.splitLongLines.line1"),
							new TranslatableText("nbttooltip.config.splitLongLines.line2") 
					)
					.setSaveConsumer(val -> {ModConfig.INSTANCE.splitLongLines = val;})
					.build()
		);
		
		general.addEntry(configBuilder.entryBuilder()
				.startIntField(new TranslatableText("nbttooltip.config.maxLinesShown"), ModConfig.INSTANCE.maxLinesShown)
					.setDefaultValue(10)
					.setTooltip(
							new TranslatableText("nbttooltip.config.maxLinesShown.line1"),
							new TranslatableText("nbttooltip.config.maxLinesShown.line2") 
					)
					.setSaveConsumer(val -> {ModConfig.INSTANCE.maxLinesShown = val;})
					.build()
		);
		

		
		general.addEntry(configBuilder.entryBuilder()
				.startIntField(new TranslatableText("nbttooltip.config.ticksBeforeScroll"), ModConfig.INSTANCE.ticksBeforeScroll)
					.setDefaultValue(20)
					.setTooltip(
							new TranslatableText("nbttooltip.config.ticksBeforeScroll.line1"),
							new TranslatableText("nbttooltip.config.ticksBeforeScroll.line2") 
					)
					.setSaveConsumer(val -> {ModConfig.INSTANCE.ticksBeforeScroll = val;})
					.build()
		);
		
		general.addEntry(configBuilder.entryBuilder()
				.startEnumSelector(new TranslatableText("nbttooltip.config.tooltipEngine"), TooltipEngine.class, ModConfig.INSTANCE.tooltipEngine)
				.setDefaultValue(TooltipEngine.FRIENDLY)
				.setTooltip(
						new TranslatableText("nbttooltip.config.tooltipEngine.line1"),
						new TranslatableText("nbttooltip.config.tooltipEngine.line2") 
				)
				.setSaveConsumer(val -> {ModConfig.INSTANCE.tooltipEngine = val;})
				.build()
		);
		
		general.addEntry(configBuilder.entryBuilder()
				.startEnumSelector(new TranslatableText("nbttooltip.config.copyingEngine"), CopyingEngine.class, ModConfig.INSTANCE.copyingEngine)
				.setDefaultValue(CopyingEngine.JSON)
				.setTooltip(
						new TranslatableText("nbttooltip.config.copyingEngine.line1"),
						new TranslatableText("nbttooltip.config.copyingEngine.line2") 
				)
				.setSaveConsumer(val -> {ModConfig.INSTANCE.copyingEngine = val;})
				.build()
		);
		
		return configBuilder;
	}
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			return builder().setParentScreen(parent).build();
		};
	}

}
