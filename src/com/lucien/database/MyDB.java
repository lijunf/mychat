package com.lucien.database;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.lucien.util.Constants;
import com.lucien.util.MyUtil;

/**
 * 数据库环境类
 * @author Lucien
 *
 */
public class MyDB {
    public final static String DB_JTDS = "jtds";
    public final static String DB_MSSQL = "mssql";
    public final static String DB_MYSQL = "mysql";
    public final static String DB_PGSQL = "pgsql";          ///< PostgreSQL
    public final static String DB_ORACLE = "oracle";

    protected String system = DB_JTDS;
    protected String dbType = DB_PGSQL;   					///< DB_MSSQL;
    protected String dbHost = "localhost";
    protected String dbName = "";
    protected String dbSchema = "";
    protected String logUser = "";
    protected String logPwd = "";
    protected int    dbLinkSize = 25;      					///< 数据库连接池数量

    protected Vector<Connection> conns = new Vector<Connection>();

    public MyDB() {

    }

    public Connection createConnection(String dbname, String user, String password) {
		dbName = dbname;
		logUser = user;
		logPwd = password;
		int pos = dbname.indexOf(".");
		if (pos > 0) {
		    dbSchema = dbname.substring(pos + 1) + ".";
		    dbName = dbname.substring(0, pos);
		}
		return createConnection();
    }

