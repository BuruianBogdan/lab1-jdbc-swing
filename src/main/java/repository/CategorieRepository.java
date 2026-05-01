package repository;

import config.HibernateUtil;
import entity.Categorie;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CategorieRepository {

    public List<Categorie> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Categorie c order by c.id", Categorie.class).getResultList();
        } /*Lazy normal */
    }

    public Categorie findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Categorie.class, id);
        }
    }

    public Categorie findByIdWithProduse(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select distinct c from Categorie c left join fetch c.produse where c.id = :id",
                    Categorie.class
            ).setParameter("id", id).uniqueResult();
        }
    }/*Eager loading explicit*/

    public void save(Categorie categorie) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(categorie);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la salvarea categoriei", e);
        }
    }

    public void update(Categorie categorie) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(categorie);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la modificarea categoriei", e);
        }
    }

    public void delete(int id) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Categorie categorie = session.get(Categorie.class, id);
            if (categorie != null) {
                session.remove(categorie);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la stergerea categoriei", e);
        }
    }
}