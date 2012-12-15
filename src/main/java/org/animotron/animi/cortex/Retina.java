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
package org.animotron.animi.cortex;

import static org.jocl.CL.*;

import org.animotron.animi.simulator.Stimulator;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
//TODO: macula lutea
public class Retina {

	//Сетчатка
//    public static final int WIDTH = 192;
//	public static final int HEIGHT = 144;
	public static final int WIDTH = 160;
	public static final int HEIGHT = 120;
	
	/* x */
	private int width;
	/* y */
	private int height;
	
	private OnOffMatrix onOff = null;
	
	public Retina() {
		this(WIDTH, HEIGHT);
	}

	public Retina(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	private CortexZoneSimple NL = null;
	public void setNextLayer(CortexZoneSimple sz) {
		NL = sz;
		
		width = sz.width();
		height = sz.height();

		initialize();
	}
	
    // создание связей сенсорных полей
	private void initialize() {
//		sensorField = new OnOffReceptiveField[NL.width()][NL.height()];
//
//        double XScale = (width - onOff.regionSize()) / (double)NL.width();
//        double YScale = (height - onOff.regionSize()) / (double)NL.height();
//
//        int X, Y;
//        int NC, NP;
//
//        int fl = 3;
//        
//        for (int ix = 0; ix < NL.width(); ix++) {
////        	fl++; if (fl == 3) fl = 1;
//        	for (int iy = 0; iy < NL.height(); iy++) {
//
//        		OnOffReceptiveField mSensPol = new OnOffReceptiveField();
//                sensorField[ix][iy] = mSensPol;
//                mSensPol.center = new int[onOff.numInCenter()][2];
//        		mSensPol.periphery = new int[onOff.numInPeref()][2];
//
//                // распределение он и офф полей
//            	mSensPol.type = fl;
//
//                X = (int)Math.round( ix * XScale + onOff.radius() + 1 );
//                Y = (int)Math.round( iy * YScale + onOff.radius() + 1 );
//
//                NC = 0;
//                NP = 0;
//
//                for (int i = 0; i < onOff.regionSize(); i++) {
//                    for (int j = 0; j < onOff.regionSize(); j++) {
//
//                    	switch (onOff.getType(i, j)) {
//
//                        case 0:
//                            break;
//                        case 1:
//
//                            mSensPol.periphery[NP][0] = X - onOff.radius() + i;
//                            mSensPol.periphery[NP][1] = Y - onOff.radius() + j;
//
//                            NP = NP + 1;
//                            break;
//                        case 2:
//
//                        	mSensPol.center[NC][0] = X - onOff.radius() + i;
//                        	mSensPol.center[NC][1] = Y - onOff.radius() + j;
//
//                            NC = NC + 1;
//                            break;
//						default:
//							break;
//                		}
//                    }
//                }
//        	}
//        }
    }
	
	//constants
    //минимальное соотношение средней контрасности переферии и центра сенсорного поля, необходимое для активации
    //контрастность для темных элементов (0)
    public static float KContr1 = 1.45f;
    //контрастность для светлых элементов (Level_Bright)
    public static float KContr2 = 1.15f;
	//минимальная контрастность
    public static float KContr3 = 1.15f;
	//(0..255)
    public static int Level_Bright = 100;
    public static int Lelel_min = 10;// * N_Col;

    int step = 0;

    BufferedImage image = null;
	float XScale = 0;
	float YScale = 0;
	
	int[] shiftMatrix = new int[] {
		0, 0,
		1, 1,
		1,-1,
		2, 0,
		3, 1,
		3,-1,
		4, 0,
	};
	
	public final int safeZone = 20;

	public void process(Stimulator stimulator) {
		
		if (onOff == null)
			onOff = new OnOffMatrix(NL.mc.context);
		
		if (step == 0) {
			image = stimulator.getNextImage();

			XScale = (image.getWidth()  - (safeZone*2)) / (float)NL.width;
			YScale = (image.getHeight() - (safeZone*2)) / (float)NL.height;
			
			NL.reset();
		}
		
		int shiftX = shiftMatrix[step*2  ];
		int shiftY = shiftMatrix[step*2+1];
		
//		Graphics g = image.getGraphics();
//		g.drawRect(10, 10, image.getWidth() - 20, image.getHeight() - 20);

        DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] _data_ = dataBuffer.getData();
        int[] data = new int[_data_.length];
        System.arraycopy(_data_, 0, data, 0, _data_.length);
        
//        System.out.println("image: "+Arrays.toString(data));

        RetinaTask retinaTask = 
			new RetinaTask(
    			(CortexZoneSimple)NL,
    			shiftX, shiftY,
    			XScale, YScale,
    			data,
    			image.getWidth(), image.getHeight()
			);

    	try {
            NL.mc.taskQueue.put(retinaTask);
        
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    	NL.mc.finish();
		

//        double XScale = physicalImage.getWidth() / (double)width;
//        double YScale = physicalImage.getHeight() / (double)height;
//
//        //preprocessing
//    	for (int x = 0; x < width; x++)
//        	for (int y = 0; y < height; y++)
//        		preprocessed[x][y] = Utils.calcGrey(physicalImage, (int)(x * XScale), (int)(y * YScale)) / 255;
//    	
//        XScale = width / (double)NL.width();
//        YScale = height / (double)NL.height();
//        
//        for (int ix = 0; ix < NL.width(); ix++) {
//        	for (int iy = 0; iy < NL.height(); iy++) {
//    			NL.shift(ix, iy, 0);
//        	}
//        }
//        
//        int nextX, nextY;
//        
////    	double SP, SC, SA;
////        double K_cont;
//        for (int ix = 0; ix < NL.width(); ix++) {
//        	for (int iy = 0; iy < NL.height(); iy++) {
//
////        		OnOffReceptiveField mSensPol = sensorField[ix][iy];
//
//        		nextX = ix + thisX;
//        		nextY = iy + thisY;
//        		
//        		if (nextX >= 0 && nextX < width && nextY >= 0 && nextY < height) {
//        			NL.shift(nextX, nextY, preprocessed[(int)(ix * XScale)][(int)(iy * YScale)]);
//        		}
//
////                NL.set(ix,iy,false);
////
////                SC = 0;
////                for (int j = 0; j < onOff.numInCenter(); j++) {
////
////                    SC += preprocessed[mSensPol.center[j][0]][mSensPol.center[j][1]];
////                }
////
////                SP = 0;
////                for (int j = 0; j < onOff.numInPeref(); j++) {
////
////                    SP += preprocessed[mSensPol.periphery[j][0]][mSensPol.periphery[j][1]];
////                }
////
////                SA = ((SP + SC) / (double)(onOff.numInCenter() + onOff.numInPeref()));
////
////                K_cont = KContr1 + SA * (KContr2 - KContr1) / Level_Bright;
////
////                if (K_cont < KContr3) K_cont = KContr3;
////
////                SC = SC / onOff.numInCenter();
////                SP = SP / onOff.numInPeref();
////
////                if (SA > Lelel_min) {
////                	switch (mSensPol.type) {
////                    case 1:
////
////                        if (SC / SP > K_cont)
////                        	NL.set(ix,iy,true);
////
////						break;
////
////                    case 2:
////
////                        if (SP / SC > K_cont)
////                        	NL.set(ix,iy,true);
////
////                        break;
////
////                    case 3:
////
////                        if (SC / SP > K_cont || SP / SC > K_cont)
////                        	NL.set(ix,iy,true);
////
////                        break;
////					default:
////						break;
////					}
////                }
//        	}
//        }
//        System.out.println("");
        NL.refreshImage();
        
		step++;
		if (step * 2 >= shiftMatrix.length) {
			step = 0;
		}
    }
    
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}

