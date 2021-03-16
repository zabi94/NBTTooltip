package zabi.minecraft.nbttooltip;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.SystemToast.Type;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import zabi.minecraft.nbttooltip.config.ModConfig;
import zabi.minecraft.nbttooltip.mixin.NbttooltipKeybindAccessor;
import zabi.minecraft.nbttooltip.parse_engine.NbtTagParser;

public class NBTTooltip implements ClientModInitializer {

	public static int ticks = 0;
	public static int line_scrolled = 0;

	public static final String FORMAT = Formatting.ITALIC.toString()+Formatting.DARK_GRAY;

	public static KeyBinding COPY_TO_CLIPBOARD = new KeyBinding("key.nbttooltip.copy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, "key.category.nbttooltip");
	public static KeyBinding TOGGLE_NBT = new KeyBinding("key.nbttooltip.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UP, "key.category.nbttooltip");
	public static boolean flipflop_key_copy = false;
	public static boolean flipflop_key_toggle = false;
	
	public static boolean nbtKeyToggled = false;
	public static boolean nbtKeyPressed = false;
	
	@Override
	public void onInitializeClient() {
		ModConfig.init();
		ClientTickEvents.END_CLIENT_TICK.register(NBTTooltip::clientTick);
		ItemTooltipCallback.EVENT.register(NBTTooltip::onInjectTooltip);
		KeyBindingHelper.registerKeyBinding(COPY_TO_CLIPBOARD);
		KeyBindingHelper.registerKeyBinding(TOGGLE_NBT);
	}

	public static void clientTick(MinecraftClient mc) {
		if (!Screen.hasShiftDown()) {
			NBTTooltip.ticks++;
			int factor = 1;
			if (Screen.hasAltDown()) {
				factor = 4;
			}
			if (NBTTooltip.ticks >= ModConfig.INSTANCE.ticksBeforeScroll/factor) {
				NBTTooltip.ticks = 0;
				NBTTooltip.line_scrolled++;
			}
		}
		
		if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(TOGGLE_NBT.getBoundKeyTranslationKey()).getCode())) {
			if (!flipflop_key_toggle) {
				nbtKeyToggled = !nbtKeyToggled;
			}
			flipflop_key_toggle = true;
			nbtKeyPressed = true;
		} else {
			flipflop_key_toggle = false;
			nbtKeyPressed = false;
		}
		
	}

	public static ArrayList<Text> transformTtip(ArrayList<Text> ttip, int lines) {
		ArrayList<Text> newttip = new ArrayList<Text>(lines);
		if (ModConfig.INSTANCE.showSeparator) {
			newttip.add(new LiteralText("- NBTTooltip -"));
		}
		if (ttip.size()>lines) {
			if (lines+line_scrolled>ttip.size()) line_scrolled = 0;
			for (int i = 0; i < lines; i++) {
				newttip.add(ttip.get(i+line_scrolled));
			}
			return newttip;
		} else {
			line_scrolled = 0;
			newttip.addAll(ttip);
			return newttip;
		}
	}

	public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
		handleClipboardCopy(stack);
		if (ModConfig.INSTANCE.triggerType.shouldShowTooltip(context)) {
			int lines = ModConfig.INSTANCE.maxLinesShown;
			if (ModConfig.INSTANCE.ctrlSuppressesRest && Screen.hasControlDown()) {
				lines += list.size();
				list.clear();
			} else {
				list.add(new LiteralText(""));
			}
			CompoundTag tag = stack.getTag();
			ArrayList<Text> ttip = new ArrayList<Text>(lines);
			if (tag!=null) {
				if (ModConfig.INSTANCE.showDelimiters) {
					ttip.add(new LiteralText(Formatting.DARK_PURPLE+" - nbt start -"));
				}
				if (ModConfig.INSTANCE.compress) {
					ttip.add(new LiteralText(FORMAT+tag.toString()));
				} else {
					getRenderingEngine().parseTagToList(ttip, tag, ModConfig.INSTANCE.splitLongLines);
				}
				if (ModConfig.INSTANCE.showDelimiters) {
					ttip.add(new LiteralText(Formatting.DARK_PURPLE+" - nbt end -"));
				}
				ttip = NBTTooltip.transformTtip(ttip, lines);
				list.addAll(ttip);
			} else {
				list.add(new LiteralText(FORMAT+"No NBT tag"));
			}
		}
	}

	private static void handleClipboardCopy(ItemStack stack) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.currentScreen != null) {
			boolean pressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), ((NbttooltipKeybindAccessor) COPY_TO_CLIPBOARD).getBoundKey().getCode());
			if (pressed) {
				if (!flipflop_key_copy) {
					flipflop_key_copy = true;
					copyToClipboard(stack, mc);
				}
			} else {
				flipflop_key_copy = false;
			}
		}
	}

	private static void copyToClipboard(ItemStack stack, MinecraftClient mc) {
		StringBuilder sb = new StringBuilder();
		String name = I18n.translate(stack.getTranslationKey());
		ArrayList<Text> nbtData = new ArrayList<>();
		getCopyingEngine().parseTagToList(nbtData, stack.getTag(), false);
		nbtData.forEach(t -> {
			sb.append(t.asString().replaceAll("§[0-9a-gk-or]", ""));
			sb.append("\n");
		});
		try {
			mc.keyboard.setClipboard(sb.toString());
			mc.getToastManager().add(new SystemToast(Type.TUTORIAL_HINT, new TranslatableText("nbttooltip.copied_to_clipboard"), new TranslatableText("nbttooltip.object_details", name)));
		} catch (Exception e) {
			mc.getToastManager().add(new SystemToast(Type.TUTORIAL_HINT, new TranslatableText("nbttooltip.copy_failed"), new LiteralText(e.getMessage())));
			e.printStackTrace();
		}
	}
	
	private static NbtTagParser getRenderingEngine() {
		return ModConfig.INSTANCE.tooltipEngine.getEngine();
	}
	
	private static NbtTagParser getCopyingEngine() {
		return ModConfig.INSTANCE.copyingEngine.getEngine();
	}

}
