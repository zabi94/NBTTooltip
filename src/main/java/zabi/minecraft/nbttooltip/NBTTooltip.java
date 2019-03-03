package zabi.minecraft.nbttooltip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;

public class NBTTooltip implements ModInitializer {

	public static int ticks = 0;
	public static int line_scrolled = 0;
	
	public static final String FORMAT = TextFormat.ITALIC.toString()+TextFormat.DARK_GRAY;
	
	@Override
	public void onInitialize() {
		System.out.println("Creating fake config");
		ModConfig.maxLinesShown = 10;
		ModConfig.compress = false;
		ModConfig.requiresf3 = true;
		ModConfig.showDelimiters = true;
		ModConfig.showSeparator = true;
		ModConfig.ticksBeforeScroll = 20;
	}
	
	public static ArrayList<TextComponent> transformTtip(ArrayList<TextComponent> ttip) {
		ArrayList<TextComponent> newttip = new ArrayList<TextComponent>(ModConfig.maxLinesShown);
		if (ModConfig.showSeparator) {
			newttip.add(new StringTextComponent("- NBTTooltip -"));
		}
		if (ttip.size()>ModConfig.maxLinesShown) {
			if (ModConfig.maxLinesShown+line_scrolled>ttip.size()) line_scrolled = 0;
			for (int i = 0; i < ModConfig.maxLinesShown; i++) {
				newttip.add(ttip.get(i+line_scrolled));
			}
			return newttip;
		} else {
			line_scrolled = 0;
			newttip.addAll(ttip);
			return newttip;
		}
	}

	public static void unwrapTag(List<TextComponent> tooltip, Tag base, String pad, String tagName, String padIncrement) {
		if (base instanceof CompoundTag) {
			CompoundTag tag = (CompoundTag) base;
			tag.getKeys().forEach(s -> {
				boolean nested = (tag.getTag(s) instanceof AbstractListTag) || (tag.getTag(s) instanceof CompoundTag);
				if (nested) {
					tooltip.add(new StringTextComponent(pad+s+": {"));
					unwrapTag(tooltip, tag.getTag(s), pad+padIncrement, s, padIncrement);
					tooltip.add(new StringTextComponent(pad+"}"));
				} else {
					addValueToTooltip(tooltip, tag.getTag(s), s, pad);
				}
			});
		} else if (base instanceof AbstractListTag) {
			AbstractListTag<?> tag = (AbstractListTag<?>) base;
			int index = 0;
			Iterator<? extends Tag> iter = tag.iterator();
			while (iter.hasNext()) {
				Tag nbtnext = iter.next();
				if (nbtnext instanceof AbstractListTag || nbtnext instanceof CompoundTag) {
					tooltip.add(new StringTextComponent(pad + "["+index+"]: {"));
					unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
					tooltip.add(new StringTextComponent(pad+"}"));
				} else {
					tooltip.add(new StringTextComponent(pad+"["+index+"] -> "+nbtnext.toString()));
				}
				index++;
			}
		} else {
			addValueToTooltip(tooltip, base, tagName, pad);
		}
	}
	
	private static void addValueToTooltip(List<TextComponent> tooltip, Tag nbt, String name, String pad) {
		tooltip.add(new StringTextComponent(pad+name+": "+nbt.toString()));
	}

}
