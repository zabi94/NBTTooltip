package zabi.minecraft.nbttooltip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class NBTTooltip implements ModInitializer {

	public static int ticks = 0;
	public static int line_scrolled = 0;
	
	public static final String FORMAT = Formatting.ITALIC.toString()+Formatting.DARK_GRAY;
	
	@Override
	public void onInitialize() {
		ModConfig.init();
	}
	
	public static ArrayList<Text> transformTtip(ArrayList<Text> ttip, int lines) {
		ArrayList<Text> newttip = new ArrayList<Text>(lines);
		if (ModConfig.showSeparator) {
			newttip.add(new LiteralText("- NBTTooltip -"));
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

	public static void unwrapTag(List<Text> tooltip, Tag base, String pad, String tagName, String padIncrement) {
		if (base instanceof CompoundTag) {
			CompoundTag tag = (CompoundTag) base;
			tag.getKeys().forEach(s -> {
				boolean nested = (tag.get(s) instanceof AbstractListTag) || (tag.get(s) instanceof CompoundTag);
				if (nested) {
					tooltip.add(new LiteralText(pad+s+": {"));
					unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement);
					tooltip.add(new LiteralText(pad+"}"));
				} else {
					addValueToTooltip(tooltip, tag.get(s), s, pad);
				}
			});
		} else if (base instanceof AbstractListTag) {
			AbstractListTag<?> tag = (AbstractListTag<?>) base;
			int index = 0;
			Iterator<? extends Tag> iter = tag.iterator();
			while (iter.hasNext()) {
				Tag nbtnext = iter.next();
				if (nbtnext instanceof AbstractListTag || nbtnext instanceof CompoundTag) {
					tooltip.add(new LiteralText(pad + "["+index+"]: {"));
					unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
					tooltip.add(new LiteralText(pad+"}"));
				} else {
					tooltip.add(new LiteralText(pad+"["+index+"] -> "+nbtnext.toString()));
				}
				index++;
			}
		} else {
			addValueToTooltip(tooltip, base, tagName, pad);
		}
	}
	
	private static void addValueToTooltip(List<Text> tooltip, Tag nbt, String name, String pad) {
		tooltip.add(new LiteralText(pad+name+": "+nbt.toString()));
	}
	
	public static void onInjectTooltip(Object stackIn, World world, List<Text> list, TooltipContext context) {
		if (!ModConfig.requiresf3 || context.isAdvanced()) {
			ItemStack stack = (ItemStack) stackIn;
			int lines = ModConfig.maxLinesShown;
			if (ModConfig.ctrlSuppressesRest && Screen.hasControlDown()) {
				lines += list.size();
				list.clear();
			} else {
				list.add(new LiteralText(""));
			}
			CompoundTag tag = stack.getTag();
			ArrayList<Text> ttip = new ArrayList<Text>(lines);
			if (tag!=null) {
				if (ModConfig.showDelimiters) {
					ttip.add(new LiteralText(Formatting.DARK_PURPLE+" - nbt start -"));
				}
				if (ModConfig.compress) {
					ttip.add(new LiteralText(FORMAT+tag.toString()));
				} else {
					NBTTooltip.unwrapTag(ttip, tag, FORMAT, "", ModConfig.compress?"":"  ");
				}
				if (ModConfig.showDelimiters) {
					ttip.add(new LiteralText(Formatting.DARK_PURPLE+" - nbt end -"));
				}
				ttip = NBTTooltip.transformTtip(ttip, lines);

				list.addAll(ttip);
			} else {
				list.add(new LiteralText(FORMAT+"No NBT tag"));
			}
		}
	}

}
