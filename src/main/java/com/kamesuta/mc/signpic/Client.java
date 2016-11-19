package com.kamesuta.mc.signpic;

import java.awt.GraphicsEnvironment;
import java.io.Closeable;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.kamesuta.mc.signpic.command.RootCommand;
import com.kamesuta.mc.signpic.gui.GuiMain;
import com.kamesuta.mc.signpic.gui.OverlayFrame;
import com.kamesuta.mc.signpic.render.CustomTileEntitySignRenderer;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSign;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.MovingObjectPosition;

public class Client {
	public final static Minecraft mc = FMLClientHandler.instance().getClient();

	public static CustomTileEntitySignRenderer renderer;
	public static CoreHandler handler;
	public static Locations location;

	public static String mcversion;
	public static String forgeversion;

	public static String id;
	public static String name;

	public static RootCommand rootCommand;

	public static void openEditor() {
		mc.displayGuiScreen(new GuiMain(mc.currentScreen));
	}

	public static void startSection(final String sec) {
		mc.mcProfiler.startSection(sec);
	}

	public static void endSection() {
		mc.mcProfiler.endSection();
	}

	public static TileEntitySign getTileSignLooking() {
		if (MovePos.getBlock() instanceof BlockSign) {
			final TileEntity tile = MovePos.getTile();
			if (tile instanceof TileEntitySign)
				return (TileEntitySign) tile;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static void notice(final String notice, final float duration) {
		OverlayFrame.instance.pane.addNotice1(notice, duration);
	}

	public static void notice(final String notice) {
		Client.notice(notice, 2f);
	}

	public static class MovePos {
		public int x;
		public int y;
		public int z;

		public MovePos(final int x, final int y, final int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public static MovingObjectPosition getMovingPos() {
			return mc.objectMouseOver;
		}

		public static MovePos getBlockPos() {
			final MovingObjectPosition movingPos = getMovingPos();
			if (movingPos!=null)
				return new MovePos(movingPos.blockX, movingPos.blockY, movingPos.blockZ);
			return null;
		}

		public static TileEntity getTile() {
			final MovePos movePos = getBlockPos();
			if (movePos!=null)
				return mc.theWorld.getTileEntity(movePos.x, movePos.y, movePos.z);
			return null;
		}

		public static Block getBlock() {
			final MovePos movePos = getBlockPos();
			if (movePos!=null)
				return mc.theWorld.getBlock(movePos.x, movePos.y, movePos.z);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static void deleteMod(final File mod) {
		if (mod.delete())
			return;

		try {
			final LaunchClassLoader lloader = (LaunchClassLoader) Client.class.getClassLoader();
			final URL url = mod.toURI().toURL();
			final Field f_ucp = URLClassLoader.class.getDeclaredField("ucp");
			final Class<?> sunURLClassPath = Class.forName("sun.misc.URLClassPath");
			final Field f_loaders = sunURLClassPath.getDeclaredField("loaders");
			final Field f_lmap = sunURLClassPath.getDeclaredField("lmap");
			f_ucp.setAccessible(true);
			f_loaders.setAccessible(true);
			f_lmap.setAccessible(true);

			final Object ucp = f_ucp.get(lloader);
			final Closeable loader = ((Map<String, Closeable>) f_lmap.get(ucp)).remove(urlNoFragString(url));
			if (loader!=null) {
				loader.close();
				((List<?>) f_loaders.get(ucp)).remove(loader);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if (!mod.delete()) {
			mod.deleteOnExit();
			final String msg = Reference.NAME+" was unable to delete file "+mod.getPath()+" the game will now try to delete it on exit. If this dialog appears again, delete it manually.";
			Reference.logger.error(msg);
			if (!GraphicsEnvironment.isHeadless())
				JOptionPane.showMessageDialog(null, msg, "An update error has occured", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static String urlNoFragString(final URL url) {
		final StringBuilder strForm = new StringBuilder();

		String protocol = url.getProtocol();
		if (protocol!=null) {
			/* protocol is compared case-insensitive, so convert to lowercase */
			protocol = protocol.toLowerCase();
			strForm.append(protocol);
			strForm.append("://");
		}

		String host = url.getHost();
		if (host!=null) {
			/* host is compared case-insensitive, so convert to lowercase */
			host = host.toLowerCase();
			strForm.append(host);

			int port = url.getPort();
			if (port==-1)
				/* if no port is specificed then use the protocols
				 * default, if there is one */
				port = url.getDefaultPort();
			if (port!=-1)
				strForm.append(":").append(port);
		}

		final String file = url.getFile();
		if (file!=null)
			strForm.append(file);

		return strForm.toString();
	}

	public static void deleteMod() {
		if (Client.location.modFile.isFile())
			deleteMod(Client.location.modFile);
	}
}
