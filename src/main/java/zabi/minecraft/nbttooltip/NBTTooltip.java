package zabi.minecraft.nbttooltip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.world.World;

public class NBTTooltip implements ModInitializer {

	public static int ticks = 0;
	public static int line_scrolled = 0;
	
	public static final String FORMAT = TextFormat.ITALIC.toString()+TextFormat.DARK_GRAY;
	
	@Override
	public void onInitialize() {
		ModConfig.init();
	}
	
	public static ArrayList<TextComponent> transformTtip(ArrayList<TextComponent> ttip, int lines) {
		ArrayList<TextComponent> newttip = new ArrayList<TextComponent>(lines);
		if (ModConfig.showSeparator) {
			newttip.add(new StringTextComponent("- NBTTooltip -"));
		}
		if (ttip.size()>lines) {
			if (lines+line_scrolled>ttip.size()) line_scrolled = 0;
			for (int i = 0; i < lines; i++) {
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
	
	public static void onInjectTooltip(Object stackIn, World world, List<TextComponent> list, TooltipContext context) {
		if (!ModConfig.requiresf3 || context.isAdvanced()) {
			ItemStack stack = (ItemStack) stackIn;
			int lines = ModConfig.maxLinesShown;
			if (ModConfig.ctrlSuppressesRest && Screen.hasControlDown()) {
				lines += list.size();
				list.clear();
			} else {
				list.add(new StringTextComponent(""));
			}
			CompoundTag tag = stack.getTag();
			ArrayList<TextComponent> ttip = new ArrayList<TextComponent>(lines);
			if (tag!=null) {
				if (ModConfig.showDelimiters) {
					ttip.add(new StringTextComponent(TextFormat.DARK_PURPLE+" - nbt start -"));
				}
				if (ModConfig.compress) {
					ttip.add(new StringTextComponent(FORMAT+tag.toString()));
				} else {
					NBTTooltip.unwrapTag(ttip, tag, FORMAT, "", ModConfig.compress?"":"  ");
				}
				if (ModConfig.showDelimiters) {
					ttip.add(new StringTextComponent(TextFormat.DARK_PURPLE+" - nbt end -"));
				}
				ttip = NBTTooltip.transformTtip(ttip, lines);

				list.addAll(ttip);
			} else {
				list.add(new StringTextComponent(FORMAT+"No NBT tag"));
			}
		}
	}

}
