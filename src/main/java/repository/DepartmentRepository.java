package repository;

import config.HibernateUtil;
import entity.Department;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class DepartmentRepository {
    // incarca doar departamentele, angajatii nu sunt incarcati imediat, relatia cu angajatii este LAZY
    public List<Department> findAllLazy(Session session) {
        return session
                .createQuery("select d from Department d order by d.id", Department.class)
                .getResultList();
    }
    // hibernate incarca un singur query
    public List<Department> findAllWithEmployees(Session session) {
        return session
                .createQuery(
                        "select distinct d from Department d left join fetch d.employees order by d.id",
                        Department.class
                )
                .getResultList();
    }

    public List<Department> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                    .createQuery("select d from Department d order by d.id", Department.class)
                    .getResultList();
        }
    }

    public Department findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Department.class, id);
        }
    }

    public void save(Department department) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(department);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la salvarea departamentului", e);
        }
    }

    public void updateName(Long id, String newName) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Department department = session.get(Department.class, id);

            if (department == null) {
                throw new RuntimeException("departamentul nu exista");
            }

            department.setName(newName);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la actualizarea departamentului", e);
        }
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session
                    .createQuery("select count(d) from Department d", Long.class)
                    .getSingleResult();
        }
    }
}