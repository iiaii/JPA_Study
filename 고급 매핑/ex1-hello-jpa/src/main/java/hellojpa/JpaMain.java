package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Movie movie = new Movie();
            movie.setDirector("aaaa");
            movie.setActor("bbb");
            movie.setName("바람과함께사라지다");
            movie.setPrice(10000);
            movie.setCreatedBy("kim");
            movie.setCreatedDate(LocalDateTime.now());

            em.persist(movie);

            em.flush();
            em.clear();

            BaseEntity findMovie = em.find(Movie.class, movie.getId());

            System.out.println("findMovie = " + findMovie.getCreatedBy());
//            System.out.println("findMovie = " + findMovie.getName());

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }


        emf.close();
    }
}
