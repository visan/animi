/*
 *  Copyright (C) 2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animi.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.animotron.animi.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import javax.swing.*;

import org.animotron.animi.InitParam;
import org.animotron.animi.Params;
import org.animotron.animi.cortex.MultiCortex;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class PFInitialization extends JInternalFrame {
	
	private static final long serialVersionUID = -2223763417833552625L;
	
	JPanel panel;

	public PFInitialization(final Application app, MultiCortex mc) {
	    super("Initialization params",
	            false, //resizable
	            false, //closable
	            false, //maximizable
	            false);//iconifiable
	    
	    panel = new JPanel();
	    
	    GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        
        scan(gbc, null, mc);
		
		JButton btInit = new JButton("Init");
        btInit.setSelected(true);
        btInit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.initialize();
				app.closeFrame(PFInitialization.this);
			}
		});

        JButton btCancel = new JButton("Cancel");
        btCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.closeFrame(PFInitialization.this);
			}
		});
		
//        Insets insets = btInit.getInsets();
//        insets.left = btInit.getPreferredSize().width;
//        EmptyBorder border = new EmptyBorder(insets);

        gbc.gridy++;
        gbc.gridx = 1;
        panel.add(btInit, gbc);

        gbc.gridx++;
        panel.add(btCancel, gbc);
        
		setLocation(100, 100);
		
		setContentPane(panel);
		
		pack();
	}
	
	private void scan(GridBagConstraints gbc, Field f, Object _obj) {
		Object obj = _obj;
		if (f != null)
			try {
				f.setAccessible(true);
				obj = f.get(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}

		Class<?> clazz = obj.getClass();
		
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			System.out.println(field.getName());
			if (field.isAnnotationPresent(InitParam.class)) {
				addField(gbc, field, obj);
			
			} else if (field.isAnnotationPresent(Params.class)) {
				System.out.println("# "+field.getName());
				addSep(gbc, field.getName());
				scan(gbc, field, obj);
			}
		}
	}

	private void addSep(GridBagConstraints gbc, String name) {
		JLabel label = new JLabel(name);

        gbc.gridy++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(label, gbc);
        gbc.gridwidth = 1;
	}

	private void addField(GridBagConstraints gbc, Field f, Object obj) {

        JTextField text = new JTextField(getValue(f, obj));
		
		JLabel label = new JLabel(getName(f));

        gbc.gridy++;
        gbc.gridx = 1;
        panel.add(label, gbc);

        gbc.gridx++;
        panel.add(text, gbc);

    //		group.addGroup(
//			layout.createParallelGroup(Alignment.BASELINE)
//				.addComponent(label)
//				.addComponent(text)
//		);
	}

	private String getName(Field f) {
		return f.getName();
//		return f.getAnnotation(RuntimeParam.class).name();
	}

	private String getValue(Field f, Object obj) {
		try {
			f.setAccessible(true);
			return f.get(obj).toString();
		} catch (Exception e) {
		}
		return "???";
	}
}