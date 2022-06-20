package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class NativeParser implements NbtTagParser {
	
	@Override
	public void parseTagToList(List<Text> list, @Nullable NbtElement tag, boolean split) {
		if (tag == null) {
			list.add(Text.literal("{}"));
		} else {
			list.add(Text.literal(unwrap(tag)));
		}
	}

	public String unwrap(NbtElement tag) {
		if (tag instanceof AbstractNbtNumber) {
			return stripTypeIdentifier(tag.toString());
		} else if (tag instanceof NbtString) {
			return tag.toString();
		} else if (tag instanceof AbstractNbtList) {
			return String.format("[%s%s]", listIdentifier((AbstractNbtList<?>) tag), unwrapList((AbstractNbtList<?>) tag));
		} else if (tag instanceof NbtCompound) {
			return String.format("{%s}", unwrapCompound((NbtCompound) tag));
		}
		return "";
	}

	private String unwrapCompound(NbtCompound tag) {
		if (tag.getKeys().size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s:tag.getKeys()) {
			sb.append(s);
			sb.append(":");
			sb.append(unwrap(tag.get(s)));
			sb.append(",");
		}
		return sb.substring(0, sb.toString().length() - 1); //Remove last comma
	}

	private String unwrapList(AbstractNbtList<?> tag) {
		if (tag.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tag.size() - 1; i++) {
			sb.append(unwrap(tag.get(i)));
			sb.append(',');
		}
		sb.append(unwrap(tag.get(tag.size() - 1)));
		return sb.toString();
	}
	
	private String listIdentifier(AbstractNbtList<?> tag) {
		if (tag instanceof NbtByteArray) {
			return "B;";
		} else if (tag instanceof NbtIntArray) {
			return "I;";
		} else if (tag instanceof NbtLongArray) {
			return "L;";
		} else return "";
	}

	private String stripTypeIdentifier(String string) {
		char last = string.charAt(string.length() - 1);
		if (last >= '0' && last <= '9') return string;
		return string.substring(0, string.length() - 1);
	}


}
