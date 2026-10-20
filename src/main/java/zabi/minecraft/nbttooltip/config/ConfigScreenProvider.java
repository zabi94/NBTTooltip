package zabi.minecraft.nbttooltip.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.network.chat.Component;
import zabi.minecraft.nbttooltip.config.ConfigInstance.CopyingEngine;
import zabi.minecraft.nbttooltip.config.ConfigInstance.TooltipEngine;

public class ConfigScreenProvider implements ModMenuApi {


	public static ConfigBuilder builder() {

		ConfigBuilder configBuilder = ConfigBuilder.create()
				.setTitle(Component.translatable("key.category.nbttooltip"))
				.setEditable(true)
				.setSavingRunnable(ModConfig::writeJson);

		ConfigCategory general = configBuilder.getOrCreateCategory(Component.translatable("nbttooltip.config.general"));

		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(Component.translatable("nbttooltip.config.showseparator") , ModConfig.INSTANCE.showSeparator)
					.setDefaultValue(true)
					.setTooltip(
							Component.translatable("nbttooltip.config.showseparator.line1"),
							Component.translatable("nbttooltip.config.showseparator.line2")
					)
					.setSaveConsumer(val -> ModConfig.INSTANCE.showSeparator = val)
					.build()
		);

		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(Component.translatable("nbttooltip.config.compress") , ModConfig.INSTANCE.compress)
					.setDefaultValue(false)
					.setTooltip(
							Component.translatable("nbttooltip.config.compress.line1"),
							Component.translatable("nbttooltip.config.compress.line2")
					)
					.setSaveConsumer(val -> ModConfig.INSTANCE.compress = val)
					.build()
		);

		general.addEntry(configBuilder.entryBuilder()
				.startEnumSelector(Component.translatable("nbttooltip.config.triggerType"), TriggerType.class, ModConfig.INSTANCE.triggerType)
					.setDefaultValue(TriggerType.F3H)
					.setTooltip(
							Component.translatable("nbttooltip.config.triggerType.line1"),
							Component.translatable("nbttooltip.config.triggerType.line2")
					)
					.setSaveConsumer(val -> ModConfig.INSTANCE.triggerType = val)
					.build()
		);

		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(Component.translatable("nbttooltip.config.showDelimiters") , ModConfig.INSTANCE.showDelimiters)
					.setDefaultValue(true)
					.setTooltip(
							Component.translatable("nbttooltip.config.showDelimiters.line1"),
							Component.translatable("nbttooltip.config.showDelimiters.line2")
					)
					.setSaveConsumer(val -> ModConfig.INSTANCE.showDelimiters = val)
					.build()
		);

		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(Component.translatable("nbttooltip.config.ctrlSuppressesRest") , ModConfig.INSTANCE.ctrlSuppressesRest)
					.setDefaultValue(true)
					.setTooltip(
							Component.translatable("nbttooltip.config.ctrlSuppressesRest.line1"),
							Component.translatable("nbttooltip.config.ctrlSuppressesRest.line2")
					)
					.setSaveConsumer(val -> ModConfig.INSTANCE.ctrlSuppressesRest = val)
					.build()
		);

		general.addEntry(configBuilder.entryBuilder()
				.startBooleanToggle(Component.translatable("nbttooltip.config.splitLongLines") , ModConfig.INSTANCE.splitLongLines)
					.setDefaultValue(true)
					.setTooltip(
							Component.translatable("nbttooltip.config.splitLongLines.line1"),
							Component.translatable("nbttooltip.config.splitLongLines.line2")
					)
					.setSaveConsumer(val -> ModConfig.INSTANCE.splitLongLines = val)
					.build()
		);

		general.addEntry(configBuilder.entryBuilder()
				.startIntField(Component.translatable("nbttooltip.config.maxLinesShown"), ModConfig.INSTANCE.maxLinesShown)
					.setDefaultValue(10)
					.setTooltip(
							Component.translatable("nbttooltip.config.maxLinesShown.line1"),
							Component.translatable("nbttooltip.config.maxLinesShown.line2")
					)
					.setSaveConsumer(val -> ModConfig.INSTANCE.maxLinesShown = val)
					.build()
		);



		general.addEntry(configBuilder.entryBuilder()
				.startIntField(Component.translatable("nbttooltip.config.ticksBeforeScroll"), ModConfig.INSTANCE.ticksBeforeScroll)
					.setDefaultValue(20)
					.setTooltip(
							Component.translatable("nbttooltip.config.ticksBeforeScroll.line1"),
							Component.translatable("nbttooltip.config.ticksBeforeScroll.line2")
					)
					.setSaveConsumer(val -> ModConfig.INSTANCE.ticksBeforeScroll = val)
					.build()
		);

		general.addEntry(configBuilder.entryBuilder()
				.startEnumSelector(Component.translatable("nbttooltip.config.tooltipEngine"), TooltipEngine.class, ModConfig.INSTANCE.tooltipEngine)
				.setDefaultValue(TooltipEngine.FRIENDLY)
				.setTooltip(
						Component.translatable("nbttooltip.config.tooltipEngine.line1"),
						Component.translatable("nbttooltip.config.tooltipEngine.line2")
				)
				.setSaveConsumer(val -> ModConfig.INSTANCE.tooltipEngine = val)
				.build()
		);

		general.addEntry(configBuilder.entryBuilder()
				.startEnumSelector(Component.translatable("nbttooltip.config.copyingEngine"), CopyingEngine.class, ModConfig.INSTANCE.copyingEngine)
				.setDefaultValue(CopyingEngine.JSON)
				.setTooltip(
						Component.translatable("nbttooltip.config.copyingEngine.line1"),
						Component.translatable("nbttooltip.config.copyingEngine.line2")
				)
				.setSaveConsumer(val -> ModConfig.INSTANCE.copyingEngine = val)
				.build()
		);

		return configBuilder;
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> builder().setParentScreen(parent).build();
	}

}
