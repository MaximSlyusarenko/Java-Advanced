package ru.ifmo.ctddev.slyusarenko.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public interface RemotePerson extends Remote, Person {

    public String getName() throws RemoteException;
    public String getSurname() throws RemoteException;
    public String getPassportNumber() throws RemoteException;
    public ConcurrentHashMap<String, Account> getAccounts() throws RemoteException;
    public int setAmount(String accountId, int amount) throws RemoteException;
    public String showMoney(String key) throws RemoteException;
    public int getMoney(String key) throws RemoteException;
}
