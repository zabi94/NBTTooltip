package zabi.minecraft.nbttooltip.parse_engine;

import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface NbtTagParser {

	void parseTagToList(List<Text> list, @Nullable NbtElement tag, boolean splitlines);
	
}
