
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.sql.*;

public class DnsConfig{
    // ����������
    private final String driver = "com.mysql.jdbc.Driver";

    // URLָ��Ҫ���ʵ����ݿ���wb
    private final String url = "jdbc:mysql://localhost:3306/cafe";

    // MySQL����ʱ���û���
    private final String sql_user = "root"; 
  
    // MySQL����ʱ������
    private final String sql_passwd = "526156";
    /**
     * ��ʼ��iptables����
     */
    private void initDNSSet() {
        
        //�������iptables��
	String cmd = "iptables -X";
	executeIptabels(cmd);
			
	cmd = "iptables -F -t nat";
	executeIptabels(cmd);
			
	cmd = "ipset -X";
	executeIptabels(cmd);

	//nat_tables��Ϊ������������ip��
	cmd = "ipset -N nat_tables iphash --timeout 14400";
	executeIptabels(cmd);
			
	cmd = "iptables -t nat -A PREROUTING -i wlan0 -m set ! --match-set nat_tables src -p tcp --dport 80 -j DNAT --to 192.168.5.1";
	executeIptabels(cmd);
                        
	//����443�˿�ͨ��������ͨ��google��ѯȻ����ת
	cmd = "iptables -t nat -A POSTROUTING -o eth0 -d 74.125.128.199 -p tcp --dport 443 -j MASQUERADE";
	executeIptabels(cmd);
	//����nat_tables�ڿ��Է����κε�ַ
	cmd = "iptables -t nat -A POSTROUTING -o eth0 -m set --match-set nat_tables src -j MASQUERADE";
	executeIptabels(cmd);

        //��ʼ����·��ͨ�Լ�¼
        setIsConnective(0);
        //������ͨ��
        String baidu = "www.baidu.com";
        String google = "www.google.ca";
        while(true){
            if(isConnective(baidu)==1)
                break;
            else
            {
                if(isConnective(google)==1)
                    break;
                else
                {
                    try {
                        Thread.sleep(5000);//���������5000����5000���룬Ҳ����5�룬���Ըó�����Ҫ��ʱ��
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }    
            }       
        }
        String dnsStr = getDNSAddress();
        //������������ip��53�˿�dns��ѯת��resolv.conf�µ�nds������
        cmd = "iptables -t nat -A PREROUTING -i wlan0 -d 192.168.5.1 -p udp --dport 53 -j DNAT --to "+dnsStr;
        executeIptabels(cmd);
			
        cmd = "iptables -t nat -A POSTROUTING -o eth0 -d "+dnsStr+" -p udp --dport 53 -j MASQUERADE";
        executeIptabels(cmd);
        //������ͨ��
        setIsConnective(1);
      
	}
    /**
     * ���õ�ǰ������ͨ״̬
     */
    private void setIsConnective(int flag){
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
            String sql = "select id from internetState";
				
            // �����
            ResultSet rs = statement.executeQuery(sql);
            
            sql="update internetState set id="+flag+" where id<3";
      
            if(rs.next())	
                statement.executeUpdate(sql);
        } catch (SQLException e) {                    
            e.printStackTrace();
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();
        }
    
    }
	/**
     * �жϵ�ǰ������ͨ��
     * 
     * @return ��ǰ������ͨ��
     */
    private int isConnective(String testURL) {
       try {  
           InetAddress ad = InetAddress.getByName(testURL);  
           boolean state = ad.isReachable(5000);//�����Ƿ���Դﵽ�õ�ַ  
           if(state){  
                System.out.println("���ӳɹ�" + ad.getHostAddress());
                return 1;
           }
           else{  
                System.err.println("����ʧ��");
                return 0;
           }
        } catch(Exception e){  
             System.err.println("����ʧ��");
        }
       return 0;
   }


	//ִ������
	private void executeIptabels(String c) {
            try{
		Process p = Runtime.getRuntime().exec(c);
		p.waitFor();
            }catch (IOException e) {  
                e.printStackTrace();  
            }catch (InterruptedException e) {  
                e.printStackTrace();  
            }
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
        
        public static void main(String args[])
        {
            DnsConfig initDns = new DnsConfig();
            initDns.initDNSSet();
	}

}