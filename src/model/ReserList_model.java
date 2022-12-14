package model;

import java.sql.*;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import static model.UserSession.log;
import static model.DBConnection.dbconnection;

/**
 *
 * @author 20183125 송준섭 클래스 사용 용도 : 실습실 예약 리스트 출력 및 책임자 부여 클래스
 */
//실습실 예약 리스트 출력 클래스
public class ReserList_model {

    public String[][] reserinfo = new String[100][10];
    public int number = 0;
    String SQL;
    String reser_num, name, start_time, end_time; //예약번호, 예약자, 예약 시작/종료시간
    public String user_id = log.session;
    String labnum, stdno;
    private Connection con = null;
    private Statement st = null;
    private ResultSet rs = null;
    private PreparedStatement pstmt = null;
    
    //예약 정보리스트 출력 메소드
    public String[][] reserlist(String cat) {
        try {
            //DB로부터 예약 정보 불러오는 SQL문
            if (cat.equals("today")) {
                SQL = "select * from reservation r join student s on r.stu_num = s.stu_num and r.access = 'w';";
                st = dbconnection.getInstance().getConnection().createStatement();
                rs = st.executeQuery(SQL);
                while (rs.next()) {
                    reserinfo[number][0] = rs.getString("reser_num");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][1] = rs.getString("stu_num");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][2] = rs.getString("name");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][3] = rs.getString("lab_num");
                    reserinfo[number][4] = rs.getString("seat_num");
                    reserinfo[number][5] = rs.getString("reser_date");
                    reserinfo[number][6] = rs.getString("start_time");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][7] = rs.getString("end_time");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][8] = rs.getString("tl_num");
                    reserinfo[number][9] = rs.getString("access");
                    number++;
                }
            } else {
                SQL = "select * from reservation r join student s on r.stu_num = s.stu_num;";
                st = dbconnection.getInstance().getConnection().createStatement();
                rs = st.executeQuery(SQL);
                while (rs.next()) {
                    reserinfo[number][0] = rs.getString("reser_num");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][1] = rs.getString("stu_num");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][2] = rs.getString("name");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][3] = rs.getString("lab_num");
                    reserinfo[number][4] = rs.getString("seat_num");
                    reserinfo[number][5] = rs.getString("reser_date");
                    reserinfo[number][6] = rs.getString("start_time");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][7] = rs.getString("end_time");//예약 번호 불러와 reserinfo배열에 저장
                    reserinfo[number][8] = rs.getString("tl_num");
                    reserinfo[number][9] = rs.getString("access");
                    number++;
                }
            }

        } catch (SQLException e) {
            System.out.println(e);
        }
        return reserinfo;

    }
    
    //책임자 부여 메소드
    public boolean appoint_manager(String reser_num) {
        try {
            //예약 상태가 취소 되었을 때 취소 상태로 바꿔주는 SQL문
            SQL = "update reservation set mgr = '1' where reser_num = '" + reser_num + "'";
            con = dbconnection.getConnection();
            st = con.prepareStatement(SQL);
            int addrow = st.executeUpdate(SQL);
            st.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //책임자 이전 메소드
    public boolean update_manager() {
        try {
            SQL = "select lab_num from reservation where mgr ='1' and stu_num = '" + user_id + "'";
            st = dbconnection.getInstance().getConnection().createStatement();
            rs = st.executeQuery(SQL);
            if (rs.next()) {
                labnum = rs.getString("lab_num");
                System.out.println(labnum);
            }
            
            rs.close();
            st.close();
            
            System.out.println("success1");
            System.out.println(user_id);
            SQL = "update reservation set mgr ='0' where stu_num = '" + user_id + "' and mgr = '1'";            
            con = dbconnection.getConnection();            
            st = con.prepareStatement(SQL);            
            int addrow = st.executeUpdate(SQL);            
            System.out.println("success2");
            st.close();
            
            SQL = "select stu_num, max(end_time) from reservation where lab_num = " + labnum + " and access = 'u'";
                       
            st = dbconnection.getInstance().getConnection().createStatement();
            rs = st.executeQuery(SQL);
            
            if (rs.next()) {
                stdno = rs.getString("stu_num");
                System.out.println(stdno);
            }          
            System.out.println("success3");
            rs.close();
            st.close();
            
            SQL = "update reservation set mgr = '1' where lab_num = " + labnum + " and stu_num = '" + stdno + "' and access = 'u'";
            System.out.println(SQL); 
            con = dbconnection.getConnection();        
            st = con.prepareStatement(SQL);
             System.out.println("2");
            addrow = st.executeUpdate(SQL);
            System.out.println("3");
            rs.close();
            st.close();
            System.out.println("success4");
            
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    //책임자 찾기
    public String get_manager(String lab){
        Calendar c = Calendar.getInstance();
        String date = Integer.toString(c.get(Calendar.YEAR)) + "-" + Integer.toString(c.get(Calendar.MONTH)+1) + "-" + Integer.toString(c.get(Calendar.DATE));
        System.out.println(date);
        String manager=null;
        SQL = "select s.stu_num, s.name from student s join reservation r on s.stu_num = r.stu_num where mgr='1' and reser_date='"+ date +"' and lab_num='"+lab+"'";
        try {
              st = dbconnection.getInstance().getConnection().createStatement();
            rs = st.executeQuery(SQL);
            if (rs.next()) {
                manager = rs.getString(1)+"/"+rs.getString(2);
            }else
                manager = "Empty";
        } catch (SQLException ex) {
            Logger.getLogger(ReserList_model.class.getName()).log(Level.SEVERE, null, ex);
        }
        return manager;
    }
}
