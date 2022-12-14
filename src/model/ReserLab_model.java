package model;

import static model.UserSession.log;
import static model.DBConnection.dbconnection;
import java.sql.*;
import java.util.*;
import java.time.*;

/**
 *
 * @author 20183109 강상혁 클래스 사용 용도 : 실습실 조회 및 예약 클래스
 */
public class ReserLab_model {

    java.sql.Time time = new java.sql.Time(17, 00, 00); //17시 시간 저장
    LocalTime curtime = LocalTime.now(); //현재 시간 저장
    //java.sql.Time current = java.sql.Time.valueOf(curtime); //LocalTime to java.sql.Time (형변환)
    LocalTime deadline = LocalTime.of(16, 30, 00); //예약 마감 시간 저장
    LocalDate curdate = LocalDate.now(); //현재 날짜 저장
    public String[][] lab_info = new String[100][5];
    boolean[] seat_info; //좌석 정보 배열
    int rand = (int) ((Math.random() * 300) + 100); //난수 생성
    int count = 0; //실습실 이용자수 저장받기 위한 변수
    public int number = 0; //2차원배열 위치를 위한 변수
    String SQL;
    String sql;
    private Connection con = null;
    private Statement st = null;
    private ResultSet rs = null;
    int[] labnum = {915, 916, 918, 911};
String stime;
String etime;
    public String user_id = log.session;

    //실습실 현재 사용현황 메소드
    public String[][] searchLab(int lab_num, List<Integer> num, String date) {
        try {
            for (int i = 0; i < num.size(); i++) {
                SQL = "SELECT * "
                        + " from reservation "
                        + " where ('" + num.get(i) + "' >= hour(start_time)"
                        + " and hour(end_time) > '" + num.get(i) + "')"
                        + " and reser_date = '" + date + "'"
                        + " and lab_num = '" + lab_num + "'"
                        + " and (access != 'x' or access != 'e')";
                st = dbconnection.getInstance().getConnection().createStatement();
                rs = st.executeQuery(SQL);
                while (rs.next()) {
                    lab_info[number][0] = rs.getString("reser_num");
                    lab_info[number][1] = rs.getString("seat_num");
                    lab_info[number][2] = rs.getString("reser_date");
                    lab_info[number][3] = rs.getString("start_time");
                    lab_info[number][4] = rs.getString("end_time");
                    number++;
                }
                rs.close();
                st.close();
            }
        } catch (SQLException e) {
            System.out.println(e + SQL);
        }
        return lab_info;
    }