    public BufferedImage getImage() {
        return image;
    }
    
    public class RetinaTask extends Task {
    	
    	int offsetX, offsetY;
    	float XScale, YScale;
    	
    	int input[];
    	int inputSizeX, inputSizeY;
    	protected cl_mem inputMem;

		protected RetinaTask(CortexZoneSimple sz, 
				int offsetX, int offsetY, 
				float XScale, float YScale, 
				int input[], 
				int inputSizeX, int inputSizeY) {
			
			super(sz);
			
			this.offsetX = offsetX;
	    	this.offsetY = offsetY;
	    	this.XScale = XScale;
	    	this.YScale = YScale;
	    	
	    	this.input = input;
	    	this.inputSizeX = inputSizeX;
	    	this.inputSizeY = inputSizeY;
		}

	    protected void setupArguments(cl_kernel kernel) {
	    	super.setupArguments(kernel);

	        clSetKernelArg(kernel,  2, Sizeof.cl_int, Pointer.to(new int[] {safeZone}));
	        clSetKernelArg(kernel,  3, Sizeof.cl_int, Pointer.to(new int[] {offsetX}));
	        clSetKernelArg(kernel,  4, Sizeof.cl_int, Pointer.to(new int[] {offsetY}));
	        clSetKernelArg(kernel,  5, Sizeof.cl_float, Pointer.to(new float[] {XScale}));
	        clSetKernelArg(kernel,  6, Sizeof.cl_float, Pointer.to(new float[] {YScale}));

	        clSetKernelArg(kernel,  7, Sizeof.cl_int, Pointer.to(new int[] {onOff.radius}));
	        clSetKernelArg(kernel,  8, Sizeof.cl_int, Pointer.to(new int[] {onOff.regionSize}));
	        clSetKernelArg(kernel,  9, Sizeof.cl_mem, Pointer.to(onOff.cl_matrix));
	        clSetKernelArg(kernel,  10, Sizeof.cl_int, Pointer.to(new int[] {3}));
	        clSetKernelArg(kernel,  11, Sizeof.cl_float, Pointer.to(new float[] {KContr1}));
	        clSetKernelArg(kernel,  12, Sizeof.cl_float, Pointer.to(new float[] {KContr2}));
	        clSetKernelArg(kernel,  13, Sizeof.cl_float, Pointer.to(new float[] {KContr3}));
	        clSetKernelArg(kernel,  14, Sizeof.cl_int, Pointer.to(new int[] {Level_Bright}));
	        clSetKernelArg(kernel,  15, Sizeof.cl_int, Pointer.to(new int[] {Lelel_min}));

	        inputMem = clCreateBuffer(
	    		sz.mc.context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, 
	    		input.length * Sizeof.cl_int, Pointer.to(input), null
			);
	        clSetKernelArg(kernel,  16, Sizeof.cl_mem, Pointer.to(inputMem));
	        clSetKernelArg(kernel,  17, Sizeof.cl_int, Pointer.to(new int[] {inputSizeX}));
	        clSetKernelArg(kernel,  18, Sizeof.cl_int, Pointer.to(new int[] {inputSizeY}));
	    }

	    @Override
		protected void release() {
	    	clReleaseMemObject(inputMem);
		}
    }
}
