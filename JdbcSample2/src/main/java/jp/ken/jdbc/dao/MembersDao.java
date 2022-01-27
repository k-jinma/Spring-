package jp.ken.jdbc.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jp.ken.jdbc.entity.Members;

@Component
public class MembersDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private RowMapper<Members> membersMapper = new BeanPropertyRowMapper<Members>(Members.class);

    public List<Members> getList() {
        String sql = "SELECT * FROM members";
        List<Members> membersList = jdbcTemplate.query(sql, membersMapper);
        return membersList;
    }

    public List<Members> getListByName(String name) {
        String sql = "SELECT * FROM members WHERE name LIKE ?";
        name = name.replace("%", "\\%").replace("_", "\\_");
        name = "%" + name + "%";
        Object[] parameters = { name };
        List<Members> membersList = jdbcTemplate.query(sql, parameters, membersMapper);
        return membersList;
    }

    public Members getMembersById(Integer id) {
        String sql = "SELECT * FROM members WHERE id=?";
        Object[] parameters = { id };
        try {
            Members members = jdbcTemplate.queryForObject(sql, parameters, membersMapper);
            return members;
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int insert(Members members) {
        String sql = "INSERT INTO members(name,email,phoneNumber,birthday) VALUES(?,?,?,?);";
        Object[] parameters = { members.getName(), members.getEmail(), members.getPhoneNumber(), members.getBirthday() };
        TransactionStatus transactionStatus = null;
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        int numberOfRow = 0;
        try {
            transactionStatus = transactionManager.getTransaction(transactionDefinition);
            numberOfRow = jdbcTemplate.update(sql, parameters);
            transactionManager.commit(transactionStatus);
        } catch (DataAccessException e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
        } catch (TransactionException e) {
            e.printStackTrace();
            if (transactionStatus != null) {
                transactionManager.rollback(transactionStatus);
            }
        }
        return numberOfRow;
    }

	public int update(Members members, String id) {

		String sql = "update members set name=?, email=?, phoneNumber=?, birthday=? where id=?";
		//update members set name="Sato", email="sato@sato.jp", phoneNumber="08011113333", birthday="2000-01-01" where id=1;

		Object[] parameters = { members.getName(), members.getEmail(), members.getPhoneNumber(), members.getBirthday(), new Integer(id) };
        TransactionStatus transactionStatus = null;
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        int numberOfRow = 0;
        try {
            transactionStatus = transactionManager.getTransaction(transactionDefinition);
            numberOfRow = jdbcTemplate.update(sql, parameters);
            transactionManager.commit(transactionStatus);
        } catch (DataAccessException e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
        } catch (TransactionException e) {
            e.printStackTrace();
            if (transactionStatus != null) {
                transactionManager.rollback(transactionStatus);
            }
        }
        return numberOfRow;
	}

	public int delete(String delid) {
		String sql = "delete from members where id=?";

		Object[] parameters = { new Integer(delid) };
        TransactionStatus transactionStatus = null;
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        int numberOfRow = 0;
        try {
            transactionStatus = transactionManager.getTransaction(transactionDefinition);
            numberOfRow = jdbcTemplate.update(sql, parameters);
            transactionManager.commit(transactionStatus);
        } catch (DataAccessException e) {
            e.printStackTrace();
            transactionManager.rollback(transactionStatus);
        } catch (TransactionException e) {
            e.printStackTrace();
            if (transactionStatus != null) {
                transactionManager.rollback(transactionStatus);
            }
        }
        return numberOfRow;
	}
}