//Copyright 2011-2012 Lohika .  This file is part of ALP.
//
//    ALP is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    ALP is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with ALP.  If not, see <http://www.gnu.org/licenses/>.
package com.lohika.alp.utils.object.reader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


/**
 * The Class ExcelReader.
 */
public class ExcelReader implements ObjectReader {

	/** The file name. */
	protected String fileName;
	
	/** The workbook. */
	private Workbook workbook;
	
	/** The sheet. */
	private Sheet sheet;
	
	/** Signals if should reads class fields from Horiz /Vertical. */
	protected boolean columnsHorizontal = true;
	
	/** The named index. */
	protected boolean namedIndex = false;
	
	/**
	 * Checks if is columns horizontal.
	 *
	 * @return true, if is columns horizontal
	 */
	public boolean isColumnsHorizontal() {
		return columnsHorizontal;
	}

	/**
	 * Sets the columns horizontal.
	 *
	 * @param columnsHorizontal the new columns horizontal
	 */
	public void setColumnsHorizontal(boolean columnsHorizontal) {
		this.columnsHorizontal = columnsHorizontal;
	}
	
	/**
	 * Checks if is named index.
	 *
	 * @return true, if is named index
	 */
	public boolean isNamedIndex() {
		return namedIndex;
	}

	/**
	 * Sets the named index.
	 *
	 * @param namedIndex the new named index
	 */
	public void setNamedIndex(boolean namedIndex) {
		this.namedIndex = namedIndex;
	}
	
	/**
	 * Instantiates a new excel reader.
	 *
	 * @param fileName the file name
	 * @throws BiffException the biff exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ExcelReader(String fileName)
			throws BiffException, IOException {
		this.open(fileName);
	}
	
	/**
	 * Instantiates a new excel reader.
	 *
	 * @param fileName the file name
	 * @param columnsHorizontal the columns horizontal
	 * @param namedIndex the named index
	 * @throws BiffException the biff exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ExcelReader(String fileName, boolean columnsHorizontal, boolean namedIndex)
			throws BiffException, IOException {
		URL url = getClass().getClassLoader().getResource(fileName);
		if (url == null) throw new RuntimeException("Unable get resource '"+fileName+"'");
		this.open(url.getPath());
		setColumnsHorizontal(columnsHorizontal);
		setNamedIndex(namedIndex);
	}
	
	/**
	 * Gets the columns.
	 *
	 * @return the columns
	 */
	protected Cell[] getColumns() {
		if (sheet==null)
			return null;
		if (isColumnsHorizontal())
			return sheet.getRow(0);
		else
			return sheet.getColumn(0);
	}
	
	/**
	 * Gets the record.
	 *
	 * @param index the index
	 * @return the record
	 */
	protected Cell[] getRecord(int index) {
		if (sheet==null)
			return null;
		if (isColumnsHorizontal())
			return sheet.getRow(getIndex(index));
		else
			return sheet.getColumn(getIndex(index));
	}
	
	/**
	 * Gets the index.
	 *
	 * @param index the index
	 * @return the index
	 */
	private int getIndex(int index) {
		return index+1;
	}
	
	/**
	 * Gets the indexes.
	 *
	 * @return the indexes
	 */
	public Cell[] getIndexes() {
		if (!isNamedIndex() || sheet==null)
			return null;
		if (isColumnsHorizontal())
			return sheet.getColumn(0);
		else
			return sheet.getRow(0);
	}
	
	/* (non-Javadoc)
	 * @see com.lohika.alp.utils.object.reader.ObjectReader#readObject(java.lang.Class, int)
	 */
	public Object readObject(Class<?> type, int index) throws ObjectReaderException {
		sheet = workbook.getSheet(type.getSimpleName());
		if (sheet == null)
			throw new ObjectReaderException("Sheet of type '"+type.getName()+
				"' is absent in the file '"+fileName+"'");
		
		// get fields of first row - each column value is a class field name
		Cell[] columnFields = getColumns();
		
		// get all fields from the class
		Field[] classFields = type.getDeclaredFields();

		Cell[] dataFields = getRecord(index);
		
		Object item = null;
		try {
			// instantiate object of specific type
			item = type.getDeclaredConstructor().newInstance();
	
			for (int column=0; column<classFields.length; column++) {
				if (fieldInArray(columnFields, classFields[column].getName()));
				{
						//System.out.println(dataFields[column].getContents());
						classFields[column].setAccessible(true);
						classFields[column].set(item, dataFields[column].getContents());
				}
			}
		} catch (NoSuchMethodException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		}

		return item;
	}

