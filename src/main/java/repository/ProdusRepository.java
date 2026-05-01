package repository;

import config.HibernateUtil;
import entity.Categorie;
import entity.Produs;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ProdusRepository {

    public List<Produs> findByCategorieId(int categorieId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Produs p where p.categorie.id = :categorieId order by p.id",
                    Produs.class
            ).setParameter("categorieId", categorieId).getResultList();
        }
    }

    public Produs findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Produs.class, id);
        }
    }

    public void save(String nume, double pret, int stoc, int categorieId) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Categorie categorie = session.get(Categorie.class, categorieId);
            if (categorie == null) {
                throw new RuntimeException("categoria nu exista");
            }

            Produs produs = new Produs(nume, pret, stoc, categorie);
            session.persist(produs);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la adaugarea produsului", e);
        }
    }

    public void update(int id, String nume, double pret, int stoc) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Produs produs = session.get(Produs.class, id);
            if (produs == null) {
                throw new RuntimeException("produsul nu exista");
            }

            produs.setNume(nume);
            produs.setPret(pret);
            produs.setStoc(stoc);

            session.merge(produs);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la modificarea produsului", e);
        }
    }

    public void delete(int id) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Produs produs = session.get(Produs.class, id);
            if (produs != null) {
                session.remove(produs);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("eroare la stergerea produsului", e);
        }
    }
}