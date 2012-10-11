/*
 *  Copyright (C) 2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
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
package org.animotron.animi.cortex;

import org.animotron.animi.Utils;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Simple cortex zone
 * 
 * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
 * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 */
public class CortexZoneSimple implements Layer {

    String name;
    MultiCortex mc;
    /** State of complex neurons (outputs cortical columns) **/
    public NeuronComplex[][] col;

    public CortexZoneSimple(String name, MultiCortex mc) {
        this.name = name;
        this.mc = mc;
    }
    
    public void init() {
        int width = mc.VISUAL_FIELD_WIDTH;
        int height = mc.VISUAL_FIELD_HEIGHT;
        
        this.col = new NeuronComplex[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                NeuronComplex cn = new NeuronComplex();
                cn.active = false;
                this.col[x][y] = cn;
            }
        }
    }

    public BufferedImage getImage() {
        int width = mc.VISUAL_FIELD_WIDTH;
        int height = mc.VISUAL_FIELD_HEIGHT;

        int c;
    	
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = col[x][y].active ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
                image.setRGB(x, y, Utils.create_rgb(255, c, c, c));
            }
        }
        return image;
    }


	@Override
	public Object whatAt(Point point) {
		return col[point.x][point.y];
	}

    public String getImageName() {
    	return toString();
    }

    public String toString() {
    	return name;
    }

    public int width() {
        return mc.VISUAL_FIELD_WIDTH;
    }

    public int height() {
        return mc.VISUAL_FIELD_HEIGHT;
    }

	@Override
	public void process() {
	}

	@Override
	public void set(int x, int y, boolean b) {
		col[x][y].active = b;
	}
}