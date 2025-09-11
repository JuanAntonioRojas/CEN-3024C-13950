package Mod1;

//    Name: Tony Rojas
//    Course: CEN3024C-13950.
//    Module 1 - Abstraction, Interface, Inheritance and Polymorphism
//    Date: 8/29/2025
//
//    The objective of this program is to show the basics of Abstraction, Interface, Inheritance and Polymorphism
//    This is a simple payroll program that models 3 employee types: Hourly, Salaried, and Commission based.
//    It runs a single payroll routine that computes the paycheck for all of them and prints simple pay stubs.
//
//    This program will utilize OOP with these building blocks:
//        2 Interfaces: "Payable" and "Commissionable" (only sales roles implement this one)
//        1 Abstract class: "Employee" that implements "Payable"
//
//    It will also show Inheritance and Polymorphism with these sub-classes:
//        "HourlyEmployee", "SalariedEmployee", "SalesEmployee" (that extends "Employee")
//        and 1 "runPayroll()" method that works for all employee types
//
//
//    Process in the "main":
//      1st: We create the staff: Employee[] staff = { new HourlyEmployee("Sam Walton", 20.00), new SalaryEmployee(...), etc.};
//      2nd: We create parallel arrays reflecting their hours and sales: {42, 40, 40} and {0, 0, 4000}
//      3rd: Then we run the payroll method (runPayroll) with one parameter that accepts Payable[] (the initial interface): runPayroll(staff, hours, sales);
//      4th: Finally, we format and print the payroll:
//           Sam Walton: 40×$20 = $800 regular + 2×$20×1.5 = $60 overtime → $860.00
//           Marco Rubio: $1200.00
//           Zig Ziglar: base $800 + 10% of $4,000 (=$400) → $1200.00



import java.util.Locale;
import static Mod1.Tools.*;


//  INTERFACES
interface Payable {
    double computePay(int hoursWorked, double salesAmount);
}

interface Commissionable {  //  (only some types use it)
    double commission(double salesAmount);
}


//  ABSTRACT CLASS EMPLOYEE (OOP + Inheritance)
abstract class Employee implements Payable {
    private final String name;
    protected Employee(String name) { this.name = name; }
    public String getName() { return name; }

    public abstract String getRole();   //  forces the following 3 subclasses to describe themselves

    public void printPayStub(double pay) {    //  Common behavior shared by all employees
        System.out.printf(Locale.US, "\t\u2502 %-22s %-26s Pay: $%,10.2f %15s%n",
                bYellow + Black + " " + getName(), "(" + getRole() + ")", pay, Reset + "\u2502");
    }


}


//  HOURLY
class HourlyEmployee extends Employee {
    private final double hourlyRate;

    public HourlyEmployee(String name, double hourlyRate) {
        super(name);  //  remember HourlyEmployee "extends" Employee (its "super" or parent constructor class)
        this.hourlyRate = hourlyRate;
    }

    @Override public String getRole() { return "Hourly"; }

    // Polymorphic override
    @Override public double computePay(int hoursWorked, double salesAmount) {
        int overtime = Math.max(0, hoursWorked - 40);
        int regular  = hoursWorked - overtime;
        return regular * hourlyRate + overtime * hourlyRate * 1.5;
    }
}

//  SALARIED
class SalariedEmployee extends Employee {
    private final double weeklySalary;

    public SalariedEmployee(String name, double weeklySalary) {
        super(name);  //  SalariedEmployee "extends" Employee (its parent class)
        this.weeklySalary = weeklySalary;
    }

    @Override public String getRole() { return "Salaried"; }

    @Override public double computePay(int hoursWorked, double salesAmount) {
        return weeklySalary; // ignores hours for simplicity
    }
}

//  COMMISSION SALES
class SalesEmployee extends Employee implements Commissionable {
    private final double baseWeekly;  //  base $800
    private final double commissionRate; //  0.10 = 10% of sales

    public SalesEmployee(String name, double baseWeekly, double commissionRate) {
        super(name);  //  SalesEmployee "extends" Employee (its parent class)
        this.baseWeekly = baseWeekly;
        this.commissionRate = commissionRate;
    }

    @Override public String getRole() { return "Sales = base + com."; }

    @Override public double commission(double salesAmount) {
        return salesAmount * commissionRate;  //  10% of sales
    }

    @Override public double computePay(int hoursWorked, double salesAmount) {
        return baseWeekly + commission(salesAmount);
    }
}



public class Abs_Inter_Inher_Poly {  //  This is what otherwise would be the "Main" class

    public static void main(String[] args) {

        Employee[] staff = {
                new HourlyEmployee("Sam Walton", 20.00),  //  Top pay at a grocery store
                new SalariedEmployee("Marco Rubio", 1_200.00),  //  $62,400 a year is $1,200 a week
                new SalesEmployee("Zig Ziglar", 800.00, 0.10)  //  10% commission
        };

        //  This inputs for one week of payroll are tied by the array index to the employees: hours[0] to staff[0], etc.
        int[] hours    = {42, 40, 40};     //  2 Hrs overtime for Sam Walton, everybody else 40 hrs
        double[] sales = {0, 0, 4_000.0};  //  Zig Ziglar got $4,000 in sales this week.   sales[2] to staff[2]

        //  Now we calculate the payroll for each person on the staff array based on their hours and sales
        runPayroll(staff, hours, sales); //  Polymorphism via interface parameter
    }


    //  This subroutine, or method in Java, works for ANY Payable (polymorphism)
    static void runPayroll(Payable[] people, int[] hours, double[] sales) {

        //  The header is boxed up in blue
        boxTop(Blue, 70);
        System.out.printf(Blue + "\t\u2502 %22s %-42s  \u2502%n" + Reset, " ", "== Weekly Payroll ==");
        boxBottom(Blue, 70);

        for (int i = 0; i < people.length; i++) {
            Payable p = people[i];
            double pay = p.computePay(hours[i], sales[i]); // dynamic dispatch

            // If it's an Employee, print a nice stub using shared behavior
            if (p instanceof Employee) {
                ((Employee) p).printPayStub(pay);
            } else {
                System.out.printf(Locale.US, "Payable item #%d -> $%,.2f%n", i + 1, pay);
            }
        }
        boxBottom(Reset, 70);
    }
}
