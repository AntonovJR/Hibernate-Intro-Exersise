import entities.*;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Engine implements Runnable {
    private final Scanner scanner = new Scanner(System.in);
    private final EntityManager entityManager;

    public Engine(EntityManager entityManager) {
        this.entityManager = entityManager;

    }

    @Override
    public void run() {

        System.out.println("Select exercise number: ");

        int exNumber = Integer.parseInt(scanner.nextLine());
        switch (exNumber) {

            case 2 -> exerciseTwo(); // 2. Change casing
            case 3 -> exerciseThree(); // 3. Contains Employee
            case 4 -> exerciseFour(); // 4. Employees with Salary Over 50 000
            case 5 -> exerciseFive(); // 5. Employees from Department
            case 6 -> exerciseSix(); // 6. Adding a New Address and Updating Employee
            case 7 -> exerciseSeven(); // 7. Addresses with Employee Count
            case 8 -> exerciseEight(); //8. Get Employee with Project
            case 9 -> exerciseNine(); //9. Find Latest 10 Projects
            case 10 -> exerciseTen(); //10. Increase Salaries
            case 11 -> exerciseEleven(); //11. Find Employees by First Name
            case 12 -> exerciseTwelve(); //12. Employees Maximum Salaries
            case 13 -> exerciseThirteen(); //13. Remove Towns
            default -> System.out.println("Invalid exercise number.");
        }
    }


    private void exerciseThirteen() {
        System.out.println("Enter town name: ");
        String cityName = scanner.nextLine();

        entityManager.getTransaction().begin();

        List<Address> addresses = entityManager
                .createQuery("SELECT a FROM Address a Where a.town.name = :townName", Address.class)
                .setParameter("townName", cityName)
                .getResultList();


        System.out.printf("%d address in %s deleted%n", addresses.size(), cityName);
        if (addresses.size() == 0) {
            System.exit(0);
        }

        for (Address address : addresses) {
            for (Employee e : address.getEmployees()) {
                e.setAddress(null);
            }
            entityManager.flush();
            entityManager.remove(address);
        }
        Town town = addresses.get(0).getTown();
        entityManager.remove(town);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private void exerciseTwelve() {
        List<Employee> employeeList = entityManager
                .createQuery("SELECT e FROM Employee e WHERE e.salary NOT BETWEEN 30000 AND 70000", Employee.class)
                .getResultList();

        Set<Department> departmentList = employeeList.stream().map(Employee::getDepartment).collect(Collectors.toSet());

        for (Department d : departmentList) {
            BigDecimal max = BigDecimal.valueOf(0);

            for (Employee e : employeeList) {
                if (e.getDepartment().getId().equals(d.getId())) {
                    if (max.compareTo(e.getSalary()) < 1) {
                        max = e.getSalary();
                    }
                }
            }
            System.out.printf("%s - $%.2f%n", d.getName(), max);
        }


    }

    private void exerciseEleven() {
        System.out.println("Enter symbols: ");
        String input = scanner.nextLine() + "%";
        List<Employee> employeeList = entityManager.createQuery("SELECT e from Employee e where e.firstName like :input",
                Employee.class)
                .setParameter("input", input)
                .getResultList();
        if (employeeList.size() == 0) {
            System.out.println("No such employees");
            System.exit(0);
        }
        for (Employee employee : employeeList) {
            System.out.printf("%s %s - %s - ($%.2f)%n", employee.getFirstName(), employee.getLastName(), employee.getJobTitle(), employee.getSalary());
        }
    }

    private void exerciseTen() {
        entityManager.getTransaction().begin();
        int affectedRows = entityManager.createQuery("update Employee e set e.salary = e.salary*1.12 " +
                "where e.department.id IN(1,2,4,11)")
                .executeUpdate();
        entityManager.getTransaction().commit();
        List<Employee> employeeList = entityManager.createQuery("select e from Employee e " +
                "where e.department.id IN(1,2,4,11)", Employee.class).getResultList();
        System.out.printf("%d employees have received salary raise!%n", affectedRows);
        for (Employee employee : employeeList) {
            System.out.printf("%s %s ($%.2f)%n", employee.getFirstName(), employee.getLastName(), employee.getSalary());
        }
        entityManager.close();
    }

    private void exerciseNine() {
        List<Project> projectList = entityManager.createQuery("select p from Project p order by p.startDate desc ",
                Project.class)
                .setMaxResults(10).getResultList();
        projectList.stream().sorted(Comparator.comparing(Project::getName))
                .forEach(project -> System.out.printf("Project name: %s%n\tProject Description: %s...%n\tProject Start Date: %s%n" +
                                "\tProject End Date: %s%n"
                        , project.getName(), project.getDescription().substring(0, 35), project.getStartDate()
                        , project.getEndDate()));


    }

    private void exerciseEight() {
        System.out.println("Enter employee id: ");
        int employeeId = Integer.parseInt(scanner.nextLine());
        try {
            Employee employee = entityManager.find(Employee.class, employeeId);

            System.out.printf("%s %s - %s%n", employee.getFirstName(), employee.getLastName(), employee.getJobTitle());
            employee.getProjects().stream()
                    .sorted(Comparator.comparing(Project::getName))
                    .forEach(project -> System.out.printf("\t%s%n", project.getName()));
        } catch (Exception e) {
            System.out.println("Invalid employee id");
        }


    }

    private void exerciseSeven() {
        List<Address> addressList = entityManager.createQuery("select a from Address a order by a.employees.size desc"
                , Address.class)
                .setMaxResults(10)
                .getResultList();
        for (Address address : addressList) {
            System.out.printf("%s, %s - %d employees%n", address.getText(), address.getTown().getName(),
                    address.getEmployees().size());
        }
    }

    private void exerciseSix() {
        System.out.println("Enter employee last name: ");
        String lastName = scanner.nextLine();
        Address address = new Address();
        address.setText("Vitoshka 15");
        address.setTown(entityManager.find(Town.class, 32));
        entityManager.getTransaction().begin();
        entityManager.persist(address);
        entityManager.getTransaction().commit();

        try {
            Employee employee = entityManager.createQuery("select  e from Employee e where e.lastName = :name",
                    Employee.class)
                    .setParameter("name", lastName)
                    .getSingleResult();
            entityManager.getTransaction().begin();
            employee.setAddress(address);
            entityManager.getTransaction().commit();
            entityManager.close();

            System.out.printf("Done! Employee %s %s is already at address %s!%n", employee.getFirstName(),
                    employee.getLastName(), address.getText());
        } catch (Exception e) {
            System.out.println("There is no such employee.");
        }
    }

    private void exerciseFive() {
        List<Employee> employees = entityManager.createQuery("select e from Employee e where e.department.name = " +
                "'Research and Development' order by e.salary asc ", Employee.class)
                .getResultList();
        entityManager.getTransaction().begin();
        for (Employee employee : employees) {
            System.out.printf("%s %s from %s - $%.2f%n", employee.getFirstName(), employee.getLastName(),
                    employee.getDepartment().getName(), employee.getSalary());
        }
        entityManager.close();
    }

    private void exerciseFour() {
        List<Employee> employees = entityManager.createQuery("Select e from  Employee e where e.salary> 50000",
                Employee.class).getResultList();
        entityManager.getTransaction().begin();
        for (Employee employee : employees) {
            System.out.println(employee.getFirstName());
        }
        entityManager.close();

    }

    private void exerciseThree() {
        System.out.println("Enter employee full name: ");
        String fullName = scanner.nextLine();
        List<Employee> employees = entityManager.createQuery("Select e from  Employee e  " +
                "where concat(e.firstName,' ',e.lastName) = :name", Employee.class)
                .setParameter("name", fullName)
                .getResultList();
        System.out.println(employees.size() == 0 ? "No" : "Yes");

    }

    private void exerciseTwo() {
        List<Town> towns = entityManager.createQuery("Select t from  Town t  where length(t.name)<= 5 ",
                Town.class).getResultList();
        System.out.printf("Done! %d towns was affected!", towns.size());
        entityManager.getTransaction().begin();
        towns.forEach(entityManager::detach);
        for (Town town : towns) {
            town.setName(town.getName().toUpperCase());
        }
        towns.forEach(entityManager::merge);
        entityManager.flush();
        entityManager.getTransaction().commit();
        entityManager.close();


    }


}
