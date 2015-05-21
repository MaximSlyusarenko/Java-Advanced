package ru.ifmo.ctddev.slyusarenko.bank;

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class BankImpl implements Bank {

    private ConcurrentHashMap<String, RemotePerson> persons;
    private static Registry registry = null;

    public BankImpl() {
        persons = new ConcurrentHashMap<>();
        try {
            registry = LocateRegistry.createRegistry(8888);
        } catch (RemoteException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public Person getPerson(String passportNumber, int type) throws RemoteException {
        if (type != 0 && type != 1) {
            return null;
        }
        if (!persons.containsKey(passportNumber)) {
            return null;
        }
        if (type == 0) {// serialization
            return new LocalPersonImpl(persons.get(passportNumber));
        }
        return persons.get(passportNumber);
    }

    @Override
    public boolean createPerson(String name, String surname, String passportNumber) throws RemoteException {
        try {
            if (!persons.containsKey(passportNumber)) {
                persons.put(passportNumber, new RemotePersonImpl(name, surname, passportNumber));
                return true;
            } else {
                return false;
            }
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override
    public void getBack() throws RemoteException {
        try (FileInputStream fis = new FileInputStream("back.ser");
        ObjectInputStream ois = new ObjectInputStream(fis)) {
            LocalPerson person;
            while (true) {
                try {
                    person = (LocalPerson) ois.readObject();
                    break;
                } catch (ClassNotFoundException ignored) {
                }
            }
            persons.put(person.getPassportNumber(), new RemotePersonImpl(person));
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        Bank bank = new BankImpl();
        try {
            Bank b = (Bank) UnicastRemoteObject.exportObject(bank, 8888);
            registry.bind("Bank", b);
        } catch (RemoteException | AlreadyBoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