    //실습실 예약 정보 입력 메소드
    public String reservation(List<String> stu_num, int[] lab_num, String[] seat_num, java.sql.Date[] date, java.sql.Time[] start_time, java.sql.Time[] end_time) {
        try {
            for (int i = 0; i < stu_num.size(); i++) { //입력받은 학생의 크기만큼 반복
                //입력받은 학생들이 student 테이블에 존재여부 확인
                System.out.println(i + " " + stu_num.get(i));
                SQL = "select stu_num from student where stu_num = '" + stu_num.get(i) + "'";
                System.out.println(SQL);
                st = dbconnection.getInstance().getConnection().createStatement();
                rs = st.executeQuery(SQL);
                if (rs.next()) {
                    System.out.println("학생 있음");
                    rs.close();
                    st.close();
                } else {
                    System.out.println("회원가입 하지 않은 학생이 입력되었습니다. 다시 입력해주세요.");
                    return "nonstd";
                }
            }
            
            for (int i = 0; i < stu_num.size(); i++) {
                stime = start_time[i].toString().substring(0,start_time[i].toString().indexOf(":"));
                etime = end_time[i].toString().substring(0,end_time[i].toString().indexOf(":"));
                if (stime.equals("09")){
                    stime = "9";
                }
                
                SQL = "select * from reservation where stu_num= '"+  stu_num.get(i) +"' and reser_date='"+date[i]+"' and " //요일 체크
                + "(( "+ stime +" <= hour(start_time) and hour(end_time) <="+etime+") or " // 겹치는 강의 시간 체크
                + "( "+stime+" >= hour(start_time) and hour(end_time) <="+etime+" and hour(end_time) >"+ stime+")or "
                + "( "+stime+" <= hour(start_time) and hour(end_time) >="+etime+" and hour(start_time) <"+etime+") or "
                + "( "+stime+" >= hour(start_time) and hour(end_time) >="+etime+"))";
                st = dbconnection.getInstance().getConnection().createStatement();
                rs = st.executeQuery(SQL);
                if(rs.next()){
                    return "exist";
                }else
                    System.out.println("예약 없음");
            }
            rs.close();
            st.close();
            
            for (int i = 0; i < stu_num.size(); i++) {//입력받은 학생의 크기만큼 반복
                //예약 테이블의 존재하지 않는 예약번호가 나올때까지 난수 생성
                while (true) {
                    String sql = "select * from reservation where reser_num =" + rand;
                    st = dbconnection.getInstance().getConnection().createStatement();
                    rs = st.executeQuery(sql);
                    if (rs.next()) {
                        rand = (int) ((Math.random() * 300) + 100);
                    } else {
                        break;
                    }
                }
                //예약 시작시간이 17시 이전일 경우, 자동 승인
                if (start_time[i].before(time)) {
                    SQL = "insert into reservation(reser_num, stu_num, lab_num, mgr, "
                            + "seat_num, reser_date, start_time, end_time, tl_num, access) "
                            + "values(" + rand + ",'"
                            + stu_num.get(i) + "',"
                            + lab_num[i] + ","
                            + "default" + ",'"
                            + seat_num[i] + "','"
                            + date[i] + "','"
                            + start_time[i] + "','"
                            + end_time[i] + "','"
                            + user_id + "',"
                            + "'y'" + ")";
                    con = dbconnection.getConnection();
                    st = con.prepareStatement(SQL);
                    st.executeUpdate(SQL);
                    st.close();
                } //예약 시작 시간이 17시부터일 경우, 예약 승인 대기 상태 
                else {
                    SQL = "insert into reservation(reser_num, stu_num, lab_num, mgr, "
                            + "seat_num, reser_date, start_time, end_time, tl_num, access) "
                            + "values(" + rand + ",'"
                            + stu_num.get(i) + "',"
                            + lab_num[i] + ","
                            + "default" + ",'"
                            + seat_num[i] + "','"
                            + date[i] + "','"
                            + start_time[i] + "','"
                            + end_time[i] + "','"
                            + user_id + "',"
                            + "'w'" + ")";
                    con = dbconnection.getConnection();
                    st = con.prepareStatement(SQL);
                    st.executeUpdate(SQL);
                    st.close();
                }
                rs.close();
            }
            return "success";
        } catch (SQLException e) {
            System.out.println(e + SQL);
            return "failed";
        }
    }
    
    //강의실 순서
    
    //실습실 만석여부 확인 메소드
    public boolean isFull(int lab_num, List<Integer> num, String date) {
        try {
            //예약 시작시간부터 한시간마다 체크하여 만석여부 확인
            for (int i = 0; i < num.size(); i++) {
                SQL = "SELECT count(*) count "
                        + " from reservation "
                        + " where ('" + num.get(i) + "' >= hour(start_time)"
                        + " and hour(end_time) > '" + num.get(i) + "')"
                        + " and reser_date = '" + date + "'"
                        + " and lab_num = '" + lab_num + "'"
                        + " and (access != 'x' or access != 'e')"
                        + " group by lab_num;";
                st = dbconnection.getInstance().getConnection().createStatement();
                rs = st.executeQuery(SQL);
                if (rs.next()) {
                    count = rs.getInt(1);
                    System.out.println(count);
                }

                if (count >= 20) {
                    return false;
                }
                st.close();
                rs.close();
            }
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }
}
