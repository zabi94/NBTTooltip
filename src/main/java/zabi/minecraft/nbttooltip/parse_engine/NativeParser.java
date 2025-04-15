package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;
import java.util.stream.Collectors;

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
			return String.format("[%s%s]", listIdentifier((AbstractNbtList) tag), unwrapList((AbstractNbtList) tag));
		} else if (tag instanceof NbtCompound) {
			return String.format("{%s}", unwrapCompound((NbtCompound) tag));
		}
		return "";
	}

	private String unwrapCompound(NbtCompound tag) {
		if (tag.getKeys().isEmpty()) {
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

	private String unwrapList(AbstractNbtList tag) {
		if (tag.isEmpty()) {
			return "";
		}

		return tag.stream().map(this::unwrap).collect(Collectors.joining(","));
	}

	private String listIdentifier(AbstractNbtList tag) {
		return switch (tag) {
			case NbtByteArray ignored -> "B;";
			case NbtIntArray ignored -> "I;";
			case NbtLongArray ignored -> "L;";
			case null, default -> "";
		};
	}

	private String stripTypeIdentifier(String string) {
		char last = string.charAt(string.length() - 1);
		if (last >= '0' && last <= '9') return string;
		return string.substring(0, string.length() - 1);
	}
}
