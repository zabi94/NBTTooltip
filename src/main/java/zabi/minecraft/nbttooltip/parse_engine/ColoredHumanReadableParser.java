package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import zabi.minecraft.nbttooltip.config.ModConfig;

public class ColoredHumanReadableParser implements NbtTagParser {
	
	private static final int line_split_threshold = 30;
	
	private static final Formatting LISTINDEX = Formatting.GREEN;
	private static final Formatting STRING = Formatting.LIGHT_PURPLE;
	private static final Formatting STRUCTURE = Formatting.GRAY;
	private static final Formatting TAGNAME = Formatting.GOLD;

	@Override
	public void parseTagToList(List<Text> list, @Nullable NbtElement tag, boolean split) {
		if (tag == null) {
			list.add(Text.literal("No NBT tag").formatted(Formatting.DARK_GRAY));
		} else {
			unwrapTag(list, tag, "", "", ModConfig.INSTANCE.compress?"":"  ", split);
		}
	}
	
	private void unwrapTag(List<Text> tooltip, NbtElement base, String pad, String tagName, String padIncrement, boolean splitLongStrings) {
		if (base instanceof NbtCompound) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else if (base instanceof AbstractNbtList) {
			addListToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else {
			addValueToTooltip(tooltip, base, Text.literal(tagName).formatted(TAGNAME), pad, splitLongStrings);
		}
	}
	
	private void addCompoundToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement, boolean splitLongStrings) {
		NbtCompound tag = (NbtCompound) base;
		tag.getKeys().forEach(s -> {
			NbtElement element = tag.get(s);
			if (element != null) {
				boolean nested = (element instanceof AbstractNbtList) || (element instanceof NbtCompound);
				if (nested) {
					Text subtreeName = Text.literal(s).formatted(TAGNAME);
					Text intro = Text.literal(pad).append(subtreeName).append(Text.literal(": {").formatted(STRUCTURE));
					tooltip.add(intro);
					unwrapTag(tooltip, element, pad+padIncrement, s, padIncrement, splitLongStrings);
					tooltip.add(Text.literal(pad+"}").formatted(STRUCTURE));
				} else {
					addValueToTooltip(tooltip, element, Text.literal(s).formatted(TAGNAME), pad, splitLongStrings);
				}
			}
		});
	}
	
	private void addListToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement, boolean splitLongStrings) {
		AbstractNbtList tag = (AbstractNbtList) base;
		int index = 0;
		for (NbtElement nbtnext : tag) {
			if (nbtnext instanceof AbstractNbtList || nbtnext instanceof NbtCompound) {
				tooltip.add(Text.literal(pad + " [").append(Text.literal("" + index).formatted(LISTINDEX)).append(Text.literal("]: {")).formatted(STRUCTURE));
				unwrapTag(tooltip, nbtnext, pad + padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(Text.literal(pad + "}").formatted(STRUCTURE));
			} else {
				addValueToTooltip(tooltip, nbtnext, Text.literal("[").append(Text.literal("" + index).formatted(LISTINDEX)).append(Text.literal("]"))
						.formatted(STRUCTURE), pad, splitLongStrings);
			}
			index++;
		}
	}
	
	private static void addValueToTooltip(List<Text> tooltip, NbtElement nbt, Text name, String pad, boolean splitLongStrings) {
		String toBeAdded = nbt.toString();
		if (!splitLongStrings || toBeAdded.length() < line_split_threshold) {
			tooltip.add(Text.literal(pad).append(name).append(Text.literal(": ")).append(Text.literal(nbt.toString()).formatted(STRING)));
		} else {
			Text separator = Text.literal("|").formatted(Formatting.AQUA);
			int added = 0;
			tooltip.add(Text.literal(pad).append(name).append(Text.literal(":")));
			while (added < toBeAdded.length()) {
				int nextChunk = Math.min(line_split_threshold, toBeAdded.length() - added);
				Text chunk = Text.literal(toBeAdded.substring(added, added+nextChunk)).formatted(STRING);
				tooltip.add(separator.copy().append(Text.literal(pad+"   ")).append(chunk));
				added += nextChunk;
			}
		}
	}

}
