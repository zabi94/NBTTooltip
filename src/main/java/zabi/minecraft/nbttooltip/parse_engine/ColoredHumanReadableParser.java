package zabi.minecraft.nbttooltip.parse_engine;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import zabi.minecraft.nbttooltip.ModConfig;

public class ColoredHumanReadableParser implements NbtTagParser {
	
	private static final int line_split_threshold = 30;
	
	private static final Formatting LISTINDEX = Formatting.GREEN;
	private static final Formatting STRING = Formatting.LIGHT_PURPLE;
	private static final Formatting STRUCTURE = Formatting.GRAY;
	private static final Formatting TAGNAME = Formatting.GOLD;

	@Override
	public void parseTagToList(List<Text> list, @Nullable Tag tag, boolean split) {
		if (tag == null) {
			list.add(new LiteralText("No NBT tag").formatted(Formatting.DARK_GRAY));
		} else {
			unwrapTag(list, tag, "", "", ModConfig.compress?"":"  ", split);
		}
	}
	
	private void unwrapTag(List<Text> tooltip, Tag base, String pad, String tagName, String padIncrement, boolean splitLongStrings) {
		if (base instanceof CompoundTag) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else if (base instanceof AbstractListTag) {
			addListToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else {
			addValueToTooltip(tooltip, base, new LiteralText(tagName).formatted(TAGNAME), pad, splitLongStrings);
		}
	}
	
	private void addCompoundToTooltip(List<Text> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		CompoundTag tag = (CompoundTag) base;
		tag.getKeys().forEach(s -> {
			boolean nested = (tag.get(s) instanceof AbstractListTag) || (tag.get(s) instanceof CompoundTag);
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
	
	private void addListToTooltip(List<Text> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		AbstractListTag<?> tag = (AbstractListTag<?>) base;
		int index = 0;
		Iterator<? extends Tag> iter = tag.iterator();
		while (iter.hasNext()) {
			Tag nbtnext = iter.next();
			if (nbtnext instanceof AbstractListTag || nbtnext instanceof CompoundTag) {
				tooltip.add(new TranslatableText("%s [%s]: {", pad, new LiteralText(""+index).formatted(LISTINDEX)).formatted(STRUCTURE));
				unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(new LiteralText(pad+"}").formatted(STRUCTURE));
			} else {
				addValueToTooltip(tooltip, nbtnext, new TranslatableText("[%s]", new LiteralText(""+index).formatted(LISTINDEX))
						.formatted(STRUCTURE), pad, splitLongStrings);
			}
			index++;
		}
	}
	
	private static void addValueToTooltip(List<Text> tooltip, Tag nbt, Text name, String pad, boolean splitLongStrings) {
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