    protected Connection createConnection() {
		if (dbName == null || dbName.equals("")) return null;
		if (dbHost == null || dbHost.equals(""))  dbHost = "localhost";
		if (logUser == null || logUser.equals(""))  logUser = "sa";
		if (logPwd == null) logPwd = "123456";
		String jdbc = "";
		Connection conn = null;
		try {
			if (dbType.equalsIgnoreCase(DB_MSSQL)) {
				// MS SQL SERVER
				if (system.equalsIgnoreCase("linux")) {
				    java.lang.Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
				    jdbc = "jdbc:microsoft:sqlserver://" + dbHost;
				    if (dbHost.indexOf(":") <= 0) {
				    	jdbc += ":1433";
				    }
				    jdbc += ";databasename=" + dbName + ";user=" + logUser;
				    if (!logPwd.equals("")) {
				    	jdbc += ";password=" + logPwd;
				    }
				    conn = java.sql.DriverManager.getConnection(jdbc);
		
				} else if (system.equalsIgnoreCase(DB_JTDS)) {
				    java.lang.Class.forName("net.sourceforge.jtds.jdbc.Driver");
				    jdbc = "jdbc:jtds:sqlserver://" + dbHost;
				    if (dbHost.indexOf(":") <= 0) {
				    	jdbc += ":1433";
				    }
				    jdbc += "/" + dbName + ";SelectMethod=cursor";
				    conn = java.sql.DriverManager.getConnection(jdbc, logUser, logPwd);
		
				} else {
				    java.lang.Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				    jdbc = "jdbc:odbc:" + dbName;
				    conn = java.sql.DriverManager.getConnection(jdbc, logUser, logPwd);
				}
		
				conn.setAutoCommit(false);
		
				try {
				    conn.setTransactionIsolation(Connection.TRANSACTION_NONE);
				} catch (Exception e) {
		
				}
			} else if (dbType.equalsIgnoreCase(DB_MYSQL)) {
				// MY SQL
				java.lang.Class.forName("org.gjt.mm.mysql.Driver") .newInstance();
				jdbc = "jdbc:mysql://" + dbHost + "/" + dbName + "?user=" + logUser + "&password=" + logPwd;
		
				conn = java.sql.DriverManager.getConnection(jdbc);
	
		    } else if (dbType.equalsIgnoreCase(DB_PGSQL)) {
				// PostgreSQL 8.4
				java.lang.Class.forName("org.postgresql.Driver");
				jdbc = "jdbc:postgresql://" + dbHost;
				if (dbHost.indexOf(":") <= 0) {
				    jdbc += ":5432";
				}
				jdbc += "/" + dbName;
				conn = java.sql.DriverManager.getConnection(jdbc, logUser, logPwd);
	
		    } else if (dbType.equalsIgnoreCase(DB_ORACLE)) {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				java.sql.DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
				jdbc = "jdbc:oracle:thin:@" + dbHost;
				if (dbHost.indexOf(":") <= 0) {
				    jdbc += ":1521";
				}
	
				jdbc += ":" + dbName;
				conn = java.sql.DriverManager.getConnection(jdbc, logUser, logPwd);
		    }
		    conn.setAutoCommit(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return conn;
    }

    /**
     * 创建数据库连接
     * @return 返回数据库连接对象
     */
    public Connection connect() {
    	Connection conn = null;
    	synchronized(this) {
	    	try {
	    		if (conns.size() > 0) {
	    			conn = conns.remove(0);
	    		}
	        	if (conn == null || conn.isClosed()) {
	        		conn = createConnection();
	            	if (conn != null && conn.isClosed()) {
	            		conn = null;
	            	}
	        	}
	    	} catch(Exception e) {
	    		conn = null;
	    	}
    	}
    	return conn;
    }

    /**
     * 把当前数据库连接加回缓冲池
     * @param conn	数据库连接对象
     */
    public void pushback(Connection conn) {
		try {
		    if (conn != null) {
				synchronized (this) {
				    if (conns.size() < dbLinkSize && !conn.isClosed()) {
					conns.add(conn);
				    } else {
					conn.close();
				    }
				}
		    }
		} catch (Exception e) {
			
		}
    }

    /**
     * 清除数据库连接池
     */
    public void disconnect() {
		while (conns.size() > 0) {
		    Connection conn = conns.remove(0);
		    disconnect(conn);
		}
    }

    public void disconnect(Connection conn) {
		if (conn != null) {
		    try {
		    	conn.close();
		    } catch (Exception e) {
		    	
		    }
		}
    }

    public void jdbcRelease() {
    	disconnect();
    }

    public void setSystem(String sys) {
    	system = sys;
    }

    public void setDBHost(String host) {
    	dbHost = host;
    }

    public void setDBName(String name) {
    	dbName = name;
    }

    public void setLogUser(String user) {
    	logUser = user;
    }

    public void setLogPwd(String password) {
    	logPwd = password;
    }

    public void setDBType(String type) {
    	dbType = type;
    }

    public void setDbLinkSize(int dbLinkSize) {
		this.dbLinkSize = dbLinkSize;
	}

	public String getDBType() {
    	return dbType;
    }

    public String getDBName() {
    	return dbName;
    }

    public String getDbSchema() {
    	return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
    	this.dbSchema = dbSchema;
    }

    public String getLogUser() {
    	return logUser;
    }

    public String getLogPwd() {
    	return logPwd;
    }
    
    /**
	 * 根据Class将sql查询结果封装为对象
	 * @param sql		sql查询结果只有一条
	 * @param claszz			 需要构建对象的Class
	 * @return
	 */
    public Object queryForObject(String sql, Class<?> claszz) {
    	Object obj = null;
    	List<?> objs = queryForObjects(sql, claszz);
    	if (objs != null && objs.size() > 0) {
    		obj = objs.get(0);
    	}
    	return obj;
    }
    
    /**
     * 根据Class讲查询结果封装为对象集合
     * @param sql	sql查询语句
     * @param clazz		需要构建对象的Class
     * @return
     */
    public List<?> queryForObjects(String sql, Class<?> clazz) {
    	List<Object> objs = null;
		if (clazz != null && !MyUtil.isEmpty(sql)) {
			Statement stmt = null;
			Connection conn = connect();
			ResultSet rs = null;
			try {
				if (conn != null && !conn.isClosed()) {
					objs = new ArrayList<Object>();
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					while (rs.next()) {
						Object obj = Class.forName(clazz.getName()).newInstance();
						Field[] fields = clazz.getDeclaredFields();
						for (Field field : fields) {
							int modify = field.getModifiers();
							if (Modifier.isPrivate(modify)) {
								String fieldname = field.getName();
								Object fieldvalue = rs.getObject(fieldname);
								if (fieldvalue != null) {
									field.setAccessible(true);
									field.set(obj, fieldvalue);
								}
							}
						}
						objs.add(obj);
					}
				}
			} catch (Exception e) {
				MyUtil.debug("error:" + sql);
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
				    if (stmt != null) {
				    	stmt.close();
				    	pushback(conn);
				    }
				} catch (Exception e) {
				}
				MyUtil.debug("sql:" + sql);
			}
		}
		return objs;
    }
    
    /**
     * 根据表名（可以是视图名），进行分页查询
     * @param tablename		数据库表名或视图名
     * @param filter		针对表或者视图的过滤条件
     * @param clazz			查询结果封装对象的class
     * @param pagesize		每页显示的大小
     * @param current		当然查询第几页
     * @return				返回分页对象
     */
    public MyPage queryForObjects(MySql mySql, Class<?> clazz, int pagesize, int current) {
    	MyPage myPage = null;
    	if (mySql != null && clazz != null) {
    		mySql.MyDB(this);
    		String sql = mySql.countSql();
    		int rows = queryForInt(sql);
    		if (rows > 0) {
    			myPage = new MyPage(pagesize, current, rows);
    			sql = mySql.pageSql(myPage);
    			List<?> rs = queryForObjects(sql, clazz);
    			myPage.setRs(rs);
    		}
    	}
    	return myPage;
    }
    
    /**
     * 执行查询结果只返回一个int值的sql，
     * @param sql	sql查询语句
     * @return		返回查询结果
     */
    public int queryForInt(String sql) {
    	int result = -1;
    	if (!MyUtil.isEmpty(sql)) {
    		Statement stmt = null;
    		Connection conn = connect();
    		ResultSet rs = null;
    		try {
    			if (conn != null && !conn.isClosed()) {
    				stmt = conn.createStatement();
    				rs = stmt.executeQuery(sql);
    				if (rs.next()) {
    					result = rs.getInt(1);
    				}
    			}
    		} catch (Exception e) {
    			MyUtil.debug("error:" + sql);
    		} finally {
    			try {
    				if (rs != null) {
    					rs.close();
    				}
    			    if (stmt != null) {
    			    	stmt.close();
    			    	pushback(conn);
    			    }
    			} catch (Exception e) {
    			}
    			MyUtil.debug("sql:" + sql);
    		}
    	}
    	return result;
    }
    
    /**
     * 保存实体对象,将对象的所有非静态的私有属性全部保存入数据库
     * @param obj	对象引用
     * @param c		对象class
     * @return
     */
    public boolean saveObject(Object obj) {
    	boolean result = false;
    	if (obj != null) {
    		List<Object> vals = new ArrayList<Object>();
    		String fldstr = "";
    		String values = "";
    		String mark = "";
    		Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				int modify = field.getModifiers();
				if (Modifier.isPrivate(modify)) {
					try {
						String fieldname = field.getName();
						field.setAccessible(true);
						Object fieldvalue = field.get(obj);
						if (fieldvalue != null) {
							if (fieldvalue instanceof Date) {
								fieldvalue = MyUtil.parseSqlDate((Date) fieldvalue);
							}
							vals.add(fieldvalue);
							fldstr += mark + fieldname;
							values += mark + "?";
						}
						mark = ",";
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			String sql = "insert into " + Constants.dbprefix + obj.getClass().getSimpleName().toLowerCase() + "(" + fldstr + ") values (" + values + ")";
			result = executePrepareStatement(sql, vals);
    	}
    	return result;
    }
    
    /**
     * 执行带参数的sql语句
     * @param sql	sql语句
     * @param list	参数集合
     * @return
     */
    public boolean executePrepareStatement(String sql, List<Object> objs) {
    	boolean result = true;
    	if (!MyUtil.isEmpty(sql) && objs != null && objs.size() > 0) {
    		Connection conn = connect();
    		PreparedStatement stmt = null;
    		if (conn != null) {
    			try {
    				stmt = conn.prepareStatement(sql);
    				for (int index = 0; index < objs.size(); index++) {
    					stmt.setObject(index + 1, objs.get(index));
    				}
    				stmt.execute();
    			} catch (Exception e) {
    				result = false;
    				MyUtil.debug(sql);
					e.printStackTrace();
				} finally {
	    			try {
	    			    if (stmt != null) {
	    			    	stmt.close();
	    			    	pushback(conn);
	    			    }
	    			} catch (Exception e) {
	    			}
	    			MyUtil.debug("sql:" + sql);
	    		}
    		}
    	}
    	return result;
    }
    
    /**
     * 执行sql语句
     * @param sql
     * @return
     */
    public boolean executeStatement(String sql) {
		boolean result = true;
		Connection conn = null;
		Statement stmt = null;
		try {
		    conn = connect();
		    if (sql == null || conn == null || conn.isClosed()) {
		    	return false;
		    } else {
				stmt = conn.createStatement();
				stmt.execute(sql);
		    }
		} catch (Exception e) {
			result = false;
		    e.printStackTrace();
		} finally {
		    try {
				if (stmt != null) {
				    stmt.close();
				    pushback(conn);
				}
		    } catch (Exception e) {
		    	
		    }
		    MyUtil.debug("sql:" + sql);
		}
		return result;
    }

    /**
     * 执行存储过程
     * @param sql
     * @return
     */
    public boolean executeStoreProc(String sql) {
		boolean result = false;
		if (!MyUtil.isEmpty(sql)) {
		    Connection conn = connect();
		    if (conn != null) {
				CallableStatement proc = null;
				try {
				    synchronized (conn) {
						proc = conn.prepareCall(sql);
						proc.execute();
				    }
				    result = true;
				} catch (Exception e) {
				    MyUtil.debug("Error sql: ", sql);	  
				    e.printStackTrace();
				    result = false;
				} finally {
				    if (proc != null) {
						try {
						    proc.close();
						    pushback(conn);
						} catch (Exception ex) {
			
						}
				    }
				    MyUtil.debug("sql:", sql);
				}
		    }
		}
		return result;
    }
    
    /**
     * 执行批量数据动作
     * @param sql 动作
     * @param params 参数
     * @throws SQLException 
     */
    public boolean executeBatchStatement(String sql, List<List<String>> params) throws SQLException {
    	boolean result = false;
    	if (!MyUtil.isEmpty(sql)) {
    		Connection conn = connect();
    		PreparedStatement stmt = null;
		    if (conn != null) {  	
		    	try {
		    		conn.setAutoCommit(false);
			    	stmt = conn.prepareStatement(sql);
	    			for (List<String> p : params){
	    				try {
		    				if (p != null && p.size() > 0) {
		    					int index = 1;
		    					for (String val : p) {
		    						if (val != null) {
		    							val = val.trim();
						    			if (val.startsWith("'") && val.endsWith("'")) {
						    				val = val.substring(val.indexOf("'") + 1, val.lastIndexOf("'")).trim();
						    			}
		    						}
									stmt.setString(index, val);
									index ++;
		    					}
		    					stmt.addBatch();
		    				}	
	    				} catch (SQLException e) {
			    			throw e;
			    		} catch (Exception e) {
			    			
			    		}
	    			}		    		
			    	stmt.executeBatch();
					conn.commit();
					result = true;
				} catch (SQLException e) {
					try {
						if (conn != null) {
							conn.rollback();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					throw e;
				} finally {
					try {
						if (stmt != null) {
							stmt.close();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						try {
							conn.setAutoCommit(true);
							pushback(conn);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					MyUtil.debug("sql:" + sql);
				}
		    }
    	}
    	return result;
    }

}
