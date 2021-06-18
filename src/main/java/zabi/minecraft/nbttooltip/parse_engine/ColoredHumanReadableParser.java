package zabi.minecraft.nbttooltip.parse_engine;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import zabi.minecraft.nbttooltip.config.ModConfig;

import java.util.List;

public class ColoredHumanReadableParser implements NbtTagParser {
	
	private static final int line_split_threshold = 30;
	
	private static final Formatting LISTINDEX = Formatting.GREEN;
	private static final Formatting STRING = Formatting.LIGHT_PURPLE;
	private static final Formatting STRUCTURE = Formatting.GRAY;
	private static final Formatting TAGNAME = Formatting.GOLD;

	@Override
	public void parseTagToList(List<Text> list, @Nullable NbtElement tag, boolean split) {
		if (tag == null) {
			list.add(new LiteralText("No NBT tag").formatted(Formatting.DARK_GRAY));
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
			addValueToTooltip(tooltip, base, new LiteralText(tagName).formatted(TAGNAME), pad, splitLongStrings);
		}
	}
	
	private void addCompoundToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement, boolean splitLongStrings) {
		NbtCompound tag = (NbtCompound) base;
		tag.getKeys().forEach(s -> {
			boolean nested = (tag.get(s) instanceof AbstractNbtList) || (tag.get(s) instanceof NbtCompound);
			if (nested) {
				Text subtreeName = new LiteralText(s).formatted(TAGNAME);
				Text intro = new TranslatableText("%s%s%s", pad, subtreeName, new LiteralText(": {").formatted(STRUCTURE));
				tooltip.add(intro);
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement, splitLongStrings);
				tooltip.add(new LiteralText(pad+"}").formatted(STRUCTURE));
			} else {
				addValueToTooltip(tooltip, tag.get(s), new LiteralText(s).formatted(TAGNAME), pad, splitLongStrings);
			}
		});
	}
	
	private void addListToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement, boolean splitLongStrings) {
		AbstractNbtList<?> tag = (AbstractNbtList<?>) base;
		int index = 0;
		for (NbtElement nbtnext : tag) {
			if (nbtnext instanceof AbstractNbtList || nbtnext instanceof NbtCompound) {
				tooltip.add(new TranslatableText("%s [%s]: {", pad, new LiteralText("" + index).formatted(LISTINDEX)).formatted(STRUCTURE));
				unwrapTag(tooltip, nbtnext, pad + padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(new LiteralText(pad + "}").formatted(STRUCTURE));
			} else {
				addValueToTooltip(tooltip, nbtnext, new TranslatableText("[%s]", new LiteralText("" + index).formatted(LISTINDEX))
						.formatted(STRUCTURE), pad, splitLongStrings);
			}
			index++;
		}
	}
	
	private static void addValueToTooltip(List<Text> tooltip, NbtElement nbt, Text name, String pad, boolean splitLongStrings) {
		String toBeAdded = nbt.toString();
		if (!splitLongStrings || toBeAdded.length() < line_split_threshold) {
			tooltip.add(new TranslatableText(pad+"%s: %s", name, new LiteralText(nbt.toString()).formatted(STRING)));
		} else {
			Text separator = new LiteralText("|").formatted(Formatting.AQUA);
			int added = 0;
			tooltip.add(new TranslatableText(pad+"%s:", name));
			while (added < toBeAdded.length()) {
				int nextChunk = Math.min(line_split_threshold, toBeAdded.length() - added);
				Text chunk = new LiteralText(toBeAdded.substring(added, added+nextChunk)).formatted(STRING);
				tooltip.add(new TranslatableText("%s"+pad+"   %s", separator, chunk));
				added += nextChunk;
			}
		}
	}

}
