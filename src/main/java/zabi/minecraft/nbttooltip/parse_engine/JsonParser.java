package zabi.minecraft.nbttooltip.parse_engine;

import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.*;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import zabi.minecraft.nbttooltip.config.ModConfig;

public class JsonParser implements NbtTagParser {
	
	@Override
	public void parseTagToList(List<Text> list, @Nullable NbtElement tag, boolean split) {
		if (tag == null) {
			list.add(new LiteralText("{}"));
		} else {
			list.add(new LiteralText("{"));
			unwrapTag(list, tag, ModConfig.INSTANCE.compress?"":"  ", "", ModConfig.INSTANCE.compress?"":"  ");
			list.add(new LiteralText("}"));
		}
	}
	
	private void unwrapTag(List<Text> tooltip, NbtElement base, String pad, String tagName, String padIncrement) {
		if (base instanceof NbtCompound) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement);
		} else if (base instanceof AbstractNbtList) {
			addListToTooltip(tooltip, base, pad, padIncrement);
		} else {
			addValueToTooltip(tooltip, base, tagName, pad, false);
		}
	}
	
	private void addCompoundToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement) {
		NbtCompound tag = (NbtCompound) base;
		Iterator<String> iter = tag.getKeys().iterator();
		while (iter.hasNext()) {
			String s = iter.next();
			if (tag.get(s) instanceof NbtCompound) {
				tooltip.add(new LiteralText(pad+'"'+s+"\": {"));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement);
				tooltip.add(new LiteralText(pad+"}"+(iter.hasNext()?",":"")));
			} else if (tag.get(s) instanceof AbstractNbtList) {
				tooltip.add(new LiteralText(pad+'"'+s+"\": ["));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement);
				tooltip.add(new LiteralText(pad+"]"+(iter.hasNext()?",":"")));
			} else {
				addNamedValueToTooltip(tooltip, tag.get(s), s, pad, iter.hasNext());
			}
		}
	}
	
	private void addListToTooltip(List<Text> tooltip, NbtElement base, String pad, String padIncrement) {
		AbstractNbtList<?> tag = (AbstractNbtList<?>) base;
		Iterator<? extends NbtElement> iter = tag.iterator();
		while (iter.hasNext()) {
			NbtElement nbtnext = iter.next();
			if (nbtnext instanceof NbtCompound) {
				tooltip.add(new LiteralText(pad + "{"));
				unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
				tooltip.add(new LiteralText(pad+"}"+(iter.hasNext()?",":"")));
			} else if (nbtnext instanceof AbstractNbtList) {
				tooltip.add(new LiteralText(pad + "["));
				unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
				tooltip.add(new LiteralText(pad+"]"+(iter.hasNext()?",":"")));
			} else {
				addValueToTooltip(tooltip, nbtnext, "", pad, iter.hasNext());
			}
		}
	}
	
	private static void addNamedValueToTooltip(List<Text> tooltip, NbtElement nbt, String name, String pad, boolean addComma) {
		String cleanString = nbt instanceof NbtString ? escapeChars(nbt.toString()) : stripTypeIdentifiers(nbt.toString());
		tooltip.add(new LiteralText(pad+'"'+name+"\": "+cleanString+(addComma ? "," : "")));
	}
	
	private static void addValueToTooltip(List<Text> tooltip, NbtElement nbt, String name, String pad, boolean addComma) {
		String cleanString = nbt instanceof NbtString ? escapeChars(nbt.toString()) : stripTypeIdentifiers(nbt.toString());
		tooltip.add(new LiteralText(pad+cleanString+(addComma ? "," : "")));
	}
	
	private static String stripTypeIdentifiers(String string) {
		char last = string.charAt(string.length() - 1);
		if (last >= '0' && last <= '9') return string;
		return string.substring(0, string.length() - 1);
	}

	private static String escapeChars(String in) {
		if ((in.charAt(0) == '"' && in.charAt(in.length()-1) == '"') || (in.charAt(0) == '\'' && in.charAt(in.length()-1) == '\'')) {
			in = in.substring(1, in.length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (c == '"') {
				sb.append('\\');
				sb.append('"');
			} else {
				sb.append(c);
			}
		}
		sb.append('"');
		return sb.toString();
	}

}
