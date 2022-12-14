package model.State;

import model.State.State;
import java.sql.*;
import static model.DBConnection.dbconnection;

public class UseLabState implements State {

    String user_id, reser_num;
    String SQL;
    private Connection con = null;
    private Statement st = null;
    private ResultSet rs = null;
    private PreparedStatement pstmt = null;
    String access = "u";

    public UseLabState(String user_id) {
        this.user_id = user_id;
    }

    public void updateState() {

        try {
            SQL = "select reser_num from reservation where stu_num = '" + user_id
                    + "' and hour(now()) >= hour(start_time)"
                    + " and hour(end_time) > hour(now())"
                    + " and reser_date = DATE_FORMAT(now(),'%Y-%m-%d')"
                    + " and access = 'y'";
            st = dbconnection.getInstance().getConnection().createStatement();
            rs = st.executeQuery(SQL);
            if (rs.next()) {
                reser_num = rs.getString("reser_num");
            }
            rs.close();
            st.close();

            //예약 상태가 사용중이 되었을 때 사용 중 상태로 바꿔주는 SQL문
            SQL = "update reservation set access = '" + access + "' where reser_num = '" + reser_num + "'";
            con = dbconnection.getConnection();
            st = con.prepareStatement(SQL);
            int addrow = st.executeUpdate(SQL);
            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
