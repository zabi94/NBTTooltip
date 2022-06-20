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
			boolean nested = (tag.get(s) instanceof AbstractNbtList) || (tag.get(s) instanceof NbtCompound);
			if (nested) {
				Text subtreeName = Text.literal(s).formatted(TAGNAME);
				Text intro = Text.translatable("%s%s%s", pad, subtreeName, Text.literal(": {").formatted(STRUCTURE));
				tooltip.add(intro);
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement, splitLongStrings);
				tooltip.add(Text.literal(pad+"}").formatted(STRUCTURE));
			} else {
				addValueToTooltip(tooltip, tag.get(s), Text.literal(s).formatted(TAGNAME), pad, splitLongStrings);
			}
		});
	}
	
	private void addListToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement, boolean splitLongStrings) {
		AbstractNbtList<?> tag = (AbstractNbtList<?>) base;
		int index = 0;
		for (NbtElement nbtnext : tag) {
			if (nbtnext instanceof AbstractNbtList || nbtnext instanceof NbtCompound) {
				tooltip.add(Text.translatable("%s [%s]: {", pad, Text.literal("" + index).formatted(LISTINDEX)).formatted(STRUCTURE));
				unwrapTag(tooltip, nbtnext, pad + padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(Text.literal(pad + "}").formatted(STRUCTURE));
			} else {
				addValueToTooltip(tooltip, nbtnext, Text.translatable("[%s]", Text.literal("" + index).formatted(LISTINDEX))
						.formatted(STRUCTURE), pad, splitLongStrings);
			}
			index++;
		}
	}
	
	private static void addValueToTooltip(List<Text> tooltip, NbtElement nbt, Text name, String pad, boolean splitLongStrings) {
		String toBeAdded = nbt.toString();
		if (!splitLongStrings || toBeAdded.length() < line_split_threshold) {
			tooltip.add(Text.translatable(pad+"%s: %s", name, Text.literal(nbt.toString()).formatted(STRING)));
		} else {
			Text separator = Text.literal("|").formatted(Formatting.AQUA);
			int added = 0;
			tooltip.add(Text.translatable(pad+"%s:", name));
			while (added < toBeAdded.length()) {
				int nextChunk = Math.min(line_split_threshold, toBeAdded.length() - added);
				Text chunk = Text.literal(toBeAdded.substring(added, added+nextChunk)).formatted(STRING);
				tooltip.add(Text.translatable("%s"+pad+"   %s", separator, chunk));
				added += nextChunk;
			}
		}
	}

}
