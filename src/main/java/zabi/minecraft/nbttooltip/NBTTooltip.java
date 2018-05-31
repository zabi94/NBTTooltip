package zabi.minecraft.nbttooltip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zabi.minecraft.nbttooltip.ModConfig.EnumFetchType;

@Mod(name=NBTTooltip.MOD_NAME, modid=NBTTooltip.MOD_ID, version="0.6", clientSideOnly=true, acceptedMinecraftVersions="[1.11,1.13)", updateJSON="http://zabi.altervista.org/minecraft/nbttooltip/update.json")
public class NBTTooltip {
	
	public static final String MOD_NAME = "NBT Tooltip";
	public static final String MOD_ID = "nbttooltip";
	
	private static final String FORMAT = TextFormatting.ITALIC.toString()+TextFormatting.DARK_GRAY;
	private static int line_scrolled = 0, time = 0;
	
	@SideOnly(Side.CLIENT)
	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onTooltip(ItemTooltipEvent evt) {
		if (!ModConfig.requiresf3 || evt.getFlags().isAdvanced()) {
			NBTTagCompound tag = evt.getItemStack().getTagCompound();
			ArrayList<String> ttip = new ArrayList<String>(ModConfig.maxLinesShown);
			if (tag!=null) {
				evt.getToolTip().add("");

				if (ModConfig.showDelimiters) {
					ttip.add(TextFormatting.DARK_PURPLE+" - nbt start -");
				}
				if (ModConfig.compress) {
					ttip.add(FORMAT+tag.toString());
				} else {
					unwrapTag(ttip, tag, FORMAT, "", ModConfig.compress?"":"  ");
				}
				if (ModConfig.showDelimiters) {
					ttip.add(TextFormatting.DARK_PURPLE+" - nbt end -");
				}
				ttip = transformTtip(ttip);

				evt.getToolTip().addAll(ttip);
			} else {
				evt.getToolTip().add(FORMAT+"No NBT tag");
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRightClick(RightClickBlock evt) {
		if (ModConfig.fetchType==EnumFetchType.DISABLED) return;
		if ((evt.getWorld().isRemote && ModConfig.fetchType != EnumFetchType.SERVER) || (!evt.getWorld().isRemote && ModConfig.fetchType!=EnumFetchType.CLIENT)) {
			ItemStack stack = evt.getEntityPlayer().getHeldItem(evt.getHand());
			if (evt.getWorld().getTileEntity(evt.getPos())!=null && stack.getItem()==Items.ARROW) {
				ArrayList<String> tag = new ArrayList<String>();
				NBTTooltip.unwrapTag(tag, evt.getWorld().getTileEntity(evt.getPos()).writeToNBT(new NBTTagCompound()), "", "", "\t");
				final StringBuilder sb = new StringBuilder();
				tag.forEach(s -> {
					sb.append(s);
					sb.append('\n');
				});
				new InfoWindow(sb.toString(), evt.getWorld().isRemote);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent evt) {
		if (evt.phase == Phase.END && !GuiScreen.isShiftKeyDown()) {
			time++;
			if (time>=ModConfig.ticksBeforeScroll/(GuiScreen.isAltKeyDown()?4:1)) {
				time = 0;
				line_scrolled++;
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static ArrayList<String> transformTtip(ArrayList<String> ttip) {
		ArrayList<String> newttip = new ArrayList<String>(ModConfig.maxLinesShown);
		if (ModConfig.showSeparator) {
			newttip.add("- NBTTooltip -");
		}
		if (ttip.size()>ModConfig.maxLinesShown) {
			if (ModConfig.maxLinesShown+line_scrolled>ttip.size()) line_scrolled = 0;
			for (int i = 0; i < ModConfig.maxLinesShown; i++) {
				newttip.add(ttip.get(i+line_scrolled));
			}
			return newttip;
		} else {
			line_scrolled = 0;
			newttip.addAll(ttip);
			return newttip;
		}
	}

	@SideOnly(Side.CLIENT)
	static void unwrapTag(List<String> tooltip, NBTBase base, String pad, @Nonnull String tagName, String padIncrement) {
		if (base.getId()==10) {
			NBTTagCompound tag = (NBTTagCompound) base;
			tag.getKeySet().forEach(s -> {
				boolean nested = tag.getTag(s).getId()==10 || tag.getTag(s).getId()==9;
				if (nested) {
					tooltip.add(pad+s+": {");
					unwrapTag(tooltip, tag.getTag(s), pad+padIncrement, s, padIncrement);
					tooltip.add(pad+"}");
				} else {
					addValueToTooltip(tooltip, tag.getTag(s), s, pad);
				}
			});
		} else if (base.getId()==9) {
			NBTTagList tag = (NBTTagList) base;
			int index = 0;
			Iterator<NBTBase> iter = tag.iterator();
			while (iter.hasNext()) {
				NBTBase nbtnext = iter.next();
				if (nbtnext.getId()==10 || nbtnext.getId()==9) {
					tooltip.add(pad + "["+index+"]: {");
					unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
					tooltip.add(pad+"}");
				} else {
					tooltip.add(pad+"["+index+"] -> "+nbtnext.toString());
				}
				index++;
			}
		} else {
			addValueToTooltip(tooltip, base, tagName, pad);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent evt) {
		if (evt.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Type.INSTANCE);
		}
	}
	
	private static void addValueToTooltip(List<String> tooltip, NBTBase nbt, String name, String pad) {
		tooltip.add(pad+name+": "+nbt.toString());
	}

}
