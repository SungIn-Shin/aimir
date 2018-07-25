package com.aimir.schedule.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.aimir.fep.util.DataUtil;
import com.nuri.bc.BlockchainClient;

/**
 * LP_EM_TR 테이블에 있는 LP 값을 Block chain에 저장한다.
 * 
 * @author elevas
 * @since 2018.7.6
 */
@Service
public class InsertLP2BlockChainTask {
    
    protected static Log log = LogFactory.getLog(InsertLP2BlockChainTask.class);

    @Resource(name="dataSource")
    DataSource ds;
    
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"spring-default.xml"}); 
        DataUtil.setApplicationContext(ctx);
        
        InsertLP2BlockChainTask task = ctx.getBean(InsertLP2BlockChainTask.class);
        task.execute();
        System.exit(0);
    }

    
    public void execute() {
        log.info("########### START InsertLP2BlockChain ###############");
        List<Map<String, Object>> lplist = getLP();
        try {
            BlockchainClient client = new BlockchainClient();
            
            log.info("TOTAL LP[" + lplist.size() + "]");
            for (Map<String, Object> lp : lplist) {
                client.recordLP2BC("SE003", (String)lp.get("METERNO"), (String)lp.get("LPTIME"), 
                        (int)lp.get("DST"), (int)lp.get("CH"), (double)lp.get("VALUE"));
                log.debug("METERNO[" + lp.get("METERNO") + "] LPTIME[" + lp.get("LPTIME") + 
                        "] DST[" + lp.get("DST") + "] CH[" + lp.get("CH") + "] VALUE[" + lp.get("VALUE") + "]");
                deleteLP(lp);
            }
        }
        catch(Exception e) {
            log.error(e, e);
        }
        log.info("########### END InsertLP2BlockChain ############");
    }//execute end
    
    
    /*
     * LP를 가져온다.
     */
    public void deleteLP(Map<String, Object> lp) {
        String query = "delete from lp_em_tr where meterno=? and lptime=? and dst=? and ch=?";
        
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ds.getConnection();
            stmt = con.prepareStatement(query);
            stmt.setString(1, (String)lp.get("METERNO"));
            stmt.setString(2, (String)lp.get("LPTIME"));
            stmt.setInt(3, (int)lp.get("DST"));
            stmt.setInt(4, (int)lp.get("CH"));
            stmt.executeUpdate();
            con.commit();
        }
        catch (Exception e) {
            log.error(e, e);
            try {
                if (con != null) con.rollback();
            }
            catch (Exception se) {}
        }
        finally {
            try {
                if (stmt != null) stmt.close();
            }
            catch (Exception e) {}
            try {
                if (con != null) con.close();
            }
            catch (Exception e) {}
        }
    }
    
    /*
     * LP를 가져온다.
     */
    public List<Map<String, Object>> getLP() {
        String query = "select * from lp_em_tr";
        
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        List<Map<String, Object>> lplist = new ArrayList<Map<String, Object>>();
        
        try {
            con = ds.getConnection();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> lp = new HashMap<String, Object>();
                lp.put("METERNO", rs.getString("METERNO"));
                lp.put("LPTIME", rs.getString("LPTIME"));
                lp.put("DST", rs.getInt("DST"));
                lp.put("CH", rs.getInt("CH"));
                lp.put("VALUE", rs.getDouble("VALUE"));
                
                lplist.add(lp);
            }
            con.commit();
        }
        catch (Exception e) {
            log.error(e, e);
            try {
                if (con != null) con.rollback();;
            }
            catch (Exception se) {}
        }
        finally {
            try {
                if (rs != null) rs.close();
            }
            catch (Exception e) {}
            try {
                if (stmt != null) stmt.close();
            }
            catch (Exception e) {}
            try {
                if (con != null) con.close();
            }
            catch (Exception e) {}
        }
        
        return lplist;
    }
}