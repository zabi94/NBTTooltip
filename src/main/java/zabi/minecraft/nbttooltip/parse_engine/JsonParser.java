package zabi.minecraft.nbttooltip.parse_engine;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import zabi.minecraft.nbttooltip.ModConfig;

public class JsonParser implements NbtTagParser {
	
	@Override
	public void parseTagToList(List<Text> list, @Nullable Tag tag, boolean split) {
		if (tag == null) {
			list.add(new LiteralText("{}"));
		} else {
			list.add(new LiteralText("{"));
			unwrapTag(list, tag, "", "", ModConfig.compress?"":"  ");
			list.add(new LiteralText("}"));
		}
	}
	
	private void unwrapTag(List<Text> tooltip, Tag base, String pad, String tagName, String padIncrement) {
		if (base instanceof CompoundTag) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement);
		} else if (base instanceof AbstractListTag) {
			addListToTooltip(tooltip, base, pad, padIncrement);
		} else {
			addValueToTooltip(tooltip, base, tagName, pad, false);
		}
	}
	
	private void addCompoundToTooltip(List<Text> tooltip, Tag base, String pad, String padIncrement) {
		CompoundTag tag = (CompoundTag) base;
		Iterator<String> iter = tag.getKeys().iterator();
		while (iter.hasNext()) {
			String s = iter.next();
			if (tag.get(s) instanceof CompoundTag) {
				tooltip.add(new LiteralText(pad+'"'+s+"\": {"));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement);
				tooltip.add(new LiteralText(pad+"}"+(iter.hasNext()?",":"")));
			} else if (tag.get(s) instanceof AbstractListTag) {
				tooltip.add(new LiteralText(pad+'"'+s+"\": ["));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement);
				tooltip.add(new LiteralText(pad+"]"+(iter.hasNext()?",":"")));
			} else {
				addNamedValueToTooltip(tooltip, tag.get(s), s, pad, iter.hasNext());
			}
		}
	}
	
	private void addListToTooltip(List<Text> tooltip, Tag base, String pad, String padIncrement) {
		AbstractListTag<?> tag = (AbstractListTag<?>) base;
		Iterator<? extends Tag> iter = tag.iterator();
		while (iter.hasNext()) {
			Tag nbtnext = iter.next();
			if (nbtnext instanceof CompoundTag) {
				tooltip.add(new LiteralText(pad + "{"));
				unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
				tooltip.add(new LiteralText(pad+"}"+(iter.hasNext()?",":"")));
			} else if (nbtnext instanceof AbstractListTag) {
				tooltip.add(new LiteralText(pad + "["));
				unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
				tooltip.add(new LiteralText(pad+"]"+(iter.hasNext()?",":"")));
			} else {
				addValueToTooltip(tooltip, nbtnext, "", pad, iter.hasNext());
			}
		}
	}
	
	private static void addNamedValueToTooltip(List<Text> tooltip, Tag nbt, String name, String pad, boolean addComma) {
		String cleanNbt = nbt instanceof StringTag ? nbt.toString() : nbt.toString().substring(0, nbt.toString().length() - 1);
		cleanNbt = cleanNbt.replaceAll(".\".", "\\\"");
		tooltip.add(new LiteralText(pad+'"'+name+"\": "+cleanNbt+(addComma ? "," : "")));
	}
	
	private static void addValueToTooltip(List<Text> tooltip, Tag nbt, String name, String pad, boolean addComma) {
		String cleanNbt = nbt.toString();
		if (nbt instanceof StringTag) {
			String inner = cleanNbt.substring(1, cleanNbt.length() - 2).replaceAll("\"", "\\\"");
			cleanNbt = cleanNbt.charAt(0) + inner + cleanNbt.charAt(cleanNbt.length()-1);
		} else {
			cleanNbt = cleanNbt.substring(0, nbt.toString().length() - 1);
		}
		tooltip.add(new LiteralText(pad+cleanNbt+(addComma ? "," : "")));
	}

}
