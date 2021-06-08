package com.digitalstables.kicadparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.monitorjbl.xlsx.StreamingReader;

public class UpdateJLCPCBDatabase {

	static String fileAddress="https://jlcpcb.com/componentSearch/uploadComponentInfo";
	static String destination = "/Users/arifainchtein/Data/digitalstables/mcu/kicad_designs/JLCPCBDatabase.xls";

	public UpdateJLCPCBDatabase() {
	}
	public void process() {
		// TODO Auto-generated method stub

		// This will get input data from the server
		InputStream inputStream = null;

		// This will read the data from the server;
		OutputStream outputStream = null;

		try {
			// This will open a socket from client to server
			URL url = new URL(fileAddress);

			// This user agent is for if the server wants real humans to visit
			String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

			// This socket type will allow to set user_agent
			URLConnection con = url.openConnection();

			// Setting the user agent
			con.setRequestProperty("User-Agent", USER_AGENT);

			//Getting content Length
			int contentLength = con.getContentLength();
			System.out.println("File contentLength = " + contentLength + " bytes");


			// Requesting input data from server
			inputStream = con.getInputStream();

			// Open local file writer
			outputStream = new FileOutputStream(destination);

			// Limiting byte written to file per loop
			byte[] buffer = new byte[2048];

			// Increments file size
			int length;
			int downloaded = 0; 

			// Looping until server finishes
			while ((length = inputStream.read(buffer)) != -1) {
				// Writing data
				outputStream.write(buffer, 0, length);
				downloaded+=length;
				System.out.print("\rDownlad Status: ");
				System.out.printf("%.2f%%", ((double)downloaded/(double)contentLength)*100.0);
			//	System.out.println("Downlad Status: " + (downloaded * 100) / (contentLength * 1.0) + "%");
			}
			//readExcelFile(destination);

			PostgresqlPersistenceManager p = PostgresqlPersistenceManager.instance();
			p.initializeDatabase();

			Workbook wb = WorkbookFactory.create(new File(destination));
			Sheet mySheet;
			Iterator<Row> rowIter;
			int rowCounter=0;
			Row row;
			int Stock;
			int totalRows;
			String   lscspart,FirstCategory , SecondCategory , MFRPart , Package , SolderJoint , Manufacturer , LibraryType , Description , Datasheet , Price ;
			
			totalRows = wb.getSheetAt(0).getPhysicalNumberOfRows() + wb.getSheetAt(1).getPhysicalNumberOfRows();
			
			for(int i=0;i<2;i++) {
				mySheet = wb.getSheetAt(i);
				rowIter = mySheet.rowIterator();
				totalRows = mySheet.getPhysicalNumberOfRows();
				
				while(rowIter.hasNext()) {
					row = rowIter.next();

					if(rowCounter>0) {
						lscspart=row.getCell(0).getStringCellValue();
						FirstCategory =row.getCell(1).getStringCellValue();
						SecondCategory =row.getCell(2).getStringCellValue();
						MFRPart =row.getCell(3).getStringCellValue();
						Package =row.getCell(4).getStringCellValue();
						SolderJoint =row.getCell(5).getStringCellValue();
						Manufacturer =row.getCell(6).getStringCellValue();
						LibraryType =row.getCell(7).getStringCellValue();
						Description =row.getCell(8).getStringCellValue();
						Datasheet =row.getCell(9).getStringCellValue();
						Price =row.getCell(10).getStringCellValue();
						Stock=Integer.parseInt(row.getCell(11).getStringCellValue());
						p.insertRecord( lscspart,FirstCategory , SecondCategory , MFRPart , Package , SolderJoint , Manufacturer , LibraryType , Description , Datasheet , Price , Stock);
						//	        		System.out.println("lscs part= " + row.getCell(0).getStringCellValue());
						//	        		System.out.println("First Category= " + row.getCell(1).getStringCellValue());
						//	        		System.out.println("Second Category= " + row.getCell(2).getStringCellValue());
						//	        		System.out.println("MFR.Part= " + row.getCell(3).getStringCellValue());
						//	        		System.out.println("Package= " + row.getCell(4).getStringCellValue());
						//	        		System.out.println("Solder Joint= " + row.getCell(5).getStringCellValue());
						//	        		System.out.println("Manufacturer= " + row.getCell(6).getStringCellValue());
						//	        		System.out.println("Library Type= " + row.getCell(7).getStringCellValue());
						//	        		System.out.println("Description= " + row.getCell(8).getStringCellValue());
						//	        		System.out.println("Datasheet= " + row.getCell(9).getStringCellValue());
						//	        		System.out.println("Price= " + row.getCell(10).getStringCellValue());
						//	        		System.out.println("Stock= " + row.getCell(11).getStringCellValue());
					}
					System.out.print("\r rebuilding component database " );
					System.out.printf("%.2f%%", ((double)rowCounter/(double)totalRows)*100.0);
				
					rowCounter++;
				}
			}



		}catch(Exception e) {
			System.out.println("fail excel file : " + e);
		}



		// closing used resources
		// The computer will not be able to use the image
		// This is a must
		try {
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {

		new UpdateJLCPCBDatabase();
	}






}
