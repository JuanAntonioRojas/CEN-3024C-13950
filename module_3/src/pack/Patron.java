package pack;

import java.util.Objects;
import java.math.BigDecimal;
import java.math.RoundingMode;



//  This class is a Plain Old Java Object (POJO). The aim here is to encapsulate its data with private fields,
//    public getters and setters, equality by ID, and two string representations:
//   - toString(): human-friendly row for console table (no newline)
//   - toStringForFile(): machine-friendly "ID-Name-Address-Fine" line for saving data to a file.
//  Basically, the Patron class serves as a simple data holder (a "model" or "entity") for each library patron.


public class Patron {
    private String id, name, address;
    private BigDecimal fineAmount;


    public Patron(String id, String name, String address, BigDecimal  fineAmount) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.fineAmount = fineAmount;
    }

    //The Getters and Setters:

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }



    //  For UI output we use a console-friendly row presentation:
    @Override
    /*public String toString() {  //  Old way: It never aligns perfectly
        return id + "\t" + name + "\t\t\t" + address + "\t\t\t" + fineAmount + "\n";
    }*/
    //  New way: columns are aligned by printf in the UI
    public String toString() {
        String fineStr = fineAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        return String.format("%-10s  %-24s  %-48s  $%8s", id, name, address, fineStr);

    }



    //  Now, we need to format a Patron object's data into a specific string format (e.g., ID-Name-Address-Fine) that can be
    //    written to a file and later read back in. This way we're encapsulating the file formatting logic within the Patron class.
    //  This is a File-friendly format, so we can save and load reliably.
    public String toStringForFile() {
        return String.format("%s-%s-%s-%s", id, name, address,
                fineAmount.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }




    //  Note:
    //  Two patrons are considered the same person (object) in our library system if they share the same ID.
    //  To determine if two Patron objects represent the same person, we need to override the "equals" method.
    //  The default "equals" works on primitive types and is not useful when comparing object contents rather than their memory addresses
    //  This override defines what it means for two Patron objects to be considered the same.
    @Override
    public boolean equals(Object aPatron) {
        if (this == aPatron) return true;   //  used to compare attributes and methods on 2 ojbects: same object in memory.

        if (aPatron == null || getClass() != aPatron.getClass()) return false;//  Comparing Patrons to any other object will return false.
        Patron patron = (Patron) aPatron;  //  then we safely cast the generic Object 'aPatron' back to a 'Patron' object
        return Objects.equals(this.id, patron.id);  //  Here, the two patrons are equal if and only if their ID fields are equal.
    }




    //  We override the hashCode() method to ensure that objects that are considered equal by our custom equals() method
    //    produce the same hash code. The golden rule is: If you override equals(), you must also override hashCode()...
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

