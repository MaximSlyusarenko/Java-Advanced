package ru.ifmo.ctddev.slyusarenko.bank;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) {
        Bank bank;
        if (args == null || args.length != 6 || args[0] == null || args[1] == null || args[2] == null || args[3] == null || args[4] == null || args[5] == null) {
            System.err.println("Incorrect arguments");
            return;
        }
        if (!args[5].equals("0") && !args[5].equals("1")) {
            System.err.println("Incorrect arguments");
        }

        try {
            Registry registry = LocateRegistry.getRegistry(null, 8888);
            bank = (Bank) registry.lookup("Bank");
        } catch (NotBoundException | RemoteException e) {
            System.err.println(e.getMessage());
            return;
        }
        if ("1".equals(args[5])) {
            try {
                RemotePerson person = (RemotePerson) bank.getPerson(args[2], Integer.parseInt(args[5]));
                if (person == null) {
                    if (!bank.createPerson(args[0], args[1], args[2])) {
                        System.out.println("Can't create person");
                        return;
                    }
                } else if (!check(person, args[0], args[1])) {
                    System.out.println("This passport number is already registered for other person");
                    return;
                }
                person = (RemotePerson) bank.getPerson(args[2], Integer.parseInt(args[5]));
                person.setAmount(args[3], Integer.parseInt(args[4]) + person.getMoney(args[3]));
                System.out.println(person.showMoney(args[3]));
            } catch (RemoteException e) {
                System.err.println(e.getMessage());
            }
        } else {
            try {
                LocalPerson person = (LocalPerson) bank.getPerson(args[2], Integer.parseInt(args[5]));
                if (person == null) {
                    if (!bank.createPerson(args[0], args[1], args[2])) {
                        System.out.println("Can't create person");
                        return;
                    }
                } else if (!check(person, args[0], args[1])) {
                    System.out.println("This passport number is already registered for other person");
                    return;
                }
                person = (LocalPerson) bank.getPerson(args[2], Integer.parseInt(args[5]));
                person.setAmount(args[3], Integer.parseInt(args[4]) + person.getMoney(args[3]));
                try (FileOutputStream fos = new FileOutputStream("back.ser");
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(person);
                    bank.getBack();
                } catch (Exception ignored) {
                }
                System.out.println(person.showMoney(args[3]));
            } catch (RemoteException e) {
                System.err.println(e.getMessage());
            }
        }

    }

    private static boolean check(Person person, String name, String surname) {
        try {
            return person.getName().equals(name) && person.getSurname().equals(surname);
        } catch (Exception e) {
            return false;
        }
    }
}
