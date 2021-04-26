package com.digitalstables.kicadparser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class PostgresqlPersistenceManager {

	private static PostgresqlPersistenceManager aPostgresqlPersistenceManager;
	private final String DATABASE_URL = "postgres://postgres:sazirac@localhost:5432/postgres";
	private ConnectionPool connectionPool;

	public PostgresqlPersistenceManager() {

	}

	public static PostgresqlPersistenceManager instance() {

		if(aPostgresqlPersistenceManager==null){
			aPostgresqlPersistenceManager = new PostgresqlPersistenceManager();
			aPostgresqlPersistenceManager.init();
		}
		return aPostgresqlPersistenceManager;
	}

	public void init(){

		URI dbUri;
		int port=-1;
		String pwd="";
		try {



			//String DATABASE_URL = "postgres://postgres:"+pwd+"@localhost:"+ port +"/lucille";
			dbUri = new URI(DATABASE_URL);
			String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() +  dbUri.getPath() ;

			connectionPool = new ConnectionPool();

			if (dbUri.getUserInfo() != null) {
				connectionPool.setUsername(dbUri.getUserInfo().split(":")[0]);
				connectionPool.setPassword(dbUri.getUserInfo().split(":")[1]);
			}
			connectionPool.setDriverClassName("org.postgresql.Driver");
			connectionPool.setUrl(dbUrl);
			connectionPool.setMaxTotal(10);
			connectionPool.setMaxWaitMillis(180000);
			connectionPool.setInitialSize(3); 


		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			String s = getStringException(e);
			System.out.println(s);

		}
	}

	public void initializeDatabase() {
		String sql ="delete from  jlcpcb";
		Connection connection=null;
		PreparedStatement preparedStatement=null;

		try {
			connection = connectionPool.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(getStringException(e));
		}finally{
			if(preparedStatement!=null)
				try {

					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(getStringException(e));
				}
			if(connection!=null)closeConnection(connection);
		}
	}


	public void closeConnection(Connection con){
		try {
			connectionPool.closeConnection(con);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(getStringException(e));

		}
	}

	public void insertRecord(String lscspart, String FirstCategory ,String SecondCategory ,String MFRPart ,String Package ,String SolderJoint ,String Manufacturer ,String LibraryType ,String Description ,String Datasheet ,String Price ,int Stock  ) {
		String sql ="insert into  jlcpcb(lscspart,  FirstCategory , SecondCategory , MFRPart , Package , SolderJoint , Manufacturer , LibraryType , Description , Datasheet , Price , Stock ) values(?,?,?,?,?,?,?,?,?,?,?,?)";
		Connection connection=null;
		PreparedStatement preparedStatement=null;

		try {
			connection = connectionPool.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, lscspart);
			preparedStatement.setString(2, FirstCategory);
			preparedStatement.setString(3, SecondCategory);
			preparedStatement.setString(4, MFRPart);
			preparedStatement.setString(5, Package);
			preparedStatement.setString(6, SolderJoint);
			preparedStatement.setString(7, Manufacturer);
			preparedStatement.setString(8, LibraryType);
			preparedStatement.setString(9, Description);
			preparedStatement.setString(10, Datasheet);
			preparedStatement.setString(11, Price);
			preparedStatement.setInt(12, Stock);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(getStringException(e));
		}finally{
			if(preparedStatement!=null)
				try {

					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(getStringException(e));
				}
			if(connection!=null)closeConnection(connection);
		}

	}

	public JSONObject getRecordByLscspart (String lscspart ) {
		String sql = "select  FirstCategory , SecondCategory , MFRPart , Package , SolderJoint , Manufacturer , LibraryType , Description , Datasheet , Price , Stock from jlcpcb   where lscspart = ? ";
		Connection connection=null;
		PreparedStatement preparedStatement=null;
		String   FirstCategory , SecondCategory , MFRPart , Package , SolderJoint , Manufacturer , LibraryType , Description , Datasheet , Price ;
		ResultSet rs=null;
		int Stock;

		JSONObject toReturn=new JSONObject();

		try {
			connection = connectionPool.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, lscspart);

			rs = preparedStatement.executeQuery();
			while(rs.next()) {


				FirstCategory=rs.getString(1);
				SecondCategory = rs.getString(2);
				MFRPart=rs.getString(3);
				Package=rs.getString(4);
				SolderJoint=rs.getString(5);
				Manufacturer=rs.getString(6);
				LibraryType=rs.getString(7);  
				Description=rs.getString(8);  

				Datasheet = rs.getString(9);
				Price = rs.getString(10);
				Stock = rs.getInt(11);


				toReturn=new JSONObject();

				toReturn.put("FirstCategory", FirstCategory);
				toReturn.put("SecondCategory", SecondCategory);
				toReturn.put("MFRPart", MFRPart);
				toReturn.put("Package", Package);
				toReturn.put("SolderJoint", SolderJoint);
				toReturn.put("Manufacturer", Manufacturer);
				toReturn.put("LibraryType", LibraryType);
				toReturn.put("Description", Description);
				toReturn.put("Datasheet", Datasheet);
				toReturn.put("Price", Price);
				toReturn.put("Stock", Stock);
				toReturn.put("lscspart",lscspart);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(getStringException(e));
		}finally{
			if(preparedStatement!=null) {
				try {
					if(rs!=null)rs.close();
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(getStringException(e));
				}
			}

			if(connection!=null)closeConnection(connection);
		}
		return toReturn;
	}

	public int getStockByLscspart (String lscspart ) {
		String sql = "select   Stock from jlcpcb   where lscspart = ? ";
		Connection connection=null;
		PreparedStatement preparedStatement=null;
		String   FirstCategory , SecondCategory , MFRPart , Package , SolderJoint , Manufacturer , LibraryType , Description , Datasheet , Price ;
		ResultSet rs=null;
		int Stock=0;

		JSONObject toReturn=new JSONObject();

		try {
			connection = connectionPool.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, lscspart);

			rs = preparedStatement.executeQuery();
			while(rs.next()) {
				Stock = rs.getInt(1);
			}



		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(getStringException(e));
		}finally{
			if(preparedStatement!=null) {
				try {
					if(rs!=null)rs.close();
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(getStringException(e));
				}
			}

			if(connection!=null)closeConnection(connection);
		}
		return Stock;
	}
	
	public String getPriceByLscspart (String lscspart ) {
		String sql = "select   price from jlcpcb   where lscspart = ? ";
		Connection connection=null;
		PreparedStatement preparedStatement=null;
		String   FirstCategory , SecondCategory , MFRPart , Package , SolderJoint , Manufacturer , LibraryType , Description , Datasheet , Price ;
		ResultSet rs=null;
		String price="";

		JSONObject toReturn=new JSONObject();

		try {
			connection = connectionPool.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, lscspart);

			rs = preparedStatement.executeQuery();
			while(rs.next()) {
				price = rs.getString(1);
			}



		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(getStringException(e));
		}finally{
			if(preparedStatement!=null) {
				try {
					if(rs!=null)rs.close();
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println(getStringException(e));
				}
			}

			if(connection!=null)closeConnection(connection);
		}
		return price;
	}
	public static String getStringException(Exception e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		return exceptionAsString;
	}

}
