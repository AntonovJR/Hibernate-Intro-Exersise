import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        // Hello mate! Please enter your database credentials in "persistence.xml"
        // Import fresh database only at the beginning and everything should be fine!
        // You can find "soft_uni_db" file in "resources" folder.
        // SQL comments are turned off in the "persistence.xml"

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("soft_uni");
        EntityManager entityManager = factory.createEntityManager();
        Engine engine = new Engine(entityManager);

        engine.run();


    }
}
