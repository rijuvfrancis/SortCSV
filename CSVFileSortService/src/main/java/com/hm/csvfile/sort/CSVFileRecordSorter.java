package com.hm.csvfile.sort;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hm.csvfile.sort.exception.CSVSorterException;
import com.hm.csvfile.sort.log.Logger;

/**
 * The Class CSVFileRecordSorter.
 *
 * @author Riju Francis
 */
public class CSVFileRecordSorter {

	/**
	 * The main method.
	 *
	 * @param args
	 *            the args
	 * @throws IOException
	 *             the IO exception
	 */
	public static void main(String[] args) throws IOException {
		Logger.info("Executing csv file sorter");

		if (args.length < 4) {
			Logger.error("Invalid inputs");
			Logger.info(
					"Please use the format : java [your-main-class] [file-to-be-sorted] [column-to-sort-on] [sort-order-definition-file] [sort-order-definition-column]");
			return;
		}

		List<List<String>> sortedFileRecordsList = new CSVFileRecordSorter().sortCSVFileRecords(args[0], args[1],
				args[2], args[3]);
		sortedFileRecordsList.forEach(sortedRecordsList -> System.out
				.println(sortedRecordsList.get(0) + "," + sortedRecordsList.get(1) + "," + sortedRecordsList.get(2)));

		Logger.info("Sorter csv file records execution completed");
	}

	/**
	 * Sort csv file records.
	 *
	 * @param sourceDataRecordFileName
	 *            the source data record file name
	 * @param columnToBeSortOn
	 *            the column to be sort on
	 * @param sortOrderDefinitionFileName
	 *            the sort order definition file name
	 * @param sortOrderdefinitionColumn
	 *            the sort orderdefinition column
	 * @return the list< list< string>>
	 */
	private List<List<String>> sortCSVFileRecords(String sourceDataRecordFileName, String columnToBeSortOn,
			String sortOrderDefinitionFileName, String sortOrderdefinitionColumn) {

		List<String> sourceDataRecordFileHeader = new ArrayList<>();
		List<String> sortOrderDefinitionFileHeader = new ArrayList<>();

		List<List<String>> sourceDataRecordFileRecordsList = new ArrayList<>();
		List<List<String>> sortOrderDefinitionFileRecordsList = new ArrayList<>();

		Logger.debug("Reading records from source csv file");
		getFileContentAsList(sourceDataRecordFileName, sourceDataRecordFileHeader, sourceDataRecordFileRecordsList);

		Logger.debug("Reading records from sorter definision csv file");
		getFileContentAsList(sortOrderDefinitionFileName, sortOrderDefinitionFileHeader,
				sortOrderDefinitionFileRecordsList);

		Logger.debug("Getting sort order list");
		List<String> sortOrderList = getSortOrderList(sortOrderDefinitionFileRecordsList,
				sortOrderDefinitionFileHeader.indexOf(sortOrderdefinitionColumn));

		Logger.debug("Sorting records");
		Collections.sort(sourceDataRecordFileRecordsList, new Comparator<List<String>>() {
			@Override
			public int compare(List<String> record1, List<String> record2) {
				int valueIndex = sourceDataRecordFileHeader.indexOf(columnToBeSortOn);
				if (valueIndex < 0) {
					Logger.error("Unable to find sort order record");
					throw new CSVSorterException("Unable to find sort order record");
				}
				String value1 = record1.get(valueIndex);
				String value2 = record2.get(valueIndex);

				return Integer.valueOf(sortOrderList.indexOf(value1)).compareTo(sortOrderList.indexOf(value2));
			}
		});

		return sourceDataRecordFileRecordsList;
	}

	/**
	 * Gets the sort order list.
	 *
	 * @param sortOrderDefinitionFileRecordsList
	 *            the sort order definition file records list
	 * @param columnIndex
	 *            the column index
	 * @return the sort order list
	 */
	private List<String> getSortOrderList(List<List<String>> sortOrderDefinitionFileRecordsList, int columnIndex) {
		if (columnIndex < 0) {
			Logger.error("Unable to find sort definition in the file");
			throw new CSVSorterException("Unable to find sort definition in the file");
		}
		List<String> sortOrderList = new ArrayList<>();
		sortOrderDefinitionFileRecordsList.forEach(record -> {
			sortOrderList.add(record.get(columnIndex));
		});
		return sortOrderList;
	}

	/**
	 * Gets the file content as list.
	 *
	 * @param csvFileName
	 *            the csv file name
	 * @param dataRecordFileHeader
	 *            the data record file header
	 * @param dataRecordFileRecordsList
	 *            the data record file records list
	 */
	private void getFileContentAsList(String csvFileName, List<String> dataRecordFileHeader,
			List<List<String>> dataRecordFileRecordsList) {
		Logger.debug("Reading file " + csvFileName);
		ClassLoader classLoader = this.getClass().getClassLoader();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(classLoader.getResource(csvFileName).getFile()));
			String line = reader.readLine();
			int lineIndex = 0;
			while (line != null) {
				if (lineIndex == 0) {
					dataRecordFileHeader.addAll(parserCSVRecord(line));
				} else {
					dataRecordFileRecordsList.add(parserCSVRecord(line));
				}
				lineIndex++;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			Logger.error("Unable to open the file");
			throw new CSVSorterException("Unable to read file");
		}
	}

	/**
	 * Parser csv record.
	 *
	 * @param line
	 *            the line
	 * @return the list< string>
	 */
	private List<String> parserCSVRecord(String line) {
		if (line != null && line.length() > 0) {
			return new ArrayList<>(Arrays.asList(line.split(",")));
		}
		return null;
	}
}