	/* (non-Javadoc)
	 * @see com.lohika.alp.utils.object.reader.ObjectReader#readAllObjects(java.lang.Class)
	 */
	public List<?> readAllObjects(Class<?> type) throws Exception {
		sheet = workbook.getSheet(type.getSimpleName());
		if (sheet == null)
			throw new Exception("Sheet of type '"+type.getName()+
				"' is absent in the file '"+fileName+"'");
		
		// get fields of first row - each column value is a class field name
		Cell[] columnFields = getColumns();
		
		// get all fields from the class
		Field[] classFields = type.getDeclaredFields();

		int count = sheet.getRows()-1;
        List<Object> result = new ArrayList<Object>();

		for (int row=0; row<count; row++) {
			Cell[] dataFields = sheet.getRow(row+1);
			Object item = type.getDeclaredConstructor().newInstance();

			for (int column=0; column<classFields.length; column++) {
				if (fieldInArray(columnFields, classFields[column].getName()));
				{
						//System.out.println(dataFields[column].getContents());
						classFields[column].setAccessible(true);
						classFields[column].set(item, dataFields[column].getContents());
				}
			}
			result.add(item);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see com.lohika.alp.utils.object.reader.ObjectReader#open(java.lang.String)
	 */
	public void open(String fileName) throws BiffException, IOException {
		workbook = Workbook.getWorkbook(new File(fileName));
		this.fileName = fileName;
	}

	/* (non-Javadoc)
	 * @see com.lohika.alp.utils.object.reader.ObjectReader#close()
	 */
	public void close() {
		workbook.close();
	}

	/**
	 * check if the excel columns contain fieldName.
	 *
	 * @param cell the cell
	 * @param fieldName the field name
	 * @return true, if successful
	 */
	private boolean fieldInArray(Cell[] cell, String fieldName) {
		for (int i=0; i<cell.length; i++) 
			if (cell[i].getContents().equals(fieldName))
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see com.lohika.alp.utils.object.reader.ObjectReader#readObject(java.lang.Class, java.lang.String)
	 */
	public Object readObject(Class<?> type, String index) throws ObjectReaderException {
		if (index==null || type==null)
			throw new ObjectReaderException("Parameters should not be null");

		sheet = workbook.getSheet(type.getSimpleName());

		if (sheet == null)
			throw new ObjectReaderException("Sheet of type '"+type.getName()+
				"' is absent in the file '"+fileName+"'");
			
		Integer objectIndex = null;
		Cell[] indexes = getIndexes();
		for (Cell cell: indexes) {
			if (index.equals(cell.getContents()))
				if (isColumnsHorizontal())
					objectIndex = cell.getRow()-1;
				else
					objectIndex = cell.getColumn()-1;
		}
		
		if (objectIndex==null)
			throw new ObjectReaderException("Record with '"+index+"' was not found");
		
		Cell[] columnFields = getColumns();
		Field[] classFields = type.getDeclaredFields();
		Cell[] dataFields = getRecord(objectIndex);

		Object item = null;
		try {
			// instantiate object of specific type
			item = type.getDeclaredConstructor().newInstance();
	
			for (int column=0; column<classFields.length; column++) {
				if (fieldInArray(columnFields, classFields[column].getName()));
				{
						classFields[column].setAccessible(true);
						if (isNamedIndex())
							classFields[column].set(item,
								dataFields[column+1].getContents());
						else
							classFields[column].set(item,
								dataFields[column].getContents());
				}
			}
		} catch (NoSuchMethodException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new ObjectReaderException(e.getMessage(), e);
		}

		return item;
	}

}
