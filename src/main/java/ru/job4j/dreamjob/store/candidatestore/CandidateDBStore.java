package ru.job4j.dreamjob.store.candidatestore;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CandidateDBStore implements CandidateStore {

    private final BasicDataSource pool;
    private static final Logger LOG = Logger.getLogger(CandidateDBStore.class);

    @Autowired
    public CandidateDBStore(BasicDataSource pool) {
        this.pool = pool;
    }

    @Override
    public List<Candidate> findAll() {
        String sql = "SELECT * FROM candidates ORDER BY 1";
        List<Candidate> candidates = new ArrayList<>();
        LOG.info("Trying to get all candidates from DB");
        try (Connection cn = pool.getConnection()) {
            PreparedStatement ps = cn.prepareStatement(sql);
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    Candidate candidate = new Candidate(it.getString("name"),
                            it.getString("description"),
                            it.getTimestamp("created"),
                            it.getBytes("photo")
                    );
                    candidate.setId(it.getInt("id"));
                    candidates.add(candidate);
                }
            }
            LOG.info("Success!");
        } catch (Exception e) {
            LOG.error("Not successful: " + e.getMessage(), e);
        }
        return candidates;
    }

    @Override
    public Candidate add(Candidate candidate) {
        String sql = "INSERT INTO candidates (name, description, created, photo)"
                + " VALUES (? , ? , ? , ?)";
        LOG.info("Trying to add a candidate to DB");
        try (Connection cn = pool.getConnection()) {
            PreparedStatement ps =
                    cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, candidate.getName());
            ps.setString(2, candidate.getDescription());
            ps.setTimestamp(3,
                    Timestamp.valueOf(LocalDateTime.now().withNano(0))
            );
            ps.setBytes(4, candidate.getPhoto());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                while (id.next()) {
                    candidate.setId(id.getInt("id"));
                }
            }
            LOG.info("Success!");
        } catch (Exception e) {
            LOG.error("Not successful: " + e.getMessage(), e);
        }
        return candidate;
    }

    @Override
    public void update(Candidate candidate) {
        String sql = "UPDATE candidates SET name = ? , description = ? , "
                + "created = ? , photo = ? WHERE id = ?";
        LOG.info("Trying to update candidate in the DB");
        try (Connection cn = pool.getConnection()) {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, candidate.getName());
            ps.setString(2, candidate.getDescription());
            ps.setTimestamp(3,
                    Timestamp.valueOf(LocalDateTime.now().withNano(0))
            );
            ps.setBytes(4, candidate.getPhoto());
            ps.setInt(5, candidate.getId());
            ps.executeUpdate();
            LOG.info("Success!");
        } catch (SQLException e) {
            LOG.error("Not successful: " + e.getMessage(), e);
        }
    }

    @Override
    public Candidate findById(int id) {
        Candidate rsl = null;
        String sql = "SELECT * FROM candidates WHERE id = ?";
        LOG.info("Trying to find a candidate by id");
        try (Connection cn = pool.getConnection()) {
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, id);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    rsl = new Candidate(it.getString("name"),
                            it.getString("description"),
                            it.getTimestamp("created"),
                            it.getBytes("photo")
                    );
                    rsl.setId(it.getInt("id"));
                }
            }
            LOG.info("Success!");
        } catch (Exception e) {
            LOG.error("Not successful: " + e.getMessage(), e);
        }
        return rsl;
    }
}
