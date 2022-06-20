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
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.SystemToast.Type;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import zabi.minecraft.nbttooltip.config.ModConfig;
import zabi.minecraft.nbttooltip.parse_engine.NbtTagParser;

public class NBTTooltip implements ClientModInitializer {

	public static int ticks = 0;
	public static int line_scrolled = 0;

	public static final String FORMAT = Formatting.ITALIC.toString()+Formatting.DARK_GRAY;

	public static final int WAITTIME_BEFORE_FAST_SCROLL = 10;

	public static KeyBinding COPY_TO_CLIPBOARD = new KeyBinding("key.nbttooltip.copy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "key.category.nbttooltip");
	public static KeyBinding TOGGLE_NBT = new KeyBinding("key.nbttooltip.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, "key.category.nbttooltip");
	public static KeyBinding SCROLL_UP = new KeyBinding("key.nbttooltip.scroll_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UP, "key.category.nbttooltip");
	public static KeyBinding SCROLL_DOWN = new KeyBinding("key.nbttooltip.scroll_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, "key.category.nbttooltip");

	public static boolean flipflop_key_copy = false;
	public static boolean flipflop_key_toggle = false;
	
	public static boolean nbtKeyToggled = false;
	public static boolean nbtKeyPressed = false;

	public static int fast_scroll_warmup = 0;
	public static int autoscroll_locks = 0;
	
	@Override
	public void onInitializeClient() {
		ModConfig.init();
		ClientTickEvents.END_CLIENT_TICK.register(NBTTooltip::clientTick);
		ItemTooltipCallback.EVENT.register(NBTTooltip::onInjectTooltip);
		KeyBindingHelper.registerKeyBinding(COPY_TO_CLIPBOARD);
		KeyBindingHelper.registerKeyBinding(TOGGLE_NBT);
		KeyBindingHelper.registerKeyBinding(SCROLL_DOWN);
		KeyBindingHelper.registerKeyBinding(SCROLL_UP);
	}

	public static void clientTick(MinecraftClient mc) {
		
		if (autoscroll_locks > 0) autoscroll_locks--;
		
		if (!Screen.hasShiftDown() && !isPressed(mc, SCROLL_DOWN) && !isPressed(mc, SCROLL_UP) && autoscroll_locks == 0) {
			NBTTooltip.ticks++;
			int factor = 1;
			if (Screen.hasAltDown()) {
				factor = 4;
			}
			if (NBTTooltip.ticks >= ModConfig.INSTANCE.ticksBeforeScroll/factor) {
				NBTTooltip.ticks = 0;
				if (ModConfig.INSTANCE.ticksBeforeScroll > 0) {
					NBTTooltip.line_scrolled++;
				}
			}
		}
		
		if (isPressed(mc, TOGGLE_NBT)) {
			if (!flipflop_key_toggle) {
				nbtKeyToggled = !nbtKeyToggled;
			}
			flipflop_key_toggle = true;
			nbtKeyPressed = true;
		} else {
			flipflop_key_toggle = false;
			nbtKeyPressed = false;
		}


		if (!isPressed(mc, SCROLL_DOWN) && isPressed(mc, SCROLL_UP) && line_scrolled > 0 && cooldownTimeAcceptable()) {
			line_scrolled--;
		}

		if (isPressed(mc, SCROLL_DOWN) && !isPressed(mc, SCROLL_UP) && cooldownTimeAcceptable()) {
			line_scrolled++;
		}

		if (isPressed(mc, SCROLL_DOWN) || isPressed(mc, SCROLL_UP)) {
			if (fast_scroll_warmup < WAITTIME_BEFORE_FAST_SCROLL) fast_scroll_warmup++;
			autoscroll_locks = 2;
		} else {
			fast_scroll_warmup = 0;
		}
	}

	private static boolean cooldownTimeAcceptable() {
		return fast_scroll_warmup == 0 || fast_scroll_warmup >= WAITTIME_BEFORE_FAST_SCROLL;
	}

	private static boolean isPressed(MinecraftClient mc, KeyBinding key) {
		return !key.isUnbound() && InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode());
	}

	public static ArrayList<Text> transformTtip(ArrayList<Text> ttip, int lines) {
		ArrayList<Text> newttip = new ArrayList<>(lines);
		if (ModConfig.INSTANCE.showSeparator) {
			newttip.add(Text.literal("- NBTTooltip -"));
		}
		if (ttip.size()>lines) {
			if (lines+line_scrolled>ttip.size()) {
				if (isPressed(MinecraftClient.getInstance(), SCROLL_DOWN)) {
					line_scrolled = ttip.size() - lines;
				} else {
					line_scrolled = 0;
				}
			}
			for (int i = 0; i < lines; i++) {
				newttip.add(ttip.get(i+line_scrolled));
			}
		} else {
			line_scrolled = 0;
			newttip.addAll(ttip);
		}
		return newttip;
	}

	public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
		handleClipboardCopy(stack);
		if (ModConfig.INSTANCE.triggerType.shouldShowTooltip(context)) {
			if (autoscroll_locks > 0) autoscroll_locks = 2;
			int lines = ModConfig.INSTANCE.maxLinesShown;
			if (ModConfig.INSTANCE.ctrlSuppressesRest && Screen.hasControlDown()) {
				lines += list.size();
				list.clear();
			} else {
				list.add(Text.literal(""));
			}
			NbtCompound tag = stack.getNbt();
			ArrayList<Text> ttip = new ArrayList<>(lines);
			if (tag!=null) {
				if (ModConfig.INSTANCE.showDelimiters) {
					ttip.add(Text.literal(Formatting.DARK_PURPLE+" - nbt start -"));
				}
				if (ModConfig.INSTANCE.compress) {
					ttip.add(Text.literal(FORMAT+ tag));
				} else {
					getRenderingEngine().parseTagToList(ttip, tag, ModConfig.INSTANCE.splitLongLines);
				}
				if (ModConfig.INSTANCE.showDelimiters) {
					ttip.add(Text.literal(Formatting.DARK_PURPLE+" - nbt end -"));
				}
				ttip = NBTTooltip.transformTtip(ttip, lines);
				list.addAll(ttip);
			} else {
				list.add(Text.literal(FORMAT+"No NBT tag"));
			}
		}
	}

	private static void handleClipboardCopy(ItemStack stack) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.currentScreen != null) {
			if (isPressed(mc, COPY_TO_CLIPBOARD)) {
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
		getCopyingEngine().parseTagToList(nbtData, stack.getNbt(), false);
		nbtData.forEach(t -> {
			sb.append(t.getString().replaceAll("§[0-9a-gk-or]", ""));
			sb.append("\n");
		});
		try {
			mc.keyboard.setClipboard(sb.toString());
			mc.getToastManager().add(new SystemToast(Type.TUTORIAL_HINT, Text.translatable("nbttooltip.copied_to_clipboard"), Text.translatable("nbttooltip.object_details", name)));
		} catch (Exception e) {
			mc.getToastManager().add(new SystemToast(Type.TUTORIAL_HINT, Text.translatable("nbttooltip.copy_failed"), Text.literal(e.getMessage())));
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
