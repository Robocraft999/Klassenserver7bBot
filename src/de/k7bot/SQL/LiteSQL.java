package de.k7bot.SQL;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiteSQL {
	private static Connection conn;
	private static Statement stmt;
	public final static Logger dblog = LoggerFactory.getLogger("DB-Log");

	public static void connect() {
		conn = null;

		try {
			File file = new File("resources/datenbank.db");
				file.createNewFile();

			String url = "jdbc:sqlite:" + file.getPath();
			conn = DriverManager.getConnection(url);
			stmt = conn.createStatement();
			dblog.info("Datenbankverbindung hergestellt");
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void disconnect() {
		try {
			if (conn != null) {
				conn.close();
				dblog.info("Datenbankverbindung getrennt");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void onUpdate(String sql) {
		try {
			stmt.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static ResultSet onQuery(String sql){
			
			try {
				return stmt.executeQuery(sql);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
			
	}

	public static Logger getdblog() {
		return dblog;
	}
}