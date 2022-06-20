package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import zabi.minecraft.nbttooltip.NBTTooltip;
import zabi.minecraft.nbttooltip.config.ModConfig;

public class BWHumanReadableParser implements NbtTagParser {
	
	private static final int line_split_threshold = 30;

	@Override
	public void parseTagToList(List<Text> list, @Nullable NbtElement tag, boolean split) {
		if (tag == null) {
			list.add(Text.literal("No NBT tag"));
		} else {
			unwrapTag(list, tag, NBTTooltip.FORMAT, "", ModConfig.INSTANCE.compress?"":"  ", split);
		}
	}
	
	private void unwrapTag(List<Text> tooltip, NbtElement base, String pad, String tagName, String padIncrement, boolean splitLongStrings) {
		if (base instanceof NbtCompound) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else if (base instanceof AbstractNbtList) {
			addListToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else {
			addValueToTooltip(tooltip, base, tagName, pad, splitLongStrings);
		}
	}
	
	private void addCompoundToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement, boolean splitLongStrings) {
		NbtCompound tag = (NbtCompound) base;
		tag.getKeys().forEach(s -> {
			boolean nested = (tag.get(s) instanceof AbstractNbtList) || (tag.get(s) instanceof NbtCompound);
			if (nested) {
				tooltip.add(Text.literal(pad+s+": {"));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement, splitLongStrings);
				tooltip.add(Text.literal(pad+"}"));
			} else {
				addValueToTooltip(tooltip, tag.get(s), s, pad, splitLongStrings);
			}
		});
	}
	
	private void addListToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement, boolean splitLongStrings) {
		AbstractNbtList<?> tag = (AbstractNbtList<?>) base;
		int index = 0;
		for (NbtElement nbtnext : tag) {
			if (nbtnext instanceof AbstractNbtList || nbtnext instanceof NbtCompound) {
				tooltip.add(Text.literal(pad + "[" + index + "]: {"));
				unwrapTag(tooltip, nbtnext, pad + padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(Text.literal(pad + "}"));
			} else {
				addValueToTooltip(tooltip, nbtnext, "[" + index + "]", pad, splitLongStrings);
			}
			index++;
		}
	}
	
	private static void addValueToTooltip(List<Text> tooltip, NbtElement nbt, String name, String pad, boolean splitLongStrings) {
		String toBeAdded = nbt.toString();
		if (!splitLongStrings || toBeAdded.length() < line_split_threshold) {
			tooltip.add(Text.literal(pad+name+": "+ nbt));
		} else {
			int added = 0;
			tooltip.add(Text.literal(pad+name+":"));
			while (added < toBeAdded.length()) {
				int nextChunk = Math.min(line_split_threshold, toBeAdded.length() - added);
				String sb = new StringBuilder()
						.append(Formatting.AQUA).append("|")
						.append(Formatting.RESET).append(pad)
						.append("   ")
						.append(toBeAdded, added, added + nextChunk).toString();
				tooltip.add(Text.literal(sb));
				added += nextChunk;
			}
		}
	}

}
