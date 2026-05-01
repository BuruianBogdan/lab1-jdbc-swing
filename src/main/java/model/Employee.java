package model;

public class Employee {
    private int id;
    private String name;
    private int departmentId;
    private double salary;

    public Employee(int id, String name, int departmentId, double salary) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public double getSalary() {
        return salary;
    }
}