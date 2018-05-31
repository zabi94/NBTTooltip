package zabi.minecraft.nbttooltip;

import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import net.minecraft.client.resources.I18n;

public class InfoWindow extends Frame {

	private static final long serialVersionUID = 8935325049409596603L;
	
	private static InfoWindow client = null;
	private static InfoWindow server = null;
	
	protected boolean isRemote;

	public InfoWindow(String tag, boolean isRemote) {
		this.setSize(400, 300);
		this.setTitle(I18n.format("reader.window", I18n.format("reader.side_"+(isRemote?"client":"server"))));
		TextArea ta = new TextArea(tag);
		this.add(ta);
		this.isRemote = isRemote;
		this.setAutoRequestFocus(false);
		this.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				InfoWindow.this.setVisible(false);
				InfoWindow.this.dispose();
				if (InfoWindow.this.isRemote) client = null;
				else server = null;
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
	
		if (isRemote) {
			if (client!=null && client.isValid()) {
				this.setBounds(client.getX(), client.getY(), client.getWidth(), client.getHeight());
				client.setVisible(false);
				client.dispose();
			}
			client = this;
		} else {
			if (server!=null && server.isValid()) {
				this.setBounds(server.getX(), server.getY(), server.getWidth(), server.getHeight());
				server.setVisible(false);
				server.dispose();
			}
			server = this;
		}
		
		this.setVisible(true);
	}

}
