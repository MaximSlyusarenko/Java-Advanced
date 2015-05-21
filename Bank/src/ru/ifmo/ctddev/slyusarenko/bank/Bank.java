package ru.ifmo.ctddev.slyusarenko.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {

    public Person getPerson(String passportNumber, int type) throws RemoteException;
    public boolean createPerson(String name, String surname, String passportNumber) throws RemoteException;
    public void getBack() throws RemoteException;
}
