package model.State;

import model.State.State;
import java.sql.*;
import static model.DBConnection.dbconnection;

public class WaitAccessState implements State {

    String reser_num;
    String SQL;
    private Connection con = null;
    private Statement st = null;
    private ResultSet rs = null;
    private PreparedStatement pstmt = null;
    String access = "w";

    public WaitAccessState(String resernum) {
        this.reser_num = resernum;
    }

    public boolean updateState() {

        try {
            //예약 상태가 대기 되었을 때 대기 상태로 바꿔주는 SQL문
            SQL = "update reservation set access = ?" + "where reser_num = ?";
            con = dbconnection.getConnection();
            st = con.prepareStatement(SQL);
            pstmt.setString(1, access);
            pstmt.setString(2, reser_num);
            st.executeUpdate(SQL);

            rs.close();
            st.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
            }

        }
    }
}
