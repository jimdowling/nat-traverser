/*
 * Copyright (c) 2008
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package se.sics.gvod.simulator.croupier.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

/**
 * Class to work with text files.
 */

public class FileIO {

//----------------------------------------------------------------------------------
	/**
	 * Writes a string into a file.
	 * @param str Specifies the string that should be written into file.
	 * @param fileName Specifies the name of storing file.
	 * @throws IOException Thrown if it can not open the file or write in it. 
 	 */
	public static void write(String str, String fileName) {
		try {
			Writer output = null;
			FileWriter file = new FileWriter(fileName, false);
			output = new BufferedWriter(file);
			output.write(str);
			output.close();
		}
		catch(IOException e) {
            System.err.println("can not write in file " + fileName);
		}
	}

//----------------------------------------------------------------------------------
	/**
	 * Appends a string at the end of an existing file. It the file is not exist it creates it.
	 * @param str Specifies the string that should be written into file.
	 * @param fileName Specifies the name of storing file.
	 * @throws IOException Thrown if it can not open the file or append the content at the of file. 
	 */
	public static void append(String str, String fileName) {
		try {
			Writer output = null;
			FileWriter file = new FileWriter(fileName, true);
			output = new BufferedWriter(file);
			output.write(str);
			output.close();
		}
		catch(IOException e) {
            System.err.println("can not append to file " + fileName);
		}
	}

//----------------------------------------------------------------------------------
	/**
	 * Reads the content of a file and returns it as a string.
	 * @param fileName Specifies the name of storing file.
	 * @return A string that contains the whole content of file.
	 * @throws IOException Thrown if the the file is not exist. 
	 */
	public static String read(String fileName) {
		int numRead = 0;
		int curRead = 0;
		String str = null;

		try {
			File file = new File(fileName);
			InputStream in = new FileInputStream(file);
			long length = file.length();
			byte[] bytes = new byte[(int)length];

			while(curRead != length) {
				numRead = in.read(bytes, curRead, bytes.length - curRead);
				curRead += numRead;
			}
			
			str = new String(bytes);
			in.close();
        } catch (IOException e) {
            System.err.println("can not read from file " + fileName);
        }
        
        return str;
	}
	
}