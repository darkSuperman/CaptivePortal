/**
 * ���ڹ���iptables�����״̬����
 * 
 * @author hz<khzliu@163.com>
 */

 package com.cafe.servlet;

import java.io.InputStreamReader;  
import java.io.LineNumberReader;  
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;


public class CaptivePortal extends HttpServlet{
	// ����������
    private final String driver = "com.mysql.jdbc.Driver";

    // URLָ��Ҫ���ʵ����ݿ���wb
    private final String url = "jdbc:mysql://localhost:3306/cafe";

	// MySQL����ʱ���û���
    private final String sql_user = "root"; 
  
	// MySQL����ʱ������
	private final String sql_passwd = "526156";

	public void service(HttpServletRequest req,HttpServletResponse res) 
		throws ServletException,IOException
	{
		process(req,res);
	}
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);	
	}

	public void destroy() {
		//�������iptables��
		String cmd = "iptables -X";
		executeIptabels(cmd);
			
		cmd = "iptables -F -t nat";
		executeIptabels(cmd);
			
		cmd = "ipset -X";
		executeIptabels(cmd);
          
    }  

	private void process(HttpServletRequest req,HttpServletResponse res)
		throws IOException
	{
		String ip = req.getRemoteAddr();//��ÿͻ���IP��ַ;
		String mac = getMACAddress(ip);
	
		String passwd = req.getParameter("password");
		int flag = userAuthentication(passwd); //��֤����
		PrintWriter out=res.getWriter();
		if (flag == 1)
		{	
			addUser(ip,mac);
			internetAccessLog();
			out.print(1);
		}
		else
		{	
			out.print(0);
		}

	}
    /**
     * ��ӽ���Internet���û�
     */
    public void addUser(String ip,String mac) {
			String cmd = "ipset -A nat_tables "+ip;
			executeIptabels(cmd);

			try {
			// ������������
			Class.forName(driver);

			// �������ݿ�
			Connection conn = DriverManager.getConnection(url, sql_user, sql_passwd);

			if(!conn.isClosed())
				System.out.println("Succeeded connecting to the Database!");
					

			// statement����ִ��SQL���
			Statement statement = conn.createStatement();
				
				
			// Ҫִ�е�SQL���
			String sql = "select mac from ipMacLoginTables where ip='"+ip+"'";
				
			// �����
			ResultSet rs = statement.executeQuery(sql);

			if (rs.next())
				{
					
					sql="update ipMacLoginTables set mac='"+mac+"' where ip='"+ip+"'";
					
					statement.executeUpdate(sql);
					
				}
				rs.close();
				conn.close();

			} catch(ClassNotFoundException e) {


				System.out.println("Sorry,can`t find the Driver!"); 
				e.printStackTrace();


           } catch(SQLException e) {


            e.printStackTrace();


           }
  
    }

	   /**
        * �û���֤
        * 
        * @return ��֤���
        */

	    private int userAuthentication(String pwd) {
			int flag = 0;
			try {
				// ������������
				Class.forName(driver);

				// �������ݿ�
				Connection conn = DriverManager.getConnection(url, sql_user, sql_passwd);

				if(!conn.isClosed())
					System.out.println("Succeeded connecting to the Database!");
					

				// statement����ִ��SQL���
				Statement statement = conn.createStatement();
				
				
				// Ҫִ�е�SQL���
				String sql = "select password from internetPassword";
				
				// �����
				ResultSet rs = statement.executeQuery(sql);

				

				if(rs.next())
				{
					if(rs.getString("password").equals(pwd))
						flag = 1;
				}
				
			}catch (SQLException e) {  
				e.printStackTrace();
				return 0;
			}catch (ClassNotFoundException e) {  
				e.printStackTrace();
				return 0;
			}

			return flag;

	    }


		
        // ͳ��ĳ����������Internet���û�����

		private void internetAccessLog()
		{
			 try { 
				// ������������
				Class.forName(driver);

				// �������ݿ�
				Connection conn = DriverManager.getConnection(url, sql_user, sql_passwd);

				if(!conn.isClosed())
					System.out.println("Succeeded connecting to the Database!");
					

				// statement����ִ��SQL���
				Statement statement = conn.createStatement();
				
				//��ȡ��ǰ����
				String date = getCal();
				
				// Ҫִ�е�SQL���
				String sql = "select times from internetAuthro where Date_id ='"+date+"'";
				
				// �����
				ResultSet rs = statement.executeQuery(sql);
				
				if (rs.next())
				{
					
					int count = rs.getInt("times")+1;
					
					sql="update internetAuthro set times="+count+" where Date_id='"+date+"'";
					
					statement.executeUpdate(sql);
					
					
				}else {
					
					sql="insert into internetAuthro(Date_id,times) values('"+date+"',1)";
					statement.executeUpdate(sql);
	
				}
				
				rs.close();
				conn.close();

			} catch(ClassNotFoundException e) {


				System.out.println("Sorry,can`t find the Driver!"); 
				e.printStackTrace();


           } catch(SQLException e) {


            e.printStackTrace();


           } catch(Exception e) {


            e.printStackTrace();


           } 

		}

		//��ȡ��ǰ����
		private String getCal() {
			int y=0,m=0,d=0;
			String sm="",sd="";
			
			// ������������
				
			NumberFormat nf = new DecimalFormat("00");
				
			Calendar cal=Calendar.getInstance(); 
				
			y=cal.get(Calendar.YEAR);
				
			m=cal.get(Calendar.MONTH);
				
			sm = nf.format(m);
				
			d=cal.get(Calendar.DATE);
				
			sd = nf.format(d);
			
			
			return y+sm+sd;
		}

		//ִ������
		private void executeIptabels(String c) {
			try
			{
				Process p = Runtime.getRuntime().exec(c);
				p.waitFor();
			}catch (IOException e) {  
             e.printStackTrace();  
			}catch (InterruptedException e) {  
             e.printStackTrace();  
			}
		}

		//��ȡmac��ַ
		public String getMACAddress(String ip) {
        String str = "";
        String macAddress = "";
        try {
            Process p = Runtime.getRuntime().exec("arp -n");
            InputStreamReader ir = new InputStreamReader(p.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
			p.waitFor();
			boolean flag = true;
            while(flag) {
                str = input.readLine();
                if (str != null) {
                    if (str.indexOf(ip) > 1) {
						int temp = str.indexOf("at");
                        macAddress = str.substring(
                                temp + 3, temp + 20);
                        break;
                    }
                } else
					flag = false;
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        } catch (InterruptedException e) {
			e.printStackTrace(System.out);
		}
        return macAddress;
    }

	//��ȡdns
	public String getDNSAddress() {
		String line = "";
		String DNSString = "8.8.8.8";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("/etc/resolv.conf"));
			line = br.readLine();
			String arrays[] = line.split(" ");
			if(arrays[1]==null)
				return DNSString;
			else
				DNSString = arrays[1];
			br.close();
		} catch (FileNotFoundException e) {	
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("��ȡ�ļ�ʧ��");
		} 

        return DNSString;
	}

}